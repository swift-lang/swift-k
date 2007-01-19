// ----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Apr 26, 2005
 */
package org.globus.cog.karajan.workflow.nodes;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.arguments.ArgUtil;
import org.globus.cog.karajan.arguments.Arguments;
import org.globus.cog.karajan.arguments.TrackingNamedArguments;
import org.globus.cog.karajan.arguments.TrackingVariableArguments;
import org.globus.cog.karajan.arguments.VariableArguments;
import org.globus.cog.karajan.stack.VariableNotFoundException;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.util.Cache;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.globus.cog.karajan.workflow.events.FailureNotificationEvent;
import org.globus.cog.karajan.workflow.events.NotificationEvent;
import org.globus.cog.karajan.workflow.events.NotificationEventType;

public class CacheNode extends PartialArgumentsContainer {
	public static final Arg A_ON = new Arg.Optional("on");

	public static final String KEY = "##cachekey";

	private static final Map instances;

	static {
		setArguments(CacheNode.class, new Arg[] { A_ON });
		instances = new HashMap();
	}

	protected void partialArgumentsEvaluated(VariableStack stack) throws ExecutionException {
		cpre(A_ON.getValue(stack, getProperty(UID)), stack);
	}

	protected void cpre(Object key, VariableStack stack) throws ExecutionException {
		stack.setVar(KEY, key);
		Cache cache = getCache(stack);
		synchronized (cache) {
			if (cache.isCached(key)) {
				returnCachedArguments(stack, (Arguments) cache.getCachedValue(key));
				complete(stack);
				return;
			}

			synchronized (instances) {
				List inst = (List) instances.get(key);
				if (inst == null) {
					inst = new LinkedList();
					instances.put(key, inst);
				}
				else {
					inst.add(stack);
					return;
				}
			}
		}
		super.partialArgumentsEvaluated(stack);
		addTrackingArguments(stack);
		startRest(stack);
	}

	public void post(VariableStack stack) throws ExecutionException {
		Arguments ret = getTrackingArguments(stack);
		Object key = stack.currentFrame().getVar(KEY);
		Cache cache = getCache(stack);
		synchronized (cache) {
			cache.addValue(key, ret);

			synchronized (instances) {
				if (instances.containsKey(key)) {
					List l = (List) instances.get(key);
					while (l.size() > 0) {
						VariableStack st = (VariableStack) l.remove(0);
						returnCachedArguments(st, ret);
						complete(st);
					}
					instances.remove(key);
				}
			}
		}
		super.post(stack);
	}

	protected void notificationEvent(NotificationEvent e) throws ExecutionException {
		if (e.getType().equals(NotificationEventType.EXECUTION_FAILED)) {
			Object key = e.getStack().currentFrame().getVar(KEY);
			List l = null;
			synchronized (instances) {
				if (instances.containsKey(key)) {
					l = (List) instances.remove(key);
				}
			}
			if (l != null) {
				failAll(l, (FailureNotificationEvent) e);
			}
		}
		super.notificationEvent(e);
	}

	protected void failAll(List l, FailureNotificationEvent e) throws ExecutionException {
		Iterator i = l.iterator();
		while (i.hasNext()) {
			VariableStack stack = (VariableStack) i.next();
			super.notificationEvent(new FailureNotificationEvent(e.getFlowElement(), stack,
					e.getInitialStack(), e.getMessage(), e.getException()));
		}
	}

	private void addTrackingArguments(VariableStack stack) throws ExecutionException {
		try {
			ArgUtil.setNamedArguments(stack, new TrackingNamedArguments(
					ArgUtil.getNamedReturn(stack)));
		}
		catch (VariableNotFoundException e) {
		}
		try {
			ArgUtil.setVariableArguments(stack, new TrackingVariableArguments(
					ArgUtil.getVariableReturn(stack)));
		}
		catch (VariableNotFoundException e1) {
		}
		Set channels = ArgUtil.getDefinedChannels(stack);
		Iterator i = channels.iterator();
		while (i.hasNext()) {
			Arg.Channel channel = (Arg.Channel) i.next();
			ArgUtil.createChannel(stack, channel, new TrackingVariableArguments(
					ArgUtil.getChannelReturn(stack, channel)));
		}
	}

	private Arguments getTrackingArguments(VariableStack stack) throws VariableNotFoundException {
		Arguments args = new Arguments();
		args.setNamed(ArgUtil.getNamedArguments(stack));
		args.setVargs(ArgUtil.getVariableArguments(stack));
		Set channels = ArgUtil.getDefinedChannels(stack);
		Iterator i = channels.iterator();
		while (i.hasNext()) {
			Arg.Channel channel = (Arg.Channel) i.next();
			args.getChannels().put(channel, ArgUtil.getChannelArguments(stack, channel));
		}
		return args;
	}

	private void returnCachedArguments(VariableStack stack, Arguments ret)
			throws VariableNotFoundException {
		ArgUtil.getNamedReturn(stack).addAll(ret.getNamed().getAll());
		ArgUtil.getVariableReturn(stack).appendAll(ret.getVargs().getAll());
		Iterator i = ret.getChannels().keySet().iterator();
		while (i.hasNext()) {
			Arg.Channel channel = (Arg.Channel) i.next();
			ArgUtil.getChannelReturn(stack, channel).merge(
					(VariableArguments) ret.getChannels().get(channel));
		}
	}

	protected Cache getCache(VariableStack stack) throws ExecutionException {
		return stack.getExecutionContext().getTree().getCache();
	}
}
