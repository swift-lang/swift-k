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
 * Created on Jul 21, 2005
 */
package org.globus.cog.coaster.handlers;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.globus.cog.coaster.ProtocolException;
import org.globus.cog.coaster.channels.ChannelOptions;
import org.globus.cog.coaster.channels.ChannelOptions.CompressionType;
import org.globus.cog.coaster.channels.CoasterChannel;
import org.globus.cog.coaster.channels.SendCallback;


public class ChannelConfigurationHandler extends RequestHandler implements SendCallback {
    private static final Logger logger = Logger.getLogger(ChannelConfigurationHandler.class);

    public static final String NAME = "CHANNELCONFIG";
    
    private EnumSet<CompressionType> clientCompression;
    boolean deflate;
    
	public void requestComplete() throws ProtocolException {
		for (int i = 0; i < getInDataSize(); i++) {
            parseLine(getInDataAsString(i));
        }
		
		if (clientCompression.contains(CompressionType.DEFLATE)) {
		    CoasterChannel channel = getChannel();
		    if (channel.supportsOption(ChannelOptions.Type.COMPRESSION, CompressionType.DEFLATE)) {
		        logger.info("Enabling stream compression on " + channel);
		        deflate = true;
		        sendReply("compression: " + CompressionType.DEFLATE);
		        channel.setOption(ChannelOptions.Type.DOWN_COMPRESSION, CompressionType.DEFLATE);
		        return;
		    }
		}
		sendReply("compression: " + CompressionType.NONE);
	}

	@Override
    public void dataSent() {
	    if (deflate) {
	        getChannel().setOption(ChannelOptions.Type.UP_COMPRESSION, CompressionType.DEFLATE);
	    }
    }

    /*
	 * Override to receive notifications when data actually sent
	 */
	public void send(boolean err) throws ProtocolException {   
        CoasterChannel channel = getChannel();
        Collection<byte[]> outData = getOutData();
        boolean fin = (outData == null) || (outData.size() == 0);
        if (!fin) {
            Iterator<byte[]> i = outData.iterator();
            while (i.hasNext()) {
                byte[] buf = i.next();
                channel.sendTaggedReply(getId(), buf, !i.hasNext(), err, fin ? this : null);
            }
        }
        unregister();
    }

	
	private void parseLine(String s) {
        String[] ss = s.split("\\s+");
        if (ss[0].equals("compression:")) {
            clientCompression = EnumSet.noneOf(CompressionType.class);
            for (int i = 1; i < ss.length; i++) {
                clientCompression.add(CompressionType.valueOf(ss[1]));
            }
	    }
    }
}
