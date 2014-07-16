// ----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Apr 7, 2005
 */
package org.globus.cog.karajan.compiled.nodes.grid;

import k.rt.ExecutionException;
import k.rt.Stack;

import org.globus.cog.abstraction.interfaces.FileOperationSpecification;
import org.globus.cog.karajan.analyzer.ArgRef;
import org.globus.cog.karajan.analyzer.Signature;
import org.globus.cog.karajan.util.BoundContact;

public class MakeDir extends AbstractFileOperation {
	private ArgRef<String> name;
    
    @Override
    protected Signature getSignature() {
        return new Signature(params("name", optional("host", BoundContact.LOCALHOST), 
        		optional("provider", "local")));
    }

	protected String getOperation(Stack stack) throws ExecutionException {
        return FileOperationSpecification.MKDIRS;
    }

    protected String[] getArguments(Stack stack) throws ExecutionException {
        return new String[] { name.getValue(stack) };
    }
}
