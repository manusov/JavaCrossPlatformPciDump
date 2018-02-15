//---------- PCI dump utility. (C)2018 IC Book Labs ----------------------------
// Module for system binary image analysing.

package pcidump;

import static pcidump.PCI.BYTES_PER_BUS;
import pcidump.PCI.CAP;
import static pcidump.PCI.DEVICES_PER_BUS;
import static pcidump.PCI.FUNCTIONS_PER_DEVICE;
import java.nio.MappedByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;

public class SystemManager 
{
private final DatabaseManager dm;
// private final PCI pci;
private final PCIbb pci;

private int bus, dev, fnc;
private int limitBus;

/*
protected SystemManager( DatabaseManager x1, byte[] x2 )
    {
    dm = x1;
    pci = new PCI(x2);
    }
*/

protected SystemManager( DatabaseManager x1, MappedByteBuffer x2 )
    {
    dm = x1;
    pci = new PCIbb(x2);
    }


protected OperationStatus load()
    {
        
//---------- Load file as array of bytes ---------------------------------------
        
    boolean f = true;
    int mcfgSize = 0;

/*    
        if ( pci.getData() != null) 
            {
            mcfgSize = pci.getData().length; 
            }
*/

        if ( pci.getMBB() != null) 
            {
            mcfgSize = pci.getMBB().limit();
            }

        StringBuilder s2 = new StringBuilder 
           (  "MCFG size = " + mcfgSize + " bytes"
            + "\r\n\r\nBus Dev Fnc VID  DID   SVID SDID   Class"
            + "\r\n----------------------------------------------------------");

//---------- Brief list --------------------------------------------------------

    if ( f && (mcfgSize>0) )
        {
        limitBus = mcfgSize / BYTES_PER_BUS;
        if ( ( mcfgSize % BYTES_PER_BUS ) > 0 ) limitBus++;
        
        for ( bus=0; bus<limitBus; bus++ )
            {
            for ( dev=0; dev<DEVICES_PER_BUS; dev++ )
                {
                int x = pci.readDword( bus, dev, fnc, 0xC );
                boolean multifunction = false;
                if ( (x & 0x800000) != 0 ) multifunction = true;
                int fncmax = 1;
                if (multifunction) fncmax = FUNCTIONS_PER_DEVICE;
                    
                for ( fnc=0; fnc<fncmax; fnc++ )
                    {
                        
                    int r00 = pci.readDword( bus, dev, fnc, 0 );
                    int r08 = pci.readDword( bus, dev, fnc, 0x08 );
                    int r0C = pci.readDword( bus, dev, fnc, 0x0C );
                    int r2C = pci.readDword( bus, dev, fnc, 0x2C );
                    
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
                        
                        if (header0)
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
                        String name = "  " +
                            dm.getClassName( baseClass, subClass, programIF );
                        
                        s2.append(digits);
                        s2.append(name);
                        }
                    }
                }
            }
        s2.append("\r\n");
        

//---------- Detail list with dump ---------------------------------------------

        for ( bus=0; bus<limitBus; bus++ )
            {
            for ( dev=0; dev<DEVICES_PER_BUS; dev++ )
                {
                int x = pci.readDword( bus, dev, fnc, 0xC );
                boolean multifunction = false;
                if ( (x & 0x800000) != 0 ) multifunction = true;
                int fncmax = 1;
                if (multifunction) fncmax = FUNCTIONS_PER_DEVICE;
                    
                for ( fnc=0; fnc<fncmax; fnc++ )
                    {
                    int r00 = pci.readDword( bus, dev, fnc, 0 );
                    int vid = r00 & 0xFFFF;
                    int did = ( r00 >> 16 ) & 0xFFFF;
                    int rid = pci.readByte( bus, dev, fnc, 8 );

                    if ( r00 != -1 )
                        {
                        //--- Vendor ID and Device ID ---
                        int x1 = pci.readDword( bus, dev, fnc, 0xC );
                        boolean header = false;
                        if ( (x1 & 0x7F0000) == 0 ) header = true;
                        int r2C = pci.readDword( bus, dev, fnc, 0x2C );
                        int svid = r2C & 0xFFFF;
                        int sdid = ( r2C >> 16 ) & 0xFFFF;
                        String[] names = dm.getName
                            ( vid, did, rid, svid, sdid, header );
                        String det1 = String.format
                            ( "\r\nBus = %02X  Device = %02X  Function = %02X",
                            bus, dev, fnc );
                        String det2 = String.format
                            ( "\r\nVendor = %04X = " + names[0] , vid );
                        String det3 = String.format
                            ( "\r\nDevice = %04X = " + names[1] , did );
                        s2.append(det1);
                        s2.append(det2);
                        s2.append(det3);
                        s2.append("\r\n");
                        //--- Subsystem Vendor ID and Device ID ---
                        if ( ( names.length>2 )   &&
                             ( names[2] != null ) &&
                             ( names[3] != null ) )
                            {
                            det2 = String.format
                            ( "SubVendor = %04X = " + names[2] , svid );
                            det3 = String.format
                            ( "\r\nSubDevice = %04X = " + names[3] , sdid );
                            s2.append(det2);
                            s2.append(det3);
                            s2.append("\r\n");
                            }

                        //--- PCI standard capabilities list ---
                        ArrayList scap = 
                            pci.stdandardCapabilitiesList( bus, dev, fnc );
                        if ( (scap!=null)&&(!scap.isEmpty()))
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
                                String sc = dm.getStandardCapabilityName(b);
                                table.append("\r\n").
                                    append(sa).append(sb).append(sc);
                                }
                            s2.append(table).append("\r\n");
                            }
                        
                        //--- PCI-X/PCIe extended capabilities list ---
                        ArrayList ecap = 
                            pci.extendedCapabilitiesList( bus, dev, fnc );
                        if ( (ecap!=null)&&(!ecap.isEmpty()))
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
                                String sc = dm.getExtendedCapabilityName(b);
                                table.append("\r\n").
                                    append(sa).append(sb).append(sc);
                                }
                            s2.append(table).append("\r\n");
                            }

                        //--- Hex dump ---
                        int j=0;
                        for ( int j1=0; j1<256; j1++ )
                            {
                            s2.append( String.format("\r\n%04X  ", j) );
                            for ( int j2=0; j2<16; j2++ )
                                {
                                int a = pci.readByte(bus, dev, fnc, j);
                                j++;
                                s2.append( String.format(" %02X", a) );
                                }
                            }
                        
                        s2.append("\r\n");
                        }
                    }
                }
            }
        }

//---------- Return ------------------------------------------------------------

    return new OperationStatus( s2.toString(),f );
    }
    
}
