/*
 * Swift Parallel Scripting Language (http://swift-lang.org)
 * Code from Java CoG Kit Project (see notice below) with modifications.
 *
 * Copyright 2005-2014 University of Chicago
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
import java.util.Arrays;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.file.coaster.buffers.Buffers.Direction;
import org.globus.cog.abstraction.impl.file.coaster.buffers.ThrottleManager;
import org.globus.cog.abstraction.impl.file.coaster.handlers.providers.IOHandle;
import org.globus.cog.abstraction.impl.file.coaster.handlers.providers.IOProvider;
import org.globus.cog.abstraction.impl.file.coaster.handlers.providers.IOProviderFactory;
import org.globus.cog.abstraction.impl.file.coaster.handlers.providers.IOWriter;
import org.globus.cog.abstraction.impl.file.coaster.handlers.providers.WriteIOCallback;
import org.globus.cog.coaster.ProtocolException;
import org.globus.cog.coaster.channels.CoasterChannel;

public class PutFileHandler extends CoasterFileRequestHandler implements WriteIOCallback {
    public static final Logger logger = Logger.getLogger(PutFileHandler.class);
    
    public static final byte[] STOP = "STOP".getBytes();
    public static final byte[] CONTINUE = "CONTINUE".getBytes();

    private long len = -1;
    private String src, dst;
    private IOProvider provider;
    private IOWriter writer;
    private boolean done, suspended;

    public void requestComplete() throws ProtocolException {
        if (writer != null && provider.isDirect()) {
            try {
                writer.close();
            }
            catch (IOException e) {
                sendError("Failed to close file", e);
            }
        }
    }

    protected void addInData(boolean fin, boolean err, byte[] data) {
    	if (logger.isDebugEnabled()) {
    		logger.debug(this + " got data, fin = " + fin + 
    				", err = " + err + ", sz = " + data.length);
    	}
        try {
            if (err) {
                super.addInData(fin, err, data);
            }
            else if (len == -1) {
                len = unpackLong(data);
                if (logger.isDebugEnabled()) {
                    logger.debug(this + " " + dst + " Size: " + len);
                }
            }
            else if (src == null) {
                src = new String(data);
                if (logger.isInfoEnabled()) {
                    logger.info(this + " source: " + src);
                }
            }
            else if (dst == null) {
                dst = new String(data);
                if (logger.isInfoEnabled()) {
                    logger.info(this + " destination: " + dst);
                }
                provider = IOProviderFactory.getDefault().instance(getProtocol(dst));
                writer = provider.push(src, dst, this);
                if (!provider.isDirect()) {
                    writer.setUpThrottling();
                }
                writer.setLength(len);
            }
            else {
                if (provider.isDirect()) {
                    if (writer == null) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("Writer was not initialized properly, but data still being sent. Discarding.");
                        }
                    }
                    else {
                        writer.write(fin, data);
                    }
                }
                else {
                    sendError("Spurious data in request");
                }
            }
        }
        catch (Exception e) {
            try {
                sendError(e.getMessage(), e);
            }
            catch (ProtocolException e1) {
                logger.warn("Failed to send error", e1);
            }
        }
    }
    
    public void suspend() {
        synchronized(this) {
            if (done) {
                return;
            }
        }
        int tag = getId();
        if (logger.isDebugEnabled()) {
            logger.debug(this + " suspending");
        }
        suspended = true;
        getChannel().sendTaggedReply(tag, STOP, CoasterChannel.SIGNAL_FLAG);
        writer.suspend();
    }
    
    public void resume() {
        synchronized(this) {
            if (done) {
                return;
            }
            suspended = false;
        }
        int tag = getId();
        if (logger.isDebugEnabled()) {
            logger.debug(this + " resuming");
        }
        getChannel().sendTaggedReply(tag, CONTINUE, CoasterChannel.SIGNAL_FLAG);
        writer.resume();
    }

    public void done(IOHandle op) {
        synchronized(this) {
            done = true;
        }
        if (!provider.isDirect()) {
            writer.cancelThrottling();
        }
        try {
        	if (logger.isInfoEnabled()) {
        		logger.info(this + " Transfer done");
        	}
            sendReply("OK");
        }
        catch (ProtocolException e) {
            logger.warn("Failed to send reply", e);
        }
        catch (Exception e) {
            logger.warn("Failed to send reply", e);
        }
    }

    public void error(IOHandle op, Exception e) {
        try {
        	logger.info("Failed to write file data", e);
            sendError("Failed to write file data: " + e.getMessage());
        }
        catch (ProtocolException ee) {
            logger.warn("Failed to send reply", ee);
        }
    }
    
    public void info(String s) {
    	if (logger.isInfoEnabled()) {
    		logger.info(this + " -> " + s);
    	}
    }

    public void sendError(String error, Throwable e) throws ProtocolException {
        if (provider != null && writer != null) {
            try {
                provider.abort(writer);
            }
            catch (Exception ee) {
                logger.warn("Failed to abort transfer", ee);
            }
        }
        super.sendError(error, e);
    }

    public void errorReceived(String msg, Exception t) {
        logger.info(msg, t);
        if (provider != null) {
            try {
                provider.abort(writer);
            }
            catch (IOException e) {
                logger.info("Failed to close output stream", e);
            }
        }
        ThrottleManager.getDefault(Direction.OUT).unregister(this);
        unregister();
    }

    @Override
    public void handleSignal(byte[] data) {
        if (Arrays.equals(data, STOP)) {
        	suspended = true;
        }
        else if (Arrays.equals(data, CONTINUE)) {
        	synchronized(this) {
        	    suspended = false;
        	}
        }
        else {
        	logger.warn("Unhandled signal: " + String.valueOf(data));
        }
    }
}
