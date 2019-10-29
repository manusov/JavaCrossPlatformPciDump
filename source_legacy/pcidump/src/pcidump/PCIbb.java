//---------- PCI dump utility. (C)2018 IC Book Labs ----------------------------
// PCI hierarchy image for byte buffer

package pcidump;

import java.nio.MappedByteBuffer;

public class PCIbb extends PCI
{
protected MappedByteBuffer mbb;
    
protected PCIbb ( MappedByteBuffer x )
    {
    mbb = x;
    }

protected MappedByteBuffer getMBB()
    {
    return mbb;
    }

@Override protected int readByte( int bus, int dev, int fnc, int reg )
    {
    int i = bus * BYTES_PER_BUS 
          + dev * BYTES_PER_DEVICE
          + fnc * BYTES_PER_FUNCTION
          + reg;
    if ( i >= ( mbb.limit() ) ) return -1;
    int a = mbb.get(i) & 0xFF;
    return a;
    }

@Override protected int readWord( int bus, int dev, int fnc, int reg )
    {
    int i = bus * BYTES_PER_BUS 
          + dev * BYTES_PER_DEVICE
          + fnc * BYTES_PER_FUNCTION
          + reg;
    if ( (i+1) >= ( mbb.limit() ) ) return -1;
    int a = ( mbb.get(i)   & 0xFF );
    int b = ( mbb.get(i+1) & 0xFF ) << 8;
    return a + b;
    }

@Override protected int readDword( int bus, int dev, int fnc, int reg )
    {
    int i = bus * BYTES_PER_BUS 
          + dev * BYTES_PER_DEVICE
          + fnc * BYTES_PER_FUNCTION
          + reg;
    if ( (i+3)>=( mbb.limit() ) ) return -1;
    int a = ( mbb.get(i)   & 0xFF );
    int b = ( mbb.get(i+1) & 0xFF ) << 8;
    int c = ( mbb.get(i+2) & 0xFF ) << 16;
    int d = ( mbb.get(i+3) & 0xFF ) << 24;
    return a + b + c + d;
    }


}
