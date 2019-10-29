//---------- PCI dump utility. (C)2018 IC Book Labs ----------------------------
// Module for PCI database management, text file compiler.

// Can refactor: change some entries format.
// Can refactor: single try-catch per global cycle, not duplicate try-catch.
// Can refactor: divide this class to some classes, otherwise too big.
// PCI.INF can be located in the JAR and divided:
// vid:did , svid:sdid
// classes
// registers
// capabilities.

// *** BREAKPOINT AT STRING 809 ***
// *** LEARN REGULAR EXPRESSIONS ***
// *** OR MAKE METHOD FOR DIVIDE STRING INTO FIELDS ***
// *** PROBABLY DIVIDE ONE TEXT FILE INTO 3+ BLOCKS ***

// can replace with divideString(), 
// better use method split(" "); but this can be slow, use selective.

// Required add pins and commands for complete model.

package pcidump;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class DatabaseManager 
{

//---------- Separators strings ------------------------------------------------
    
private final static String MARKER_DEVICES   = ".PCIDEVICES";
private final static String MARKER_CLASSES   = ".PCICLASSES";
private final static String MARKER_REGISTERS = ".PCIREGISTERS";

private final static String MARKER_SCAP = "SCAP";
private final static String MARKER_ECAP = "ECAP";

//---------- Array of database strings -----------------------------------------

private final List<String> srcRows;

//---------- Database #1 : PCI Vendors, Devices, Subsystems --------------------

private LinkedHashMap<Integer, PCIvendor> pciDevices;

private class PCIvendor
{
private final String vendorName;
private final LinkedHashMap<Integer, PCIdevice> devicesMap;

private PCIvendor( String s )
    {
    vendorName = s;
    devicesMap = new LinkedHashMap();
    }
private String getName()
    {
    return vendorName;
    }
private LinkedHashMap<Integer, PCIdevice> getMap()
    {
    return devicesMap;
    }
}

private class PCIdevice
{
    
private final String deviceName;
private final ArrayList<PCIsubDevice> subDevicesMap;
private PCIdevice( String s )
    {
    deviceName = s;
    subDevicesMap = new ArrayList();
    }
private String getName()
    {
    return deviceName;
    }
private ArrayList<PCIsubDevice> getMap()
    {
    return subDevicesMap;
    }
}

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
private String getName()
    {
    return subDeviceName;
    }
private int getSVID()
    {
    return subVendorID;
    }
private int getSDID()
    {
    return subDeviceID;
    }
}

//---------- Database #2 : PCI classes, Subclasses, Program interfaces ---------

private LinkedHashMap<Integer, PCIbaseClass> pciClasses;

private class PCIbaseClass
{
private final String pciClassName;
private final LinkedHashMap<Integer, PCIsubClass> subClassesMap;

private PCIbaseClass( String s )
    {
    pciClassName = s;
    subClassesMap = new LinkedHashMap();
    }
private String getName()
    {
    return pciClassName;
    }
private LinkedHashMap<Integer, PCIsubClass> getMap()
    {
    return subClassesMap;
    }
}

private class PCIsubClass
{
private final String pciSubClassName;
private final ArrayList<PCIprogramInterface> programInterfacesMap;

private PCIsubClass( String s )
    {
    pciSubClassName = s;
    programInterfacesMap = new ArrayList();
    }
private String getName()
    {
    return pciSubClassName;
    }
private ArrayList<PCIprogramInterface> getMap()
    {
    return programInterfacesMap;
    }
}

private class PCIprogramInterface
{
private final String programInterfaceName;
private final int programInterfaceID;
private PCIprogramInterface( String s, int p  )
    {
    programInterfaceName = s;
    programInterfaceID = p;
    }
private String getName()
    {
    return programInterfaceName;
    }
private int getID()
    {
    return programInterfaceID;
    }
}

//---------- Database #3 : Headers, Registers, Capabilities --------------------

