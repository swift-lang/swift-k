//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jul 14, 2005
 */
package org.globus.cog.karajan.arguments;

import java.util.Iterator;
import java.util.List;

import org.globus.cog.karajan.stack.StackFrame;

/**
 * This class represents a set of chains of channel buffers. There
 * is one chain for each non-commutative channel.
 * 
 * @author Mihael Hategan
 *
 */
public final class ParallelChannelBuffer {
	private final List dest;

	public ParallelChannelBuffer(List dest) {
		this.dest = dest;
	}

	/**
	 * Add a buffer for each channel to the supplied stack frame and linked
	 * it with the previous buffer in each chain.  
	 */
	public void add(StackFrame frame) {
		Iterator i = dest.iterator();
		while (i.hasNext()) {
			NameChannelPair pair = (NameChannelPair) i.next();
			OrderedParallelVariableArguments opva = new OrderedParallelVariableArguments(
					pair.getValues(), pair.getLast());
			pair.setLast(opva);
			frame.setVar(pair.getChannel().getVariableName(), opva);
		}
	}

	/**
	 * Closes all buffers, which commits their values to the destination channels. 
	 */
	public void close(StackFrame frame) {
		Iterator i = dest.iterator();
		while (i.hasNext()) {
			NameChannelPair pair = (NameChannelPair) i.next();
			OrderedParallelVariableArguments opva = (OrderedParallelVariableArguments) frame.getVar(pair.getChannel().getVariableName());
			opva.close();
		}
	}

	public List getDest() {
		return dest;
	}

	public String toString() {
		return "ChannelBuffers";
	}
}
