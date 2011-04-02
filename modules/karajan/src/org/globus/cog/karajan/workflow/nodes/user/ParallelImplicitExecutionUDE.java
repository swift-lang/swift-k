// ----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Mar 21, 2005
 */
package org.globus.cog.karajan.workflow.nodes.user;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.arguments.ArgUtil;
import org.globus.cog.karajan.arguments.Arguments;
import org.globus.cog.karajan.arguments.NamedArguments;
import org.globus.cog.karajan.arguments.NamedArgumentsImpl;
import org.globus.cog.karajan.arguments.VariableArguments;
import org.globus.cog.karajan.stack.VariableNotFoundException;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.stack.VariableUtil;
import org.globus.cog.karajan.util.DefUtil;
import org.globus.cog.karajan.util.DefinitionEnvironment;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.globus.cog.karajan.workflow.events.NotificationEvent;
import org.globus.cog.karajan.workflow.events.NotificationEventType;
import org.globus.cog.karajan.workflow.futures.Future;
import org.globus.cog.karajan.workflow.futures.FutureNameBindingVariableArguments;
import org.globus.cog.karajan.workflow.futures.FutureNamedArgument;
import org.globus.cog.karajan.workflow.futures.FutureVariableArguments;

public class ParallelImplicitExecutionUDE extends UserDefinedElement {
	public static final Logger logger = Logger.getLogger(ParallelImplicitExecutionUDE.class);

	public static final String WRAPPER = "#wrapper";

	static {
		setArguments(ParallelImplicitExecutionUDE.class, new Arg[] { A_NAME, A_ARGUMENTS, A_VARGS,
				A_NAMED, A_CHANNELS, A_OPTARGS });
	}

	public void startInstance(VariableStack stack, UDEWrapper wrapper, DefinitionEnvironment env)
			throws ExecutionException {
		VariableStack copy = stack.copy();
		copy.leave();
		copy.enter();
		copy.setCaller(this);
		copy.setVar(ARGUMENTS_THREAD, true);
		prepareArguments(copy, wrapper);
		Arguments args = getUDEArguments(copy);
		startArguments(copy, wrapper);
		stack.setCaller(this);
		stack.setVar(DefUtil.ENV, env);
		startBody(stack, args);
	}

	private void removeReturns(VariableStack stack) {
		ArgUtil.removeVariableArguments(stack);
		ArgUtil.removeNamedArguments(stack);
		ArgUtil.removeChannels(stack, getChannels());
	}

	protected void prepareArguments(VariableStack stack, UDEWrapper wrapper)
			throws ExecutionException {
		boolean vargsInitialized = false;
		if (hasNestedArgs()) {
			NamedArguments nargs = new NamedArgumentsImpl();
			// add arguments from properties
			Iterator i = wrapper.getStaticArguments().entrySet().iterator();
			while (i.hasNext()) {
				Map.Entry e = (Map.Entry) i.next();
				nargs.add((String) e.getKey(), VariableUtil.expand(e.getValue(), stack));
			}
			boolean text = A_INLINE_TEXT.isPresentStatic(this);
			if (wrapper.getNonpropargs().size() > 0 && (wrapper.elementCount() > 0 || text)) {
				VariableArguments args = new FutureNameBindingVariableArguments(nargs,
						wrapper.getNonpropargs());
				ArgUtil.setVariableArguments(stack, args);
				vargsInitialized = true;
			}
			ArgUtil.setNamedArguments(stack, nargs);
		}

		if (this.hasVargs() && !vargsInitialized) {
			ArgUtil.setVariableArguments(stack, new FutureVariableArguments());
		}

		Iterator i = getChannels().iterator();
		while(i.hasNext()) {
			ArgUtil.createChannel(stack, (Arg.Channel) i.next(), new FutureVariableArguments());
		}
	}

	protected void startArguments(VariableStack stack, UDEWrapper wrapper)
			throws ExecutionException {
		wrapper.executeWrapper(stack);
	}

	protected void closeArgs(VariableStack stack) throws ExecutionException {
		if (Arg.VARGS.isPresent(stack)) {
			((FutureVariableArguments) Arg.VARGS.get(stack)).close();
		}
		Iterator i = getChannels().iterator();
		while(i.hasNext()) {
			Arg.Channel channel = (Arg.Channel) i.next();
			((FutureVariableArguments) ArgUtil.getChannelArguments(stack, channel)).close();
		}
	}

	protected void setArguments(VariableStack stack) {

	}

	protected Arguments getUDEArguments(VariableStack stack) throws ExecutionException {
		Arguments fnargs = new Arguments();

		fnargs.setNamed(ArgUtil.getNamedArguments(stack));
		if (hasVargs()) {
			fnargs.setVargs(Arg.VARGS.get(stack));
		}

		Iterator i = getChannels().iterator();
		while(i.hasNext()) {
			Arg.Channel channel = (Arg.Channel) i.next();
			fnargs.addChannel(channel, ArgUtil.getChannelArguments(stack, channel));
		}

		return fnargs;
	}

	private void returnCachedArguments(VariableStack stack, Arguments ret)
			throws VariableNotFoundException {
		ArgUtil.getNamedReturn(stack).addAll(ret.getNamed().getAll());
		ArgUtil.getVariableReturn(stack).appendAll(ret.getVargs().getAll());
	}

	protected void notificationEvent(NotificationEvent e) throws ExecutionException {
		if (NotificationEventType.EXECUTION_COMPLETED.equals(e.getType())) {
			VariableStack stack = e.getStack();
			if (stack.currentFrame().isDefined(ARGUMENTS_THREAD)) {
				stack.currentFrame().deleteVar(ARGUMENTS_THREAD);
				closeArgs(stack);
			}
			else {
				super.notificationEvent(e);
			}
		}
		else {
			super.notificationEvent(e);
		}
	}

	protected void prepareInstanceArguments(VariableStack stack, Arguments fnargs)
			throws ExecutionException {
		final String[] arguments = getArguments();
		for (int i = 0; i < arguments.length; i++) {
			String name = arguments[i];
			Object value = fnargs.getNamed().getArgument(name);
			if (value != null) {
				stack.setVar(name, value);
			}
			else {
				stack.setVar(name, new FutureNamedArgument(name, fnargs.getNamed()));
			}
		}

		final String[] optargs = getOptargs();
		if (optargs != null) {
			for (int i = 0; i < optargs.length; i++) {
				String name = optargs[i];
				Object value = fnargs.getNamed().getArgument(name);
				if (value != null) {
					stack.setVar(name, value);
				}
				else {
					stack.setVar(name, new FutureNamedArgument(name, fnargs.getNamed()));
				}
			}
		}

		if (hasVargs()) {
			if (getKmode()) {
				stack.setVar("...", fnargs.getVargs());
			}
			else {
				stack.setVar("vargs", fnargs.getVargs());
			}
		}

		Iterator i = getChannels().iterator();
		while(i.hasNext()) {
			Arg.Channel channel = (Arg.Channel) i.next();
			stack.setVar(channel.getName(), fnargs.getChannels().get(channel));
		}

		if (this.hasNamed()) {
			Map named = new HashMap();
			i = fnargs.getNamed().getNames();
			while (i.hasNext()) {
				String name = (String) i.next();
				Object value = fnargs.getNamed().getArgument(name);
				if (value instanceof Future) {
					named.put(name, ((Future) value).getValue());
				}
				else {
					named.put(name, value);
				}
			}
			stack.setVar("named", named);
		}
	}

}