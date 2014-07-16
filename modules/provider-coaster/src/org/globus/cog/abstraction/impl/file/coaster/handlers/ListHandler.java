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
import java.util.Date;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.interfaces.GridFile;
import org.globus.cog.coaster.ProtocolException;

public class ListHandler extends CoasterFileRequestHandler {
    public static final Logger logger = Logger.getLogger(ListHandler.class);

    public void requestComplete() throws ProtocolException {
        File f = normalize(getInDataAsString(0));
        if (logger.isInfoEnabled()) {
            logger.info("Listing files in " + f.getAbsolutePath());
        }
        File[] l = f.listFiles();
        if (l == null) {
            if (logger.isInfoEnabled()) {
                logger.info("Invalid directory: " + f.getAbsolutePath());
            }
            sendError("No such directory: " + f.getAbsolutePath());
        }
        else {
            if (logger.isInfoEnabled()) {
                logger.info("Listing " + l.length + " files");
            }
            for (int i = 0; i < l.length; i++) {
                f = l[i];
                addOutData(f.getAbsolutePath());
                addOutData(new Date().toString());
                addOutData(f.isDirectory() ? GridFile.DIRECTORY
                        : GridFile.FILE);
                addOutData(f.length());
                addOutData(0);
                addOutData(0);
                addOutData(0);
            }
            if (logger.isInfoEnabled()) {
                logger.info("Sending reply");
            }
            sendReply();
        }
    }

}
