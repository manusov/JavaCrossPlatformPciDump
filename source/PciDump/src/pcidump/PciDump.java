/*
PCI dump utility: text report = f( binary dump ). (C)2020 IC Book Labs.
------------------------------------------------------------------------
Main module.
Text Report = F ( Binary File)
Usage:
java -jar pcidump.jar <inputfile.bin> <outputfile.txt>
Input file is binary MCFG space dump,
Output file is text report.
*/

package pcidump;

import java.io.IOException;
import java.nio.file.*;

public class PciDump 
{
private final static String MSG_ABOUT =
    "PCI binary image analyser v0.00.44. (C)2020 IC Book Labs.";
private final static String MSG_LINE_ERROR =
    "Invalid command line, usage:" +
    "\r\njava -jar pcidump.jar <inputfile.bin> <outputfile.txt>";
private final static String RESOURCE_FILE =
    "/pcidump/resources/pci.inf";

public static void main( String[] args ) 
    {
    /*
    First unconditional message    
    */
    System.out.println( MSG_ABOUT );
    /*
    Global variables
    */
    String message;
    boolean validInput = false;
    boolean validInternal = false;
    boolean validOutput = false;
    SystemManager sm;
    DatabaseManager dm;
    OperationManager om = null;
    String nameIn;
    String nameOut = null;
    /*
    Check command line parameters: (1) input file name, (2) output file name.
    Initializing database manager, system manager, operation manager,
    load input binary file and application internal resoures.
    */
    if ( ( args == null ) || ( args.length != 2 ) )
        {  // wrong arguments count, error
        message = MSG_LINE_ERROR;
        }
    else
        {  // 2 arguments found, continue
        nameIn  = args[0];
        nameOut = args[1];
        System.out.println( "Input file  = " + nameIn );
        System.out.println( "Output file = " + nameOut );
        sm = new SystemManager( nameIn );
        if ( sm.getOperationStatus().getStatusFlag() )
            {
            validInput = true;
            System.out.println( String.format
                ( "Mapped %d bytes from %s\r\nInitializing database...",
                   sm.getMappedSize(), nameIn ) );
            dm = new DatabaseManager( RESOURCE_FILE );
            if( dm.getOperationStatus().getStatusFlag() )
                {
                om = new OperationManager( sm, dm );
                if ( om.getOperationStatus().getStatusFlag() )
                    {
                    validInternal = true;
                    message = dm.getOperationStatus().getStatusString();
                    }
                else
                    {
                    message = om.getOperationStatus().getStatusString();
                    }
                }
            else
                {
                message = dm.getOperationStatus().getStatusString();
                }
            }
        else
            {
            message = sm.getOperationStatus().getStatusString();
            }
        }
    System.out.println( message );
    /*
    Generating text report, arguments:
    loaded binary file: MCFG image
    internal resource PCI database file: vendors, devices, registers fields
    */
    if ( validInput && validInternal )
        {
        System.out.println( "Build report..." );
        if ( om != null )
            {
            om.buildSystemInfo();
            validOutput = om.getOperationStatus().getStatusFlag();
            if ( ! validOutput )
                {
                System.out.println
                    ( om.getOperationStatus().getStatusString() );
                }
            }
        }
    /*
    Saving text report to output text file and exit application.
    */
    if ( validOutput && ( om != null )&&( om.getOperationStatus() != null )&&
        ( nameOut != null ) )
        {
        System.out.println( "Saving report..." );
        String s = MSG_ABOUT + "\r\n" + "Report file." + "\r\n\r\n" + 
                   om.getOperationStatus().getStatusString();
        byte[] reportData = s.getBytes();
        try
            {
            Path reportPath = Paths.get( nameOut );
            Files.write( reportPath, reportData );
            }
        catch ( IOException e )
            {
            System.out.println( "Error saving report: " + e );
            validOutput = false;
            }
        }
    message = validOutput ? "Result saved OK." : "Error(s) occurred.";
    System.out.println( message );
    System.exit( 0 );
    }
}
