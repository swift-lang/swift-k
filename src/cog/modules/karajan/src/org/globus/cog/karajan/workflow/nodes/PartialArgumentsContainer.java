// ----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Apr 19, 2005
 */
package org.globus.cog.karajan.workflow.nodes;

import java.util.HashSet;
import java.util.Set;

import org.globus.cog.karajan.arguments.ArgUtil;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.globus.cog.karajan.workflow.futures.Future;
import org.globus.cog.karajan.workflow.futures.FutureFault;
import org.globus.cog.karajan.workflow.futures.FutureListener;
import org.globus.cog.karajan.workflow.nodes.functions.Argument;

public class PartialArgumentsContainer extends AbstractSequentialWithArguments {
	private int argCount;

	protected void initializeStatic() {
		super.initializeStatic();
		argCount = getNonpropargs().size();
		Set optional = getOptionalArgs();
		if (optional == null) {
			return;
		}
		optional = new HashSet(optional);
		optional.removeAll(getStaticArguments().keySet());
		for (int i = 0; i < optional.size() + argCount; i++) {
			if (i >= elementCount()) {
				break;
			}
			// Horrible
			FlowElement fe = getElement(i);
			if ("kernel:named".equals(fe.getElementType())
					&& optional.contains(Argument.A_NAME.getStatic(fe))) {
				argCount++;
				optional.remove(Argument.A_NAME.getStatic(fe));
			}
		}
	}

	protected void executeChildren(VariableStack stack) throws ExecutionException {
		if (argCount == 0) {
			if (getQuotedArgs()) {
				stack.currentFrame().deleteVar(QUOTED);
			}
			try {
                partialArgumentsEvaluated(stack);
            }
            catch (FutureFault e) {
                e.getFuture().addModificationAction(new PartialResume(), stack);
            }
		}
		else {
			super.executeChildren(stack);
		}
	}

	public void completed(VariableStack stack) throws ExecutionException {
		if (!getArgsDone(stack)) {
			int index = ArgUtil.getNamedArguments(stack).size() - this.getStaticArguments().size();
			if (index < argCount) {
				super.completed(stack);
			}
			else if (index >= argCount) {
				if (getQuotedArgs()) {
					stack.currentFrame().deleteVar(QUOTED);
				}
				processArguments(stack);
				try {
					partialArgumentsEvaluated(stack);
				}
				catch (FutureFault e) {
					e.getFuture().addModificationAction(new PartialResume(), stack);
				}
			}
		}
		else {
			nonArgChildCompleted(stack);
		}
	}
	
	public class PartialResume implements FutureListener {
		public void futureModified(Future f, VariableStack stack) {
			try {
				partialArgumentsEvaluated(stack);
			}
			catch (FutureFault e) {
				e.getFuture().addModificationAction(new PartialResume(), stack);
			}
			catch (ExecutionException e) {
				failImmediately(stack, e);
			}
		}	
	}

	protected void nonArgChildCompleted(VariableStack stack) throws ExecutionException {
		super.completed(stack);
	}

	protected void startRest(VariableStack stack) throws ExecutionException {
		stack.setCaller(this);
		setIndex(stack, argCount);
		startNext(stack);
	}

	protected void partialArgumentsEvaluated(VariableStack stack) throws ExecutionException {
		ArgUtil.removeVariableArguments(stack);
		ArgUtil.removeNamedArguments(stack);
		setArgsDone(stack);
	}

	public int getArgCount() {
		return argCount;
	}

	protected final void setArgsDone(VariableStack stack) {
		stack.getRegs().setBA(true);
	}

	protected final boolean getArgsDone(VariableStack stack) {
		return stack.getRegs().getBA();
	}
}
