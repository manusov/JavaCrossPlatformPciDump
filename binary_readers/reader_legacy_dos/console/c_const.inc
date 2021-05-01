;------------------------------------------------------------------------------;
;                         Console support library:                             ; 
;     globally used data constants and variables with pre-defined state.       ; 
;------------------------------------------------------------------------------;

; Strings for console output, messages for exit by incompatible configuration

MsgBad             DB  0Dh, 0Ah,'Incompatible configuration: ', 024h
MsgBadCPU          DB  '80386+ CPU required.', 024h
MsgBadMemory       DB  'Insufficient low memory, required 100KB minimum.', 024h
MsgBadDOS          DB  'Required DOS 3.x minimum.', 024h
MsgBadMode         DB  'Real mode required, please remove EMM386/QEMM.', 024h
MsgBadUnknown      DB  'Unknown error.',024h
MsgAnyKey          DB  0Dh, 0Ah, 'Press any key...', 024h
CrLf_24            DB  0Dh, 0Ah, 024h 

; Strings for console output, messages normal execution

StartMsg           DB  0Dh, 0Ah, 'Starting...', 0Dh, 0Ah, 0Dh, 0Ah, 0
DoneMsgWait        DB  'Done, press any key...', 0
DoneMsgNoWait      DB  'Done.', 0
TitleString        DB  'Hardware Shell v0.01 (DOS 16-bit)', 0
CrLf2              DB  0Dh, 0Ah
CrLf               DB  0Dh, 0Ah, 0

; Names for scenario file and report file

InputName          DB  'input.txt',0
OutputName         DB  'output.txt',0

; Error messages
MsgError           DB  'ERROR: ', 0
MsgErrorTab        DB  '       ', 0
MsgErrorOS         DB  'Code ', 0
MsgUnknownOption   DB  'Unknown option.', 0 
MsgOption          DB  'Bad option string.', 0
MsgInputSize       DB  'Input scenario file size too big.', 0 

; Options descriptors, for values, controlled by scenario file INPUT.TXT 

OpDesc:
OPTION_KEYS        NameDisplay     , OptionDisplay     , WordDisplay     , KeyOnOff
OPTION_KEYS        NameReport      , OptionReport      , WordReport      , KeyOnOff
OPTION_KEYS        NameWaitkey     , OptionWaitkey     , WordWaitkey     , KeyOnOff
OPTION_STRING      NameDumpfile    , OptionDumpfile    , WordDumpfile
OPTION_DECIMAL_32  NameMinbus      , OptionMinbus      , WordMinbus
OPTION_DECIMAL_32  NameMaxbus      , OptionMaxbus      , WordMaxbus
OPTION_KEYS        NameAutomaxbus  , OptionAutomaxbus  , WordAutomaxbus  , KeyOnOff 
OPTION_KEYS        NameSkipnodev   , OptionSkipnodev   , WordSkipnodev   , KeyOnOff
OPTION_KEYS        NameSkipnofnc   , OptionSkipnofnc   , WordSkipnofnc   , KeyOnOff
OPTION_END

; Options values, controlled by scenario file INPUT.TXT
; Located at constants part, because this variables has pre-defined values

OptionDisplay      DB  1               ; on = console output enabled, off = disabled
OptionReport       DB  1               ; on = save report to file output.txt enabled, off = disabled
OptionWaitkey      DB  1               ; on = wait "Press any key" after operation, off = skip this waiting
OptionDumpfile     DW  BufferDumpname  ; source file path, used for read and write
OptionMinbus       DD  0               ; minimum bus number for scan
OptionMaxbus       DD  255             ; maximum bus number for scan 
OptionAutomaxbus   DB  0               ; on = auto detect maximun bus, off = scan all from minimum to maximum 
OptionSkipnodev    DB  1               ; on = skip device if PCI VID:DID = FFFFh, off = raw read all
OptionSkipnofnc    DB  1               ; on = skip functions 1-7 of single function device, off = raw read all

DefaultDumpfile    DB  'pci.bin',0     ; default path and name for source files 

; Long names for options, used for display and save report with parameters list

NameDisplay        DB  'Display console messages'              , 0  
NameReport         DB  'Generate report file'                  , 0
NameWaitkey        DB  'Wait key press from user'              , 0
NameDumpfile       DB  'Binary dump file name'                 , 0
NameMinbus         DB  'Minimum PCI bus number'                , 0
NameMaxbus         DB  'Maximum PCI bus number'                , 0
NameAutomaxbus     DB  'Auto detect maximum PCI bus'           , 0
NameSkipnodev      DB  'Skip device if PCI VID=DID=FFFFh'      , 0  
NameSkipnofnc      DB  'Skip functions 1-7 if single function' , 0

; Short single word names for options, used for parsing

WordDisplay        DB  'display'    , 0
WordReport         DB  'report'     , 0
WordWaitkey        DB  'waitkey'    , 0
WordDumpfile       DB  'dumpfile'   , 0
WordMinbus         DB  'minbus'     , 0
WordMaxbus         DB  'maxbus'     , 0
WordAutomaxbus     DB  'automaxbus' , 0
WordSkipnodev      DB  'skipnodev'  , 0
WordSkipnofnc      DB  'skipnofnc'  , 0

; Keywords for options

KeyOnOff           DB  'off', 0, 'on', 0, 0
   
