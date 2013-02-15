// ----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Apr 7, 2005
 */
package org.globus.cog.karajan.workflow.nodes.grid;

import org.globus.cog.abstraction.interfaces.FileOperationSpecification;
import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.util.TypeUtil;
import org.globus.cog.karajan.workflow.ExecutionException;

public class Rename extends AbstractFileOperation {
    public static final Arg A_FROM = new Arg.Positional("from");
    public static final Arg A_TO = new Arg.Positional("to");
    
	static {
		setArguments(Rename.class, new Arg[] { A_FROM, A_TO, OA_HOST, OA_PROVIDER });
	}

	protected String getOperation(VariableStack stack) throws ExecutionException {
		return FileOperationSpecification.RENAME;
	}

	protected String[] getArguments(VariableStack stack) throws ExecutionException {
		return new String[] { TypeUtil.toString(A_FROM.getValue(stack)),
				TypeUtil.toString(A_TO.getValue(stack)) };
	}

}
