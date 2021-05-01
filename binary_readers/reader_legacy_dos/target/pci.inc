;--- Target functionality: save PCI configuration space to binary file -----;
;                                                                           ;
; INPUT:   CX = Pointer to work buffer, size fixed = 32 KB = 32768 bytes    ;
;          DX = Pointer to 3 output status words:                           ;
;               string 1, string 2 pointers and OS error code               ;
;          DS = Segment SEG_DATA                                            ;
;          All offsets (pointers) associated with SEG_DATA segment.         ;
;                                                                           ;
; OUTPUT:  AX = Status code                                                 ;
;          3 word wariables: string 1, string 2, status at DS:DX updated.   ;
;---------------------------------------------------------------------------;

PCI_BUFFER_POINTER  EQU  word [bp + 20]   ; input CX 
PCI_STATUS_POINTER  EQU  word [bp + 16]   ; input DX
PCI_MIN_BUS         EQU  byte [bp + 00]   ; minimum bus number by option
PCI_MAX_BUS         EQU  byte [bp + 01]   ; maximum bus number by option/overr.
PCI_CURRENT_BUS     EQU  byte [bp + 02]   ; current bus number during save
PCI_SKIP_DEVICE     EQU  byte [bp + 03]   ; skip device mode for 8 functions
PCI_DATA_SIZE       EQU  32768            ; data buffer size


PCI_Context:
cld
xor eax,eax
push es ebx ecx edx esi edi ebp eax   ; EAX as local variable
mov bp,sp

; Blank output error message pointers and status
mov bx,dx
xor ax,ax
mov [bx + 0],ax
mov [bx + 2],ax
mov [bx + 4],ax

; Message before detecting bus
mov cx,Msg_PCI_Detect
call InternalConsoleWrite

; Check input parameters bus numbers validity
mov eax,[OptionMinbus]
mov ecx,[OptionMaxbus]
mov dx,ErrorMinbus
cmp eax,255
ja .PciParmsError
mov dx,ErrorMaxbus
cmp ecx,255
ja .PciParmsError
mov dx,ErrorMinmax
cmp eax,ecx
ja .PciParmsError 

; Detect PCI bus by PCIBIOS and verify PCI bus detection results
mov ax,0B101h
int 1Ah
jc .PciNotDetected     ; Go error if PCIBIOS return CF = 1
test ah,ah
jnz .PciNotDetected    ; Go error if PCIBIOS Status (AH) non zero     
cmp edx,'PCI '
jne .PciNotDetected    ; Go error if PCIBIOS signature mismatch  

; Load options, override maximum bus if selected
; Don't destroy AL, CL, BX
mov ah, byte [OptionMinbus]
mov PCI_MIN_BUS,ah
mov PCI_CURRENT_BUS,ah
mov ah, byte [OptionMaxbus]
cmp [OptionAutomaxbus],0
je @f
mov ah,cl  ; override maximum bus by PCIBIOS return, if override enabled
@@:
mov PCI_MAX_BUS,ah

; Message after detecting bus, start reading PCI and write to binary file
; BH = major, BL = minor, CL = Last bus, AL = mechanism bitmap, 
mov di,PCI_BUFFER_POINTER
mov si,Msg_PCI_Parms
push ax
call StringWrite
mov dx,bx
movzx eax,dh
mov bl,0
call DecimalPrint32   ; PCI version major, BH after PCIBIOS call
mov al,'.'
stosb
movzx eax,dl
call DecimalPrint32   ; PCI version minor, BL after PCIBIOS call
call StringWrite
movzx eax,cl
call DecimalPrint32   ; PCI last bus number, CL after PCIBIOS call 
call StringWrite
pop ax
call HexPrint8        ; PCI config. mechanisms bitmap, AL after PCIBIOS call  
call StringWrite
mov al,0
stosb
mov cx,PCI_BUFFER_POINTER
call InternalConsoleWrite

; Create binary file
mov ah,3Ch              ; AH = 3Ch = Create file function
mov dx,BufferDumpname   ; DS:DX = Pointer to file path and name string
xor cx,cx               ; CX = File attributes
int 21h
jc .PciFileError        ; Go if file operation error
xchg bx,ax              ; BX = File handle

; Begin cycle for buses
.CycleBuses:
xor si,si               ; 32 devices (0-31) per one bus

; Begin cycle for devices at one bus
.CycleDevices:
mov PCI_SKIP_DEVICE,0

; Read data portion, one device ( 8 functions * 4KB registers ) = 32 KB
; Extended configuration space ( registers 0100h - 0FFFh ) not supported
; by legacy mechanism, blanked to 00h
push bx si
mov bx,si
shl bx,3                    ; BL = PCIBIOS parameter: Device and Function
mov bh,PCI_CURRENT_BUS      ; BH = PCIBIOS parameter: Bus
mov si,PCI_BUFFER_POINTER

.CycleFunctions:
xor di,di                   ; DI = PCIBIOS parameter: Register

