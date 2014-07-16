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
import org.globus.cog.abstraction.interfaces.DelegatedTaskHandler;

public class CachingFileTransferTaskHandler extends FileTransferTaskHandler {
    
    protected DelegatedTaskHandler newDelegatedTaskHandler(int type)
            throws TaskSubmissionException {
        return new CachingDelegatedFileTransferHandler();
    }
}