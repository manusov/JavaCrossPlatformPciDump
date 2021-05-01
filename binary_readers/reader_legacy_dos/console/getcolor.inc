;---------- Get console color, saved at start-------------------------------;
; INPUT:   DS = Segment SEG_DATA                                            ;
; OUTPUT:  AL = Color code, same as CGA/EGA/VGA video attribute             ;
;---------------------------------------------------------------------------;

GetColor:
mov al,[DefaultColor]
ret
