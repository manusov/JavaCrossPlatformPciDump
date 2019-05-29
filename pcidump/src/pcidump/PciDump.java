//---------- PCI dump utility. (C)2018 IC Book Labs ----------------------------
// Main module.

// Text Report = F ( Binary File)
// Usage:
// java -jar pcidump.jar <inputfile.bin> <outputfile.txt>
// Input file is binary MCFG space dump,
// Output file is text report.

// Version v0.06 replaces byte[] array to MappedByteBuffer,
// otherwise insufficient memory errors

// REMOVE OLD_* DATABASE FILES, OTHERWISE UNUSED DATA IMPROVE APPLICATION SIZE.
// TODO: report database access error, use "message=" string.
//

package pcidump;

import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.URL;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class PciDump 
{

private static final String MSG_ABOUT =
    "PCI binary image analyser v0.25. (C)2019 IC Book Labs.";

private static final String MSG_LINE_ERROR =
    "Invalid command line, usage:" +
    "\r\njava -jar pcidump.jar <inputfile.bin> <outputfile.txt>";

private static String nameIn, nameOut;             // input, output files names
private static String message = "?";               // message string

// private static byte[] srcData = null;           // storage for input binary
private static FileChannel srcFC = null;
private static MappedByteBuffer srcBB = null;
private static List<String> baseRows = null;       // storage for PCI database
private static byte dstData[] = null;              // storage for output text

private static DatabaseManager dm;                 // PCI database compiler
private static SystemManager sm;                   // PCI image analyser


public static void main(String[] args) 
    {
        
//----------( ! )--- This fragments for debug ----------------------------------
/*
        args = new String[]
        {
        "C:\\TEMP2\\test.bin" , 
        "C:\\TEMP2\\test.txt" , 
        };
*/
        
/*        
    args = new String[]
        {
        // "C:\\tmp\\mcfg.bin" ,
        // "C:\\tmp\\mcfg.txt"
        "C:\\tmp\\input1.bin" ,
        "C:\\tmp\\output1.txt"
        // "C:\\tmp\\input2_part.bin" ,
        // "C:\\tmp\\output2_part.txt"
        };
*/

/*        
    long t1 = System.nanoTime();
    // int nb = 1024*1024*256;
    int nb = 1024*1024*128;
    //
    srcData = new byte[nb];
    for ( int ib=0; ib<nb; ib++ )
        {
        srcData[ib]=1;
        }
    long t2 = System.nanoTime();
    double seconds = ( t2 - t1 ) / 1000000000.0;
    double megabytes = nb / 1048576.0;
    double mbps = megabytes / seconds;
    String smbps = String.format( "MBPS=%.3f", mbps );
    System.out.println(smbps);
*/
        
//----------( 1 )--- Initializing application and load input binary file -------
        
    boolean validInput = false;
    boolean validInternal = false;
    boolean validOutput = false;
    
    int n = 0;
    long sizeFC = 0;
    System.out.println(MSG_ABOUT);
    if ( ( args == null ) || ( args.length != 2 ) )
        {
        message = MSG_LINE_ERROR;
        }
    else
        {
        nameIn  = args[0];
        nameOut = args[1];
        System.out.println( "Input file  = " + nameIn );
        System.out.println( "Output file = " + nameOut );

        try
            {
            // Path srcPath = Paths.get(nameIn);
            // srcData = Files.readAllBytes(srcPath);
            srcFC = new RandomAccessFile( nameIn , "r" ).getChannel();
            sizeFC = srcFC.size();
            srcBB = srcFC.map( FileChannel.MapMode.READ_ONLY, 0, sizeFC );
            //
            }
        catch (Exception e)
            {
            message = "Loading error = " + e;
            n = 0;
            }
        if ( srcBB != null )     //  ( srcData != null )
            {
            n = (int)sizeFC;    // srcData.length;
            }
        if ( n >= 4096 )
            {
            // message = String.format ( "Loaded %d bytes from " + nameIn, n );
            message = String.format ( "Mapped %d bytes from " + nameIn, n );    
            //
            validInput = true;
            }
        else
            {
            message = String.format ( nameIn + " file invalid, %d bytes", n );
            }
        }
        System.out.println(message);

//----------( 2 )--- Load internal database, compiling loaded INF file ---------

    if (validInput)
        {
        URL resource = PciDump.class.getResource
            ( "/pcidump/resources/pci.inf" ); 
        if ( resource != null )
            {
                
            try (InputStream input = resource.openStream()) 
                {
                int m = input.available();
                int x;
                boolean previous = false;
                StringBuilder s = new StringBuilder("");
                baseRows = new ArrayList();
                for ( int i=0; i<m; i++ )
                    {
                    x = input.read();
                    //--- break if no data ---
                    if (x<0) break;
                    //--- add string if next string ---
                    if ( (x=='\r') || (x=='\n') && (!previous) )
                        {
                        previous = true;
                        baseRows.add( s.toString() );
                        s = new StringBuilder("");
                        }
                    //--- simple skip if duplicate skip string --- 
                    else if ( (x=='\r') || (x=='\n') && previous )
                        {
                        // simple skip
                        }
                    //--- add current char to current string ---
                    else
                        {
                        s.append((char)x);
                        previous = false;
                        }
                    }
//--- Initializing database compiler ---
                dm = new DatabaseManager(baseRows);
                OperationStatus osdm = dm.load();
                validInternal = osdm.getStatusFlag();
                String s1 = osdm.getStatusString();
                System.out.println( "Initializing database...\r\n" + s1 );
                }
//--- Errors handling ---
            catch (Exception e)
                {
                message = "Internal database read error: " + e;
                validInternal = false;
                }
            }
        else
            {
            message = "Internal database resource missing.";
            validInternal = false;
            }
                
        }    
    
//----------( 3 )--- Built and write output text file --------------------------

    if ( (validInput) && (validInternal) )
        {
        System.out.println( "Built list..." );
        // sm = new SystemManager( dm, srcData );
        sm = new SystemManager( dm, srcBB );
        //
        OperationStatus ossm = sm.load();
        String s1 = ossm.getStatusString();
        validOutput = ossm.getStatusFlag();
        if (validOutput)
            {
            System.out.println( "Saving list..." );

        s1 = MSG_ABOUT + "\r\n" + "Report file." + "\r\n\r\n" + s1; 
        dstData = s1.getBytes();
        
        try
            {
            Path dstPath = Paths.get(nameOut);
            Files.write( dstPath, dstData );
            }
            catch (Exception e)
                {
                message = "Saving error = " + e;
                validOutput = false;
                }
            }
        }
    
//----------( 4 )--- Final message, exit application ---------------------------

        String s;
        if (validOutput)
            {
            s = "Result saved OK.";
            }
        else
            {
            s = "Error(s) occurred.";
            }
        System.out.println(s);
        System.exit(0);
    }
    
}
