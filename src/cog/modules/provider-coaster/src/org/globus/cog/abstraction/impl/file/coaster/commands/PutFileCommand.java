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

import org.apache.log4j.Logger;
import org.globus.cog.karajan.workflow.service.ProtocolException;
import org.globus.cog.karajan.workflow.service.channels.KarajanChannel;
import org.globus.cog.karajan.workflow.service.commands.Command;

public class PutFileCommand extends Command {
    public static final Logger logger = Logger
            .getLogger(PutFileCommand.class);

    public static final String NAME = "PUT";

    private String remote;
    private long size;
    private int chunks;
    private FileInputStream is;
    private Exception ex;

    public PutFileCommand(String local, String remote)
            throws FileNotFoundException {
        super(NAME);
        this.remote = remote;
        size = new File(local).length();
        is = new FileInputStream(local);
        chunks = (int) (1 + (size / 16384));
    }

    public void send() throws ProtocolException {
        KarajanChannel channel = getChannel();
        if (logger.isInfoEnabled()) {
            logger.info("Sending " + this + " on " + channel);
        }
        if (channel == null) {
            throw new ProtocolException("Unregistered command");
        }

        try {
            channel.sendTaggedData(getId(), false, getOutCmd().getBytes());
            channel.sendTaggedData(getId(), false, pack(size));
            channel.sendTaggedData(getId(), size == 0, remote.getBytes());
            if (logger.isInfoEnabled()) {
                logger.info("Sending data");
            }
            int avail;
            byte[] buf = new byte[16384];
            while ((avail = is.available()) > 0) {
                if (avail > buf.length) {
                    is.read(buf);
                    channel.sendTaggedData(getId(), false, buf);
                }
                else {
                    byte[] mb = new byte[avail];
                    is.read(mb);
                    channel.sendTaggedData(getId(), true, mb);
                }
            }
            if (logger.isInfoEnabled()) {
                logger.info("Data sent");
            }
            is.close();
            setupReplyTimeoutChecker();
        }
        catch (IOException e) {
            reexecute(e.getMessage(), e);
        }
    }
}
