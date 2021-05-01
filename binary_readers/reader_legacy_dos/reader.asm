; --- TODO ---
; "+-" under verification
; "++" verified OK  
;
; 1)  [+-] Option skipnodev.
; 2)  [+-] Option skipnofnc.
; 3)  [+-] Show PCIBIOS error message and error group name.
; 4)  [+-] Remove duplicated string "PCI bus not found".
; 5)  [+-] Bug fix: PCI binary file not closed if runtime error, correct branch.
; 6)  [+-] Progress visualization by buses numbers.
; 7)  [+-] Error detection for special case: disk full.
; 8)  [  ] Additional check parameters compatibility, for example bus number > 255.
; 9)  [  ] Verify error reporting for scenario (input.txt) file parsing.
; 10) [  ] Verify error detection: "system\c_const.inc" strings "EMsg_...".
; 11) [  ] 16-bit parse scenario and some other use 32-bit addressing,
;          this possible if address bits [31-16] = 0,
;          scan for "[e" and replace to 16-bit (Note Big Real Mode not used here).
; 12) [  ] Elements of 32-bit addressing ("[eax] ... [edi]" ) 
;          should be removed at 16-bit version (Note Big Real Mode not used here).
;          scan for "[e" and replace to 16-bit.
; 13) [  ] Select right messages for cases wait key enabled and disabled.
; 14) [  ] Global variables and subroutines input parameters design must be regular.
; 15) [  ] Errors classification, example PCIBIOS unexpected error code must view.   
; 16) [  ] Verify options: display, report, waitkey.
; 17) [  ] Total verify all options.
; ---

;==============================================================================;
;                                                                              ;
;   Template for native agent console application build. DOS 16-bit version.   ;
;  This file is main module: translation object, interconnecting all modules.  ;
;       This implementation: read PCI configuration space to binary file,      ;
;  legacy 16-bit DOS version by PCIBIOS, without extended configuration space. ;
;                                                                              ;
;        Translation by Flat Assembler version 1.73.27 (Jan 27, 2021)          ;
;                         http://flatassembler.net/                            ;
;                                                                              ;
;       Edit by FASM Editor 2.0, use this editor for correct tabulations.      ;
;               http://asmworld.ru/instrumenty/fasm-editor-2-0/                ;
;                                                                              ;
;==============================================================================;

format MZ
entry SEG_CODE:start  ; program entry point
stack 4000h           ; stack size, default 100h is small for registers dump

;------------------------------ Definitions -----------------------------------;
include 'console\c_equ.inc'         ; Equations for color console support
include 'dump\c_equ.inc'            ; Equations for CPU regs. and mem. dump
include 'system\c_equ.inc'          ; Equations for OS and hardware support
include 'target\c_equ.inc'          ; Equations for PCI cfg. space

TEMP_BUFFER_ALIGNMENT  EQU  4096    ; Temporary buffer alignment = page
TEMP_BUFFER_SIZE       EQU  32768   ; Temporary buffer size = 32 KB 
IPB_CONST              EQU  4096    ; Input Parameters Block size, bytes
OPB_CONST              EQU  4096    ; Output Parameters Block size, bytes
DEFAULT_COLOR          EQU  7       ; Default color is white on black

;----------------------------- Code segment -----------------------------------; 
segment SEG_CODE
start:

; Set DS, ES for data segment addressing
push SEG_DATA
pop ds
push ds
pop es

; Clear report support variables, required before first ConsoleWrite
xor ax,ax
mov [ReportHandle],ax       ; Report file handle = 0 means report disabled
mov [ReportName],ax

; Start application, detect 80386+ processor and other minimal requirements
call System_Verify
jc ErrorMinimalRequirements

; Detect processor real mode
; This restriction skipped when debug under Windows XP 32-bit
; smsw ax
; test al,1
; mov al,4
; jnz ErrorMinimalRequirements 

