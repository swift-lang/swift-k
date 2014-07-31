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

import k.rt.ExecutionException;
import k.rt.Stack;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.analyzer.ArgRef;
import org.globus.cog.karajan.analyzer.Signature;
import org.griphyn.vdl.mapping.DSHandle;
import org.griphyn.vdl.mapping.DataDependentException;
import org.griphyn.vdl.mapping.MappingDependentException;
import org.griphyn.vdl.mapping.Path;

public class SetFutureFault extends SwiftFunction {
	public static final Logger logger = Logger.getLogger(SetFutureFault.class);
	
	private ArgRef<DSHandle> var;
	private ArgRef<Object> path;
	private ArgRef<Exception> exception;
	private ArgRef<Boolean> mapping;

	@Override
    protected Signature getSignature() {
        return new Signature(params("var", optional("path", Path.EMPTY_PATH), 
            optional("exception", null), optional("mapping", false)));
    }

	@Override
	public Object function(Stack stack) {
		DSHandle var = this.var.getValue(stack);
		boolean mapping = this.mapping.getValue(stack);
		try {
			Path path = parsePath(this.path.getValue(stack));
			DSHandle leaf = var.getField(path);
			if (logger.isInfoEnabled()) {
				logger.info("Failing " + leaf + " (mapping=" + mapping + ")");
			}
			synchronized (leaf) {
				Exception e = this.exception.getValue(stack);
				if (mapping) {
					leaf.setValue(new MappingDependentException(leaf, e));
				}
				else {
					leaf.setValue(new DataDependentException(leaf, e));
				}
			}
		}
		catch (Exception e) {
			throw new ExecutionException(this, e);
		}
		return null;
	}
}
