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
 * Created on Aug 1, 2005
 */
package org.globus.cog.coaster;

import org.globus.cog.coaster.channels.ChannelContext;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;

public class UserContext {

	private String name;
	private GSSCredential credential;
	private final ChannelContext channelContext;
	
	public UserContext(ChannelContext channelContext) {
		this(null, channelContext);
	}
	
	public UserContext(GSSCredential cred, ChannelContext channelContext) {
	    this.credential = cred;
	    this.name = getName(cred);
		this.channelContext = channelContext;
		if (channelContext == null) {
			throw new IllegalArgumentException("channelContext cannot be null");
		}
		if (channelContext.getServiceContext() != null) {
			channelContext.getServiceContext().registerUserContext(this);
		}
	}

	public static String getName(GSSCredential cred) {
		if (cred != null) {
            try {
                return cred.getName().toString();
            }
            catch (GSSException e) {
                throw new RuntimeException(e);
            }
        }
        else {
            return null;
        }
	}

	public GSSCredential getCredential() {
		return credential;
	}

	public void setCredential(GSSCredential credential) {
		this.credential = credential;
	}

	public String getName() {
		return name;
	}


	/**
	 * Returns the channel context of the channel that created this user context
	 */
	public ChannelContext getChannelContext() {
		return channelContext;
	}
}
