// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------


    
package org.globus.cog.abstraction.impl.file;

/**
 * General Exception to handle exceptions that do not fall into the
 * specific categories
 */
public class GeneralException extends Exception
{
	/** Assign message to exception */
    public GeneralException(String message)
    {
        super(message);
    }

/** Assign message and throwable for exception*/
    public GeneralException(String message, Throwable parent)
    {
        super(message, parent);
    }
}
