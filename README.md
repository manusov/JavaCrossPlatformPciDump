# JavaCrossPlatformPciDump
Configuration space analysing utility.
Required binary image of MCFG 
(memory-mapped configuration space)
as input file.
MCFG binary image file must be saved by external tools
(off-line mode only supported yet).

<a href="https://composter.com.ua/content/kak-sokhranit-obraz-konfiguracionnogo-pci-prostranstva-v-dvoichnyy-fayl">How to save the image of the PCI configuration space to a binary file?</a>

PCIDUMP directory contains Java sources (NetBeans project).

BINARIES_AND_REPORTS directory contains binary images and generated reports.

Java Runtime Environment (JRE) required.

Single JAR runs under Windows 32/64, Linux 32/64.
Required console mode to run, for example PowerShell or Linux terminal.


java -jar pcidump.jar input.bin output.txt


input.bin , input binary file,
MCFG binary image, saved by external tools.

output.txt, output text file,
generated text report.


