// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/*
 * Created on Jul 7, 2003
 */
package org.globus.cog.karajan.workflow.nodes;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.util.Identifier;
import org.globus.cog.karajan.util.KarajanIterator;
import org.globus.cog.karajan.util.TypeUtil;
import org.globus.cog.karajan.workflow.ExecutionException;

public abstract class AbstractIterator extends PartialArgumentsContainer {
	private static final Logger logger = Logger.getLogger(AbstractIterator.class);
	
	public static final Arg A_NAME = new Arg.Positional("name", 0);
	public static final Arg A_IN = new Arg.Positional("in", 1);

	public static final String ITERATOR = "##iter:vals";
	public static final String VAR = "##iter:var";

	public static final int ITERATE = 0;

	public AbstractIterator() {
		setQuotedArgs(true);
	}

	protected final void setRunning(VariableStack stack, int running) {
		stack.getRegs().setIB(running);
	}

	protected final synchronized int preDecRunning(VariableStack stack) {
		return stack.getRegs().preDecIB();
	}

	protected final synchronized int preIncRunning(VariableStack stack) {
		return stack.getRegs().preIncIB();
	}

	protected void partialArgumentsEvaluated(VariableStack stack) throws ExecutionException {
		Identifier var = TypeUtil.toIdentifier(A_NAME.getValue(stack));
		Object iter = A_IN.getValue(stack);
		KarajanIterator i;
		if (iter instanceof Identifier) {
			i = TypeUtil.toIterator(((Identifier) iter).getValue(stack));
		}
		else {
			i = TypeUtil.toIterator(iter);
		}
		super.partialArgumentsEvaluated(stack);
		iterate(stack, var, i);
	}

	public void iterate(VariableStack stack, KarajanIterator i) throws ExecutionException {
		Identifier var = (Identifier) stack.currentFrame().getVar(VAR);
		iterate(stack, var, i);
	}

	public abstract void iterate(VariableStack stack, Identifier var, KarajanIterator i)
			throws ExecutionException;
}