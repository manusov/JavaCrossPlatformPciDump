;---------- Create report file ---------------------------------------------;
; After this function successfully call, function ConsoleWrite              ;
; starts save output information to report file                             ;
;                                                                           ;
; INPUT:   CX = Pointer to report file name, 0-terminated ASCII string      ;
;          DX = Pointer to report file handle, return handle = 0 if error   ;
;          DS = Segment SEG_DATA, all offsets relative this segment base    ;
;                                                                           ;
; OUTPUT:  AX = Status code                                                 ;
;               Variable report handle at [input CX] =                      ;
;               Temporary handle, used as flag for write report file enable ;
;---------------------------------------------------------------------------;

CreateReport:

; Entry
push ebx ecx edx esi edi ebp
mov bx,dx            ; DS:BX = Pointer to file handle

; Verify name pointer valid
mov word [bx],0      ; Clear handle for skip branch
jcxz .Exit           ; Skip operation if file name pointer = 0

; Create file, by nput parameter CX = Pointer to file name
mov ah,3Ch           ; Function = 3Ch = Create file
mov dx,cx            ; DS:DX = Pointer to file name
xor cx,cx            ; CX = File attributes
int 21h
jc .Exit

; Store handle
mov [bx],ax

; Close file
.Close:
mov ah,3Eh           ; Function = 3Eh = Close file, BX = Handle
mov bx,[bx]          ; BX = File handle
test bx,bx
jz .Exit
int 21h

.Exit:
setnc al
pop ebp edi esi edx ecx ebx
ret