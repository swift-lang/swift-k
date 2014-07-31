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
 * Created on Sep 24, 2008
 */
package org.globus.cog.abstraction.impl.file.coaster.commands;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.file.coaster.buffers.Buffers;
import org.globus.cog.abstraction.impl.file.coaster.buffers.ReadBuffer;
import org.globus.cog.abstraction.impl.file.coaster.buffers.ReadBufferCallback;
import org.globus.cog.abstraction.impl.file.coaster.buffers.Buffers.Direction;
import org.globus.cog.coaster.ProtocolException;
import org.globus.cog.coaster.channels.CoasterChannel;
import org.globus.cog.coaster.commands.Command;

public class PutFileCommand extends Command implements ReadBufferCallback {
    public static final Logger logger = Logger.getLogger(PutFileCommand.class);

    public static final String NAME = "PUT";
    public static final String QUEUED = "QUEUED";

    private String dest;
    private long size;
    // private int chunks;
    private ReadBuffer rbuf;
    // private Exception ex;
    private String src;
    private boolean done;
    
    public PutFileCommand(String src, String dest) throws IOException, InterruptedException {
        this(src, dest, new File(src).length());
    }

    public PutFileCommand(String src, String dest, long length) throws IOException, InterruptedException {
        super(NAME);
        this.dest = dest;
        this.src = src;
        size = length;
        if (size != 0) {
            rbuf = createBuffer();
        }
    }

    protected ReadBuffer createBuffer() throws FileNotFoundException, InterruptedException {
        return Buffers.newReadBuffer(Buffers.getBuffers(Direction.OUT), new FileInputStream(src).getChannel(), size, this);
    }

    public void send() throws ProtocolException {
        CoasterChannel channel = getChannel();
        if (logger.isInfoEnabled()) {
            logger.info("Sending " + this + " on " + channel);
        }
        if (channel == null) {
            throw new ProtocolException("Unregistered command");
        }
        
        if (logger.isDebugEnabled()) {
            logger.debug(this + ", src: " + src + ", dest: " + dest + ", size: " + size);
        }
        channel.sendTaggedData(getId(), CoasterChannel.INITIAL_FLAG, getOutCmd().getBytes());
        channel.sendTaggedData(getId(), false, pack(size));
        channel.sendTaggedData(getId(), false, src.getBytes());
        channel.sendTaggedData(getId(), size == 0, dest.getBytes());
        if (logger.isInfoEnabled()) {
            logger.info(this + " sending data");
        }
    }

    public void dataSent() {
        super.dataSent();
        if (logger.isDebugEnabled()) {
            logger.debug(this + " data sent");
        }
        rbuf.freeFirst();
    }

    public void dataRead(boolean last, ByteBuffer buf) {
        if (logger.isDebugEnabled()) {
            logger.debug(this + " data read, last = " + last);
        }
        getChannel().sendTaggedData(getId(), last ? CoasterChannel.FINAL_FLAG : 0, buf, this);
        if (last) {
            done = true;
            closeBuffer();
        }
    }

    public void queued() {
        getChannel().sendTaggedData(getId(), CoasterChannel.SIGNAL_FLAG, QUEUED.getBytes());
    }

    private void closeBuffer() {
    	try {
            rbuf.close();
        }
        catch (IOException e) {
            logger.warn("Failed to close read buffer", e);
        }
    }

    public void error(boolean last, Exception e) {
        getChannel().sendTaggedReply(getId(), e.getMessage().getBytes(), true, true, null);
        closeBuffer();
    }

    public String toString() {
        return super.toString() + (done ? " (d)" : " (t)");
    }
}
