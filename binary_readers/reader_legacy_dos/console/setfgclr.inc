;--- Set console foreground color, background color as saved at start ------;
; INPUT:   AL = Color code, same as CGA/EGA/VGA video attribute             ;
;               here used only bits [3-0], bits [7-4] ignored,              ;
;               because this function set foreground color only             ;
;          DS = Segment SEG_DATA                                            ; 
; OUTPUT:  None                                                             ;
;---------------------------------------------------------------------------;

SetFgColor:
and al,SELECT_FOREGROUND
and [ActiveColor],SELECT_BACKGROUND
or [ActiveColor],al
ret
