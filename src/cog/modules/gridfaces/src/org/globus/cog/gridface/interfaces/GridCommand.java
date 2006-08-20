
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.gridface.interfaces;

import java.util.Calendar;
import java.util.Enumeration;
import java.util.Vector;
import java.util.Hashtable;

import org.globus.cog.abstraction.interfaces.ExecutableObject;
import org.globus.cog.abstraction.interfaces.StatusListener;
import org.globus.cog.abstraction.interfaces.Task;

/**
 * Grid command can be executed in cog-abstraction as tasks.
*/
public interface GridCommand extends StatusListener, ExecutableObject {

    public Task getTask();
    public void setTask(Task newTask);
    public Integer getId();
	 
    /** set the command to be executed */
    public void setCommand(String command);

    /** get the command name */
    public String getCommand();

    /** set an array of input arguments */
    public void setArguments(String[] arguments);

    /** add one argument at a time */
    public void addArgument(String argument);

    /** get the nth argument of the command */
    public String getArgument(int n);

    /** get the total number of arguments */
    public int getArgumentsSize();

    /** get all arguments in the form of a vector */
    public Vector getArguments();

    /** prepare the executable object for this command */
    public ExecutableObject prepareTask() throws Exception;

    /** set an attribute. Attributes are the carriers of information. */
    public void setAttribute(String name, Object value);

    /** get an attribute */
    public Object getAttribute(String name);

    /** return the hashtable containing all attributes. This is useful in transferring attributes to the task specification */
    public Hashtable getAttributes();

    public void setAttributes(Hashtable attribs);
    
    /** add listener to the command */
    public void addStatusListener(StatusListener listener);

    /** remove listener for this command */
    public void removeStatusListener(StatusListener listener);

    /** Get a list of all listeners */
    public Enumeration getStatusListeners();

    /** validate the given command and return true or false */
    public boolean validate();

    public Calendar getSubmittedTime();

    public Calendar getCompletedTime();
    
    /** Get output of the given command when status changes to COMPLETED */
    public Object getOutput();

    /** Get error msg for the given command when status changes to FAILED */
    public String getError();

    /** Set attributes for the task to be prepared for this command */
    public void setTaskAttribute(String key, Object value);

    /** get  attributes set for the given command */
    public Object getTaskAttribute(String key);
    

	/** Returns the exception for the current status object
	 * this is here so it can be overridden by the exec object
	 * to make sence of the error codes of jglobus
	 */
	public Exception getException();
	
	/**
	 * returns an excpetion and its stack to a string using
	 * the static method in gridfaces.impl.util.LoggerImpl
	 * @return
	 */
	public String getExceptionString();

}
