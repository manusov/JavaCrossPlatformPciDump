;------------------------------------------------------------------------------;
;                Registers and memory dump subroutines library:                ;
;             connect include files with globally used subroutines.            ; 
;------------------------------------------------------------------------------;

;--- Dump 8 32-bit general purpose registers ----;
; INPUT:   GPR registers values for dump         ;
;          DS = Segment SEG_DATA                 ;
; OUTPUT:  None                                  ; 
;------------------------------------------------;

DumpGPR32:
; Save registers for non-volatile and for dump
push es eax ebx ecx edx esi edi ebp
push ds
pop es
lea eax,[esp + 7*4 + 2 + 2]   ; Correct ESP copy: 7 * 32 bit, 16-bit ES and IP
push eax
; Initializing dump cycle
cld
mov bx,8
mov si,NamesGPR32
lea ebp,[esp + 7*4 ]
; Dump cycle with 8 Read instructions
.L0:
mov al,REGISTER_NAME_COLOR
call SetFgColor
mov cx,si
call InternalConsoleWrite   ; Print register name, accept DS:CX 
mov al,REGISTER_VALUE_COLOR
call SetFgColor
mov di,TEMP_BUFFER
mov cx,di
mov al,' '
stosb
mov eax,[bp]
call HexPrint32
mov al,0
stosb
call InternalConsoleWrite   ; Print register value, accept DS:CX
mov cx,CrLf
call InternalConsoleWrite   ; Print CR, LF, accept DS:CX
sub bp,4                    ; Select next register at stack frame
add si,4                    ; Select next text string for register name
dec bx                      ; Cycle counter for 16 general-purpose registers
jnz .L0
; Restore original color
call GetColor
call SetColor
; Insert empty string
mov cx,CrLf
call InternalConsoleWrite
; Restore registers and return
pop eax ebp edi esi edx ecx ebx eax es
ret

;--- Dump 6 16-bit segment selectors registers ----------;
; INPUT:   Segment selectors registers values for dump,  ;
;          special note for data segment:                ;
;          DS = Segment SEG_DATA                         ;
; OUTPUT:  None                                          ; 
;--------------------------------------------------------;

DumpSelectors:
; Push registers include volatile for API
push es eax ebx ecx edx esi edi ebp
; Push 6 selectors
xor eax,eax
mov ax,gs
push eax      ; PUSH #1
mov ax,fs
push eax      ; PUSH #2
mov ax,ss
push eax      ; PUSH #3
mov ax,es
push eax      ; PUSH #4
mov ax,ds
push eax      ; PUSH #5
mov ax,cs
push eax      ; PUSH #6
; Initializing dump cycle
cld
mov bx,6
mov si,NamesSelectors
push ds
pop es
; Dump cycle with pop 6 selectors
.L0:
mov al,REGISTER_NAME_COLOR
call SetFgColor
mov cx,si
call InternalConsoleWrite 
mov al,REGISTER_VALUE_COLOR
call SetFgColor
mov di,TEMP_BUFFER
mov cx,di
mov al,' '
stosb
pop eax             ; POP #[6-1] 
call HexPrint16
mov al,0
stosb
call InternalConsoleWrite
mov cx,CrLf
call InternalConsoleWrite
add si,3           ; Select next text string for register name
dec bx             ; Cycle counter for 6 segment selectors registers
jnz .L0
; Restore original color
call GetColor
call SetColor
; Insert empty string
mov cx,CrLf
call InternalConsoleWrite
; Restore registers and return
pop ebp edi esi edx ecx ebx eax es
ret

;--- Dump 8 x87 FPU registers --------------------------;
; INPUT:   FPU registers values for dump                ;
;          DS = Segment SEG_DATA                        ;
; OUTPUT:  None                                         ; 
;-------------------------------------------------------;

