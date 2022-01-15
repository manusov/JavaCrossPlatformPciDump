;---------- Print 64-bit Hex Number ---------------------------;
; INPUT:  EDX:EAX = Number, EDX=High32, EAX=Low32              ;
;         ES:DI = Destination Pointer (segment:offset)         ;
; OUTPUT: DI = Modify                                          ;
;--------------------------------------------------------------;

HexPrint64:
xchg eax,edx
call HexPrint32
xchg eax,edx
; no RET, continue at next subroutine

;---------- Print 32-bit Hex Number ---------------------------;
; INPUT:  EAX = Number                                         ;
;         ES:DI = Destination Pointer (segment:offset)         ;
; OUTPUT: DI = Modify                                          ;
;--------------------------------------------------------------;

HexPrint32:
push eax
ror eax,16
call HexPrint16
pop eax
; no RET, continue at next subroutine

;---------- Print 16-bit Hex Number ---------------------------;
; INPUT:  AX  = Number                                         ;
;         ES:DI = Destination Pointer (segment:offset)         ;
; OUTPUT: DI = Modify                                          ;
;--------------------------------------------------------------;

HexPrint16:
push eax
xchg al,ah
call HexPrint8
pop eax
; no RET, continue at next subroutine

;---------- Print 8-bit Hex Number ----------------------------;
; INPUT:  AL  = Number                                         ;
;         ES:DI = Destination Pointer (segment:offset)         ;
; OUTPUT: DI = Modify                                          ;
;--------------------------------------------------------------;

HexPrint8:
push eax
ror al,4
call HexPrint4
pop eax
; no RET, continue at next subroutine

;---------- Print 4-bit Hex Number ----------------------------;
; INPUT:  AL  = Number (bits 0-3)                              ;
;         ES:DI = Destination Pointer (segment:offset)         ;
; OUTPUT: DI = Modify                                          ;
;--------------------------------------------------------------;

HexPrint4:
cld
push eax
and al,0Fh
add al,90h
daa
adc al,40h
daa
stosb
pop eax
ret