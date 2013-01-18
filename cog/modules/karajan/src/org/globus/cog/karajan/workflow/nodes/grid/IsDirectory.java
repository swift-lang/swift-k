// ----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Apr 7, 2005
 */
package org.globus.cog.karajan.workflow.nodes.grid;

import org.globus.cog.abstraction.impl.common.StatusEvent;
import org.globus.cog.abstraction.interfaces.FileOperationSpecification;
import org.globus.cog.abstraction.interfaces.Task;
import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.arguments.ArgUtil;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.util.TypeUtil;
import org.globus.cog.karajan.workflow.ExecutionException;

public class IsDirectory extends AbstractFileOperation {
	public static final Arg A_NAME = new Arg.Positional("name");
	
	static {
		setArguments(IsDirectory.class, new Arg[] { A_NAME, OA_HOST, OA_PROVIDER });
	}

	protected String getOperation(VariableStack stack) throws ExecutionException {
		return FileOperationSpecification.ISDIRECTORY;
	}

	protected String[] getArguments(VariableStack stack) throws ExecutionException {
		return new String[] { TypeUtil.toString(A_NAME.getValue(stack)) };
	}

	protected void taskCompleted(StatusEvent e, VariableStack stack) throws ExecutionException {
		try {
			Task task = (Task) e.getSource();
			Boolean exists = (Boolean) task.getAttribute("output");
			ArgUtil.getVariableReturn(stack).append(exists);
		}
		catch (Exception ex) {
			throw new ExecutionException("Exception caugh while retrieving output", ex);
		}
		super.taskCompleted(e, stack);
	}
}
