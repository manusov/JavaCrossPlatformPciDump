;---------- Show details about detected error and wait key press -----------;
;                                                                           ;
; INPUT:  DS:CX = Pointer to error description first string,                ; 
;                 CX = 0 means skip                                         ;
;         DS:DX = Pointer to error description second string,               ; 
;                 DX = 0 means skip                                         ;
;         AX    = DOS error code for decoding by strings pool and           ;
;                 show string "<Error name> (code)", AX = 0 means skip      ;
;         DS:DI = Pointer to work ( transit ) buffer for prepare text data  ;
;                 no limits provided, caller must associate buffer size and ;
;                 text output size, typically additional space available    ;
;         DS    = Segment SEG_DATA                                          ;
;                                                                           ;
; OUTPUT: None                                                              ;
;         Use memory at [input DI]                                          ;
;                                                                           ;         
;---------------------------------------------------------------------------;


ShowError:
push es eax ebx ecx edx esi edi ebp
push ds
pop es

; Set color and write "Error: " message part
push di ax dx cx
mov al,FOREGROUND_RED + FOREGROUND_INTENSITY
call SetFgColor                 ; Color for "Error: " message part
mov cx,MsgError
call InternalConsoleWrite
pop cx                          ; CX = Input CX = string 1

; Set color and conditionally write first string
mov al, FOREGROUND_RED + FOREGROUND_GREEN + FOREGROUND_BLUE + FOREGROUND_INTENSITY
call SetFgColor
jcxz @f                         ; Go skip if string pointer = 0
call InternalConsoleWrite       ; First string about error
@@:
pop cx                          ; CX = Input DX = string 2

; Conditionally write second string with alignment for "Error: " part
jecxz @f                        ; Go skip if string pointer = 0
push cx
mov cx,CrLf
call InternalConsoleWrite       ; Next string
mov cx,MsgErrorTab
call InternalConsoleWrite       ; Tabulation for alignment for "Error: " part
pop cx
call InternalConsoleWrite       ; Second string about error
@@:
pop ax di

; Conditionally write third string with alignment for "Error: " part
movzx ebx,ax                    ; EBX = Input AX = DOS error code
test ebx,ebx
jz @f                           ; Go skip if error code = 0
mov cx,CrLf
call InternalConsoleWrite       ; Next string
mov cx,MsgErrorTab
call InternalConsoleWrite       ; Tabulation for alignment for "Error: " part 
push di
mov si,MsgErrorOS               ; SI = Pointer to string, EDI = To buffer
call StringWrite                ; Write "OS error" to buffer
xchg eax,ebx                    ; EAX = WinAPI error code
push ax
; mov bl,0                      ; BL  = Numeric template control
; call DecimalPrint32           ; Write error code decimal number to buffer
call HexPrint8
mov al,'h'
stosb
;
mov ax,' ='
stosw
stosb
pop ax                           ; AX = Input AX = DOS error code
call DecodeError                 ; Write OS error description string to buffer
mov al,0
stosb
pop cx                          ; CX = Input DI = buffer pointer
call InternalConsoleWrite       ; Write from buffer to console 
@@:

; Restore console color, skip string and write done message "Press any key..."
call GetColor
call SetColor                    ; Restore original color
mov cx,CrLf2
call InternalConsoleWrite
mov cx,DoneMsgNoWait             ; CX = Pointer to message 1
cmp [OptionWaitkey],0
je  @f
mov cx,DoneMsgWait               ; CX = Pointer to message 2
@@:
call InternalConsoleWrite

; Wait key press, after key pressed skip string
call ConsoleRead
mov cx,CrLf2
call InternalConsoleWrite

; Exit
pop ebp edi esi edx ecx ebx eax es 
ret
