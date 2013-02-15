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
import org.globus.cog.karajan.util.TypeUtil;
import org.globus.cog.karajan.workflow.ExecutionException;

public class CacheNode extends PartialArgumentsContainer {
	public static final Arg A_ON = new Arg.Optional("on");
	public static final Arg A_STATIC = new Arg.Optional("static");

	public static final String KEY = "##cachekey";
	public static final String STATICDEF = "##staticdef";

	private static final Map instances;

	static {
		setArguments(CacheNode.class, new Arg[] { A_ON });
		instances = new HashMap();
	}

	protected void partialArgumentsEvaluated(VariableStack stack) throws ExecutionException {
		cpre(A_ON.getValue(stack, getProperty(UID)), Boolean.valueOf(!A_ON.isPresent(stack)), stack);
	}

	protected void cpre(Object key, Boolean staticdef, VariableStack stack)
			throws ExecutionException {
		stack.setVar(KEY, key);
		stack.setVar(STATICDEF, staticdef);
		Cache cache = getCache(stack, staticdef);
		Arguments cached = null;
		synchronized (cache) {
			if (cache.isCached(key)) {
				cached = (Arguments) cache.getCachedValue(key);
			}
			else {
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
		}
		if (cached != null) {
			returnCachedArguments(stack, cached);
		    complete(stack);
            return;
		}
		super.partialArgumentsEvaluated(stack);
		addTrackingArguments(stack);
		startRest(stack);
	}

	public void post(VariableStack stack) throws ExecutionException {
		Arguments ret = getTrackingArguments(stack);
		Object key = stack.currentFrame().getVar(KEY);
		Boolean staticdef = (Boolean) stack.currentFrame().getVar(STATICDEF);
		Cache cache = getCache(stack, staticdef);
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

	public void failed(VariableStack stack, ExecutionException e) throws ExecutionException {
		Object key = stack.currentFrame().getVar(KEY);
		List l = null;
		synchronized (instances) {
			if (instances.containsKey(key)) {
				l = (List) instances.remove(key);
			}
		}
		if (l != null) {
			failAll(l, e);
		}
		super.failed(stack, e);
	}

	protected void failAll(List l, ExecutionException e) throws ExecutionException {
		Iterator i = l.iterator();
		while (i.hasNext()) {
			VariableStack stack = (VariableStack) i.next();
			super.failed(stack, e);
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
			args.addChannel(channel, ArgUtil.getChannelArguments(stack, channel));
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

	private static final Cache scache = new Cache();

	protected Cache getCache(VariableStack stack, Boolean staticdef) throws ExecutionException {
		boolean _static = TypeUtil.toBoolean(A_STATIC.getValue(stack, staticdef));
		if (_static) {
			return scache;
		}
		else {
			return stack.getExecutionContext().getCache();
		}
	}
}
