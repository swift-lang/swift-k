package org.globus.cog.gridshell.model;


//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------
 

public class ShellHistoryException extends Exception
{
 public ShellHistoryException(String message)
 {
     super(message);
 }

 public ShellHistoryException(String message, Throwable parent)
 {
     super(message, parent);
 }
}
