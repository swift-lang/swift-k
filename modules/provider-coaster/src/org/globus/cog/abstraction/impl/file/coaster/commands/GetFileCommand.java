//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Sep 24, 2008
 */
package org.globus.cog.abstraction.impl.file.coaster.commands;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.file.coaster.buffers.Buffers;
import org.globus.cog.abstraction.impl.file.coaster.buffers.Buffers.Direction;
import org.globus.cog.abstraction.impl.file.coaster.buffers.WriteBuffer;
import org.globus.cog.abstraction.impl.file.coaster.buffers.WriteBufferCallback;
import org.globus.cog.abstraction.impl.file.coaster.handlers.GetFileHandler;
import org.globus.cog.abstraction.interfaces.ProgressMonitor;
import org.globus.cog.coaster.commands.Command;

public class GetFileCommand extends Command implements WriteBufferCallback {
    Logger logger = Logger.getLogger(GetFileCommand.class);
    public static final String NAME = "GET";
    private long len = -1;
    private WriteBuffer wt;
    private String dst;
    private boolean queued;
    // private ProgressMonitor pm;
    
    public GetFileCommand(String src, String dst, ProgressMonitor pm)
            throws IOException {
        super(NAME);
        
        if (logger.isDebugEnabled()) {
            logger.debug("GetFileCommand: " + src + " " + dst);
        }
        
        addOutData(src);
        addOutData(dst);
        this.dst = dst;
        // this.pm = pm;
        wt = createWriteBuffer(); 
    }

    protected WriteBuffer createWriteBuffer() throws IOException {
        return Buffers.newWriteBuffer(Buffers.getBuffers(Direction.IN), new File(dst), this);
    }
    
    protected void addInData(boolean fin, boolean err, byte[] data) {
        queued = false;
        if (err) {
            super.addInData(fin, err, data);
        }
        else {
            if (getLen() == -1) {
                setLen(unpackLong(data));
            }
            else {
                try {
                    wt.write(fin, data);
                }
                catch (Exception e) {
                    errorReceived(e.getMessage(), e);
                }
            }
        }
    }

    public void receiveCompleted() {
        super.receiveCompleted();
        try {
            wt.close();
        }
        catch (IOException e) {
            errorReceived(e.getMessage(), e);
        }
    }

    public void done(boolean last) {
    	if (last) {
    		try {
                wt.close();
            }
            catch (IOException e) {
                this.errorReceived("Failed to close file channel", e);
            }
    	}
    }

    @Override
    public void handleSignal(byte[] data) {
        if (Arrays.equals(GetFileHandler.QUEUED.getBytes(), data)) {
            setQueued(true);
        }
    }
    
    protected void setQueued(boolean queued) {
        if (logger.isInfoEnabled()) {
            logger.info(this + " queued");
        }
        this.queued = queued;
    }
    
    public void errorReceived(String msg, Exception t) {
    	if (logger.isInfoEnabled()) {
    		logger.info(this + " Error received: " + msg, t);
    	}
    	super.errorReceived(msg, t);
    }

    public void error(boolean last, Exception e) {
    	this.errorReceived("Failed to write file data", e);
    }

    protected long getLen() {
        return len;
    }

    protected void setLen(long len) {
        this.len = len;
    }
}
