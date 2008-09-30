//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Sep 26, 2008
 */
package org.globus.cog.abstraction.impl.file.coaster.handlers;

import java.io.File;

import org.globus.cog.abstraction.interfaces.GridFile;
import org.globus.cog.karajan.workflow.service.ProtocolException;

public class ListHandler extends CoasterFileRequestHandler {

    public void requestComplete() throws ProtocolException {
        File f = normalize(getInDataAsString(0));

        File[] l = f.listFiles();
        for (int i = 0; i < l.length; i++) {
            f = l[i];
            addOutData(f.getAbsolutePath());
            addOutData(f.isDirectory() ? GridFile.DIRECTORY : GridFile.FILE);
            addOutData("");
            addOutData(f.length());
            addOutData(0);
            addOutData(0);
            addOutData(0);
        }
        sendReply();
    }

}