DumpFPU:
; Push registers include volatile for API, initialize ESP and ES
push es esp eax ebx ecx edx esi edi ebp
and esp,0000FFFFh  ; Prevent GP fault if ESP.31-16 non zero at real mode
push ds            ; Required ES = DS for stosb and other ES usage
pop es
; Store 8 registers
sub esp,64
fstp qword [esp + 8*0]
fstp qword [esp + 8*1]
fstp qword [esp + 8*2]
fstp qword [esp + 8*3]
fstp qword [esp + 8*4]
fstp qword [esp + 8*5]
fstp qword [esp + 8*6]
fstp qword [esp + 8*7]
; Initializing dump cycle
cld
mov bp,8
mov si,NamesFPU
; Dump cycle with pop 8 registers
.L0:
mov al,REGISTER_NAME_COLOR
call SetFgColor
mov cx,si
call InternalConsoleWrite 
mov al,REGISTER_VALUE_COLOR
call SetFgColor
mov di,TEMP_BUFFER
mov cx,di
mov al,' '
stosb
pop eax edx         ; POP #[8-1] 
mov bx,0700h
call DoublePrint
mov al,0
stosb
call InternalConsoleWrite
mov cx,CrLf
call InternalConsoleWrite
add si,4           ; Select next text string for register name
dec bp             ; Cycle counter for 8 FPU selectors registers
jnz .L0
; Entry point for return
DumpReturn:
; Restore original color
call GetColor
call SetColor
; Insert empty string
mov cx,CrLf
call InternalConsoleWrite
; Restore registers and return
pop ebp edi esi edx ecx ebx eax esp es
ret

;--- Dump 8 MMX registers ------------------------------;
; INPUT:   MMX registers values for dump                ;
;          DS = Segment SEG_DATA                        ;
; OUTPUT:  None                                         ; 
;-------------------------------------------------------;

DumpMMX:
; Push registers include volatile for API, initialize ESP and ES
push es esp eax ebx ecx edx esi edi ebp
and esp,0000FFFFh  ; Prevent GP fault if ESP.31-16 non zero at real mode
push ds            ; Required ES = DS for stosb and other ES usage
pop es
; Store 8 registers
sub esp,64
movq [esp + 8*0],mm0
movq [esp + 8*1],mm1
movq [esp + 8*2],mm2
movq [esp + 8*3],mm3
movq [esp + 8*4],mm4
movq [esp + 8*5],mm5
movq [esp + 8*6],mm6
movq [esp + 8*7],mm7
; Initializing dump cycle
cld
mov bp,8
mov si,NamesMMX
; Dump cycle with pop 8 registers
.L0:
mov al,REGISTER_NAME_COLOR
call SetFgColor
mov cx,si
call InternalConsoleWrite 
mov al,REGISTER_VALUE_COLOR
call SetFgColor
mov di,TEMP_BUFFER
mov cx,di
mov al,' '
stosb
pop eax edx         ; POP #[8-1] 
call HexPrint64
mov al,0
stosb
call InternalConsoleWrite
mov cx,CrLf
call InternalConsoleWrite
add si,4           ; Select next text string for register name
dec bp             ; Cycle counter for 8 MMX registers
jnz .L0
; Go to restore original color, insert empty string, restore registers
jmp DumpReturn

;--- Dump 8 SSE registers ------------------------------;
; INPUT:   SSE registers values for dump                ;
;          DS = Segment SEG_DATA                        ;
; OUTPUT:  None                                         ; 
;-------------------------------------------------------;

DumpSSE:
; Push registers include volatile for API, initialize ESP and ES
push es esp eax ebx ecx edx esi edi ebp
and esp,0000FFFFh  ; Prevent GP fault if ESP.31-16 non zero at real mode
push ds            ; Required ES = DS for stosb and other ES usage
pop es
; Store 8 registers
sub esp,128
movups [esp + 16*00],xmm0
movups [esp + 16*01],xmm1
movups [esp + 16*02],xmm2
movups [esp + 16*03],xmm3
movups [esp + 16*04],xmm4
movups [esp + 16*05],xmm5
movups [esp + 16*06],xmm6
movups [esp + 16*07],xmm7
; Initializing dump cycle
cld
xor ebp,ebp
; Dump cycle with pop 16 registers
.L0:
; Register name
mov al,REGISTER_NAME_COLOR
call SetFgColor
mov si,NameSSE
mov di,TEMP_BUFFER
call StringWrite
mov eax,ebp
mov bl,0
call DecimalPrint32   ; This number at register name, XMM0-XMM15
cmp bp,9
ja .L1
mov al,' '
stosb
.L1:
mov al,0
stosb
mov cx,TEMP_BUFFER
call InternalConsoleWrite
; Register value
mov al,REGISTER_VALUE_COLOR
call SetFgColor
mov di,TEMP_BUFFER
mov cx,di
mov al,' '
stosb
; XMM[i] data frame start 
mov eax,[esp + 00]
mov edx,[esp + 04]
call HexPrint64
mov al,' '
stosb
mov eax,[esp + 08]
mov edx,[esp + 12] 
call HexPrint64
add esp,16
; XMM[i] data frame start
mov al,0
stosb
call InternalConsoleWrite
; Cycle
mov cx,CrLf
call InternalConsoleWrite
inc bp
cmp bp,8
jnz .L0             ; Cycle counter for 8 SSE registers
; Go to restore original color, insert empty string, restore registers
jmp DumpReturn

