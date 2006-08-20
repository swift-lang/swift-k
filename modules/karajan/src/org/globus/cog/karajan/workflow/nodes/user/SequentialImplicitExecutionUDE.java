// ----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Mar 21, 2005
 */
package org.globus.cog.karajan.workflow.nodes.user;

import java.util.Iterator;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.arguments.ArgUtil;
import org.globus.cog.karajan.arguments.Arguments;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.util.DefUtil;
import org.globus.cog.karajan.util.DefinitionEnvironment;
import org.globus.cog.karajan.workflow.ExecutionException;

public class SequentialImplicitExecutionUDE extends UserDefinedElement {
	private static final Logger logger = Logger.getLogger(SequentialImplicitExecutionUDE.class);

	public static final String WRAPPER = "#wrapper";

	static {
		setArguments(SequentialImplicitExecutionUDE.class, new Arg[] { A_NAME, A_ARGUMENTS, A_VARGS,
				A_NAMED, A_CHANNELS, A_OPTARGS });
	}

	public void startInstance(VariableStack stack, UDEWrapper wrapper, DefinitionEnvironment env)
			throws ExecutionException {
		stack.setVar(CALLER, this);
		stack.setVar(ARGUMENTS_THREAD, env);
		startArguments(stack, wrapper);
	}

	protected void startArguments(VariableStack stack, UDEWrapper wrapper)
			throws ExecutionException {
		wrapper.setNestedArgs(hasNestedArgs());
		wrapper.setHasVargs(hasVargs());
		wrapper.initializeArgs(stack);
		ArgUtil.createChannels(stack, getChannels());
		wrapper.executeWrapper(stack);
	}

	protected void startBody(VariableStack stack, DefinitionEnvironment env) throws ExecutionException {
		Arguments args = getUDEArguments(stack);
		ArgUtil.removeNamedArguments(stack);
		ArgUtil.removeVariableArguments(stack);
		ArgUtil.removeChannels(stack, getChannels());
		stack.setVar(DefUtil.ENV, env);
		startBody(stack, args);
	}

	protected void setArguments(VariableStack stack) {

	}

	protected Arguments getUDEArguments(VariableStack stack) throws ExecutionException {
		Arguments fnargs = new Arguments();

		fnargs.setNamed(ArgUtil.getNamedArguments(stack));
		if (hasVargs()) {
			fnargs.setVargs(ArgUtil.getVariableArguments(stack));
		}

		Iterator i = getChannels().iterator();
		while (i.hasNext()) {
			Arg.Channel channel = (Arg.Channel) i.next();
			fnargs.getChannels().put(channel, ArgUtil.getChannelArguments(stack, channel));
		}

		return fnargs;
	}

	protected void childCompleted(VariableStack stack) throws ExecutionException {
		if (stack.currentFrame().isDefined(ARGUMENTS_THREAD)) {
			DefinitionEnvironment env = (DefinitionEnvironment) stack.currentFrame().getVarAndDelete(
					ARGUMENTS_THREAD);
			this.startBody(stack, env);
		}
		else {
			super.childCompleted(stack);
		}
	}
}