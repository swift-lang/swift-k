// ----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Mar 21, 2005
 */
package org.globus.cog.karajan.workflow.nodes.user;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.arguments.ArgUtil;
import org.globus.cog.karajan.arguments.Arguments;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.util.DefinitionEnvironment;
import org.globus.cog.karajan.workflow.ExecutionException;

public class ExplicitExecutionUDE extends UserDefinedElement {
	public static final Logger logger = Logger.getLogger(ExplicitExecutionUDE.class);

	public static final Arg A_NAME = new Arg.Positional("name");
	
	static {
		setArguments(ExplicitExecutionUDE.class, new Arg[] { A_NAME });
	}

	protected void initializeStatic() {
		setProperty("_kmode", false);
	}

	public void startInstance(VariableStack stack, UDEWrapper wrapper, DefinitionEnvironment env)
			throws ExecutionException {
		stack.setVar("...", wrapper.elements());
		startBody(stack);
	}

	protected void startBody(VariableStack stack) throws ExecutionException {
		Arguments args = getUDEArguments(stack);
		ArgUtil.removeNamedArguments(stack);
		if (getKmode()) {
			setIndex(stack, getSkip());
		}
		else {
			setIndex(stack, 0);
		}
		stack.setVar(CALLER, this);

		startNext(stack);
	}

	protected void setArguments(VariableStack stack) {

	}

	protected Arguments getUDEArguments(VariableStack stack) throws ExecutionException {
		Arguments fnargs = new Arguments();
		fnargs.setNamed(ArgUtil.getNamedArguments(stack));
		return fnargs;
	}

	protected boolean checkFirstArg(VariableStack stack) throws ExecutionException {
		ArgUtil.getVariableReturn(stack).append(this);
		complete(stack);
		return true;
	}

}