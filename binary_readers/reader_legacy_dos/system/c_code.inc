;------------------------------------------------------------------------------;
;    Hardware platform and operating system helpers library, this part is:     ;
;                   connect include files with subroutines.                    ;
;------------------------------------------------------------------------------;

include 'sysver.inc'    ; Check minimum requirements
include 'decerr.inc'    ; Translation error code to error name string
include 'cpuid.inc'     ; Verify CPUID support, max. sup. function
include 'tscclk.inc'    ; Measure CPU TSC clock frequency
