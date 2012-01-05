// ----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Oct 4, 2004
 */
package org.globus.cog.karajan.workflow.nodes;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.globus.cog.karajan.workflow.nodes.functions.Argument;
import org.globus.cog.karajan.workflow.nodes.functions.Logic;
import org.globus.cog.karajan.workflow.nodes.functions.Map;
import org.globus.cog.karajan.workflow.nodes.functions.Misc;

public abstract class FlowContainer extends FlowNode {
	private static final Logger logger = Logger.getLogger(FlowContainer.class);

	private boolean optimize;

	private static Set safeToOptimize;

	static {
		safeToOptimize = new HashSet();
		safeToOptimize.add(Sequential.class);
		safeToOptimize.add(Map.class);
		safeToOptimize.add(org.globus.cog.karajan.workflow.nodes.functions.List.class);
		safeToOptimize.add(Logic.class);
		safeToOptimize.add(ConditionNode.class);
		safeToOptimize.add(SetVar.class);
		safeToOptimize.add(Argument.class);
		safeToOptimize.add(Misc.class);
		safeToOptimize.add(Echo.class);
	}

	protected void initializeStatic() {
		super.initializeStatic();
		if (safeToOptimize.contains(this.getClass())) {
			optimize = true;
		}
	}
	
	protected boolean isOptimizable() {
		return optimize;
	}
	
	protected void pre(VariableStack stack) throws ExecutionException {
	}

	protected void post(VariableStack stack) throws ExecutionException {
		complete(stack);
	}

	public final void execute(VariableStack stack) throws ExecutionException {
		pre(stack);
		executeChildren(stack);
	}

	protected abstract void executeChildren(VariableStack stack) throws ExecutionException;

	
	public void setOptimize(boolean optimize) {
		this.optimize = optimize;
	}

	protected final void setChildFailed(VariableStack stack, boolean value) {
		stack.getRegs().setBB(value);
	}

	protected final boolean getChildFailed(VariableStack stack) {
		return stack.getRegs().getBB();
	}
}