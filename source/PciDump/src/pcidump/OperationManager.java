/*
PCI dump utility: text report = f( binary dump ). (C)2023 IC Book Labs.
------------------------------------------------------------------------
Class for convert binary image to text report, use database.
Binary system image file loaded and represented by SystemManager class.
Database text file loaded and represented by DatabaseManager class.
*/

package pcidump;

import java.util.ArrayList;
import java.util.Iterator;
import static pcidump.SystemManager.BYTES_PER_BUS;
import pcidump.SystemManager.CAP;
import static pcidump.SystemManager.DEVICES_PER_BUS;
import static pcidump.SystemManager.FUNCTIONS_PER_DEVICE;

class OperationManager 
{
private boolean status = false;
private String statusString = "N/A";

private final SystemManager systemManager;
private final DatabaseManager databaseManager;

/*
Class constructor
*/
OperationManager( SystemManager sm, DatabaseManager dm )
    {
    systemManager = sm;
    databaseManager = dm;
    if ( ( systemManager != null )&&( databaseManager != null ) )
        {
        status = true;
        }
    else
        {
        status = false;
        statusString = "Error: Operation Manager detect NULL objects";
        }
    }

/*
Get status object: boolean flag ( true=OK, false=ERROR ) + string
*/
OperationStatus getOperationStatus()
    {
    return new OperationStatus( status, statusString );
    }

/*
Build system information report = f ( binary dump, pci database )
*/
void buildSystemInfo()
    {
    int mcfgSize = systemManager.getMappedSize();
    
    StringBuilder systemInfo = new StringBuilder 
        ( "MCFG size = " + mcfgSize + " bytes"
          + "\r\n\r\nBus Dev Fnc VID  DID   SVID SDID   Class"
          + "\r\n----------------------------------------------------------" );
    
    int limitBus = mcfgSize / BYTES_PER_BUS;
    int bus, dev, fnc;
    if ( ( mcfgSize % BYTES_PER_BUS ) > 0 ) limitBus++;
    /*
    Brief list
    */        
    for ( bus=0; bus<limitBus; bus++ )
        {
        for ( dev=0; dev<DEVICES_PER_BUS; dev++ )
            {
            fnc = 0;  // v0.11 bug fix
            int x = systemManager.readDword( bus, dev, fnc, 0xC );
            boolean multifunction = false;
            if ( ( x & 0x800000 ) != 0 ) multifunction = true;
            int fncmax = 1;
            if ( multifunction ) fncmax = FUNCTIONS_PER_DEVICE;
            for ( fnc=0; fnc<fncmax; fnc++ )
                {
                int r00 = systemManager.readDword( bus, dev, fnc, 0 );
                int r08 = systemManager.readDword( bus, dev, fnc, 0x08 );
                int r0C = systemManager.readDword( bus, dev, fnc, 0x0C );
                int r2C = systemManager.readDword( bus, dev, fnc, 0x2C );
                int baseClass = ( r08 >> 24 ) & 0xFF;
                int subClass  = ( r08 >> 16 ) & 0xFF;
                int programIF = ( r08 >> 8  ) & 0xFF;
                boolean header0 = false;
                if ( (r0C & 0x7F0000) == 0 ) header0 = true;
                String digits;
                if ( r00 != -1 )
                    {
                    int vid = r00 & 0xFFFF;
                    int did = ( r00 >> 16 ) & 0xFFFF;
                    if ( header0 )
                        {
                        int svid = r2C & 0xFFFF;
                        int sdid = ( r2C >> 16 ) & 0xFFFF;
                        digits = String.format
                            ( "\r\n%02X  %02X  %02X  %04X:%04X  %04X:%04X" +
                              "   %02X %02X %02X",
                            bus, dev, fnc, vid, did, svid, sdid,
                            baseClass, subClass, programIF );
                        }
                    else
                        {
                        digits = String.format
                            ( "\r\n%02X  %02X  %02X  %04X:%04X  -        " +
                              "   %02X %02X %02X",
                            bus, dev, fnc, vid, did,
                            baseClass, subClass, programIF );
                        }
                    String name = "  " + databaseManager.getClassName
                        ( baseClass, subClass, programIF );
                    systemInfo.append( digits );
                    systemInfo.append( name );
                    }
                }
            }
        }
        systemInfo.append( "\r\n" );
    /*        
    Detail list with dump
    */
    for ( bus=0; bus<limitBus; bus++ )
        {
        for ( dev=0; dev<DEVICES_PER_BUS; dev++ )
            {
            fnc = 0;  // v0.11 bug fix
            int x = systemManager.readDword( bus, dev, fnc, 0xC );
            boolean multifunction = false;
            if ( ( x & 0x800000 ) != 0 ) multifunction = true;
            int fncmax = 1;
            if ( multifunction ) fncmax = FUNCTIONS_PER_DEVICE;
            for ( fnc=0; fnc<fncmax; fnc++ )
                {
                int r00 = systemManager.readDword( bus, dev, fnc, 0 );
                int vid = r00 & 0xFFFF;
                int did = ( r00 >> 16 ) & 0xFFFF;
                int rid = systemManager.readByte( bus, dev, fnc, 8 );
                if ( r00 != -1 )
                    {  // Vendor ID and Device ID
                    int x1 = systemManager.readDword( bus, dev, fnc, 0xC );
                    boolean header = false;
                    if ( (x1 & 0x7F0000) == 0 ) header = true;
                    int r2C = systemManager.readDword( bus, dev, fnc, 0x2C );
                    int svid = r2C & 0xFFFF;
                    int sdid = ( r2C >> 16 ) & 0xFFFF;
                    String[] names = databaseManager.getName
                        ( vid, did, rid, svid, sdid, header );
                    String det1 = String.format
                        ( "\r\nBus = %02X  Device = %02X  Function = %02X",
                          bus, dev, fnc );
                    String det2 = String.format
                        ( "\r\nVendor = %04X = " + names[0] , vid );
                    String det3 = String.format
                        ( "\r\nDevice = %04X = " + names[1] , did );
                    systemInfo.append( det1 );
                    systemInfo.append( det2 );
                    systemInfo.append( det3 );
                    systemInfo.append( "\r\n" );
                    // Subsystem Vendor ID and Device ID
                    if ( ( names.length > 2 ) &&
                         ( names[2] != null ) &&
                         ( names[3] != null ) )
                        {
                        det2 = String.format
                            ( "SubVendor = %04X = " + names[2] , svid );
                        det3 = String.format
                            ( "\r\nSubDevice = %04X = " + names[3] , sdid );
                        systemInfo.append( det2 );
                        systemInfo.append( det3 );
                        systemInfo.append( "\r\n" );
                        }
                    // PCI standard capabilities list
                    ArrayList scap = systemManager.
                        standardCapabilitiesList( bus, dev, fnc );
                    if ( ( scap != null )&&( !scap.isEmpty() ) )
                        {
                        StringBuilder table = new StringBuilder
                           ( "\r\nStandard capabilities" + 
                             "\r\nAddr. ID" +                             
                             "\r\n----------------" );
                        Iterator it = scap.iterator();
                        while ( it.hasNext() )
                            {
                            CAP cap = (CAP) it.next();
                            int a = cap.offset;
                            int b = cap.id;
                            String sa = String.format( "%04X  ", a );
                            String sb = String.format( "%02X = ", b );
                            String sc = databaseManager.
                                getStandardCapabilityName( b );
                            table.append("\r\n").
                                append(sa).append(sb).append(sc);
                            }
                        systemInfo.append(table).append( "\r\n" );
                        }
                    // PCI-X/PCIe extended capabilities list
                    ArrayList ecap = systemManager.
                        extendedCapabilitiesList( bus, dev, fnc );
                    if ( ( ecap!=null )&&( !ecap.isEmpty() ) )
                        {
                        StringBuilder table = new StringBuilder
                           ( "\r\nExtended capabilities" + 
                             "\r\nAddr. ID" +                             
                             "\r\n----------------" );
                        Iterator it = ecap.iterator();
                        while ( it.hasNext() )
                            {
                            CAP cap = (CAP) it.next();
                            int a = cap.offset;
                            int b = cap.id;
                            String sa = String.format( "%04X  ", a );
                            String sb = String.format( "%04X = ", b );
                            String sc = databaseManager.
                                getExtendedCapabilityName( b );
                            table.append( "\r\n" ).
                                append(sa).append(sb).append(sc);
                            }
                        systemInfo.append(table).append( "\r\n" );
                        }
                    // Hex dump
                    int j = 0;
                    for ( int j1=0; j1<256; j1++ )
                        {
                        systemInfo.append( String.format( "\r\n%04X  ", j ) );
                        for ( int j2=0; j2<16; j2++ )
                            {
                            int a = systemManager.readByte(bus, dev, fnc, j);
                            j++;
                            systemInfo.append( String.format(" %02X", a) );
                            }
                        }
                    systemInfo.append("\r\n");
                    }
                }
            }
        }
    status = true;
    statusString = systemInfo.toString();
    }
}
