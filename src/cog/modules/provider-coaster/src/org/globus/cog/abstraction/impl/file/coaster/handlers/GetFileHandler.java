//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Sep 26, 2008
 */
package org.globus.cog.abstraction.impl.file.coaster.handlers;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.file.coaster.handlers.providers.IOHandle;
import org.globus.cog.abstraction.impl.file.coaster.handlers.providers.IOProvider;
import org.globus.cog.abstraction.impl.file.coaster.handlers.providers.IOProviderFactory;
import org.globus.cog.abstraction.impl.file.coaster.handlers.providers.IOReader;
import org.globus.cog.abstraction.impl.file.coaster.handlers.providers.ReadIOCallback;
import org.globus.cog.coaster.ProtocolException;
import org.globus.cog.coaster.channels.CoasterChannel;
import org.globus.cog.coaster.channels.SendCallback;

public class GetFileHandler extends CoasterFileRequestHandler implements SendCallback, ReadIOCallback {
    public static final Logger logger = Logger.getLogger(GetFileHandler.class);
    
    public static final String QUEUED = "QUEUED";
    public static final String ABORT = "ABORT";
    public static final String ABORTED = "ABORTED";
    public static final String SUSPEND = "SUSPEND";
    public static final String RESUME = "RESUME";

    // private long size;
    // private Exception ex;
    private IOProvider provider;
    private IOReader reader;
    private boolean lengthSent, aborted;

    public void requestComplete() throws ProtocolException {
        String src = getInDataAsString(0);
        try {
            if (logger.isInfoEnabled()) {
                logger.info(this + " request complete; src=" + src);
            }
            provider = IOProviderFactory.getDefault().instance(getProtocol(src));
            sendReply();
        }
        catch (Exception e) {
            throw new ProtocolException(e);
        }
    }

    public void send() throws ProtocolException {
        CoasterChannel channel = getChannel();
        String src = getInDataAsString(0);
        String dst = getInDataAsString(1);
        if (channel == null) {
            throw new ProtocolException("Unregistered command");
        }
        if (!provider.isDirect()) {
            // send this first so that it doesn't get sent
            // after the final reply due to funny sequencing
            channel.sendTaggedReply(getId(), pack(-1), false, false);
        }
        try {
            reader = provider.pull(src, dst, this);
            if (logger.isInfoEnabled()) {
                logger.info(this + " reader: " + reader);
            }
            reader.start();
        }
        catch (IOException e) {
            throw new ProtocolException(e);
        }
    }
    
    public void dataSent() {
        if (logger.isDebugEnabled()) {
            logger.debug(this + " data sent");
        }
        reader.dataSent();
    }

    public void data(IOHandle handle, ByteBuffer data, boolean last) {
    	if (aborted) {
    		if (logger.isInfoEnabled()) {
    			logger.info(this + " Got data on aborted handler. Calling dataSent()");
    		}
    		dataSent();
    		return;
    	}
        if (!lengthSent) {
            throw new RuntimeException("No length provided");
        }
        if (logger.isDebugEnabled()) {
            logger.debug(this + " sending " + data.limit());
        }
        getChannel().sendTaggedReply(getId(), data, last, false, this);
        if (last) {
            if (logger.isInfoEnabled()) {
                logger.info(this + " unregistering (last)");
            }
        	unregister();
        }
    }
    
    @Override
    public void errorReceived(String msg, Exception t) {
        // this comes from the request side
        // but usually represents a dead channel
        abort();
        // abort() calls sendError() which calls unregister()
        // so don't bother
        // unregister();
    }

    public void queued() {
        if (logger.isInfoEnabled()) {
            logger.info(this + " sending queued signal (input throttled)");
        }
        getChannel().sendTaggedReply(getId(), QUEUED.getBytes(), CoasterChannel.SIGNAL_FLAG, null);
    }

    public void info(String msg) {
        if (logger.isInfoEnabled()) {
            logger.info(this + " -> " + msg);
        }
    }

    public void done(IOHandle op) {
        if (logger.isInfoEnabled()) {
            logger.info(this + " read done");
        }
        if (!provider.isDirect()) {
            getChannel().sendTaggedReply(getId(), "OK".getBytes(), true, false);
            reader.close();
        }
    }

    public void error(IOHandle op, Exception e) {
        if (e == null) {
            getChannel().sendTaggedReply(getId(), "Unknown error".getBytes(), CoasterChannel.FINAL_FLAG + CoasterChannel.ERROR_FLAG);
        }
        else {
            getChannel().sendTaggedReply(getId(), e.getMessage() != null ? e.getMessage().getBytes() : e.toString().getBytes(), 
        		CoasterChannel.FINAL_FLAG + CoasterChannel.ERROR_FLAG);
        }
    }

    public void length(long len) {
        if (provider.isDirect()) {
            if (lengthSent) {
                logger.warn("length() called twice", new Throwable("xz0001"));
            }
            if (logger.isInfoEnabled()) {
                logger.info(this + " sending length: " + len + ", " + System.identityHashCode(this));
            }
            lengthSent = true;
            getChannel().sendTaggedReply(getId(), pack(len), len == 0, false);
        }
    }

    @Override
    public void handleSignal(byte[] data) {
        if (Arrays.equals(data, ABORT.getBytes())) {
        	if (logger.isInfoEnabled()) {
        	    logger.info(this + " abort requested");
        	}
        	abort();
        }
        else if (Arrays.equals(data, SUSPEND.getBytes())) {
            if (logger.isInfoEnabled()) {
                logger.info(this + " suspending");
            }
            reader.suspend();
        }
        else if (Arrays.equals(data, RESUME.getBytes())) {
            if (logger.isInfoEnabled()) {
                logger.info(this + " resuming");
            }
            reader.resume();
        }
    }
    
    protected void abort() {
        try {
            if (logger.isInfoEnabled()) {
                logger.info(this + " aborting");
            }
            aborted = true;
            provider.abort(reader);
            // acknowledge abort
            sendError(ABORTED);
        }
        catch (Exception e) {
            logger.warn("Failed to abort transfer", e);
        }
    }
}
