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
 * Created on Sep 30, 2005
 */
package org.globus.cog.coaster.channels;


public class NullChannel extends AbstractCoasterChannel {
	private boolean sink;

	protected NullChannel() {
		super(null, null, false);
	}
	
	protected NullChannel(boolean sink) {
        super(null, null, false);
        this.sink = sink;
    }
	
	protected void configureHeartBeat() {
		// override to do nothing
	}
	
	public void configureTimeoutChecks() {
		// do nothing
	}

	public void sendTaggedData(int i, int flags, byte[] bytes, SendCallback cb) {
		if (!sink) {
			throw new ChannelIOException("Null channel");
		}
	}

	public boolean isOffline() {
		return true;
	}
	
	public String toString() {
		return "NullChannel";
	}

	public void start() throws ChannelException {
	}

	public boolean isStarted() {
		return true;
	}
}
