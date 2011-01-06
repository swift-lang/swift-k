// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/*
 * Created on Jul 7, 2003
 */
package org.globus.cog.karajan.workflow.nodes;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.arguments.ArgUtil;
import org.globus.cog.karajan.arguments.NamedArguments;
import org.globus.cog.karajan.arguments.VariableArguments;
import org.globus.cog.karajan.stack.StackFrame;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.util.Identifier;
import org.globus.cog.karajan.util.KarajanIterator;
import org.globus.cog.karajan.util.ThreadingContext;
import org.globus.cog.karajan.workflow.ExecutionException;

public class RaceFor extends AbstractParallelIterator {
	public static final Arg A_NAME = new Arg.Positional("name");
	public static final Arg A_IN = new Arg.Positional("in");

	static {
		setArguments(RaceFor.class, new Arg[] { A_NAME, A_IN });
	}

	

	public void iterate(VariableStack stack, Identifier var, KarajanIterator i)
			throws ExecutionException {
		stack.enter();
		ThreadingContext.set(stack, ThreadingContext.get(stack).split(0));
		super.iterate(stack, var, i);
	}

	protected void initializeChannelBuffers(VariableStack stack) throws ExecutionException {
		super.initializeChannelBuffers(stack);
	}

	protected void addChannelBuffers(VariableStack stack) throws ExecutionException {
		ArgUtil.initializeNamedArguments(stack);
		ArgUtil.initializeVariableArguments(stack);
		ArgUtil.duplicateChannels(stack);
	}

	protected void closeBuffers(VariableStack stack) throws ExecutionException {
		NamedArguments named = null;
		VariableArguments vargs = null;
		Map channels = null;

		named = ArgUtil.getNamedArguments(stack);
		vargs = ArgUtil.getVariableArguments(stack);
		Set dchannels = ArgUtil.getDefinedChannels(stack);
		channels = new Hashtable();
		Iterator i = dchannels.iterator();
		while (i.hasNext()) {
			Arg.Channel channel = (Arg.Channel) i.next();
			channels.put(channel, ArgUtil.getChannelArguments(stack, channel));
		}

		stack.leave();

		VariableArguments ret = ArgUtil.getVariableReturn(stack);
		ArgUtil.getNamedReturn(stack).merge(named);
		ArgUtil.getVariableReturn(stack).merge(vargs);
		i = channels.keySet().iterator();
		while (i.hasNext()) {
			Arg.Channel channel = (Arg.Channel) i.next();
			channel.getReturn(stack).merge((VariableArguments) channels.get(channel));
		}
	}

	protected void iterationCompleted(VariableStack stack) throws ExecutionException {
		StackFrame parent = stack.parentFrame();
		synchronized (parent) {
			boolean b = parent.getRegs().getBB();
			if (b) {
				return;
			}
			else {
				parent.getRegs().setBB(true);
			}
		}
		closeBuffers(stack);
		stack.setVar("#abort", true);
		stack.getExecutionContext().getStateManager().abortContext(ThreadingContext.get(stack));
		stack.leave();
		complete(stack);
	}

	protected boolean testAndSetChildFailed(VariableStack stack) {
		StackFrame parent = stack.parentFrame();
		synchronized (parent) {
			boolean b = parent.getRegs().getBB();
			parent.getRegs().setBB(true);
			return b;
		}
	}

}
