;---------- String write in ASCII ------------------------------------------;
;                                                                           ;
; INPUT:   CX = Pointer to 0-terminated ASCII string, string output         ; 
;               to console and optional to report file (if BX non zero)     ;
;          BX = Report file handle, used as report validity flag only,      ;
;               report file must be re-opened before write                  ;
;          DX = Pointer to report file name and path,                       ;
;               0-terminated ASCII string                                   ;
;          DS = Segment SEG_DATA                                            ;
;          All offsets (pointers) associated with SEG_DATA segment.         ;
;                                                                           ;
; OUTPUT:  AX = Status code                                                 ;
;---------------------------------------------------------------------------;

InternalConsoleWrite:   ; This special entry point not required input BX, DX
                        ; Required only DS:CX
push ebx edx
mov bx,[ReportHandle]
mov dx,[ReportName]
jmp ConsoleWriteEntry

ConsoleWrite:           ; This normal entry point required input CX, BX, DX, DS
push ebx edx

ConsoleWriteEntry:
push ecx esi edi ebp 
cld

; Check criteria for write report file - optional operation
cmp [OptionDisplay],0
je .L3

; Write console - optional
push bx dx cx
mov si,cx
.L0:
mov bx,word [ActiveColor]  ; BH = Video page, BL = Video attribute 
mov cx,1                   ; CX = Number of chars for write
mov ah,9                   ; AH = Function number for BIOS INT 10h, write 
lodsb                      ; AL = Char
cmp al,0
je .L1                     ; Go exit if terminator detected
cmp al,' '
jb .L2                     ; Go teletype mode if control char detected
int 10h
mov bh,[ActivePage]
mov ah,3
int 10h                    ; Get cursor position
inc dl                     ; This means move cursor right
cmp dl,[ActiveColumns]
jae .L0                    ; Skip move cursor if end of line
mov ah,2
int 10h                    ; Set cursor position
jmp .L0
.L2:
mov ah,0Eh                 ; AH = Function number for BIOS INT 10h, teletype
int 10h
jmp .L0
.L1:
pop cx dx bx
.L3:

; Check criteria for write report file - optional operation
test bx,bx
jz .L4               ; Go skip write report file if handle = 0

; Open report file, here DS:DX = Pointer to file path and name string 
mov ax,3D01h         ; Function = 3Dh = Open file, mode = 01h = write
int 21h
jc .L3
mov bx,ax            ; Store handle

; Positioning pointer to end of report file, BX = Handle
push cx
mov ax,4202h         ; Function = 42h = Seek, mode = 02h = end of file
xor cx,cx            ; CX:DX = 32-bit offset from selected position (end)
xor dx,dx
int 21h
pop cx
jc .L6

; Calculate length of nul-terminated write data
mov si,cx            ; DS:SI = Pointer to data buffer, for calculate size 
mov dx,cx            ; DS:DX = Pointer to data buffer, for write function 
cld
.L5:
lodsb
cmp al,0
jne .L5
sub si,cx
lea cx,[si-1]        ; CX = Length 

; Write report file, DS:DX = Pointer to data buffer, CX = Length, BX = Handle
mov ah,40h           ; Function = 40h = Write to file
int 21h
; Status here ignored, process continued even if write report error  

; Close report file
.L6:
mov ah,3Eh           ; Function = 3Eh = Close file, BX = Handle
test bx,bx           ; BX = File handle
jz .L4
int 21h

.L4:
setnc al             ; AL = 1 if CF = 0 (no errors), otherwise AL = 0
pop ebp edi esi ecx edx ebx
ret