; Reserved places
; Initializing console input handle - not used for Legacy DOS 16-bit version 
; Initializing console output handle - not used for Legacy DOS 16-bit version
; Detect command line - not used for Legacy DOS 16-bit version
; Title string - not used for Legacy DOS 16-bit version
; Get console screen buffer information - not used for Legacy DOS 16-bit version

; Set video output defaults
mov ah,0Fh                  ; Video BIOS: read current video mode
int 10h
mov word [ActiveMode],ax    ; AH = Columns, AL = Current mode
mov bl,DEFAULT_COLOR
mov word [ActiveColor],bx   ; BH = Current page, BL = Color
mov [DefaultColor],bl

; Initializing string-type options,
; for source and destination files by file I/O benchmark scenario
mov si,DefaultDumpfile      ; SI = Pointer to default path of binary file
mov di,BufferDumpname       ; DI = Pointer to buffer for binary file path
call StringWrite            ; Copy default path, later can override by scenario
mov al,0
stosb                       ; Terminate path string by 0

; Load scenario file: INPUT.TXT
mov cx,InputName                ; CX = Pointer to scenario file name
mov dx,ScenarioHandle           ; DX = Pointer to scenario file handle
mov si,ScenarioBase             ; SI = Pointer to pointer to buffer
mov di,ScenarioSize             ; DI = Pointer to pointer to size
mov word [si],TEMP_BUFFER       ; Write buffer base address
mov word [di],TEMP_BUFFER_SIZE  ; Write buffer size limit
call ReadScenario

; Check loaded scenario file size, detect error if loaded size = buffer size
cmp [ScenarioSize], TEMP_BUFFER_SIZE
mov cx,MsgInputSize             ; CX = Base address for error message
jae ErrorProgramSingleParm      ; Go error if size limit 

; Interpreting input ( scenario ) file, update options values variables
mov cx,TEMP_BUFFER              ; CX = Pointer to buffer with scenario file
mov dx,[ScenarioSize]
add dx,cx                       ; DX = Buffer limit, addr. of first not valid
mov si,OpDesc                   ; SI = Pointer to options descriptors list
mov di,ReportStatus             ; DI = Pointer to error status info
call ParseScenario

; Check option " display = on|off " , clear output handle if " off "
; Not used for Legacy DOS 16-bit version, interpreted at ConsoleWrite subrout.
; Check option " waitkey = on|off " , clear input handle if " off "
; Not used for Legacy DOS 16-bit version, interpreted at ConsoleRead subrout.

; Check parsing status, this must be after options interpreting
mov cx,[ErrorPointer1]      ; CX = Pointer to first error description string
mov dx,[ErrorPointer2]      ; DX = Pointer to second error description string
test ax,ax
jz ErrorProgramDualParm     ; Go if input scenario file parsing error

; Start message, only after loading options, possible " display = off "
mov cx,StartMsg             ; CX = Pointer to string for output
mov bx,[ReportHandle]       ; BX = Report file handle
mov dx,[ReportName]         ; DX = Report file name
call ConsoleWrite           ; Output first message, output = display + file
test ax,ax
jz ExitProgram              ; Silent exit if console write failed

; Initializing save output ( report ) file mechanism: OUTPUT.TXT 
cmp [OptionReport],0
je @f                       ; Go skip create report if option " report = off "
mov cx,OutputName           ; CX = Pointer to report file name
mov dx,ReportHandle         ; DX = Pointer to report file handle
mov [ReportName],cx
call CreateReport
@@:
; Show list with options settings
mov cx,OpDesc               ; DS:CX = Pointer to options descriptors list
mov dx,TEMP_BUFFER          ; DS:DX = Pointer to buffer for build text
call ShowScenario

;---------- Call target debug fragment and visual results ---------------------;
; Task = Save PCI configuration space,
; console output ( optional ) and write to report file ( optional )
; executed in this subroutine 
mov cx,TEMP_BUFFER          ; DS:CX = Pointer to transit buffer
mov dx,ReportStatus         ; DS:DX = Pointer to error status info
call PCI_Context
test ax,ax
mov cx,[ErrorPointer1]      ; CX = Pointer to first error description string
mov dx,[ErrorPointer2]      ; DX = Pointer to second error description string
mov ax,[ErrorCode]          ; AX = OS API error code
jz ErrorProgramTripleParm   ; Go if input scenario file parsing error

