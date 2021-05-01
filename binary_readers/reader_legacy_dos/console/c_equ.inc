;------------------------------------------------------------------------------;
;                         Console support library:                             ;
;                       global used data definitions.                          ;
;            All offsets (pointers) associated with SEG_DATA segment.          ; 
;------------------------------------------------------------------------------;

; Color masks

CLEAR_FOREGROUND       EQU  0F0h
CLEAR_BACKGROUND       EQU  00Fh
SELECT_FOREGROUND      EQU  00Fh
SELECT_BACKGROUND      EQU  0F0h

; Color values

FOREGROUND_BLUE        EQU  01h
FOREGROUND_GREEN       EQU  02h
FOREGROUND_RED         EQU  04h
FOREGROUND_INTENSITY   EQU  08h
BACKGROUND_BLUE        EQU  010h
BACKGROUND_GREEN       EQU  020h
BACKGROUND_RED         EQU  040h
FOREGROUND_BLINKING    EQU  080h

; Options descriptors

XEND    EQU  0      ; Types of descriptors
XKEY    EQU  1 
XDEC    EQU  2
XHEX    EQU  3
XSIZE   EQU  4
XSTR    EQU  5
XLAST   EQU  5

X0      EQU  0      ; Offsets of descriptors fields
X1      EQU  1
X2      EQU  3
X3      EQU  5
X4      EQU  7

XBIG    EQU  9      ; Addends for addressing descriptors sequence
XSMALL  EQU  7
XDELTA  EQU  XBIG - XSMALL

macro OPTION_END
{
DB  XEND         ; ID = 0 = Terminator for list of options descriptors
}

macro OPTION_KEYS  x1, x2, x3, x4
{
DB  XKEY   ; ID = 1 = means option is list of keywords
DW  x1     ; Pointer to option long name string, 0-terminated
DW  x2     ; Pointer to option value = byte 
DW  x3     ; Pointer to option single word short name string, for detection
DW  x4     ; Pointer to list of 0-terminated keywords, 0,0 means end of list 
}

macro OPTION_DECIMAL_32  x1, x2, x3
{
DB  XDEC   ; ID = 2 = means 32-bit unsigned value, interpreted as decimal
DW  x1     ; Pointer to option long name string, 0-terminated
DW  x2     ; Pointer to option value = dword
DW  x3     ; Pointer to option single word short name string, for detection 
}

macro OPTION_HEX_64  x1, x2, x3
{
DB  XHEX   ; ID = 3 = means 64-bit unsigned value, interpreted as hex
DW  x1     ; Pointer to option long name string, 0-terminated
DW  x2     ; Pointer to option value = qword
DW  x3     ; Pointer to option single word short name string, for detection 
}

macro OPTION_SIZE_64  x1, x2, x3
{
DB  XSIZE  ; ID = 3 = means 64-bit unsigned value, interpreted as hex
DW  x1     ; Pointer to option long name string, 0-terminated
DW  x2     ; Pointer to option value = qword
DW  x3     ; Pointer to option single word short name string, for detection 
}

macro OPTION_STRING  x1, x2, x3
{
DB  XSTR   ; ID = 5 = means pointer to pointer to string
DW  x1     ; Pointer to option long name string, 0-terminated
DW  x2     ; Pointer to option value = pointer to string, 0-terminated
DW  x3     ; Pointer to option single word short name string, for detection 
}

; Support strings formatting and options strings save

OPTION_NAME_FORMAT  EQU  38    ; Formatted output left part before " = " size  
PATH_BUFFER_SIZE    EQU  256   ; Limit for buffers with paths, include last 0
