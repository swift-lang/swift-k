// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.karajan.workflow.nodes;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.util.TypeUtil;
import org.globus.cog.karajan.workflow.ExecutionException;

public class Print extends SequentialWithArguments {
	private static final Logger logger = Logger.getLogger(Print.class);

	public static final Arg A_MESSAGE = new Arg.Positional("message", 0);
	public static final Arg A_NL = new Arg.Optional("nl", Boolean.TRUE);

	static {
		setArguments(Print.class, new Arg[] { A_MESSAGE, A_NL, Arg.VARGS });
	}

	public Print() {
		setAcceptsInlineText(true);
	}

	public void post(VariableStack stack) throws ExecutionException {
		String message;
		if (A_MESSAGE.isPresent(stack)) {
			Object[] vargs = Arg.VARGS.asArray(stack);
			message = TypeUtil.toString(A_MESSAGE.getValue(stack));
			if (vargs != null && vargs.length > 0) {
				StringBuffer sb = new StringBuffer();
				sb.append(message);
				for (int i = 0; i < vargs.length; i++) {
					sb.append(TypeUtil.toString(vargs[i]));
				}
				message = sb.toString();
			}
		}
		else {
			message = "";
		}
		if (TypeUtil.toBoolean(A_NL.getValue(stack))) {
			message += '\n';
		}
		print(stack, message);
		super.post(stack);
	}

	protected void print(VariableStack stack, String message) throws ExecutionException {
		STDOUT.ret(stack, message);
	}
}