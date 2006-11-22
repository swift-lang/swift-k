// ----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Feb 10, 2005
 */
package org.globus.cog.karajan.arguments;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.stack.VariableNotFoundException;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.globus.cog.karajan.workflow.KarajanRuntimeException;

public final class ArgUtil {
	private final static Logger logger = Logger.getLogger(ArgUtil.class);

	public static final String NARGS = "#nargs";
	public static final String VARGS = "#vargs";
	public static final String CHANNEL_LIST = "#channels";

	private static final NamedArgumentsImpl EMPTYNARGS = new NamedArgumentsImpl();
	private static final VariableArgumentsImpl EMPTYVARGS = new VariableArgumentsImpl();

	public static NamedArguments getNamedReturn(VariableStack stack)
			throws VariableNotFoundException {
		return (NamedArguments) stack.getVarFromFrame(NARGS, 1);
	}

	public static VariableArguments getVariableReturn(VariableStack stack)
			throws VariableNotFoundException {
		return (VariableArguments) stack.getVarFromFrame(VARGS, 1);
	}

	public static VariableArguments getChannelReturn(VariableStack stack, Arg.Channel channel)
			throws VariableNotFoundException {
		return (VariableArguments) stack.getVarFromFrame(channel.getVariableName(), 1);
	}

	public static void initializeNamedArguments(VariableStack stack) {
		stack.currentFrame().setVar(NARGS, new NamedArgumentsImpl());
	}

	public static VariableArguments initializeVariableArguments(VariableStack stack) {
		VariableArguments vargs = new VariableArgumentsImpl();
		stack.currentFrame().setVar(VARGS, vargs);
		return vargs;
	}

	public static void removeNamedArguments(VariableStack stack) {
		stack.currentFrame().deleteVar(NARGS);
	}

	public static void removeVariableArguments(VariableStack stack) {
		stack.currentFrame().deleteVar(VARGS);
	}

	public static void setNamedArguments(VariableStack stack, NamedArguments args) {
		stack.currentFrame().setVar(NARGS, args);
	}

	public static void setVariableArguments(VariableStack stack, VariableArguments args) {
		stack.currentFrame().setVar(VARGS, args);
	}

	public static NamedArguments getNamedArguments(VariableStack stack) {
		return (NamedArguments) stack.currentFrame().getVar(NARGS);
	}

	public static VariableArguments getVariableArguments(VariableStack stack) {
		return (VariableArguments) stack.currentFrame().getVar(VARGS);
	}

	public static VariableArguments getChannelArguments(VariableStack stack, Arg.Channel channel) {
		return (VariableArguments) stack.currentFrame().getVar(channel.getVariableName());
	}

	public static boolean isReceiverPresent(VariableStack stack, Arg.Channel channel) {
		return stack.isDefined(channel.getVariableName());
	}

	public static void createChannel(VariableStack stack, Arg.Channel channel,
			VariableArguments data) {
		createChannelNL(stack, channel, data);
		addChannelToList(stack, channel);
	}


	private static void createChannelNL(VariableStack stack, Arg.Channel channel,
			VariableArguments data) {
		stack.currentFrame().setVar(channel.getVariableName(), data);
	}

	public static void createChannel(VariableStack stack, Arg.Channel channel) {
		createChannel(stack, channel, new VariableArgumentsImpl(channel.isCommutative()));
	}

	public static void removeChannel(VariableStack stack, Arg.Channel channel) {
		stack.currentFrame().deleteVar(channel.getVariableName());
		removeChannelFromList(stack, channel);
	}

	public synchronized static String channelVar(String channel) {
		return "#channel#" + channel;
	}

	private static void addChannelToList(VariableStack stack, Arg.Channel channel) {
		Set channels = getDefinedChannels(stack);
		if (!channels.contains(channel)) {
			// no need to add it if it's already there
			Set newchans = new HashSet(channels);
			newchans.add(channel);
			stack.setVar(CHANNEL_LIST, newchans);
		}
	}

	
	
