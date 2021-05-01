;---------- Translation error code to error name string -------;
;                                                              ;
; INPUT:   AX = Error code for translation                     ;
;          ES:DI = Destination address for build text string   ;
;          DS    = Segment SEG_DATA                            ;
;                                                              ;
; OUTPUT:  DI = Modified by string write                       ;
;          Memory at [Input DI] = output string                ;
;                                 not 0-terminated             ;
;--------------------------------------------------------------;

DecodeError:
push esi

dec ax                             ; Unknown if 00 ( decremented = FF )
cmp al,0FFh
je EM_10                           ; 00h = unknown
mov si,EMsg_Pool_01_23
cmp al,23h - 1
jbe EM_12                          ; 01h-23h = decode
sub al,50h - 1
jb EM_10                           ; 24h - 4Fh = unknown
mov si,EMsg_Pool_50_53
cmp al,3
jbe EM_12                          ; 50h-53h = decode
sub al,10h
jb EM_10                           ; 54h-5Fh = unknown
mov si,EMsg_Pool_60_62
cmp al,2
jbe EM_12                          ; 60h-62h = decode
sub al,21h
jb EM_10                           ; 8Ah-EFh = unknown
mov si,EMsg_Pool_81_89
cmp al,08h
jbe EM_12                          ; 81h-89h = decode
sub al,6Fh
jb EM_10                           ; 8Ah-EFh = unknown
mov si,EMsg_Pool_F0_FC
cmp al,0Ch
jbe EM_12                          ; F0h-FCh = decode
EM_10:
mov si,EMsg_Pool_FF_FF             ; FDh-FFh = unknown
jmp	EM_11
EM_12:
mov ah,0FFh                        ; No limits, AL pre-validated
call String_Select                 ; String = F( SI, AL, AH )
EM_11:
call StringWrite
mov al,'.'
stosb
pop esi
ret

;--- Select string from strings sequence ---------------------------------;
; INPUT:   AL    = Selector, number of strings to skip                    ;
;          AH    = Selector Limit, skip move if AL>AH                     ;
;          DS:SI = Source Buffer, pointer to nul-terminated strings pool  ;
;                                                                         ;
; OUTPUT:  SI    = Pointer to selected string                             ;
; ------------------------------------------------------------------------;
String_Select:
cld
cmp al,ah
ja Exit_SL
push ax cx
mov cl,al
mov ch,0
jcxz Zero_SL
Skip_SL:
lodsb
cmp al,0
jne Skip_SL
loop Skip_SL
Zero_SL:
pop cx ax
Exit_SL:
ret
