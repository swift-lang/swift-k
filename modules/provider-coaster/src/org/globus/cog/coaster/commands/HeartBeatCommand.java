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
 * Created on Jul 20, 2005
 */
package org.globus.cog.coaster.commands;

import org.apache.log4j.Logger;
import org.globus.cog.coaster.ProtocolException;
import org.globus.cog.coaster.handlers.HeartBeatHandler;


public class HeartBeatCommand extends Command {
    public static final Logger logger = Logger.getLogger(HeartBeatCommand.class);
    
    private long start;
    private static int sid;
    private int id;
    
	public HeartBeatCommand() {
		super(HeartBeatHandler.NAME);
		id = sid++;
	}
	
	public void send() throws ProtocolException {
	    start = System.currentTimeMillis();
	    addOutData(start);
		super.send();
	}

	public void replyReceived(boolean fin, boolean err, byte[] data) throws ProtocolException {
		super.replyReceived(fin, err, data);
		if (logger.isInfoEnabled()) {
            long rst = getInDataAsLong(0);
            long now = System.currentTimeMillis();
            logger.info(getChannel() + " up latency: " + (now - rst) + "ms, rtt: " + (now - start) + "ms");
        }
	}
}
