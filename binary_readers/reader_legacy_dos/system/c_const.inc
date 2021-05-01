;------------------------------------------------------------------------------;
;    Hardware platform and operating system helpers library, this part is:     ;
;     globally used data constants and variables with pre-defined state.       ;
;------------------------------------------------------------------------------;

;--- DOS Error codes 01h-23h ---

EMsg_Pool_01_23:
DB  'Bad function'                          , 0           ; 01h
DB  'File not found'                        , 0           ; 02h
DB  'Path not found'                        , 0           ; 03h
DB  'Too many files open'                   , 0           ; 04h
DB  'Access denied'                         , 0           ; 05h
DB  'Bad handle'                            , 0           ; 06h
DB  'MCB destroyed'                         , 0           ; 07h
DB  'Insufficient memory'                   , 0           ; 08h
DB  'Bad memory block address'              , 0           ; 09h
DB  'Bad environment'                       , 0           ; 0Ah
DB  'Bad format'                            , 0           ; 0Bh
DB  'Bad access code'                       , 0           ; 0Ch
DB  'Invalid date'                          , 0           ; 0Dh
DB  '?'                                     , 0           ; 0Eh
DB  'Invalid drive selected'                , 0           ; 0Fh
DB  'Cannot delete current dir'             , 0           ; 10h
DB  'Not a same device'                     , 0           ; 11h
DB  'No files'                              , 0           ; 12h
DB  'Write protect'                         , 0           ; 13h
DB  'Bad sub ID'                            , 0           ; 14h
DB  'Drive not ready'                       , 0           ; 15h
DB  'Bad command'                           , 0           ; 16h
DB  'Data error'                            , 0           ; 17h
DB  'Bad request length'                    , 0           ; 18h
DB  'Seek error'                            , 0           ; 19h
DB  'Invalid media'                         , 0           ; 1Ah
DB  'Sector not found'                      , 0           ; 1Bh
DB  'Paper end'                             , 0           ; 1Ch
DB  'Write error'                           , 0           ; 1Dh
DB  'Read error'                            , 0           ; 1Eh
DB  'Common error'                          , 0           ; 1Fh
DB  'Share error'                           , 0           ; 20h
DB  'Lock error'                            , 0           ; 21h
DB  'Disk change'                           , 0           ; 22h
DB  'FCB not available'                     , 0           ; 23h

;--- DOS Error codes 50h-53h ---

EMsg_Pool_50_53:
DB  'File exist'                            , 0           ; 50h
DB  '?'                                     , 0           ; 51h
DB  '?'                                     , 0           ; 52h
DB  'INT 24h error'                         , 0           ; 53h

;--- Special errors ---

EMsg_Pool_60_62:
DB  'Disk full'                             , 0           ; 60h
DB  'Wrong DOS version'                     , 0           ; 61h
DB  'Overlay corrupted'                     , 0           ; 62h 

;--- PCIBIOS Error Codes ---
EMsg_Pool_81_89:
DB  'Function not supported'                , 0           ; 81h
DB  '?'                                     , 0           ; 82h
DB  'Bad vendor ID'                         , 0           ; 83h
DB  '?'                                     , 0           ; 84h
DB  '?'                                     , 0           ; 85h
DB  'Device not found'                      , 0           ; 86h
DB  'Bad register number'                   , 0           ; 87h
DB  'Set failed'                            , 0           ; 88h
DB  'Buffer too small'                      , 0           ; 89h  

;--- ACPI and system functionality errors ---

EMsg_Pool_F0_FC:
DB  'Cannot use ACPI in the protected mode' , 0           ; F0h
DB  'ACPI not detected, check BIOS setup'	  , 0           ; F1h
DB  'A20 gate failure (8042 KBC used)'      , 0           ; F2h
DB  'Invalid ACPI\FADT'                     , 0           ; F3h
DB  'Invalid ACPI\DSDT'                     , 0           ; F4h
DB  'ACPI machine failure'                  , 0           ; F5h
DB  'AML object not found'                  , 0           ; F6h
DB  'AML object unexpected'                 , 0           ; F7h
DB  'AML region limit violation'            , 0           ; F8h
DB  'Invalid index for AML package'         , 0           ; F9h
DB  'PCI Bus not detected'                  , 0           ; FAh
DB  'System PnP not detected'               , 0           ; FBh
DB  'System PnP error returned'             , 0           ; FCh

;--- Unknown error ---

EMsg_Pool_FF_FF:
DB  'Unknown error'                         , 0           ; FFh

