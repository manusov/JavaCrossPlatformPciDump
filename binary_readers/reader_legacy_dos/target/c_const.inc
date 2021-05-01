;------------------------------------------------------------------------------;
;              Set of target fragments for debug, this part is:                ;
;     globally used data constants and variables with pre-defined state.       ; 
;------------------------------------------------------------------------------;

ProductID         DB  PRODUCT_ID

Msg_PCI_Detect    DB  'Detecting PCI bus... ', 0
Msg_PCI_None      DB  0Dh, 0Ah, 0
Msg_PCI_Parms     DB  'OK, PCIBIOS v', 0
                  DB  ', last bus=',0                 
                  DB  ', mechanism=',0
                  DB  'h.', 0Dh, 0Ah
                  DB  'Save dump... ', 0
Msg_PCI_OK        DB  ' OK.', 0Dh, 0Ah, 0
Msg_PCI_Progress  DB  '#',0

Error_PCI_File    DB  'Save PCI binary file error.' , 0
Error_PCI_Read    DB  'PCIBIOS error.', 0
Error_Config      DB  'Incompatible configuration.',0

ErrorMinbus       DB  'Bad minimum bus number, buses 0-255 supported only.', 0
ErrorMaxbus       DB  'Bad maximum bus number, buses 0-255 supported only.', 0
ErrorMinmax       DB  'Minimum bus number above maximum bus number.',0