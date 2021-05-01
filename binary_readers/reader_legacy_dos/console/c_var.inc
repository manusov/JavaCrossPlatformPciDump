;------------------------------------------------------------------------------;
;                         Console support library:                             ;
;                      variables without pre-defined values.                   ;
;            All offsets (pointers) associated with SEG_DATA segment.          ;     
;------------------------------------------------------------------------------;

; Console input, output, report file, scenario file control variables
ReportName      DW  ?   ; Pointer to report file name ( example = output.txt )
ReportHandle    DW  ?   ; Report file dynamically re-created handle, 0=None
ScenarioHandle  DW  ?   ; Scenario file handle 
ScenarioBase    DW  ?   ; Pointer to scenario file loading address, 0 = None
ScenarioSize    DW  ?   ; Scenario file loading size, 0 = None (load error) 
CommandLine     DW  ?   ; Pointer to command line string
; This 3 variables must be continuous for return status from subroutines 
ReportStatus:
ErrorPointer1   DW  ?   ; Pointer to first error description string, 0=none
ErrorPointer2   DW  ?   ; Pointer to second error description string, 0=none
ErrorCode       DW  ?   ; DOS error code, 0=none    
; Current used video parameters, 
; must be sequental, some fields accessed as 16-bit words 
DefaultColor    DB  ?   ; Default color for restore after operations
ActiveMode      DB  ?   ; Current video mode number
ActiveColumns   DB  ?   ; Current video mode number of columns
ActiveColor     DB  ?   ; Color, same format as CGA/EGA/VGA video attribute
ActivePage      DB  ?   ; Current active video page, actual for text modes
; Buffers for dump binary file path
BufferDumpname  DB  PATH_BUFFER_SIZE  DUP (?) ; Buffer, dump file path and name

