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
import org.globus.cog.karajan.workflow.service.ProtocolException;
import org.globus.cog.karajan.workflow.service.channels.KarajanChannel;
import org.globus.cog.karajan.workflow.service.commands.Command;

public class PutFileCommand extends Command implements ReadBufferCallback {
    public static final Logger logger = Logger.getLogger(PutFileCommand.class);

    public static final String NAME = "PUT";

    private String dest;
    private long size;
    // private int chunks;
    private ReadBuffer rbuf;
    // private Exception ex;
    private String src;
    
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
        return Buffers.newReadBuffer(new FileInputStream(src).getChannel(), size, this);
    }

    public void send() throws ProtocolException {
        KarajanChannel channel = getChannel();
        if (logger.isInfoEnabled()) {
            logger.info("Sending " + this + " on " + channel);
        }
        if (channel == null) {
            throw new ProtocolException("Unregistered command");
        }

        channel.sendTaggedData(getId(), false, getOutCmd().getBytes());
        channel.sendTaggedData(getId(), false, pack(size));
        channel.sendTaggedData(getId(), false, src.getBytes());
        channel.sendTaggedData(getId(), size == 0, dest.getBytes());
        if (logger.isInfoEnabled()) {
            logger.info("Sending data");
        }
    }

    public void dataSent() {
        super.dataSent();
        rbuf.freeFirst();
    }

    public void dataRead(boolean last, ByteBuffer buf) {
        getChannel().sendTaggedData(getId(), last ? KarajanChannel.FINAL_FLAG : 0, buf, this);
        if (last) {
            closeBuffer();
        }
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
}
