// ----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Feb 8, 2005
 */
package org.globus.cog.karajan.compiled.nodes;

import k.rt.Channel;
import k.rt.FutureMemoryChannel;

import org.globus.cog.karajan.compiled.nodes.functions.NullaryOp;

public class ChannelNew extends NullaryOp<k.rt.Channel<Object>> {
	
	@Override
	protected Channel<Object> value() {
		return new FutureMemoryChannel<Object>();
	}
}