;--- Dump 8 AVX256 registers ---------------------------;
; INPUT:   AVX256 registers values for dump             ;
;          DS = Segment SEG_DATA                        ;
; OUTPUT:  None                                         ; 
;-------------------------------------------------------;

DumpAVX256:
; Push registers include volatile for API, initialize ESP and ES
push es esp eax ebx ecx edx esi edi ebp
and esp,0000FFFFh  ; Prevent GP fault if ESP.31-16 non zero at real mode
push ds            ; Required ES = DS for stosb and other ES usage
pop es
; Store 8 registers
sub esp,256
vmovupd [esp + 32*00],ymm0
vmovupd [esp + 32*01],ymm1
vmovupd [esp + 32*02],ymm2
vmovupd [esp + 32*03],ymm3
vmovupd [esp + 32*04],ymm4
vmovupd [esp + 32*05],ymm5
vmovupd [esp + 32*06],ymm6
vmovupd [esp + 32*07],ymm7
; Initializing dump cycle
cld
xor ebp,ebp
; Dump cycle with pop 16 registers
.L0:
; Register name
mov al,REGISTER_NAME_COLOR
call SetFgColor
mov si,NameAVX256
mov di,TEMP_BUFFER
call StringWrite
mov eax,ebp
mov bl,0
call DecimalPrint32   ; This number at register name, YMM0-YMM15
cmp bp,9
ja .L1
mov al,' '
stosb
.L1:
mov al,0
stosb
mov cx,TEMP_BUFFER
call InternalConsoleWrite
; Register value
mov al,REGISTER_VALUE_COLOR
call SetFgColor
mov di,TEMP_BUFFER
mov cx,di
mov al,' '
stosb
; YMM[i] data frame start 
mov eax,[esp + 24]
mov edx,[esp + 24 + 4]
call HexPrint64
mov al,' '
stosb
mov eax,[esp + 16]
mov edx,[esp + 16 + 4] 
call HexPrint64
mov al,' '
stosb
mov eax,[esp + 08]
mov edx,[esp + 08 + 4] 
call HexPrint64
mov al,' '
stosb
mov eax,[esp + 00]
mov edx,[esp + 00 + 4] 
call HexPrint64
add esp,32
; YMM[i] data frame end
mov al,0
stosb
call InternalConsoleWrite
; Cycle
mov cx,CrLf
call InternalConsoleWrite
inc bp
cmp bp,8
jnz .L0             ; Cycle counter for 16 SSE registers
; Go to restore original color, insert empty string, restore registers
jmp DumpReturn

;--- Dump 8 AVX256 registers as double numbers ---------;
; INPUT:   AVX256 registers values for dump             ;
;          DS = Segment SEG_DATA                        ;
; OUTPUT:  None                                         ; 
;-------------------------------------------------------;

DumpAVX256asDouble:
; Push registers include volatile for API, initialize ESP and ES
push es esp eax ebx ecx edx esi edi ebp
and esp,0000FFFFh  ; Prevent GP fault if ESP.31-16 non zero at real mode
push ds            ; Required ES = DS for stosb and other ES usage
pop es
; Store 8 registers
sub esp,256
vmovupd [esp + 32*00],ymm0
vmovupd [esp + 32*01],ymm1
vmovupd [esp + 32*02],ymm2
vmovupd [esp + 32*03],ymm3
vmovupd [esp + 32*04],ymm4
vmovupd [esp + 32*05],ymm5
vmovupd [esp + 32*06],ymm6
vmovupd [esp + 32*07],ymm7
; Initializing dump cycle
cld
xor ebp,ebp
; Dump cycle with pop 16 registers
.L0:
; Register name
mov al,REGISTER_NAME_COLOR
call SetFgColor
mov si,NameAVX256
mov di,TEMP_BUFFER
call StringWrite
mov eax,ebp
mov bl,0
call DecimalPrint32   ; This number at register name, YMM0-YMM15
cmp bp,9
ja .L1
mov al,' '
stosb
.L1:
mov al,0
stosb
mov cx,TEMP_BUFFER
call InternalConsoleWrite
; Register value
mov al,REGISTER_VALUE_COLOR
call SetFgColor
mov di,TEMP_BUFFER
mov al,' '
stosb
; YMM[i] data frame start 
mov eax,[esp + 24]
mov edx,[esp + 24 + 4]
call HelperDoubleDump
mov eax,[esp + 16]
mov edx,[esp + 16 + 4] 
call HelperDoubleDump
mov eax,[esp + 08]
mov edx,[esp + 08 + 4] 
call HelperDoubleDump
mov eax,[esp + 00]
mov edx,[esp + 00 + 4] 
call HelperDoubleDump
add esp,32
; YMM[i] data frame end
mov al,0
stosb
mov cx,TEMP_BUFFER
call InternalConsoleWrite
; Cycle
mov cx,CrLf
call InternalConsoleWrite
inc bp
cmp bp,8
jnz .L0             ; Cycle counter for 16 SSE registers
; Go to restore original color, insert empty string, restore registers
jmp DumpReturn

