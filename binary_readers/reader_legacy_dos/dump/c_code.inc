;------------------------------------------------------------------------------;
;                Registers and memory dump subroutines library:                ;
;             connect include files with globally used subroutines.            ;
;------------------------------------------------------------------------------;

include  'decprn.inc'       ; Print (to memory buffer) 32-bit decimal number
include  'hexprn.inc'       ; Print 4/8/16/32/64-bit Hexadecimal numbers
include  'doubprn.inc'      ; Print double precision value
include  'sizeprn.inc'      ; Print memory block size as Integer.Float
include  'stringwr.inc'     ; Copy text string terminated by 00h
include  'dumpcpu.inc'      ; Dump CPU registers and memory areas
include  'itemwr.inc'       ; Copy text strings terminated by 00h