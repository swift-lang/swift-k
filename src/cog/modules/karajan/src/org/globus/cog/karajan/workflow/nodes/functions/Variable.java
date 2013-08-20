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

import org.apache.log4j.Logger;
import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.arguments.ArgUtil;
import org.globus.cog.karajan.arguments.VariableArguments;
import org.globus.cog.karajan.stack.StackFrame;
import org.globus.cog.karajan.stack.VariableNotFoundException;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.util.Identifier;
import org.globus.cog.karajan.util.TypeUtil;
import org.globus.cog.karajan.workflow.ExecutionException;

public class Variable extends AbstractFunction {

	final static Logger logger = Logger.getLogger(Variable.class);

	public static final Arg A_NAME = new Arg.Positional("name");

	public static final int UNINITIALIZED = -999;

	static {
		setArguments(Variable.class, new Arg[] { A_NAME });
	}

	private int frame = UNINITIALIZED;
	private String name;

	public Variable() {
		this.setAcceptsInlineText(true);
	}

	public Object function(VariableStack stack) throws ExecutionException {
		synchronized (this) {
			if (frame == UNINITIALIZED || frame == VariableStack.NO_FRAME) {
				if (name == null) {
					name = TypeUtil.toString(A_NAME.getStatic(this)).toLowerCase();
					if (getStaticArguments().size() == 1) {
						java.util.Map<String, Object> m = Collections.emptyMap();
						setStaticArguments(m);
					}
				}
			
				if (stack.parentFrame().isDefined("#quoted")) {
					Object value = new Identifier(name);
					setSimple(value);
					return value;
				}
				frame = stack.getVarFrameFromTop(name);
			}
		}
				
		switch (frame) {
			case VariableStack.NO_FRAME:
				throw new VariableNotFoundException(name);
			case VariableStack.FIRST_FRAME:
				Object value = stack.firstFrame().getVar(name);
				setSimple(value);
				return value;
			case VariableStack.DYNAMIC_FRAME:
				return stack.getVar(name);
			default:
				StackFrame sf = stack.getFrameFromTop(frame);
				value = sf.getVar(name);
				/*
				 * Must still check if defined since in the case of optional
				 * arguments, an initial invocation of this function may find
				 * a valid value at the relevant frame, while a subsequent
				 * invocation may not.
				 * 
				 * The converse (initial invocation getting a NO_FRAME) is handled
				 * since the frame is always looked up in that case (see above if)
				 */
				if (value == null) {
					if (sf.isDefined(name)) {
						return null;
					}
					else {
						throw new VariableNotFoundException(name);
					}
				}
				else {
					return value;
				}
		}
	}
	
	/*
	 * Override to allow returning java null values. Even though these are not used much,
	 * it is still possible to manipulate them using the java library.
	 */
	protected void ret(VariableStack stack, final Object value) throws ExecutionException {
		final VariableArguments vret = ArgUtil.getVariableReturn(stack);
		vret.append(value);
	}
}
