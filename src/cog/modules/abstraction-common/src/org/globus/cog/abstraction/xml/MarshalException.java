
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------


    
package org.globus.cog.abstraction.xml;

public class MarshalException extends Exception
{
    public MarshalException(String message)
    {
        super(message);
    }

    public MarshalException(String message, Throwable parent)
    {
        super(message, parent);
    }
}