.CycleRegisters:
mov ax,0B10Ah               ; PCIBIOS read dword function
int 1Ah
jc .StopRegisters           ; Go if PCIBIOS error
cmp PCI_SKIP_DEVICE,0
jne .SkipRegisters          ; Go if context for skip 8 functions
test di,di
jnz .L0                     ; Ignore skip device logic if register not VID:DID 
cmp ecx,0FFFFFFFFh
jne .L0                     ; Ignore skip device logic if VID:DID not FFFFFFFFh 
cmp [OptionSkipnofnc],0     ; Option for skip empty function
jne .SkipRegisters          ; Go skip registers block if SKIPNOFNC = on
cmp [OptionSkipnodev],0     ; Option for skip empty function
jne .SkipRegisters          ; Go skip registers block if SKIPNODEV = on
.L0:
mov [si],ecx                ; This save data from device to memory buffer
add si,4                    ; Memory pointer + 4 (dword)
add di,4                    ; PCIBIOS register address + 4
cmp di,256
jb .CycleRegisters          ; This cycle for read registers from PCI function
jmp .DoneRegisters

.SkipRegisters:
cmp [OptionSkipnodev],0
je .L1                      ; Go if skip empty devices option SKIPNODEV = off
test bl,00000111b
jnz .L1                     ; Go if this is not a function 0
mov PCI_SKIP_DEVICE,1
.L1:
mov [si],ecx                ; This save FFFFFFFFh for skip device branch 
add si,4
add di,4
cmp di,256
jb .L1

.DoneRegisters:         ; Extended configuration space not used by legacy mode
mov cx,960              ; Skip 960 dwords = ( 4096 - 256 ) bytes
xor eax,eax
.CycleBlank:
mov [si],eax
add si,4
loop .CycleBlank

inc bx
test bl,00000111b
jnz .CycleFunctions    ; This cycle for read 8 functions from PCI device  
                       ; Note CF flag = 0 if this branch, after TEST instr.
.StopRegisters:
pop si bx              ; Restore BX = File handle, SI = Registers counter
jc .PciReadErrorClose 

; Write data portion to binary file, 32 KB, one PCI device
; Not required positioning file pointer, because file not closed between writes 
mov ah,40h                   ; AH = 40h = Write file function, BX = Handle
mov dx,PCI_BUFFER_POINTER    ; DS:DX = Buffer
mov cx,PCI_DATA_SIZE         ; CX = Data size
int 21h
jc .PciFileErrorClose        ; Go if file operation error
cmp ax,cx                    ; AX = Really write size, CX = Required write size
mov ax,60h                   ; 60h error code for disk full
jb .PciFileErrorClose        ; Go if status OK, but disk full 

; Cycle for PCI devices at one bus, 32KB per device: 8 functions * 4KB = 32KB
inc si
cmp si,32                    ; 32 devices (0-31) per one bus
jb .CycleDevices             ; This cycle for read 32 devices per PCI bus  

; Output "#" 1 time per 8 bus numbers as progress indication
mov al,PCI_CURRENT_BUS
test al,0111b
jnz @f
mov cx,Msg_PCI_Progress 
call InternalConsoleWrite
@@:

; Cycle for PCI buses, 1 MB per bus: 32 devices * 32 KB = 1 MB  
mov al,PCI_CURRENT_BUS
inc ax
mov PCI_CURRENT_BUS,al
cmp al,PCI_MAX_BUS
jbe .CycleBuses              ; This cycle for read PCI buses

; Close binary file
mov ah,3Eh              ; AH = 3Eh = Close file function, BX = Handle
int 21h
jc .PciFileError        ; Go if file operation error

; Message when save file (all process) done
mov cx,Msg_PCI_OK 
call InternalConsoleWrite

; Exit point for no errors 
.Done:
mov ax,1
.Exit:
pop ebp ebp edi esi edx ecx ebx es
ret
; Exit point for errors
.Error:
xor ax,ax
jmp .Exit

.PciNotDetected:
mov dx,Error_Config
mov ax,00FAh

.ErrorEntry:
push ax
mov cx,Msg_PCI_None
call InternalConsoleWrite
pop ax
mov bx,PCI_STATUS_POINTER
mov word [bx + 0],dx
mov word [bx + 4],ax
jmp .Error

.PciFileErrorClose:     ; At this point BX = File handle for close
push ax
mov ah,3Eh              ; AH = 3Eh = Close file function, BX = Handle
int 21h
pop ax
.PciFileError:
mov dx,Error_PCI_File
jmp .ErrorEntry

.PciReadErrorClose:     ; At this point BX = File handle for close
push ax
mov ah,3Eh              ; AH = 3Eh = Close file function, BX = Handle
int 21h
pop ax
.PciReadError:
mov dx,Error_PCI_Read
movzx ax,ah
cmp al,81h
jb .L10
cmp al,89h
jbe .ErrorEntry
.L10:
mov al,0
jmp .ErrorEntry

.PciParmsError:
xor ax,ax
jmp .ErrorEntry
