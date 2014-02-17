// ----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Apr 7, 2005
 */
package org.globus.cog.karajan.compiled.nodes.grid;

import java.io.File;

import k.rt.ExecutionException;
import k.rt.Stack;

import org.globus.cog.abstraction.impl.common.StatusEvent;
import org.globus.cog.abstraction.interfaces.FileOperationSpecification;
import org.globus.cog.abstraction.interfaces.Task;
import org.globus.cog.karajan.analyzer.ArgRef;
import org.globus.cog.karajan.analyzer.ChannelRef;
import org.globus.cog.karajan.analyzer.Signature;
import org.globus.cog.karajan.util.BoundContact;

public class FileExists extends AbstractFileOperation {
	
	private ArgRef<String> name;
	private ChannelRef<Object> cr_vargs;
	
	@Override
	protected Signature getSignature() {
		return new Signature(params("name", optional("host", BoundContact.LOCALHOST), 
				optional("provider", "local")), returns(channel("...", 1)));
	}

	@Override
	protected String getOperation(Stack stack) {
		return FileOperationSpecification.EXISTS;
	}

	@Override
	protected String[] getArguments(Stack stack) {
		return new String[] { name.getValue(stack) };
	}

	@Override
	protected void taskCompleted(StatusEvent e, Stack stack) {
		try {
			Task task = (Task) e.getSource();
			Boolean exists = (Boolean) task.getAttribute("output");
			cr_vargs.append(stack, exists);
		}
		catch (Exception ex) {
			throw new ExecutionException("Exception caugh while retrieving output", ex);
		}
	}

	@Override
	protected boolean runDirectly(Stack stack, String op, String[] arguments, String cwd) {
		boolean exists = new File(cwd, arguments[0]).exists();
		cr_vargs.append(stack, exists);
		return true;
	}
}
