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
import org.globus.ftp.GridFTPSession;

public class DataChannelProtectionType extends Enumerated {
	public static final DataChannelProtectionType CLEAR = new DataChannelProtectionType("CLEAR",
			GridFTPSession.PROTECTION_CLEAR);
	public static final DataChannelProtectionType SAFE = new DataChannelProtectionType("SAFE",
			GridFTPSession.PROTECTION_SAFE);
	public static final DataChannelProtectionType CONFIDENTIAL = new DataChannelProtectionType(
			"CONFIDENTIAL", GridFTPSession.PROTECTION_CONFIDENTIAL);
	public static final DataChannelProtectionType PRIVATE = new DataChannelProtectionType(
			"PRIVATE", GridFTPSession.PROTECTION_PRIVATE);

	private DataChannelProtectionType(String literal, int value) {
		super(literal, value);
	}
}
