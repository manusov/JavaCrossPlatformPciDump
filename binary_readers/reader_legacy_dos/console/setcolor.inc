;---------- Set console color ----------------------------------------------;
; INPUT:   AL = Color code, same as CGA/EGA/VGA video attribute             ;
;          DS = Segment SEG_DATA                                            ;
; OUTPUT:  None                                                             ;
;---------------------------------------------------------------------------;

SetColor:
mov [ActiveColor],al
ret
