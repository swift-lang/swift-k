// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/*
 * Created on Jul 2, 2004
 */
package org.globus.cog.karajan.workflow.nodes.functions;

import java.util.Collections;
import java.util.Stack;

import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.stack.VariableNotFoundException;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.util.Identifier;
import org.globus.cog.karajan.util.TypeUtil;
import org.globus.cog.karajan.workflow.ExecutionException;

public class Variable extends AbstractFunction {

	public static final Arg A_NAME = new Arg.Positional("name");
	
	static {
		setArguments(Variable.class, new Arg[] { A_NAME });
	}

	private int frame = -1;
	private String name;

	public Variable() {
		this.setAcceptsInlineText(true);
	}
	
	public Object function(VariableStack stack) throws ExecutionException {
		synchronized (this) {
			if (name == null) {
				name = TypeUtil.toString(A_NAME.getStatic(this)).toLowerCase();
				if (getStaticArguments().size() == 1) {
					java.util.Map<String, Object> m = Collections.emptyMap();
					setStaticArguments(m);
				}
			}
		}
		
		if (frame == -1 || frame == VariableStack.NO_FRAME) {
			if (stack.parentFrame().isDefined("#quoted")) {
				Object value = new Identifier(name);
				setSimple(value);
				return value;
			}
			else {
				frame = stack.getVarFrameFromTop(name);
				if (frame == VariableStack.NO_FRAME) {
					throw new VariableNotFoundException(name);
				}
				else if (frame == VariableStack.FIRST_FRAME) {
					Object value = stack.firstFrame().getVar(name);
					setSimple(value);
					return value;
				}
				else {
					return stack.getFrameFromTop(frame).getVar(name);
				}
			}
		}
		else {
			return stack.getFrameFromTop(frame).getVar(name);
		}
	}
}