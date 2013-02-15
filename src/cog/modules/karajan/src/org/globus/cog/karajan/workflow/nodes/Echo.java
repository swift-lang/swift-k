// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.karajan.workflow.nodes;

import java.io.PrintStream;

import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.workflow.ExecutionException;

public class Echo extends Print {
	public static final Arg A_STREAM = new Arg.Optional("stream");

	static {
		setArguments(Echo.class, new Arg[] { A_MESSAGE, A_NL, A_STREAM, Arg.VARGS });
	}

	public Echo() {
		setAcceptsInlineText(true);
	}

	public void print(VariableStack stack, String message) throws ExecutionException {
		if (A_STREAM.isPresent(stack)) {
			PrintStream ps = (PrintStream) checkClass(A_STREAM.getValue(stack), PrintStream.class,
					"stream");
			ps.print(message);
		}
		else {
			echo(message, false);
		}
	}
}