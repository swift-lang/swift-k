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
