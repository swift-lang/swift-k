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
 * Created on Oct 30, 2009
 */
package org.globus.cog.coaster.channels;

import java.io.InputStream;
import java.io.OutputStream;

import org.globus.cog.coaster.RequestManager;
import org.globus.cog.coaster.UserContext;

public class StreamChannel extends AbstractStreamCoasterChannel {
	private boolean started;	

	public StreamChannel(InputStream is, OutputStream os, RequestManager requestManager, 
	        UserContext userContext, boolean client) {
		super(requestManager, userContext, client);
		setInputStream(is);
		setOutputStream(os);
	}
	
	protected void connect() throws ChannelException {
	}

	@Override
	public boolean isStarted() {
		return started;
	}

	@Override
	public synchronized void start() throws ChannelException {
		if (isClient()) {
			setName("C(local)");
		}
		else {
			setName("S(local)");
		}
		initialize();
		logger.info(getContact() + "Channel started");
	}

	private void initialize() throws ChannelException {
		try {
			register();
			started = true;
		}
		catch (Exception e) {
			logger.debug("Exception while starting channel", e);
			throw new ChannelException(e);
		}
	}

}