	private static void removeChannelFromList(VariableStack stack, Arg.Channel channel) {
		if (stack.currentFrame().isDefined(CHANNEL_LIST)) {
			Set channels = (Set) stack.currentFrame().getVar(CHANNEL_LIST);
			channels.remove(channel);
		}
	}

	public static Set getDefinedChannels(VariableStack stack) {
		if (stack.isDefined(CHANNEL_LIST)) {
			try {
				return (Set) stack.getVar(CHANNEL_LIST);
			}
			catch (VariableNotFoundException e) {
				throw new KarajanRuntimeException(
						"Internal error: channels list was deleted. This should not be happening",
						e);
			}
		}
		else {
			return Collections.EMPTY_SET;
		}
	}
	
	public static void createChannels(VariableStack stack, Collection channels) {
		Iterator i = channels.iterator();
		while (i.hasNext()) {
			createChannel(stack, (Arg.Channel) i.next());
		}
	}

	public static void removeChannels(VariableStack stack, Collection channels) {
		Iterator i = channels.iterator();
		while (i.hasNext()) {
			removeChannel(stack, (Arg.Channel) i.next());
		}
	}

	public static void createChannels(VariableStack stack, Arg.Channel[] channels) {
		for (int i = 0; i < channels.length; i++) {
			createChannel(stack, channels[i]);
		}
	}

	public static void removeChannels(VariableStack stack, Arg.Channel[] channels) {
		for (int i = 0; i < channels.length; i++) {
			removeChannel(stack, channels[i]);
		}
	}

	public static boolean variableArgumentsPresent(VariableStack stack) {
		return stack.currentFrame().isDefined(VARGS);
	}

	public static boolean namedArgumentsPresent(VariableStack stack) {
		return stack.currentFrame().isDefined(NARGS);
	}

	public static void initializeChannelBuffers(VariableStack stack) throws ExecutionException {
		Set channels = ArgUtil.getDefinedChannels(stack);
		ArrayList pairs = new ArrayList();
		Iterator i = channels.iterator();
		while (i.hasNext()) {
			Arg.Channel channel = (Arg.Channel) i.next();
			VariableArguments dest = ArgUtil.getChannelReturn(stack, channel);
			if (!dest.isCommutative()) {
				pairs.add(new NameChannelPair(channel, dest));
			}
		}
		try {
			VariableArguments dest = ArgUtil.getVariableReturn(stack);
			if (!dest.isCommutative()) {
				pairs.add(new NameChannelPair(Arg.VARGS, dest));
			}
		}
		catch (VariableNotFoundException e) {
			// no vargs
		}
		pairs.trimToSize();
		stack.setVar("#chanbuf", new ParallelChannelBuffer(pairs));
	}
	
	public static void duplicateChannels(VariableStack stack) throws ExecutionException {
		Set channels = ArgUtil.getDefinedChannels(stack);
		Iterator i = channels.iterator();
		while (i.hasNext()) {
			Arg.Channel channel = (Arg.Channel) i.next();
			VariableArguments dest = ArgUtil.getChannelReturn(stack, channel);
			if (dest.isCommutative()) {
				ArgUtil.createChannelNL(stack, channel, new CommutativeVariableArguments());
			}
			else {
				ArgUtil.createChannelNL(stack, channel, new VariableArgumentsImpl());
			}
		}
	}

	public static ParallelChannelBuffer getParallelBuffer(VariableStack stack, String name)
			throws ExecutionException {
		if (name == null) {
			return (ParallelChannelBuffer) stack.getVar("#chanbuf#");
		}
		else {
			return (ParallelChannelBuffer) stack.getVar("#chanbuf#" + name);
		}
	}

	public static void addChannelBuffers(VariableStack stack) throws ExecutionException {
		ParallelChannelBuffer buf = (ParallelChannelBuffer) stack.parentFrame().getVar("#chanbuf");
		buf.add(stack.currentFrame());
	}

	public static void closeBuffers(VariableStack stack) throws ExecutionException {
		ParallelChannelBuffer buf = (ParallelChannelBuffer) stack.parentFrame().getVar("#chanbuf");
		if (buf == null) {
			throw new ExecutionException("Attempted to close nonexistent channel buffers");
		}
		buf.close(stack.currentFrame());
	}
}
