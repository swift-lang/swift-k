/*
 * Copyright 2012 University of Chicago
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


/*
 * Created on Dec 26, 2006
 */
package org.griphyn.vdl.karajan.lib;

import java.util.Collection;

import k.rt.ExecutionException;
import k.rt.Future;
import k.rt.Stack;

import org.globus.cog.karajan.analyzer.ArgRef;
import org.globus.cog.karajan.analyzer.Signature;
import org.globus.cog.karajan.futures.FutureNotYetAvailable;
import org.griphyn.vdl.mapping.DSHandle;
import org.griphyn.vdl.mapping.HandleOpenException;
import org.griphyn.vdl.mapping.InvalidPathException;
import org.griphyn.vdl.mapping.Path;

public class FringePaths extends SwiftFunction {
    private ArgRef<DSHandle> var;
    private ArgRef<Object> path; 
    
    @Override
    protected Signature getSignature() {
        return new Signature(params("var", optional("path", Path.EMPTY_PATH)));
    }

    @Override
	public Object function(Stack stack) {
		DSHandle var = this.var.getValue(stack);
		DSHandle root = var.getRoot();
		try {
			var = var.getField(parsePath(path.getValue(stack)));
			Collection<Path> c;
			synchronized(root) {
				c = var.getFringePaths();
			}
			return c;
		}
		catch (InvalidPathException e) {
			throw new ExecutionException(this, e);
		}
		catch (HandleOpenException e) {
			throw new FutureNotYetAvailable((Future) e.getSource());
		}
	}
}
