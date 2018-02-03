//---------- PCI dump utility. (C) IC Book Labs --------------------------------
// PCI hierarchy image

package pcidump;

import java.nio.MappedByteBuffer;
import java.util.ArrayList;

public class PCI 
{

protected static final int BYTES_PER_BUS = 1048576;
protected static final int BYTES_PER_DEVICE = 32768;
protected static final int BYTES_PER_FUNCTION = 4096;

protected static final int DEVICES_PER_BUS = 32;
protected static final int FUNCTIONS_PER_DEVICE = 8;
    
protected byte[] srcData = null;

protected PCI()
    {
    
    }

protected PCI( byte[] x )
    {
    srcData = x;
    }

protected PCI ( MappedByteBuffer x )
    {
    
    }

/*
protected byte[] getData()
    {
    return srcData;
    }
*/

protected int readByte( int bus, int dev, int fnc, int reg )
    {
    int i = bus * BYTES_PER_BUS 
          + dev * BYTES_PER_DEVICE
          + fnc * BYTES_PER_FUNCTION
          + reg;
    if ( i >= (srcData.length) ) return -1;
    int a = srcData[i] & 0xFF;
    return a;
    }

protected int readWord( int bus, int dev, int fnc, int reg )
    {
    int i = bus * BYTES_PER_BUS 
          + dev * BYTES_PER_DEVICE
          + fnc * BYTES_PER_FUNCTION
          + reg;
    if ( (i+1)>=(srcData.length) ) return -1;
    int a = ( srcData[i]   & 0xFF );
    int b = ( srcData[i+1] & 0xFF ) << 8;
    return a + b;
    }

protected int readDword( int bus, int dev, int fnc, int reg )
    {
    int i = bus * BYTES_PER_BUS 
          + dev * BYTES_PER_DEVICE
          + fnc * BYTES_PER_FUNCTION
          + reg;
    if ( (i+3)>=(srcData.length) ) return -1;
    int a = ( srcData[i]   & 0xFF );
    int b = ( srcData[i+1] & 0xFF ) << 8;
    int c = ( srcData[i+2] & 0xFF ) << 16;
    int d = ( srcData[i+3] & 0xFF ) << 24;
    return a + b + c + d;
    }

/*
private class PCIfunction
{
private final int bus, device, function;
private final byte[] deviceData;

private PCIfunction( int b, int d, int f, byte[] data )
    {
    bus = b;
    device = d;
    function = f;
    deviceData = data;
    }
private int[] getBDF()
    {
    int [] data = { bus, device, function };
    return data;
    }
private byte[] getData()
    {
    return deviceData;
    }
private int readByte(int i)
    {
    if ( i>=(deviceData.length) ) return -1;
    return deviceData[i];
    }
private int readWord(int i)
    {
    if ( (i+1)>=(deviceData.length) ) return -1;
    int a = deviceData[i];
    int b = deviceData[i+1] << 8;
    return a + b;
    }
private int readDword(int i)
    {
    if ( (i+3)>=(deviceData.length) ) return -1;
    int a = deviceData[i];
    int b = deviceData[i+1] << 8;
    int c = deviceData[i+2] << 16;
    int d = deviceData[i+3] << 24;
    return a + b + c + d;
    }
}
*/

protected class CAP
    {
    protected int offset, id;
    protected CAP( int x1, int x2 )
        {
        offset = x1;
        id = x2;
        }
    }

// Return standard pci capabilities list as int[n][m]
// n = number of standard pci capabilities structures found
// m = 2 : offset , id

protected ArrayList<CAP> stdandardCapabilitiesList( int bus, int dev, int fnc )
    {
    ArrayList<CAP> list = new ArrayList();
    int pciStatus = readWord( bus, dev, fnc, 6 );
    if ( ( pciStatus & 0x10 ) != 0 )       // check capabilities list presence
        {
        int capPointer = readByte ( bus, dev, fnc, 0x34 );
        capPointer &= 0xFC;
        while ( capPointer != 0 )
            {
            int capID = readByte ( bus, dev, fnc, capPointer );
            CAP cap = new CAP( capPointer , capID );
            list.add(cap);
            capPointer = readByte ( bus, dev, fnc, capPointer+1 );
            capPointer &= 0xFC;
            }
        }
    return list;
    }

// Return extended pci capabilities list as int[n][m]
// n = number of extended pci capabilities structures found
// m = 2 : offset , id

protected ArrayList<CAP> extendedCapabilitiesList( int bus, int dev, int fnc )
    {
    ArrayList<CAP> list = new ArrayList();
    int capPointer = 0x100;
    int header = readDword( bus, dev, fnc, capPointer );
    int alias = readDword( bus, dev, fnc, 0 );
    if ( ( header != 0 ) && ( header != 0xFFFFFFFF ) && ( header != alias ) )
        {
        for ( int i=0; i<256; i++ )  // iterations limited, prevent hang
            {
            int capID = header & 0xFFFF;
            CAP cap = new CAP( capPointer , capID );
            list.add(cap);
            capPointer = ( header >> 20 ) & 0xFFF;
            if ((( capPointer == 0 )||( capPointer > 4092 ))) break;
            header = readDword( bus, dev, fnc, capPointer );
            }
        }
    return list;
    }


}
