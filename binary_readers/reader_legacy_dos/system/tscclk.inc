;------------------------------------------------------------------------;
; Measure CPU TSC (Time Stamp Counter) delta TSC per 18 ticks.           ;
; IMPORTANT NOTE. Output value not a frequency, timer IRQ0 period is     ;
; approx. 54.925416 ms.                                                  ;
; 18 * 54.925416 ms = 988.6 ms (not a 1 second).                         ;
; Correction required, better by floating point arithmetics.             ;   
;                                                                        ;
; INPUT:   None                                                          ;
;                                                                        ;
; OUTPUT:  CF flag = Status: 0(NC)=Measured OK, 1(C)=Measurement error	 ;
;          EDX:EAX = delta TSC per 18 timer ticks                        ;                                   
;          For frequency in Hz, required EDX:EAX correction,             ; 
;          better by floating point arithmetics                          ;
;          Output EDX:EAX valid only if CF=0(NC)                         ;
;------------------------------------------------------------------------;

MeasureCpuClk:
push ds ebx ecx esi edi 
;--- Prepare parameters, DS:BX = Pointer to DOS Timer ---
push 0
pop ds
mov bx,046Ch
;--- Wait for timer phase ---
mov cl,[bx]
.L0:
cmp cl,[bx]
je .L0
mov cl,[bx]
;--- Get TSC at EDI:ESI ---
rdtsc
mov esi,eax
mov edi,edx
;--- Wait 18 ticks ---
mov ax,18
.L1:
cmp cl,[bx]
je .L1
mov cl,[bx]
dec ax
jnz .L1 
;--- Get TSC, calculate delta TSC per 18 ticks at EDX:EAX ---
rdtsc
sub eax,esi
sbb edx,edi
;--- Done ---
pop edi esi ecx ebx ds
ret