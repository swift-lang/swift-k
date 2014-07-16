//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Nov 23, 2005
 */
package org.globus.cog.abstraction.impl.file.gridftp;

import org.globus.cog.util.Enumerated;

public class DataChannelAuthenticationType extends Enumerated {
	public static final DataChannelAuthenticationType NONE = new DataChannelAuthenticationType(
			"NONE", 0);
	public static final DataChannelAuthenticationType SELF = new DataChannelAuthenticationType(
			"SELF", 1);

	private DataChannelAuthenticationType(String literal, int value) {
		super(literal, value);
	}
}
