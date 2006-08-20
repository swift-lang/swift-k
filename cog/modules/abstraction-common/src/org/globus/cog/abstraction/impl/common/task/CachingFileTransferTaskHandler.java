// ----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Oct 11, 2004
 */
package org.globus.cog.abstraction.impl.common.task;

import org.globus.cog.abstraction.impl.fileTransfer.CachingDelegatedFileTransferHandler;
import org.globus.cog.abstraction.interfaces.Status;
import org.globus.cog.abstraction.interfaces.Task;

public class CachingFileTransferTaskHandler extends FileTransferTaskHandler {
    public void submit(Task task) throws IllegalSpecException,
            InvalidSecurityContextException, InvalidServiceContactException,
            TaskSubmissionException {
        if (task.getStatus().getStatusCode() != Status.UNSUBMITTED) {
            throw new TaskSubmissionException(
                    "TaskHandler can only handle unsubmitted tasks");
        }
        if (task.getType() != Task.FILE_TRANSFER) {
            throw new TaskSubmissionException(
                    "File transfer handler can only handle file transfer tasks");
        }

        CachingDelegatedFileTransferHandler dth = new CachingDelegatedFileTransferHandler();
        task.addStatusListener(this);

        registerTaskHandler(task, dth);

        dth.submit(task);
    }

}