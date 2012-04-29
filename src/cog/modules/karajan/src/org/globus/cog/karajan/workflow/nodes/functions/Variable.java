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
			if (name == null) {
				name = TypeUtil.toString(A_NAME.getStatic(this)).toLowerCase();
				if (getStaticArguments().size() == 1) {
					java.util.Map<String, Object> m = Collections.emptyMap();
					setStaticArguments(m);
				}
			}
		}

		switch (frame) {
			case UNINITIALIZED:
			case VariableStack.NO_FRAME:
				if (stack.parentFrame().isDefined("#quoted")) {
					Object value = new Identifier(name);
					setSimple(value);
					return value;
				}
				else {
					frame = stack.getVarFrameFromTop(name);
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
					        return stack.getFrameFromTop(frame).getVar(name);
					}
				}
			case VariableStack.FIRST_FRAME:
			    return stack.firstFrame().getVar(name);
			case VariableStack.DYNAMIC_FRAME:
			    return stack.getVar(name);
			default:
			    return stack.getFrameFromTop(frame).getVar(name);
		}
	}
}
