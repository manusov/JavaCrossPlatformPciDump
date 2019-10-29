/*
PCI dump utility: text report = f( binary dump ). (C)2019 IC Book Labs.
------------------------------------------------------------------------
Status information class, data holder for string + boolean.
*/

package pcidump;

class OperationStatus
{
private final boolean statusFlag;   // false = error , true = success
private final String statusString;  // text string with output info

OperationStatus( boolean b, String s )
    {
    statusFlag = b;
    statusString = s;
    }

boolean getStatusFlag()
    {
    return statusFlag;
    }

String getStatusString()
    {
    return statusString;
    }
}
