//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Apr 27, 2005
 */
package org.globus.cog.karajan.arguments;

import org.globus.cog.karajan.workflow.KarajanRuntimeException;

public class UnsupportedArgumentException extends KarajanRuntimeException {
	private static final long serialVersionUID = -4373714404335801518L;

	public UnsupportedArgumentException(String message) {
		super(message);

	}
}
