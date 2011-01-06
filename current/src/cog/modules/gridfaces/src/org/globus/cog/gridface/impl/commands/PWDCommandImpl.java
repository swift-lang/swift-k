
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.gridface.impl.commands;

import org.globus.cog.abstraction.interfaces.ExecutableObject;
import org.globus.cog.abstraction.interfaces.FileOperationSpecification;
import org.globus.cog.abstraction.interfaces.Status;
import org.globus.cog.gridface.interfaces.GridCommand;

public class PWDCommandImpl extends GridCommandImpl implements GridCommand {

    public PWDCommandImpl() {
        super();
        setCommand(FileOperationSpecification.PWD);
    }

    /*
     * prepares the corresponding file operation task
     */
    public ExecutableObject prepareTask() throws Exception{
        if (validate() == true) {
            this.task = prepareFileOperationTask();
            return task;
        } else {
            return null;
        }
    }

    public boolean validate() {
		if (super.validate() == false)
		return false;

        if (getAttribute("sessionid") == null)
            return false;
        else
            return true;
    }

    public Object getOutput() {
        if (getStatus().getStatusCode() == Status.COMPLETED) {
            return task.getAttribute("output");
        } else
            return null;
    }
}