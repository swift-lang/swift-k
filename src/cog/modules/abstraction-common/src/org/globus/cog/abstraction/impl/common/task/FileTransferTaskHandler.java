// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.abstraction.impl.common.task;

import org.globus.cog.abstraction.impl.common.AbstractTaskHandler;
import org.globus.cog.abstraction.impl.fileTransfer.DelegatedFileTransferHandler;
import org.globus.cog.abstraction.interfaces.DelegatedTaskHandler;
import org.globus.cog.abstraction.interfaces.TaskHandler;

public class FileTransferTaskHandler extends AbstractTaskHandler {
	
    public FileTransferTaskHandler() {
        setType(TaskHandler.FILE_TRANSFER);
    }
    
    public String getName() {
        return "FileTransferTaskHandler";
    }

    protected DelegatedTaskHandler newDelegatedTaskHandler(int type)
            throws TaskSubmissionException {
        return new DelegatedFileTransferHandler();
    }
}