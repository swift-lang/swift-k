//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Sep 29, 2008
 */
package org.globus.cog.abstraction.impl.file.coaster.handlers;

import java.io.File;

import org.globus.cog.abstraction.impl.execution.coaster.NotificationManager;
import org.globus.cog.abstraction.interfaces.RemoteFile;
import org.globus.cog.coaster.ProtocolException;
import org.globus.cog.coaster.handlers.RequestHandler;

public abstract class CoasterFileRequestHandler extends RequestHandler {
    // private static final String HOME = System.getProperty("user.home");
    private static final String CWD = new File(".").getAbsolutePath();

    public static File normalize(RemoteFile rf) {
        if (rf.isAbsolute()) {
            return new File(rf.getPath());
        }
        else {
            return new File(CWD, rf.getPath());
        }
    }
    
    public static File normalize(String path) {
        return normalize(new RemoteFile(path));
    }

    protected String getProtocol(String file) {
        int index = file.indexOf(':');
        if (index == -1) {
            return "file";
        }
        else {
            return file.substring(0, index);
        }
    }

    protected void sendReply() throws ProtocolException {
        NotificationManager.getDefault().notIdle();
        super.sendReply();
    }
}
