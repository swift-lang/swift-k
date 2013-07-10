//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Feb 14, 2008
 */
package org.globus.cog.coaster;

import java.net.URI;

import org.globus.cog.coaster.channels.CoasterChannel;

public interface Service {

	boolean isRestricted();

	URI getContact();

	ServiceContext getContext();

	void irrecoverableChannelError(CoasterChannel channel, Exception e);
}
