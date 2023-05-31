/*
PCI dump utility: text report = f( binary dump ). (C)2023 IC Book Labs.
------------------------------------------------------------------------
Class for load and interpreting PCI data base text file,
this file located from JAR resources (part of java application).
*/

package pcidump;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Set;

class DatabaseManager 
{
private boolean status = false;
private String statusString = "N/A";

private final static String MARKER_DEVICES   = ".PCIDEVICES";
private final static String MARKER_CLASSES   = ".PCICLASSES";
private final static String MARKER_REGISTERS = ".PCIREGISTERS";
private final static String MARKER_SCAP = "SCAP";
private final static String MARKER_ECAP = "ECAP";

private ArrayList<String> baseRows;
private LinkedHashMap<Integer, PCIvendor> pciDevices;
private LinkedHashMap<Integer, PCIbaseClass> pciClasses;
private LinkedHashMap<Integer, PCIheader> pciHeaders;
private LinkedHashMap<Integer, String> pciStdCaps;
private LinkedHashMap<Integer, String> pciExtCaps;

/*
Database #1 : PCI Vendors, Devices, Subsystems
PCI Vendor object
*/
private class PCIvendor
{
private final String vendorName;
private final LinkedHashMap<Integer, PCIdevice> devicesMap;
private PCIvendor( String s )
    {
    vendorName = s;
    devicesMap = new LinkedHashMap();
    }
private String getName() { return vendorName; }
private LinkedHashMap<Integer, PCIdevice> getMap() { return devicesMap; }
}
/*
PCI Device object
*/
private class PCIdevice
{
private final String deviceName;
private final ArrayList<PCIsubDevice> subDevicesMap;
private PCIdevice( String s )
    {
    deviceName = s;
    subDevicesMap = new ArrayList();
    }
private String getName() { return deviceName; }
private ArrayList<PCIsubDevice> getMap() { return subDevicesMap; }
}
/*
PCI Sub-Device object
*/
private class PCIsubDevice
{
private final String subDeviceName;
private final int subVendorID, subDeviceID;
private PCIsubDevice( String s, int sv, int sd  )
    {
    subDeviceName = s;
    subVendorID = sv;
    subDeviceID = sd;
    }
private String getName() { return subDeviceName; }
private int getSVID()    { return subVendorID;   }
private int getSDID()    { return subDeviceID;   }
}
/*
Database #2 : PCI classes, Subclasses, Program interfaces
PCI Base Class object
*/
private class PCIbaseClass
{
private final String pciClassName;
private final LinkedHashMap<Integer, PCIsubClass> subClassesMap;
private PCIbaseClass( String s )
    {
    pciClassName = s;
    subClassesMap = new LinkedHashMap();
    }
private String getName() { return pciClassName; }
private LinkedHashMap<Integer, PCIsubClass> getMap() { return subClassesMap; }
}
/*
PCI Sub Class object
*/
private class PCIsubClass
{
private final String pciSubClassName;
private final ArrayList<PCIprogramInterface> programInterfacesMap;
private PCIsubClass( String s )
    {
    pciSubClassName = s;
    programInterfacesMap = new ArrayList();
    }
private String getName() { return pciSubClassName; }
private ArrayList<PCIprogramInterface> getMap() { return programInterfacesMap; }
}
/*
PCI Program Interface object
*/
private class PCIprogramInterface
{
private final String programInterfaceName;
private final int programInterfaceID;
private PCIprogramInterface( String s, int p  )
    {
    programInterfaceName = s;
    programInterfaceID = p;
    }
private String getName() { return programInterfaceName; }
private int getID()      { return programInterfaceID;   }
}
/*
Database #3 : Headers, Registers, Capabilities
PCI Header object
*/
private class PCIheader
{
private final String headerName;
private final LinkedHashMap<Integer, PCIregister> registersMap;
private PCIheader( String s )
    {
    headerName = s;
    registersMap = new LinkedHashMap();
    }
private String getName() { return headerName; }
private LinkedHashMap<Integer, PCIregister> getMap() { return registersMap; }
}
/*
Register status text labels:
HWINIT = Hardware Initialized
RO = Read-only
WO = Write-only
RW = Read-Write
RW1C = Read, Write-1-to-clear status
ROS = Sticky (unchanged at hardware reset) - Read-only
RWS = Sticky (unchanged at hardware reset) - Read-Write
RW1CS = Sticky - Write-1-to-clear status
RSVD  = Reserved without RsvdP/RsvdZ annotation.
RSVDP = Reserved and Preserved for future RW, 
        software must preserve read value for write
RSVDZ = Reserved and Zero, software must write 0b to this bits, 
        even if read 1b.

IMPORTANT NOTE.
for correct detecting shortest strings must be later,
example; RW after RW1C.
*/
private final static String[] TYPE_NAME =
    { "HWINIT", "ROS", "WO", "RW1CS", "RO", "RWS", "RW1C", "RW",
      "RSVDP", "RSVDZ", "RSVD" };
private enum RegType
    { HWINIT, ROS, WO, RW1CS, RO, RWS, RW1C, RW, RSVDP, RSVDZ, RSVD }
/*
PCI Registers object
*/
private class PCIregister
{
private final String name;
private final int width;          // width units = bytes
private final Set<RegType> types;
private final Set<RegisterBit> bits;
private PCIregister( String s, int w )
    {
    name = s;
    width = w;
    types = new LinkedHashSet();
    bits = new LinkedHashSet();
    }
private String getName() { return name;  }
private int getWidth()   { return width; }
private Set getTypes()   { return types; }
private Set getBits()    { return bits;  }
}
/*
PCI Register Bit object
IMPORTANT NOTE.
Bit number must store explict, not as key, because some bitfields
declared many times for alternative encodings.
*/
private class RegisterBit
{
private final String name;
private final int position;
private final int width;          // width units = bits
private final Set<RegType> types;
private final Set<RegisterValue> values;
private RegisterBit( String s, int p, int w )
    {
    name = s;
    position = p;
    width = w;
    types = new LinkedHashSet();
    values = new LinkedHashSet();
    }
private String getName() { return name;   }
private int getWidth()   { return width;  }
private Set getTypes()   { return types;  }
private Set getValues()  { return values; }
}
/*
PCI Register Value object
*/
private class RegisterValue
{
private final String name;
private final int value;
private RegisterValue( String s, int v )
    {
    name = s;
    value = v;
    }
private String getName() { return name;  }
private int getWidth()   { return value; }
}

/*
Class constructor
*/
DatabaseManager( String databaseFileName )
    {
    URL resource = PciDump.class.getResource( databaseFileName ); 
    if ( resource != null ) { 
    try ( InputStream input = resource.openStream() ) {
        
    int inputSize = input.available();
    int x;
    boolean previous = false;
    StringBuilder sb = new StringBuilder( "" );
    baseRows = new ArrayList();
    for ( int i=0; i<inputSize; i++ )
        {
        x = input.read();
        // break if no data
        if ( x < 0 ) break;
        // add string if next string
        if ( ( x == '\r' ) || ( x == '\n' ) && ( ! previous ) )
            {
            previous = true;
            baseRows.add( sb.toString() );
            sb = new StringBuilder( "" );
            }
        // simple skip if duplicate skip string
        else if ( ( x == '\r' ) || ( x == '\n' ) && previous )
            {
            // simple skip, reserved place
            }
            // add current char to current string
        else
            {
            sb.append( (char) x );
            previous = false;
            }
        }
    /*
    Initializing database compiler
    Initializing database management variables
    */
    StringBuilder result = new StringBuilder( "" );
    pciDevices = new LinkedHashMap();
    pciClasses = new LinkedHashMap();
    pciHeaders = new LinkedHashMap();
    pciStdCaps = new LinkedHashMap();
    pciExtCaps = new LinkedHashMap();
    PCIvendor v = null;
    PCIdevice d = null;
    PCIbaseClass bc = null;
    PCIsubClass sc = null;
    PCIheader ph = null;
    PCIregister pr = null;
    RegisterBit rb = null;
    int vcount = 0, dcount = 0, scount = 0;
    int bccount = 0, sccount = 0, picount = 0;
    int hdcount = 0, rgcount = 0, bitcount = 0, stcount = 0, excount = 0;
    /*
    Initialize variables for parsing file as array of strings
    */
    int n = 0, m, min = 0, max = 0;
    int ns = 1, smin = 1, smax = 1;
    int start1 = 0, stop1 = 0;
    boolean modePciDevices = false;
    int start2 = 0, stop2 = 0;
    boolean modePciClasses = false;
    int start3 = 0, stop3 = 0;
    boolean modePciRegs = false, modeScap = false, modeEcap = false;
    /*
    Parsing file as array of strings
    */
    boolean error = false;
    String s;
    char c;
    if ( baseRows != null )
        {
        n = baseRows.size();
        Iterator it = baseRows.iterator();
        /*
        Initializing data from first element of array
        */
        if ( it.hasNext() )
            {
            s = baseRows.get(0);
            min = s.length();
            max = min;
            }
        /*
        Start of cycle
        */
        while ( it.hasNext() )
            {
            /*
            Get string, set current iteration status
            */
            s = (String) it.next();
            m = s.length();
            if ( m < min ) 
                { 
                min = m;
                smin = ns;
                }
            if ( m > max ) 
                {
                max = m;
                smax = ns;
                }
            if ( ( !modePciDevices )&&( s.startsWith( MARKER_DEVICES ) ) )
                {
                start1 = ns;
                modePciDevices = true;
                }
            else if ( ( modePciDevices )&&( s.startsWith( "." ) ) )
                {
                stop1 = ns;
                modePciDevices = false;
                }
            if ( ( !modePciClasses )&&( s.startsWith( MARKER_CLASSES ) ) )
                {
                start2 = ns;
                modePciClasses = true;
                }
            else if ( ( modePciClasses )&&( s.startsWith( "." ) ) )
                {
                stop2 = ns;
                modePciClasses = false;
                }
            if ( ( !modePciRegs )&&( s.startsWith( MARKER_REGISTERS ) ) )
                {
                start3 = ns;
                modePciRegs = true;
                }
            else if ( ( modePciRegs )&&( s.startsWith(".") ) )
                {
                stop3 = ns;
                modePciRegs = false; modeScap = false; modeEcap = false;
                }
/*
Detect and handling strings for mode #1 = PCI devices
text block id = .
comment = #
device  = tab
subsystem device:vendor = tab + tab
otherwise hex number = vendor ID
variables context: s = string , m = string length , ns = string number
can replace with divideString()
*/
            if ( ( modePciDevices ) && ( m > 0 ) )
                {
                c = s.charAt( 0 );
                switch ( c )
                    {
                    case '.': break;
                    case '#': break;
                    case '\t': 
                        {
                        if ( ( m > 1 ) && ( s.charAt(1) == '\t' ) )
                            {  // subvendor:subdevice
                            if ( m >= 11 )
                                {
                                String shex1 = s.substring(2,6);
                                String shex2 = s.substring(7,11);
                                String sname = "";
                                int nhex1, nhex2;
                                if ( m > 11 )
                                    {
                                    sname = ( s.substring(11) ).trim();
                                    }
                                nhex1 = Integer.parseInt(shex1, 16);
                                nhex2 = Integer.parseInt(shex2, 16);
                                PCIsubDevice sd = 
                                    new PCIsubDevice( sname, nhex1, nhex2 );
                                if ( d != null )
                                    {
                                    d.getMap().add( sd );
                                    }
                                else
                                    {
                                    error = true;
                                    }
                                scount++;
                                }
                            }
                        else
                            {  // device
                            if ( m >= 5 )
                                {
                                String shex = s.substring( 1, 5 );
                                String sname = "";
                                int nhex;
                                if ( m > 5 )
                                    {
                                    sname = ( s.substring( 5 ) ).trim();
                                    }
                                nhex = Integer.parseInt( shex, 16 );
                                d = new PCIdevice( sname );
                                if ( v != null )
                                    {
                                    v.getMap().put( nhex, d );
                                    }
                                else
                                    {
                                    error = true;
                                    }
                                dcount++;
                                }
                            }   
                        break;
                        }
                    default:
                        {  // new vendor
                        if ( m >= 4 )
                            {
                            String shex = s.substring( 0, 4 );
                            String sname = "";
                            int nhex;
                            if ( m > 4 )
                                {
                                sname = (s.substring(4)).trim();
                                }
                            nhex = Integer.parseInt( shex, 16 );
                            v = new PCIvendor( sname );
                            pciDevices.put( nhex, v );
                            vcount++;
                            }
                        else
                            {
                            error = true;
                            }
                        }
                    }
                }
/*               
Detect and handling strings for mode #2 = PCI classes
text block id = .
comment = #
class declaration = C
subclass = tab
program interface = tab + tab.
*/
            if ( ( modePciClasses ) && ( m > 0 ) )
                {
                c = s.charAt(0);
                switch ( c )
                    {
                    case '.': break;
                    case '#': break;
                    case 'C':
                        {
                        if ( m >= 3 )
                            {  // new base class
                            String shex = s.substring( 1, 4 ).trim();
                            String sname = "";
                            int nhex;
                            if ( m>4 )
                                {
                                sname = ( s.substring( 4 )).trim();
                                }
                            nhex = Integer.parseInt( shex, 16 );
                            bc = new PCIbaseClass( sname );
                            pciClasses.put( nhex, bc );
                            bccount++;
                            }
                        else
                            {
                            error = true;
                            }
                        break;
                        }
                    case '\t': 
                        {
                        if ( ( m > 1 ) && ( s.charAt(1) == '\t' ) )
                            {  // program interface
                            if ( m >= 4 )
                                {
                                String shex = s.substring( 2, 4 );
                                String sname = "";
                                int nhex;
                                if ( m > 4 )
                                    {
                                    sname = ( s.substring( 4 ) ).trim();
                                    }
                                nhex = Integer.parseInt(shex, 16);
                                PCIprogramInterface pi = 
                                    new PCIprogramInterface( sname, nhex );
                                if ( sc != null )
                                    {
                                    sc.getMap().add(pi);
                                    }
                                else
                                    {
                                    error = true;
                                    }
                                picount++;
                                }
                            }
                        else
                            {  // subclass
                            if ( m >= 3 )
                                {
                                String shex = s.substring( 1, 3 );
                                String sname = "";
                                int nhex;
                                if ( m>3 )
                                    {
                                    sname = ( s.substring( 3 )).trim();
                                    }
                                nhex = Integer.parseInt( shex, 16 );
                                sc = new PCIsubClass( sname );
                                if ( bc != null )
                                    {
                                    bc.getMap().put( nhex, sc );
                                    }
                                else
                                    {
                                    error = true;
                                    }
                                sccount++;
                                }
                            }
                        break;
                        }
                    default:
                        error = true;
                        break;
                    }
                }
/*
Detect and handling strings for mode #3 = PCI registers
text block id = .
comment = #
header declaration = H
register declaration = R
bit declaration = tab + string
standard capabilities mode = SCAP
extended capabilities mode = ECAP
standard capability = ID **
extended capability = ID ****
*/
            if( modePciRegs )
                {
                if ( s.startsWith( MARKER_SCAP ) )
                    {  // start of standard capabilities
                    modeScap = true; modeEcap = false;
                    }
                else if ( s.startsWith( MARKER_ECAP ) )
                    {  // start of extended capabilities
                    modeScap = false; modeEcap = true;
                    }
                else if ( ( modeScap )&&( s.startsWith( "ID" ) ) )
                    {  // standard capability ID
                    String scap = "";
                    int cnum = 0;
                    if ( m >= 6 )
                        {
                        String snum = s.substring(2,6).trim();
                        cnum = Integer.parseInt(snum, 16);
                        }
                    if ( m > 6 )
                        {
                        scap = s.substring( 6 ).trim();
                        }
                    pciStdCaps.put( cnum, scap );
                    stcount++;
                    }
                else if ( ( modeEcap )&&( s.startsWith( "ID" ) ) )
                    {  // extended capability ID
                    String scap = "";
                    int cnum = 0;
                    if ( m >= 8 )
                        {
                        String snum = s.substring(2,8).trim();
                        cnum = Integer.parseInt(snum, 16);
                        }
                    if ( m > 8 )
                        {
                        scap = s.substring(8).trim();
                        }
                    pciExtCaps.put( cnum, scap );
                    excount++;
                    }
                else if ( s.startsWith( "H" ) )
                    {   // PCI register block header
                    String shdr = "";
                    int hnum = 0;
                    if ( m >= 4 )
                    {
                    String snum = s.substring( 2, 4 ).trim();
                    hnum = Integer.parseInt( snum, 16 );
                    }
                    if ( m > 4 )
                        {
                        shdr = s.substring( 4 ).trim();
                        }
                    ph = new PCIheader( shdr );
                    pciHeaders.put( hnum, ph );
                    hdcount++;
                    }
                else if ( s.startsWith( "R" ) )  // PCI register
                    {
                    String sreg;
                    int rnum = 0;
                    int rwidth;
                    if ( m >= 6 )
                        {
                        String snum = s.substring( 2, 6 ).trim();
                        rnum = Integer.parseInt( snum, 16 );
                        }
                    int i1 = 7, i2;
                    if ( m > 6 )
                        {
                        i2 = s.indexOf( ' ', i1 );
                        if ( i2 > 0 )
                            {
                            String snum = s.substring( i1, i2+1 ).trim();
                            rwidth = Integer.parseInt(snum, 16);
                            int k = s.indexOf( ' ', i2+1 );
                            sreg = s.substring( k ).trim();
                            pr = new PCIregister( sreg, rwidth );
                            if ( ph != null )
                                {
                                ph.getMap().put( rnum, pr );
                                }
                            else
                                {
                                error = true;
                                }
                            i1 = i2;
                            i2 = s.indexOf( ' ', i1+1 );
                            if ( i2 > i1 )
                                {
                                String stypes = s.substring( i1, i2+1 ).trim();
                                i1 = 0;
                                int count = TYPE_NAME.length;
                                while ( i1 >= 0 )
                                    {
                                    RegType rt = null;
                                    for ( int i=0; i<count; i++ )
                                        {
                                        if ( stypes.startsWith
                                                ( TYPE_NAME[i], i1 ) )
                                            {
                                            rt = RegType.valueOf
                                                    ( TYPE_NAME[i] );
                                            break;
                                            }
                                        }
                                        if ( rt != null )
                                            {
                                            pr.getTypes().add( rt );
                                            }
                                        else
                                            {
                                            error = true;
                                            break;
                                            }
                                        i1 = stypes.indexOf( '+', i1+1 );
                                    if ( i1 > 0 ) i1++;
                                    }
                                }
                            else
                                {
                                error = true;
                                }
                            }
                        else
                            {
                            error = true;
                            }
                        }
                    rgcount++;
                    }
                else if ( s.startsWith( "\t\t" ) )
                    {  // Value of bitfield of PCI register 
                    int bitvalue;
                    String stra = s.trim();
                    String[] strb = new String[2];
                    int cnt;
                    int k1 = 0, k2 = 0, k3 = stra.length();
                    for ( cnt=0; cnt<2; cnt++ )
                        {
                        if ( k1 > k3 ) break;
                        if ( cnt<1 )
                            {
                            k2 = stra.indexOf( ' ', k1 );
                            strb[cnt] = stra.substring( k1, k2 );
                            }
                        else
                            {
                            strb[cnt] = stra.substring( k1 );
                            }
                        k1 = k2;
                        while ( ( stra.charAt(k1) == ' '  )||
                                ( stra.charAt(k1) == '\t' ) )
                            {
                            k1++;
                            if ( k1 > k3 ) break;
                            }
                        }
                    if ( cnt == 2 )
                        {
                        bitvalue = Integer.parseInt( strb[0] );
                        RegisterValue rv = new RegisterValue
                            ( strb[1], bitvalue );
                        if ( ( rb != null ) && ( rb.getValues() != null ) )
                            {
                            rb.getValues().add( rv );
                            }
                        else
                            {
                            error = true;
                            }
                        }
                    else
                        {
                        error = true;
                        }
                    }
                else if ( s.startsWith( "\t" ) )
                    {  // Bitfield of PCI register
                    int bitsnum, bitswidth;
                    String stra = s.trim();
                    String[] strb = new String[4];
                    int cnt;
                    int k1 = 0, k2 = 0, k3 = stra.length();
                    for ( cnt=0; cnt<4; cnt++ )
                        {
                        if ( k1 > k3 ) break;
                        if ( cnt<3 )
                            {
                            k2 = stra.indexOf( ' ', k1 );
                            strb[cnt] = stra.substring( k1, k2 );
                            }
                        else
                            {
                            strb[cnt] = stra.substring( k1 );
                            }
                        k1 = k2;
                        while ( ( stra.charAt(k1) == ' ')||
                                ( stra.charAt(k1) == '\t' ) )
                            {
                            k1++;
                            if ( k1 > k3 ) break;
                            }
                        }
                    if ( cnt == 4 )
                        {
                        bitsnum = Integer.parseInt( strb[0] );
                        bitswidth = Integer.parseInt( strb[1] );
                        rb = new RegisterBit ( strb[3], bitsnum, bitswidth );
                        int i1 = 0;
                        int count = TYPE_NAME.length;
                        while ( i1 >= 0 )
                            {
                            RegType rt = null;
                            for ( int j=0; j<count; j++ )
                                {
                                if ( strb[2].startsWith( TYPE_NAME[j], i1 ) )
                                    {
                                    rt = RegType.valueOf( TYPE_NAME[j] );
                                    break;
                                    }
                                }
                            if ( rt != null )
                                {
                                rb.getTypes().add( rt );
                                }
                            else
                                {
                                error = true;
                                break;
                                }
                            i1 = strb[2].indexOf( '+', i1+1 );
                            if (i1>0) i1++;
                            }
                        }
                    else
                        {
                        error = true;
                        }

                    if ( ( pr != null ) && ( pr.getBits() != null ) )
                        {
                        pr.getBits().add(rb);
                        }
                    else
                        {
                        error = true;
                        }
                    bitcount++;
                    }
                else if ( ( s.startsWith( "." ) )||( s.startsWith( "#" ) )||
                          ( m == 0 ) )
                    {  // markers, comments, empty
                    }
                else
                    {
                    error = true;  // otherwise error detected
                    }
                }
/*                
Check errors, increment string number
*/
            ns++;
            if ( error ) break;
            }
/*
End of cycle, correct end string numbers
*/
        }
        ns--;
        if ( stop1 == 0 ) stop1 = ns;
        if ( stop2 == 0 ) stop2 = ns;
        if ( stop3 == 0 ) stop3 = ns;
/*
 Built statistic text strings
*/
        result.append( String.format
            ( "Read %d strings", n ) );
        result.append( String.format
            ( ", first minimum string[%d] = %d", smin, min ) );
        result.append( String.format
            ( ", first maximum string[%d] = %d", smax, max ) );

        result.append( String.format
            ( "\r\nFound devices declarations" ) );
        result.append( String.format
            ( ", start string = %d", start1 ) );
        result.append( String.format
            ( ", end string = %d", stop1 ) );

        result.append( String.format
            ( "\r\nVendors = %d", vcount ) );
        result.append( String.format
            ( ", devices = %d", dcount ) );
        result.append( String.format
            ( ", subdevices = %d", scount ) );
        
        result.append( String.format
            ( "\r\nFound classes declarations" ) );
        result.append( String.format
            ( ", start string = %d", start2 ) );
        result.append( String.format
            ( ", end string = %d", stop2 ) );

        result.append( String.format
            ( "\r\nBase classes = %d", bccount ) );
                result.append( String.format
            ( ", sub classes = %d", sccount ) );
        result.append( String.format
            ( ", program interfaces = %d", picount ) );
            
        result.append( String.format
            ( "\r\nFound registers declarations" ) );
        result.append( String.format
            ( ", start string = %d", start3 ) );
        result.append( String.format
            ( ", end string = %d", stop3 ) );

        result.append( String.format
            ( "\r\nHeaders = %d", hdcount ) );
        result.append( String.format
            ( ", registers = %d", rgcount ) );
        result.append( String.format
            ( ", bitfields = %d", bitcount ) );
        result.append( String.format
            ( ", standard capabilities = %d", stcount ) );
        result.append( String.format
            ( ", extended capabilities = %d", excount ) );
            
        if ( ! error )
            {
            status = true;
            statusString = result.toString();
            }
        else
            {
            status = false;
            statusString = result.toString() +
                           "\r\nInternal database compiling error" +
                           "\r\nERROR AT LINE = " + ns;
            }
        }
/*
Errors handling
*/
        catch ( Exception e ) 
            { status = false;
              statusString = "Internal database read error: " + e; }  }
    else
        {
        status = false;
        statusString = "Internal database resource missing.";
        }
    }


/*
Get status flag and status string as single object
*/
OperationStatus getOperationStatus()
    {
    return new OperationStatus( status, statusString );
    }

/*
Get PCI device Vendor ID, Device ID, Sub-Vendor ID, Sub-Device ID strings array
*/
String[] getName( int vid, int did, int rid, int svid, int sdid, boolean h )
    {
    String[] names = new String[] { "?", "?", null, null };
    PCIvendor pv = pciDevices.get(vid);
    if ( pv != null )
        {
        names[0] = pv.getName();
        PCIdevice pd = pv.getMap().get( did );
        if ( pd != null )
            {
            names[1] = pd.getName();
            if ( h )
                {
                for ( PCIsubDevice sd : pd.getMap() ) 
                    {
                    if ( ( sd.getSVID() == svid )&&
                         ( sd.getSDID() == sdid ) )
                        {
                        names[3] = sd.getName();
                        break;
                        }
                    }
                PCIvendor pv1 = pciDevices.get( svid );
                if ( pv1 != null )
                    {
                    names[2] = pv1.getName();
                    if ( names[3] == null )
                        {
                        names[3] = "?";
                        }
                    }
                }
            }
        }
    return names;
    }

/*
Get PCI class name string by Base Class, Sub Class, Program Interface
*/
String getClassName ( int bc, int sc, int pi )
    {
    String name = "UNKNOWN";
    PCIbaseClass pbc = pciClasses.get(bc);
    if ( pbc != null )
        {
        name = pbc.getName();
        PCIsubClass psc = pbc.getMap().get(sc);
        if ( psc != null )
            {
            name = psc.getName();
            ArrayList al = psc.getMap();
            Iterator it = al.iterator();
            while( it.hasNext() )
                {
                PCIprogramInterface pif = (PCIprogramInterface) it.next();
                if ( pif.getID() == pi )
                    {
                    String name1 = pif.getName();
                    name = name + ": " + name1;
                    break;
                    }
                }
            }
        }
    return name; 
    }

/*
Get PCI Standard Capability name string by Capability ID
*/
String getStandardCapabilityName( int id )
    {
    String s = pciStdCaps.get( id );
    if ( s == null ) s = "UNKNOWN";
    return s;
    }

/*
Get PCI Extended Capability name string by Capability ID
*/
String getExtendedCapabilityName( int id )
    {
    String s = pciExtCaps.get( id );
    if ( s == null ) s = "UNKNOWN";
    return s;
    }

}
