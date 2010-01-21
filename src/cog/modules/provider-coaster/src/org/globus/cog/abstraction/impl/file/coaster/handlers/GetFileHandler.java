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

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.file.coaster.handlers.providers.IOHandle;
import org.globus.cog.abstraction.impl.file.coaster.handlers.providers.IOProvider;
import org.globus.cog.abstraction.impl.file.coaster.handlers.providers.IOProviderFactory;
import org.globus.cog.abstraction.impl.file.coaster.handlers.providers.IOReader;
import org.globus.cog.abstraction.impl.file.coaster.handlers.providers.ReadIOCallback;
import org.globus.cog.karajan.workflow.service.ProtocolException;
import org.globus.cog.karajan.workflow.service.channels.KarajanChannel;
import org.globus.cog.karajan.workflow.service.channels.SendCallback;

public class GetFileHandler extends CoasterFileRequestHandler implements SendCallback, ReadIOCallback {
    public static final Logger logger = Logger.getLogger(GetFileHandler.class);

    private long size;
    private Exception ex;
    private IOProvider provider;
    private IOReader reader;
    private boolean lengthSent;

    public void requestComplete() throws ProtocolException {
        String src = getInDataAsString(0);
        try {
            logger.info("01 " + src + " - request complete");
            provider = IOProviderFactory.getDefault().instance(getProtocol(src));
            logger.info("02 " + src + " - provider instantiated");
            sendReply();
            logger.info("13 " + src + " - reply sent");
        }
        catch (Exception e) {
            throw new ProtocolException(e);
        }
    }
    
    

    public void send() throws ProtocolException {
        KarajanChannel channel = getChannel();
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
            logger.info("03 " + src + " - calling pull");
            reader = provider.pull(src, dst, this);
            logger.info("04 " + reader + " - got reader");
            reader.start();
            logger.info("05 " + reader + " - reader started");
        }
        catch (IOException e) {
            throw new ProtocolException(e);
        }
    }

    public void dataSent() {
        reader.dataSent();
    }

    public void data(IOHandle handle, ByteBuffer data, boolean last) {
        if (!lengthSent) {
            throw new RuntimeException("No length provided");
        }
        logger.info("08 " + handle + " - got data");
        getChannel().sendTaggedReply(getId(), data, last, false, this);
        logger.info("09 " + handle + " - data sent");
    }

    public void done(IOHandle op) {
        if (!provider.isDirect()) {
            logger.info("10 " + op + " - done");
            getChannel().sendTaggedReply(getId(), "OK".getBytes(), true, false, null);
            logger.info("11 " + op + " - final reply sent");
            reader.close();
            logger.info("12 " + op + " - reader closed");
        }
    }

    public void error(IOHandle op, Exception e) {
        getChannel().sendTaggedReply(getId(), e.getMessage().getBytes(), true, true);
    }

    public void length(long len) {
        if (provider.isDirect()) {
            logger.info("06 " + reader + " - got length");
            lengthSent = true;
            getChannel().sendTaggedReply(getId(), pack(len), len == 0, false);
            logger.info("07 " + reader + " - sent length");
        }
    }
}