;---------- End of target debug fragment, continue console output -------------;
; This for "Press ENTER ..." not add to text report
xor ax,ax
mov [ReportHandle],ax         ; Clear report file name handle
mov [ReportName],ax           ; Clear report file name pointer 
; Restore original color
call GetColor                 ; Return EAX = Original ( OS ) console color
call SetColor                 ; Set color by input ECX
; Done message, write to console ( optional ) and report file ( optional )
mov cx,DoneMsgNoWait          ; CX = Pointer to message 1
cmp [OptionWaitkey],0
je  @f
mov cx,DoneMsgWait            ; CX = Pointer to message 2
@@:
mov bx,[ReportHandle]         ; BX = Output handle
mov dx,[ReportName]           ; DX = Pointer to report file name
call ConsoleWrite 
; Wait key press
call ConsoleRead              ; Console input
mov cx,CrLf2                  ; CX = Pointer to 0Dh, 0Ah ( CR, LF )
mov bx,[ReportHandle]         ; BX = Output handle
mov dx,[ReportName]           ; DX = Pointer to report file name
call ConsoleWrite             ; Console output

;---------- Exit application, this point used if no errors --------------------;
ExitProgram:
mov ax,4C00h
int 21h

;---------- Error handling and exit application -------------------------------; 
; This code for errors, detected after 80386+ and other platform features
; validation, no old platform restrictions.

ErrorProgramSingleParm:    ; Here valid Parm#1 = ECX = Pointer to first string
xor dx,dx                  ; Parm#2 = DX = Pointer to second string, not used 
ErrorProgramDualParm:      ; Here used 2 params: ECX, EDX
xor ax,ax                  ; Parm#3 = AX  = OS API error code, not used 
ErrorProgramTripleParm:    ; Here used all 3 params: ECX, EDX, EAX
mov di,TEMP_BUFFER         ; Parm#4 = Pointer to work buffer
call ShowError             ; Show error message
mov ax,4C01h
int 21h

;---------- Error handling and exit application -------------------------------; 
; This code can be executed before 80386+ detection, only 8086 instructions here
; AX = Pointer to error description string at SEG_CONSTANTS 

ErrorMinimalRequirements:
mov dx,MsgBadCPU
cmp al,1
je @f
mov dx,MsgBadMemory
cmp al,2 
je @f
mov dx,MsgBadDOS
cmp al,3
je @f
mov dx,MsgBadMode
cmp al,4
je @f
mov dx,MsgBadUnknown
@@:

push dx
mov dx,MsgBad
mov ah,9
int 21h
pop dx
mov ah,9
int 21h
mov dx,MsgAnyKey
mov ah,9
int 21h
mov ah,8
int 21h
mov dx,CrLf_24
mov ah,9
int 21h
mov ax,4C02h
int 21h

;---------------------- Libraries at code segment -----------------------------;
include 'console\c_code.inc'   ; Connect library subroutines
include 'dump\c_code.inc'
include 'system\c_code.inc'
include 'target\c_code.inc'    ; Connect target functionality subroutines

;------------------- Data segment, part 1, constants --------------------------;
; Constants located before variables for EXE file space minimization
segment SEG_DATA
include 'console\c_const.inc'  ; Library constants
include 'dump\c_const.inc'
include 'system\c_const.inc'
include 'target\c_const.inc'   ; Target functionality constants 

;------------------- Data segment, part 2, variables --------------------------;
; Variables located after constants for EXE file space minimization
include 'console\c_var.inc'    ; Library variables
include 'dump\c_var.inc'
include 'system\c_var.inc'
include 'target\c_var.inc'     ; Target functionality variables
; Multifunctional buffer
align  TEMP_BUFFER_ALIGNMENT 
TEMP_BUFFER  DB  TEMP_BUFFER_SIZE DUP (?)
