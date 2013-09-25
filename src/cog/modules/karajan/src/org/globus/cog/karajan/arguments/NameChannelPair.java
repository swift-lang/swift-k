//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Dec 19, 2005
 */
package org.globus.cog.karajan.arguments;

public final class NameChannelPair {
	private final Arg.Channel channel;
	private final VariableArguments values;
	private OrderedParallelVariableArguments last;
	
	public NameChannelPair(Arg.Channel channel, VariableArguments values) {
		this.channel = channel;
		this.values = values;
	}

	public VariableArguments getValues() {
		return values;
	}

	public Arg.Channel getChannel() {
		return channel;
	}

	public OrderedParallelVariableArguments getLast() {
		return last;
	}

	public void setLast(OrderedParallelVariableArguments last) {
		this.last = last;
	}
}
