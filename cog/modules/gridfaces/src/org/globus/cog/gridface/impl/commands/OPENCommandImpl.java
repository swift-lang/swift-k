
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.gridface.impl.commands;

import org.globus.cog.abstraction.interfaces.ExecutableObject;
import org.globus.cog.abstraction.interfaces.FileOperationSpecification;
import org.globus.cog.abstraction.interfaces.Identity;
import org.globus.cog.gridface.interfaces.GridCommand;

public class OPENCommandImpl extends GridCommandImpl implements GridCommand {

    public OPENCommandImpl() {
    	super();
        setCommand(FileOperationSpecification.START);
    }

    public ExecutableObject prepareTask() throws Exception{
        if (validate() == true) {
            this.task = prepareFileOperationTask();
            return task;
        } else {
            return null;
        }
    }

    public boolean validate() {
        return super.validate();
    }

    /**
     * this outputs the session id for this connection
     * do not confuse it with task.getIdentity(); which is different
     * 
     * session id allows the ls, cd, mkdir, etc to work with a particular
     * connection.
     * 
     * task.getIdentity() is the id of this task and is what is displayed
     * when doing a ps
     */
    public Object getOutput() {
    	return (Identity) task.getAttribute("output");
    }

}