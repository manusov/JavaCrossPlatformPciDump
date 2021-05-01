;---------- Wait for press any key -----------------------------------------;
; INPUT:   None                                                             ;
; OUTPUT:  AL = ASCII code after keyboard input                             ;
;          AH = Scan code after keyboard input                              ;
;---------------------------------------------------------------------------;

ConsoleRead:
cmp [OptionWaitkey],0
je @f
mov ah,0
int 16h
@@:
ret