HelperDoubleDump:
push di ax
mov cx,8
mov al,' '
rep stosb
pop ax di
push di
add di,2
mov bx,0200h
push ax
test edx,edx
js .L1
mov al,'+'
stosb
.L1:
pop ax
call DoublePrint
pop di
add di,7
mov al,' '
cmp byte [di],' '
je .L0
mov al,'\'
.L0:
stosb
ret

;--- Dump 8 AVX512 registers ---------------------------;
; INPUT:   AVX512 registers values for dump             ;
;          DS = Segment SEG_DATA                        ;
; OUTPUT:  None                                         ; 
;-------------------------------------------------------;

DumpAVX512:
; Push registers include volatile for API, initialize ESP and ES
push es esp eax ebx ecx edx esi edi ebp
and esp,0000FFFFh  ; Prevent GP fault if ESP.31-16 non zero at real mode
push ds            ; Required ES = DS for stosb and other ES usage
pop es
; Store 8 registers
sub esp,512
vmovupd [esp + 64*00],zmm0
vmovupd [esp + 64*01],zmm1
vmovupd [esp + 64*02],zmm2
vmovupd [esp + 64*03],zmm3
vmovupd [esp + 64*04],zmm4
vmovupd [esp + 64*05],zmm5
vmovupd [esp + 64*06],zmm6
vmovupd [esp + 64*07],zmm7
; Initializing dump cycle
cld
xor ebp,ebp
; Dump cycle with pop 32 registers
.L0:
; Register name
mov al,REGISTER_NAME_COLOR
call SetFgColor
mov si,NameAVX512
mov di,TEMP_BUFFER
call StringWrite
mov eax,ebp
mov bl,0
call DecimalPrint32   ; This number at register name, YMM0-YMM15
cmp bp,9
ja .L1
mov al,' '
stosb
.L1:
mov al,0
stosb
mov cx,TEMP_BUFFER
call InternalConsoleWrite
; Register value
mov al,REGISTER_VALUE_COLOR
call SetFgColor
mov di,TEMP_BUFFER
mov cx,di
mov al,' '
stosb
; ZMM[i] data frame start 
mov eax,[esp + 56]
mov edx,[esp + 56 + 4]
call HexPrint64
mov al,' '
stosb
mov eax,[esp + 48]
mov edx,[esp + 48 + 4]
call HexPrint64
mov al,' '
stosb
mov eax,[esp + 40]
mov edx,[esp + 40 + 4]
call HexPrint64
mov al,' '
stosb
mov eax,[esp + 32]
mov edx,[esp + 32 + 4]
call HexPrint64
mov si,IntervalAVX512
call StringWrite
mov eax,[esp + 24]
mov edx,[esp + 24 + 4]
call HexPrint64
mov al,' '
stosb
mov eax,[esp + 16]
mov edx,[esp + 16 + 4]
call HexPrint64
mov al,' '
stosb
mov eax,[esp + 08]
mov edx,[esp + 08 + 4]
call HexPrint64
mov al,' '
stosb
mov eax,[esp + 00]
mov edx,[esp + 00 + 4]
call HexPrint64
add esp,64
; ZMM[i] data frame end
mov al,0
stosb
call InternalConsoleWrite
; Cycle
mov cx,CrLf
call InternalConsoleWrite
inc bp
cmp bp,8
jnz .L0             ; Cycle counter for 16 SSE registers
; Go to restore original color, insert empty string, restore registers
jmp DumpReturn

;--- Dump 8 AVX512 registers as double numbers ---------;
; INPUT:   AVX512 registers values for dump             ;
;          DS = Segment SEG_DATA                        ;
; OUTPUT:  None                                         ; 
;-------------------------------------------------------;

