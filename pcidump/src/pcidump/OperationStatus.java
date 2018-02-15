//---------- PCI dump utility. (C)2018 IC Book Labs ----------------------------
// Module for status information class.

package pcidump;

public class OperationStatus
{
private final String statusString;  // text string with output info
private final boolean statusFlag;   // false = error , true = success

protected OperationStatus( String s , boolean b )
    {
    statusString = s;
    statusFlag = b;
    }

protected String getStatusString()
    {
    return statusString;
    }

protected boolean getStatusFlag()
    {
    return statusFlag;
    }

}
