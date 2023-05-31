/*
PCI dump utility: text report = f( binary dump ). (C)2023 IC Book Labs.
------------------------------------------------------------------------
Class for load and interpreting PCI MCFG ( memory-mapped configuration space )
binary file image, this file path get from command line.
*/

package pcidump;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

class SystemManager 
{
static final int BYTES_PER_BUS        = 1048576;
static final int BYTES_PER_DEVICE     = 32768;
static final int BYTES_PER_FUNCTION   = 4096;
static final int DEVICES_PER_BUS      = 32;
static final int FUNCTIONS_PER_DEVICE = 8;
    
private boolean status = false;
private String statusString = "N/A";

private FileChannel channel;
private MappedByteBuffer buffer;
private int mappedSize = 0;

/*
Class constructor
*/
SystemManager( String binaryFileName )
    {
    status = false;
    try
        {
        channel = new RandomAccessFile( binaryFileName , "r" ).getChannel();
        long size = channel.size();
        if ( ( size > 0x010000000L )||( size < 0 ) )
            {
            statusString = "Too big MCFG file, maximum is 256MB";
            }
        else if ( size < 0x1000L )
            {
            statusString = String.format
                ( "Too small MCFG file (%d bytes), minimum is 4KB", size );
            }
        else
            {
            mappedSize = (int) size;
            buffer = channel.map
                ( FileChannel.MapMode.READ_ONLY, 0, mappedSize );
            status = ( buffer != null );
            }
        }
    catch( IOException e )
        {
        status = false;
        statusString = "Error: " + e.getMessage();
        }
    
        
    }

/*
Get status
*/
OperationStatus getOperationStatus()
    {
    return new OperationStatus( status, statusString );
    }

/*
Get size of loaded MCFG image, bytes
*/
int getMappedSize()
    {
    return mappedSize;
    }

/*
Read Byte from PCI Configuration Space image buffer
*/
int readByte( int bus, int dev, int fnc, int reg )
    {
    int i = bus * BYTES_PER_BUS 
          + dev * BYTES_PER_DEVICE
          + fnc * BYTES_PER_FUNCTION
          + reg;
    if ( i >= ( buffer.limit() ) ) return -1;
    int a = buffer.get( i ) & 0xFF;
    return a;
    }

/*
Read Word (16-bit) from PCI Configuration Space image buffer
*/
int readWord( int bus, int dev, int fnc, int reg )
    {
    int i = bus * BYTES_PER_BUS 
          + dev * BYTES_PER_DEVICE
          + fnc * BYTES_PER_FUNCTION
          + reg;
    if ( ( i+1 ) >= ( buffer.limit() ) ) return -1;
    int a = ( buffer.get( i )   & 0xFF );
    int b = ( buffer.get( i+1 ) & 0xFF ) << 8;
    return a + b;
    }

/*
Read DWord (32-bit) from PCI Configuration Space image buffer
*/
int readDword( int bus, int dev, int fnc, int reg )
    {
    int i = bus * BYTES_PER_BUS 
          + dev * BYTES_PER_DEVICE
          + fnc * BYTES_PER_FUNCTION
          + reg;
    if ( ( i+3 ) >= ( buffer.limit() ) ) return -1;
    int a = ( buffer.get( i   ) & 0xFF );
    int b = ( buffer.get( i+1 ) & 0xFF ) << 8;
    int c = ( buffer.get( i+2 ) & 0xFF ) << 16;
    int d = ( buffer.get( i+3 ) & 0xFF ) << 24;
    return a + b + c + d;
    }

/*
PCI Capability Structure class
*/
class CAP
    {
    protected int offset, id;
    protected CAP( int x1, int x2 )
        {
        offset = x1;
        id = x2;
        }
    }
/*
Return detected Standard PCI Capabilities list as int[n][m]
n = number of standard pci capabilities structures found
m = 2 : offset , id
*/
ArrayList<CAP> standardCapabilitiesList( int bus, int dev, int fnc )
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
/*
Return detected Extended PCI Capabilities list as int[n][m]
n = number of extended pci capabilities structures found
m = 2 : offset , id
*/
ArrayList<CAP> extendedCapabilitiesList( int bus, int dev, int fnc )
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
