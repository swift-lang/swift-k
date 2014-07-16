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


package org.griphyn.vdl.karajan.lib;

import k.rt.ExecutionException;
import k.rt.Stack;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.analyzer.ArgRef;
import org.globus.cog.karajan.analyzer.Signature;
import org.griphyn.vdl.karajan.PairSet;
import org.griphyn.vdl.mapping.DSHandle;
import org.griphyn.vdl.mapping.InvalidPathException;
import org.griphyn.vdl.mapping.Path;
import org.griphyn.vdl.mapping.nodes.ArrayHandle;

public class GetArrayIterator extends SwiftFunction {
	public static final Logger logger = Logger.getLogger(GetArrayIterator.class);

	private ArgRef<DSHandle> var;
	private ArgRef<Object> path;

	@Override
    protected Signature getSignature() {
        return new Signature(params("var", optional("path", Path.EMPTY_PATH)));
    }

	/**
	 * Takes a supplied variable and path, and returns an array iterator.
	 */
	@Override
	public Object function(Stack stack) {
		DSHandle var = this.var.getValue(stack);
		try {
			Path path = parsePath(this.path.getValue(stack));
			if (path.hasWildcards()) {
				throw new RuntimeException("Wildcards not supported");
			}
			else {
				var = var.getField(path);
				if (!var.getType().isArray()) {
				    throw new RuntimeException("Cannot get array iterator for non-array");
				}
				synchronized(var) {
					if (var.isClosed()) {
					    if (logger.isDebugEnabled()) {
					        logger.debug("Using closed iterator for " + var);
					    }
						return new PairSet(var.getArrayValue());
					}
					else {
					    if (logger.isDebugEnabled()) {
                            logger.debug("Using future iterator for " + var);
                        }
						return ((ArrayHandle) var).entryList();
					}
				}
			}
		}
		catch (InvalidPathException e) {
			throw new ExecutionException(this, e);
		}
	}
}
