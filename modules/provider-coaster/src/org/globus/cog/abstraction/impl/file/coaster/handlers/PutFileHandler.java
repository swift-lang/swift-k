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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.workflow.service.ProtocolException;

public class PutFileHandler extends CoasterFileRequestHandler {
    public static final Logger logger = Logger.getLogger(PutFileHandler.class);
    
    private long len = -1;
    private File f;
    private FileOutputStream fos;

    public void requestComplete() throws ProtocolException {
        try {
            fos.close();
            sendReply("OK");
        }
        catch (IOException e) {
            sendError("Failed to close file", e);
        }
    }
    
    protected void addInData(byte[] data) {
        if (len == -1) {
            len = unpackLong(data);
            if (logger.isInfoEnabled()) {
                logger.info("Size: " + len);
            }
        }
        else if (f == null) {
            f = normalize(new String(data));
            if (logger.isInfoEnabled()) {
                logger.info("Name: " + f.getPath());
            }
            try {
                fos = new FileOutputStream(f);
            }
            catch (FileNotFoundException e) {
                errorReceived(e.getMessage(), e);
            }
        }
        else {
            try {
                fos.write(data);
            }
            catch (IOException e) {
                errorReceived(e.getMessage(), e);
            }
        }
    }
}
