//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on May 24, 2013
 */
package org.griphyn.vdl.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Writer;

import org.apache.log4j.FileAppender;
import org.apache.log4j.helpers.LogLog;

public class LazyFileAppender extends FileAppender {
    /**
     * Override FileAppender.setFile to avoid creating an empty log file before
     * the code has a chance to customize the file name.
     */
    public synchronized void setFile(String fileName, boolean append, boolean bufferedIO, int bufferSize)
            throws IOException {
        LogLog.debug("setFile called: " + fileName + ", " + append);
        
        if (this.qw != null) {
            return;
        }
        
        // set a stdout writer just in case
        reset();
        Writer fw = createWriter(System.out);
        this.setQWForFiles(fw);
        
        this.fileName = fileName;
        this.fileAppend = append;
        this.bufferedIO = bufferedIO;
        this.bufferSize = bufferSize;
        LogLog.debug("setFile ended");
    }
    
    /**
     * Calling this method will signal this class that the log file name
     * has been configured and that the file can now be opened.
     * @throws IOException 
     */
    public void fileNameConfigured() throws IOException {
        LogLog.debug("fileNameConfigured called");

        // It does not make sense to have immediate flush and bufferedIO.
        if (this.bufferedIO) {
            setImmediateFlush(false);
        }

        // Save file name since reset() sets it to null
        String fileName = this.fileName;
        
        // Set to null to prevent parent class from closing System.out
        this.qw = null;
        reset();
        this.fileName = fileName;
        FileOutputStream ostream = null;
        try {
            //
            // attempt to create file
            //
            ostream = new FileOutputStream(this.fileName, this.fileAppend);
        }
        catch (FileNotFoundException ex) {
            //
            // if parent directory does not exist then
            // attempt to create it and try to create file
            // see bug 9150
            //
            String parentName = new File(this.fileName).getParent();
            if (parentName != null) {
                File parentDir = new File(parentName);
                if (!parentDir.exists() && parentDir.mkdirs()) {
                    ostream = new FileOutputStream(this.fileName, this.fileAppend);
                }
                else {
                    throw ex;
                }
            }
            else {
                throw ex;
            }
        }
        Writer fw = createWriter(ostream);
        if (this.bufferedIO) {
            fw = new BufferedWriter(fw, this.bufferSize);
        }
        this.setQWForFiles(fw);
        writeHeader();
        LogLog.debug("fileNameConfigured ended");
    }
}
