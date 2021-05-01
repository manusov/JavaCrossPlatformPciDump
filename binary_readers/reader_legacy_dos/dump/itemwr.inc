;---------- Copy text string terminated by 00h ----------------;
; CR, LF added before string                                   ;
; Spaces added after string                                    ;
; Note last byte 00h not copied                                ;
;                                                              ;
; INPUT:   DS:SI = Source address (segment:offset)             ;
;          ES:DI = Destination address (segment:offset)        ;
; OUTPUT:  SI = Modified by copy                               ;
;          DI = Modified by copy                               ;
;          Memory at [Input DI] modified                       ;
;--------------------------------------------------------------;

ItemWrite_CRLF:
push eax
cld
mov ax,0A0Dh
stosw             ; CR, LF before string
pop eax
ItemWrite:
push eax
cld
@@:
movsb
cmp byte [si],0
jne @b            ; Cycle for copy null-terminated string
inc esi
mov ax,' ='
stosw             ; " = " after string
stosb
pop eax
ret
