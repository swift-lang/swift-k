// ----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Apr 7, 2005
 */
package org.globus.cog.karajan.compiled.nodes.grid;

import k.rt.Stack;

import org.globus.cog.abstraction.interfaces.FileOperationSpecification;
import org.globus.cog.karajan.analyzer.ArgRef;
import org.globus.cog.karajan.analyzer.Signature;
import org.globus.cog.karajan.util.BoundContact;

public class Rename extends AbstractFileOperation {
	private ArgRef<String> from;
	private ArgRef<String> to;
	private ArgRef<Object> host;
	private ArgRef<String> provider;

	@Override
	protected Signature getSignature() {
		return new Signature(params("from", "to", optional("host", BoundContact.LOCALHOST),
				optional("provider", "local")));
	}

	@Override
	protected String getOperation(Stack stack) {
		return FileOperationSpecification.RENAME;
	}

	@Override
	protected String[] getArguments(Stack stack) {
		return new String[] { from.getValue(stack), to.getValue(stack) };
	}

}
