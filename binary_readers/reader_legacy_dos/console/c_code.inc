;------------------------------------------------------------------------------;
;                         Console support library:                             ;
;             connect include files with globally used subroutines.            ; 
;------------------------------------------------------------------------------;

include 'conread.inc'    ; Console input
include 'conwrite.inc'   ; Console output
include 'getcolor.inc'   ; Get console color, saved at start
include 'setcolor.inc'   ; Set console color, for next outputs
include 'setfgclr.inc'   ; Set console foreground color, background
                         ; color as saved at start, for next out.
include 'createrp.inc'   ; Open and Close report file, initialize access
include 'readscn.inc'    ; Open, Read, Close scenario file
include 'parsescn.inc'   ; Parse scenario file, extract and set options
include 'showscn.inc'    ; Display current options settings by scenario
include 'showerr.inc'    ; Display details about detected error 