private LinkedHashMap<Integer, PCIheader> pciHeaders;
private LinkedHashMap<Integer, String> pciStdCaps;
private LinkedHashMap<Integer, String> pciExtCaps;

private class PCIheader
{
private final String headerName;
private final LinkedHashMap<Integer, PCIregister> registersMap;

private PCIheader( String s )
    {
    headerName = s;
    registersMap = new LinkedHashMap();
    }
private String getName()
    {
    return headerName;
    }
private LinkedHashMap<Integer, PCIregister> getMap()
    {
    return registersMap;
    }
}

/*
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
*/

// for correct detecting shortest strings must be later,
// example; RW after RW1C.

private final static String[] TYPE_NAME =
    { "HWINIT", "ROS", "WO", "RW1CS", "RO", "RWS", "RW1C", "RW",
      "RSVDP", "RSVDZ", "RSVD" };

private enum RegType
    { HWINIT, ROS, WO, RW1CS, RO, RWS, RW1C, RW, RSVDP, RSVDZ, RSVD }

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
private String getName()
    {
    return name;
    }
private int getWidth()
    {
    return width;
    }
private Set getTypes()
    {
    return types;
    }
private Set getBits()
    {
    return bits;
    }
}

// Bit number must store explict, not as key, because some bitfields
// declared many times for alternative encodings.

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
private String getName()
    {
    return name;
    }
private int getWidth()
    {
    return width;
    }
private Set getTypes()
    {
    return types;
    }
private Set getValues()
    {
    return values;
    }
}

private class RegisterValue
{
private final String name;
private final int value;

private RegisterValue( String s, int v )
    {
    name = s;
    value = v;
    }
private String getName()
    {
    return name;
    }
private int getWidth()
    {
    return value;
    }
}

//---------- Constructor -------------------------------------------------------

protected DatabaseManager(List<String> ls)
    {
    srcRows = ls;
    }

//---------- Method for database load ------------------------------------------