DumpAVX512asDouble:
; Push registers include volatile for API, initialize ESP and ES
push es esp eax ebx ecx edx esi edi ebp
and esp,0000FFFFh  ; Prevent GP fault if ESP.31-16 non zero at real mode
push ds            ; Required ES = DS for stosb and other ES usage
pop es
; Store 16 registers
sub esp,512
vmovupd [esp + 64*00],zmm0
vmovupd [esp + 64*01],zmm1
vmovupd [esp + 64*02],zmm2
vmovupd [esp + 64*03],zmm3
vmovupd [esp + 64*04],zmm4
vmovupd [esp + 64*05],zmm5
vmovupd [esp + 64*06],zmm6
vmovupd [esp + 64*07],zmm7
; Initializing dump cycle
cld
xor ebp,ebp
; Dump cycle with pop 32 registers
.L0:
; Register name
mov al,REGISTER_NAME_COLOR
call SetFgColor
mov si,NameAVX512
mov di,TEMP_BUFFER
call StringWrite
mov eax,ebp
mov bl,0
call DecimalPrint32   ; This number at register name, YMM0-YMM15
cmp bp,9
ja .L1
mov al,' '
stosb
.L1:
mov al,0
stosb
mov cx,TEMP_BUFFER
call InternalConsoleWrite
; Register value
mov al,REGISTER_VALUE_COLOR
call SetFgColor
mov di,TEMP_BUFFER
mov al,' '
stosb
; ZMM[i] data frame start 
mov eax,[esp + 56]
mov edx,[esp + 56 + 4]
call HelperDoubleDump
mov eax,[esp + 48]
mov edx,[esp + 48 + 4]
call HelperDoubleDump
mov eax,[esp + 40]
mov edx,[esp + 40 + 4]
call HelperDoubleDump
mov eax,[esp + 32]
mov edx,[esp + 32 + 4]
call HelperDoubleDump
mov eax,[esp + 24]
mov edx,[esp + 24 + 4]
call HelperDoubleDump
mov eax,[esp + 16]
mov edx,[esp + 16 + 4]
call HelperDoubleDump
mov eax,[esp + 08]
mov edx,[esp + 08 + 4]
call HelperDoubleDump
mov eax,[esp + 00]
mov edx,[esp + 00 + 4]
call HelperDoubleDump
add esp,64
; ZMM[i] data frame end
mov al,0
stosb
mov cx,TEMP_BUFFER
call InternalConsoleWrite
; Cycle
mov cx,CrLf
call InternalConsoleWrite
inc bp
cmp bp,8
jnz .L0             ; Cycle counter for 16 SSE registers
; Go to restore original color, insert empty string, restore registers
jmp DumpReturn

;--- Dump memory region --------------------------------;
; INPUT:   ES:SI = Pointer to region for dump           ;
;          BX = Region length, bytes                    ;
;          DS = Segment SEG_DATA                        ;
;          Memory [ES:SI] = data for dump               ;
; OUTPUT:  None                                         ; 
;-------------------------------------------------------;

DumpMemory:
; Push registers include volatile for API, initialize ESP and ES
push es esp eax ebx ecx edx esi edi ebp
and esp,0000FFFFh  ; Prevent GP fault if ESP.31-16 non zero at real mode
and esi,0000FFFFh
push fs es
pop fs
push ds            ; Required ES = DS for stosb and other ES usage
pop es
; Stack variable
push dword 0

.L0:
test dword [esp],0Fh
jnz .L1
mov al,DUMP_ADDRESS_COLOR
call SetFgColor
mov di,TEMP_BUFFER
mov eax,[esp]
call HexPrint32
mov ax,'  '
stosw
mov al,0
stosb
mov cx,TEMP_BUFFER
call InternalConsoleWrite

.L1:
mov al,DUMP_DATA_COLOR
call SetFgColor
mov cx,16
mov bp,bx
push dword [esp] ecx 
mov di,TEMP_BUFFER

.L2:
dec bp
js .L7
mov eax,dword [esp + 4]
mov al,[fs:esi + eax]
call HexPrint8
mov al,' '
stosb
jmp .L8
.L7:
mov ax,'  '
stosw
stosb
.L8:
inc dword [esp + 4]
loop .L2

mov al,' '
stosb
pop ecx eax

cmp cx,bx
jbe .L3
mov cx,bx
.L3:

.L4:
mov eax,dword [esp]
mov al,[fs:esi + eax]
cmp al,' '
jb .L6
cmp al,'z'
jbe .L5
.L6:
mov al,'.'
.L5:
stosb
inc dword [esp]
loop .L4

mov al,0
stosb
mov cx,TEMP_BUFFER
call InternalConsoleWrite

; Cycle
mov cx,CrLf
call InternalConsoleWrite
sub bx,16
ja .L0 

; Go to restore original color, insert empty string, restore registers
pop eax fs
jmp DumpReturn



