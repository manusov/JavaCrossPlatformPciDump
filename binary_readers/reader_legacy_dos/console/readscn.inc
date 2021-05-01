;---------- Read scenario file ---------------------------------------------;
;                                                                           ;
; INPUT:  CX = Pointer to scenario file path and name,                      ;
;              0-terminated ASCII string                                    ;
;         DX = Pointer to scenario handle                                   ;
;         SI = Pointer to loaded scenario base address variable,            ;
;              this variable is buffer base address for file read           ;
;         DI = Pointer to scenario size variable,                           ;
;              this variable is size limit for this buffer                  ;
;         DS = Segment SEG_DATA, all offsets relative this segment base     ;
;                                                                           ;
; OUTPUT:  AL = Status code, 0 = Error, 1 = OK or normally skipped          ;
;          Variable scenario handle at [input DX] = updated by file open    ;
;          Variable scenario size at [input DI] = Read size, 0 if error     ;
;---------------------------------------------------------------------------;

ReadScenario:

; Entry
push ebx ecx edx esi edi ebp
mov bx,dx            ; BX = non volatile pointer to scenario handle

; Verify name pointer valid
mov word [bx],0      ; Clear handle for skip branch
jcxz .NoData         ; Skip operation if file name pointer = 0

; Open file, by input parameters: CX = Pointer to file name
mov ax,3D00h         ; Function = 3Dh = Open file, mode = 00h = read
mov dx,cx            ; DS:DX = Pointer to file path and name string
int 21h
jc .NoData
mov [bx],ax          ; Store handle

; Read file
push bx
mov ah,3Fh           ; AH = 3Fh = Read file
mov bx,[bx]          ; BX = Handle
mov cx,[di]          ; CX = Size limit
mov dx,[si]          ; DS:DX = Buffer base address
int 21h
pop bx
jc .NoData

; Write result size
mov [di],ax          ; Write scenario size = file size if read OK 
jmp .Close
.NoData:
mov word [di],0      ; Write scenario size = 0 if read error

; Close file
.Close:
mov ah,3Eh           ; Function = 3Eh = Close file, BX = Handle
mov bx,[bx]          ; BX = File handle
test bx,bx
jz .Exit
int 21h

.Exit:
setnc al             ; AL = 1 if CF = 0 (no errors), otherwise AL = 0
pop ebp edi esi edx ecx ebx
ret