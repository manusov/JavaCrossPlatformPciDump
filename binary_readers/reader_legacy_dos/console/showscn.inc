;---------- Show scenario options settings ---------------------------------;
;                                                                           ;
; INPUT:  DS:CX = Pointer to options descriptors list                       ;
;         DS:DX = Pointer to work buffer for prepare text data              ;
;                 no limits provided, caller must associate buffer size and ;
;                 text output size, typically additional space available    ;
;         DS    = Segment SEG_DATA                                          ;
;                                                                           ;
; OUTPUT: None                                                              ;
;         Use memory at [input DS:DX]                                       ;
;                                                                           ;         
;---------------------------------------------------------------------------;

ShowScenario:

cld
push eax ebx ecx edx esi edi ebp 

; Initializing cycle for show options 
mov bx,cx              ; BX = Pointer to options descriptors list 
mov di,dx              ; DI = Pointer to work buffer for prepare text data 
push di

; Start cycle for show options, build text block in the buffer
.OpInterpreting:
mov al,[bx + X0]       ; AL = Option type from descriptor
cmp al,XEND               
je .OpDone             ; Go exit cycle if terminator detected
cmp al,XLAST
ja .OpDone             ; Go exit cycle if unknown option code

; Write option name
push ax
mov dx,di
mov cx,OPTION_NAME_FORMAT
mov al,' '
rep stosb
xchg di,dx
mov si,[bx + X1]
call StringWrite       ; Write option name, left part of string
mov di,dx
mov ax,'= '
stosw                  ; Write "= " between left and right parts of string 
pop ax                 ; Restore option type, AL = Type
mov si,[bx + X2]       ; SI = Pointer to option value, size is option-specific
; Detect option type = AL
cmp al,XKEY
je .OpKeys
cmp al,XDEC
je .OpDecimal32
cmp al,XHEX
je .OpHex64
cmp al,XSIZE
je .OpSize64

; Option handler = string
.OpString:
mov si,[si]                ; SI = Pointer to raw string
call StringWrite           ; Write option value after " = ", raw string
.OpInterpretingP25:
add bx,XSMALL              ; BX = Pointer, go to next option descriptor
mov ax,0A0Dh
stosw                      ; Make next string, write CR, LF 
jmp .OpInterpreting

; Option handler = keys
.OpKeys:
mov al,[si]                ; AL = Index for sequence of 0-terminated strings
mov ah,0FFh
mov si,[bx + X4]
call StringWriteSelected   ; Write option value after " = ", selected keyword
add bx,XBIG                ; BX = Pointer, go to next option descriptor
mov ax,0A0Dh
stosw                      ; Make next string, write CR, LF 
jmp .OpInterpreting

; Option handler = decimal 32
.OpDecimal32:
mov eax,[si]               ; EAX = Value for visual as 32-bit decimal number
push bx
mov bl,0                   ; BL = Template for print
call DecimalPrint32        ; Write option value after " = ", decimal number
pop bx
jmp .OpInterpretingP25

; Option handler = hex 64
.OpHex64:
mov eax,[si + 0]           ; EDX:EAX = Value for visual as 64-bit hex number
mov edx,[si + 4]
call HexPrint64            ; Write option value after " = ", hex number
mov al,'h'
stosb
jmp .OpInterpretingP25

; Option handler = size 64
.OpSize64:
mov eax,[si + 0]     ; EDX:EAX = Value for visual as 64-bit size, can use K/M/G
mov edx,[si + 4]
push bx
mov bl,0FFh
call SizePrint64     ; Write option value after " = ", size
pop bx
jmp .OpInterpretingP25

; Termination
.OpDone:
mov ax,0A0Dh
stosw                   ; Make next string, write CR, LF 
mov al,0
stosb                   ; Terminate all sequence of strings, write 0 byte
pop cx

; Read data from prepared buffer and display to console, 
; optionally save to report file
call InternalConsoleWrite

; Exit
pop ebp edi esi edx ecx ebx eax 
ret
