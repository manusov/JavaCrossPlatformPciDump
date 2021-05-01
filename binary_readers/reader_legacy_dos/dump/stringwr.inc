;---------- Copy selected text string terminated by 00h -------;
; Note last byte 00h not copied                                ;
;                                                              ;
; INPUT:   DS:SI = Source address (segment:offset)             ;
;          ES:DI = Destination address (segment:offset)        ;
;          AL  = Selector                                      ;
;          AH  = Limit  (if Selector>Limit, set Selector=0)    ; 
; OUTPUT:  SI = Modified by copy                               ;
;          DI = Modified by copy                               ;
;          Memory at [Input DI] modified                       ;
;--------------------------------------------------------------;

StringWriteSelected:
test al,al
jz StringWrite    ; Direct write for first string entry
cmp al,ah
ja StringWrite    ; Set limit if selector value > limit  
mov ah,al
;--- Skip AH strings ---
cld
@@:
lodsb
cmp al,0
jne @b
dec ah
jnz @b
;--- No RET continue in the next subroutine ---

;---------- Copy text string terminated by 00h ----------------;
; Note last byte 00h not copied                                ;
;                                                              ;
; INPUT:   DS:SI = Source address (segment:offset)             ;
;          ES:DI = Destination address (segment:offset)        ;
; OUTPUT:  SI = Modified by copy                               ;
;          DI = Modified by copy                               ;
;          Memory at [Input DI] modified                       ;
;--------------------------------------------------------------;

StringWrite:
cld
@@:
lodsb
cmp al,0
je @f
stosb
jmp @b
@@:
ret

;--- Customized formats support ---

;--- Print CR, LF and source string ---------------------------;
; INPUT:   DS:SI = Source address (segment:offset)             ;
;          ES:DI = Destination address (segment:offset)        ;
; OUTPUT:  SI = Modified by copy                               ;
;          DI = Modified by copy                               ;
;          Memory at [Input DI] modified                       ;
;--------------------------------------------------------------;

StringWrite_CRLF:
cld
mov ax,0A0Dh
stosw
jmp StringWrite

;--- Print " = " and source string, for " parameter = value " ----;
; INPUT:   DS:SI = Source address (segment:offset)                ;
;          ES:DI = Destination address (segment:offset)           ;
; OUTPUT:  SI = Modified by copy                                  ;
;          DI = Modified by copy                                  ;
;          Memory at [Input DI] modified                          ;
;-----------------------------------------------------------------;

StringWrite_CRLF_Equal:
call StringWrite_CRLF
mov ax,' ='
StoreChars:
stosw
stosb
ret

;--- Print CR, LF, " : " and source string, for " parameter = value " ---;
; INPUT:   DS:SI = Source address (segment:offset)                       ;
;          ES:DI = Destination address (segment:offset)                  ;
; OUTPUT:  SI = Modified by copy                                         ;
;          DI = Modified by copy                                         ;
;          Memory at [Input DI] modified                                 ;
;------------------------------------------------------------------------;

StringWrite_CRLF_Colon:
call StringWrite_CRLF
mov ax,' :'
jmp StoreChars 

