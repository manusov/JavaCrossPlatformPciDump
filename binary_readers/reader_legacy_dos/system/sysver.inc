;--- Verify System, check run program possibility -----------------------;
; Procedure compatible with 8086 instruction set, not required 386       ;
; INPUT:   None                                                          ;
; OUTPUT:  CF = Error Flag:                                              ;
;               0(NC) = OK, can run programm                             ;
;               1(C)  = Error, can't run program                         ;
;                       incompatible configuration                       ;
;          AL = Error status                                             ;
;               00h = No errors                                          ;
;               01h = Wrong CPU, required 386 minimum                    ;
;               02h = Insufficient low memory, required 100KB minimum    ;
;               03h = Wrong OS, required DOS 3.x minimum                 ;
;                    (for handle-oriented I/O)                           ;
;               04h-FFh = Reserved                                       ;
;------------------------------------------------------------------------;

System_Verify:

;--- Check CPU type, required 80386 or higher ---
mov ax,0100001000000000b  ; Important: NT=1, IF=1, DF=0
push ax                   ; Need check NT flag writeable
popf
pushf
pop	ax
test ax,0100000000000000b
jz SV_No_386              ; Cannot set NT=1 ?
mov ah,00000010b
push ax
popf
pushf
pop ax
test ax,0100000000000000b
jz SV_Yes_386              ; Cannot set NT=0 ?
SV_No_386:	
mov	al,1
stc
ret                       ; Exit with Error #1
SV_Yes_386:

;--- Check free low (conventional) memory size, required 100KB minimum ---
push bx cx
int 12h
push cs
pop bx
mov cl,6
shl ax,cl
sub ax,bx
cmp ax,100*64
pop cx bx
jae SV_Yes_Memory
mov al,2
stc
ret                       ; Exit with Error #2
SV_Yes_Memory:		

;--- Check Operating System version, required 3.x minimum ---
mov ah,30h
int 21h
cmp al,3
jae SV_Yes_OS
mov al,3
stc
ret                       ; Exit with Error #3
SV_Yes_OS:

;--- Exit point for compatible configuration ---
SV_Passed:
xor al,al                 ; AL=00h and CF=0(NC)
ret