protected OperationStatus load()    //  ( String s1 )
    {
//---------- Load file as array of strings -------------------------------------

    String s2 = "?";
    boolean f = true;
    
//---------- Initializing database management variables ------------------------

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

//---------- Parsing file as array of strings ----------------------------------

    if (f)
        {
        int n = 0, m = 0, min = 0, max = 0;
        int ns = 1, smin = 1, smax = 1;
        
        int start1 = 0, stop1 = 0;
        boolean mode1 = false;
        int start2 = 0, stop2 = 0;
        boolean mode2 = false;
        int start3 = 0, stop3 = 0;
        boolean mode3 = false, mode3a = false, mode3b = false;
        
        boolean error = false;
        String s;
        char c;
        if ( srcRows != null )
            {
            n = srcRows.size();
            Iterator it = srcRows.iterator();
            
//---------- Initializing data from first element of array ---------------------

            if ( it.hasNext() )
                {
                s = srcRows.get(0);
                min = s.length();
                max = min;
                }
            
//---------- Start of cycle ----------------------------------------------------

            while ( it.hasNext() )
                {

//---------- Get string, set current iteration status --------------------------

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
                
                if ( (!mode1)&&(s.startsWith(MARKER_DEVICES)) )
                    {
                    start1 = ns;
                    mode1 = true;
                    }
                else if ( (mode1)&&(s.startsWith(".")) )
                    {
                    stop1 = ns;
                    mode1 = false;
                    }

                if ( (!mode2)&&(s.startsWith(MARKER_CLASSES)) )
                    {
                    start2 = ns;
                    mode2 = true;
                    }
                else if ( (mode2)&&(s.startsWith(".")) )
                    {
                    stop2 = ns;
                    mode2 = false;
                    }
                
                if ( (!mode3)&&(s.startsWith(MARKER_REGISTERS)) )
                    {
                    start3 = ns;
                    mode3 = true;
                    }
                else if ( (mode3)&&(s.startsWith(".")) )
                    {
                    stop3 = ns;
                    mode3 = false; mode3a = false; mode3b = false;
                    }
                
//---------- Detect and handling strings for mode1 = PCI devices ---------------
// text block id = .
// comment = #
// device  = tab
// subsystem device:vendor = tab + tab
// otherwise hex number = vendor ID
// variables context: s = string , m = string length , ns = string number

// can replace with divideString()

    if ( ( mode1 ) && ( m>0 ) )  // mode1 = PCI devices
        {
        c = s.charAt(0);
        switch (c)
            {
            case '.'  : break;
            case '#'  : break;
            case '\t' : 
                        {
                        if ( ( m>1 ) && ( s.charAt(1) == '\t' ) )
                            //--- subvendor:subdevice ---
                            {
                            if ( m >= 11 )
                                {
                                try 
                                    {
                                    String shex1 = s.substring(2,6);
                                    String shex2 = s.substring(7,11);
                                    String sname = "";
                                    int nhex1 = 0, nhex2 = 0;
                                    if ( m>11 )
                                        {
                                        sname = (s.substring(11)).trim();
                                        }
                                    nhex1 = Integer.parseInt(shex1, 16);
                                    nhex2 = Integer.parseInt(shex2, 16);
                                    PCIsubDevice sd = 
                                        new PCIsubDevice(sname, nhex1, nhex2);
                                    if ( d != null )
                                        {
                                        d.getMap().add(sd);
                                        }
                                    else
                                        {
                                        error = true;
                                        }
                                    scount++;
                                    }
                                catch (Exception e)
                                    {
                                    error = true;
                                    }
                                }
                            }
                        else
                            //--- device ---
                            {
                            if ( m>=5 )
                                {
                                try 
                                    {
                                    String shex = s.substring(1,5);
                                    String sname = "";
                                    int nhex = 0;
                                    if ( m>5 )
                                        {
                                        sname = (s.substring(5)).trim();
                                        }
                                    nhex = Integer.parseInt(shex, 16);
                                    d = new PCIdevice(sname);
                                    if ( v != null )
                                        {
                                        v.getMap().put(nhex, d);
                                        }
                                    else
                                        {
                                        error = true;
                                        }
                                    dcount++;
                                    }
                                catch (Exception e)
                                    {
                                    error = true;
                                    }
                                }
                            }   
                        break;
                        }
            default   :
                        {
                        if ( m>=4 )
                            //--- new vendor ---
                            {
                            try 
                                {
                                String shex = s.substring(0,4);
                                String sname = "";
                                int nhex = 0;
                                if ( m>4 )
                                    {
                                    sname = (s.substring(4)).trim();
                                    }
                                nhex = Integer.parseInt(shex, 16);
                                v = new PCIvendor(sname);
                                pciDevices.put(nhex, v);
                                vcount++;
                                }
                            catch (Exception e)
                                {
                                error = true;
                                }
                            }
                        else
                            {
                            error = true;
                            }
                        }
            }
        }
               
//---------- Detect and handling strings for mode2 = PCI classes ---------------
// text block id = .
// comment = #
// class declaration = C
// subclass = tab
// program interface = tab + tab.

    if ( ( mode2 ) && ( m>0 ) )  // mode1 = PCI devices
        {
        c = s.charAt(0);
        switch (c)
            {
            case '.'  : break;
            case '#'  : break;
            case 'C'  :
                        {
                        if ( m>=3 )
                            //--- new base class ---
                            {
                            try 
                                {
                                String shex = s.substring(1,4).trim();
                                String sname = "";
                                int nhex = 0;
                                if ( m>4 )
                                    {
                                    sname = (s.substring(4)).trim();
                                    }
                                nhex = Integer.parseInt(shex, 16);
                                bc = new PCIbaseClass(sname);
                                pciClasses.put(nhex, bc);
                                bccount++;
                                }
                            catch (Exception e)
                                {
                                error = true;
                                }
                            }
                        else
                            {
                            error = true;
                            }
                        break;
                        }
            case '\t' : 
                        {
                        if ( ( m>1 ) && ( s.charAt(1) == '\t' ) )
                            //--- program interface ---
                            {
                            if ( m >= 4 )
                                {
                                try 
                                    {
                                    String shex = s.substring(2,4);
                                    String sname = "";
                                    int nhex = 0;
                                    if ( m>4 )
                                        {
                                        sname = (s.substring(4)).trim();
                                        }
                                    nhex = Integer.parseInt(shex, 16);
                                    PCIprogramInterface pi = 
                                        new PCIprogramInterface(sname, nhex);
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
                                catch (Exception e)
                                    {
                                    error = true;
                                    }
                                }
                            }
                        else
                            //--- subclass ---
                            {
                            if ( m>=3 )
                                {
                                try 
                                    {
                                    String shex = s.substring(1,3);
                                    String sname = "";
                                    int nhex = 0;
                                    if ( m>3 )
                                        {
                                        sname = (s.substring(3)).trim();
                                        }
                                    nhex = Integer.parseInt(shex, 16);
                                    sc = new PCIsubClass(sname);
                                    if ( bc != null )
                                        {
                                        bc.getMap().put(nhex, sc);
                                        }
                                    else
                                        {
                                        error = true;
                                        }
                                    sccount++;
                                    }
                                catch (Exception e)
                                    {
                                    error = true;
                                    }
                                }
                            }
                        break;
                        }
            default : error = true;
            }
        }

//---------- Detect and handling strings for mode3 = PCI registers -------------
// text block id = .
// comment = #
// header declaration = H
// register declaration = R
// bit declaration = tab + string
// standard capabilities mode = SCAP
// extended capabilities mode = ECAP
// standard capability = ID **
// extended capability = ID ****

    if(mode3)
        {
        if (s.startsWith(MARKER_SCAP))  // start of standard capabilities
            {
            mode3a = true; mode3b = false;
            }
        else if (s.startsWith(MARKER_ECAP))  // start of extended capabilities
            {
            mode3a = false; mode3b = true;
            }
        else if ( (mode3a)&&(s.startsWith("ID")) )  // standard capability ID
            {
            try
                {
                String scap = "";
                int cnum = 0;
                if (m>=6)
                    {
                    String snum = s.substring(2,6).trim();
                    cnum = Integer.parseInt(snum, 16);
                    }
                if (m>6)
                    {
                    scap = s.substring(6).trim();
                    }
                pciStdCaps.put(cnum, scap);
                stcount++;
                }
            catch (Exception e)
                {
                error = true;
                }
            }
        else if ( (mode3b)&&(s.startsWith("ID")) )  // extended capability ID
            {
            try
                {
                String scap = "";
                int cnum = 0;
                if (m>=8)
                    {
                    String snum = s.substring(2,8).trim();
                    cnum = Integer.parseInt(snum, 16);
                    }
                if (m>8)
                    {
                    scap = s.substring(8).trim();
                    }
                pciExtCaps.put(cnum, scap);
                excount++;
                }
            catch (Exception e)
                {
                error = true;
                }
            }
        else if (s.startsWith("H"))  // PCI register block header
            {
            try
                {
                String shdr = "";
                int hnum = 0;
                if (m>=4)
                    {
                    String snum = s.substring(2,4).trim();
                    hnum = Integer.parseInt(snum, 16);
                    }
                if (m>4)
                    {
                    shdr = s.substring(4).trim();
                    }
                ph = new PCIheader(shdr);
                pciHeaders.put(hnum, ph);
                hdcount++;
                }
            catch (Exception e)
                {
                error = true;
                }
            }
        else if (s.startsWith("R"))  // PCI register
            {
            try
                {
                String sreg = "";
                int rnum = 0;
                int rwidth = 0;
                if (m>=6)
                    {
                    String snum = s.substring(2,6).trim();
                    rnum = Integer.parseInt(snum, 16);
                    }
                
                int i1 = 7, i2 = 0;
                if (m>6)
                    {
                    i2 = s.indexOf( ' ', i1 );
                    if (i2>0)
                        {
                        String snum = s.substring( i1, i2+1 ).trim();
                        rwidth = Integer.parseInt(snum, 16);
                        
                        int k = s.indexOf(' ', i2+1);
                        sreg = s.substring(k).trim();
                        pr = new PCIregister(sreg, rwidth);
                        
                        if ( ph != null )
                            {
                            ph.getMap().put(rnum, pr);
                            }
                        else
                            {
                            error = true;
                            }
                        i1 = i2;
                        i2 = s.indexOf( ' ', i1+1 );
                        if ( i2>i1 )
                            {
                            String stypes = s.substring( i1, i2+1 ).trim();
                            i1 = 0;
                            int count = TYPE_NAME.length;
                            while ( i1>=0 )
                                {
                                RegType x = null;
                                for ( int i=0; i<count; i++ )
                                    {
                                    if ( stypes.startsWith( TYPE_NAME[i], i1 ) )
                                        {
                                        x = RegType.valueOf( TYPE_NAME[i] );
                                        break;
                                        }
                                    }
                                if ( x != null )
                                    {
                                    pr.getTypes().add(x);
                                    }
                                else
                                    {
                                    error = true;
                                    break;
                                    }
                                i1 = stypes.indexOf('+', i1+1);
                                if (i1>0) i1++;
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
            catch (Exception e)
                {
                error = true;
                }
            }
        else if (s.startsWith("\t\t"))  // Value of bitfield of PCI register 
            {
            try
                {
            int bitvalue = 0;
            String sa = s.trim();
            String[] sb = new String[2];
            int cnt;
            int k1 = 0, k2 = 0, k3 = sa.length();
            for ( cnt=0; cnt<2; cnt++ )
                {
                if ( k1>k3 ) break;
                if ( cnt<1 )
                    {
                    k2 = sa.indexOf(' ', k1);
                    sb[cnt] = sa.substring(k1, k2);
                    }
                else
                    {
                    sb[cnt] = sa.substring(k1);
                    }
                k1 = k2;
                while ( (sa.charAt(k1)==' ')||(sa.charAt(k1)=='\t') )
                    {
                    k1++;
                    if ( k1 > k3 ) break;
                    }
                }
            if ( cnt == 2 )
                {
                bitvalue = Integer.parseInt( sb[0] );
                RegisterValue rv = new RegisterValue ( sb[1], bitvalue );
                if ( ( rb != null ) && ( rb.getValues() != null ) )
                    {
                    rb.getValues().add(rv);
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
            catch (Exception e)
                {
                error = true;
                }
            }

        else if (s.startsWith("\t"))  // Bitfield of PCI register
            {
            try
                {
            int bitsnum = 0, bitswidth = 0;
            String sa = s.trim(); // spaces and tabulations deleted by trim()
            String[] sb = new String[4];
            int cnt;
            int k1 = 0, k2 = 0, k3 = sa.length();
            for ( cnt=0; cnt<4; cnt++ )
                {
                if ( k1>k3 ) break;
                if ( cnt<3 )
                    {
                    k2 = sa.indexOf(' ', k1);
                    sb[cnt] = sa.substring(k1, k2);
                    }
                else
                    {
                    sb[cnt] = sa.substring(k1);
                    }
                k1 = k2;
                while ( (sa.charAt(k1)==' ')||(sa.charAt(k1)=='\t') )
                    {
                    k1++;
                    if ( k1 > k3 ) break;
                    }
                }
            if ( cnt == 4 )
                {
                bitsnum = Integer.parseInt( sb[0] );
                bitswidth = Integer.parseInt( sb[1] );
                rb = new RegisterBit ( sb[3], bitsnum, bitswidth );

                int i1 = 0;
                int count = TYPE_NAME.length;
                while ( i1>=0 )
                    {
                    RegType x = null;
                    for ( int j=0; j<count; j++ )
                        {
                        if ( sb[2].startsWith( TYPE_NAME[j], i1 ) )
                            {
                            x = RegType.valueOf( TYPE_NAME[j] );
                            break;
                            }
                        }
                        if ( x != null )
                            {
                            rb.getTypes().add(x);
                            }
                        else
                            {
                            error = true;
                            break;
                            }
                        i1 = sb[2].indexOf('+', i1+1);
                        if (i1>0) i1++;
                        }
                    }
                else
                    {
                    error = true;
                    }
                }
                catch (Exception e)
                {
                error = true;    
                }
            
                if ( (pr!=null) && (pr.getBits() != null ) )
                    {
                    pr.getBits().add(rb);
                    }
                else
                    {
                    error = true;
                    }
            
            bitcount++;
            }
        else if ( (s.startsWith("."))||(s.startsWith("#"))||(m==0) )
            {
            // markers, comments, empty
            }
        else
            {
            error = true;  // otherwise error detected
            }
        }
                
//--- Check errors, increment string number ---

                ns++;
                if (error) break;
                }

//--- End of cycle, correct end string numbers ---

            }
        ns--;
        if ( stop1 == 0 ) stop1 = ns;
        if ( stop2 == 0 ) stop2 = ns;
        if ( stop3 == 0 ) stop3 = ns;

//--- Built statistic text string ---

        s2 = "Read " + n + " strings"
           + ", first minimum string[" + smin + "] = " + min
           + ", first maximum string[" + smax + "] = " + max
                
           + "\r\nFound devices declarations"
           + ", start string = " + start1
           + ", end string = "   + stop1
           + "\r\nVendors = " + vcount
           + ", devices = " + dcount
           + ", subdevices = " + scount
                
           + "\r\nFound classes declarations"
           + ", start string = " + start2
           + ", end string = "   + stop2
           + "\r\nBase classes = " + bccount
           + ", sub classes = " + sccount
           + ", program interfaces = " + picount

           + "\r\nFound registers declarations"
           + ", start string = " + start3
           + ", end string = "   + stop3
           + "\r\nHeaders = " + hdcount
           + ", registers = " + rgcount
           + ", bitfields = " + bitcount
           + ", standard capabilities = " + stcount
           + ", extended capabilities = " + excount;

        if (error)
            {
            s2 = s2 + "\r\nERROR AT LINE = " + ns;
            }
        }
//---------- Return with status string=s2 and status flag=f --------------------
    return new OperationStatus(s2,f);
    }


//---------- Methods, using created database -----------------------------------

protected String[] getName 
        ( int vid, int did, int rid, int svid, int sdid, boolean h )
    {
    String[] names = new String[] { "?", "?", null, null };
    
    PCIvendor pv = pciDevices.get(vid);
    if ( pv != null )
        {
        names[0] = pv.getName();
        PCIdevice pd = pv.getMap().get(did);
            if ( pd != null )
                {
                names[1] = pd.getName();
                if (h)
                    {
                    for ( PCIsubDevice sd : pd.getMap() ) 
                        {
                        if ( (sd.getSVID()==svid) && (sd.getSDID()==sdid) )
                            {
                            names[3] = sd.getName();
                            break;
                            }
                        }
                    PCIvendor pv1 = pciDevices.get(svid);
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
    
    // UNDER CONSTRUCTION, REQUIRED DECODE REVISION ID AND SUBSYSTEM VID:DID.
        
    return names;
    }

protected String getClassName ( int bc, int sc, int pi )
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

protected String getStandardCapabilityName( int id )
    {
    String s = pciStdCaps.get(id);
    if ( s == null ) s = "UNKNOWN";
    return s;
    }

protected String getExtendedCapabilityName( int id )
    {
    String s = pciExtCaps.get(id);
    if ( s == null ) s = "UNKNOWN";
    return s;
    }

}
