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

import k.rt.Stack;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.analyzer.ArgRef;
import org.globus.cog.karajan.analyzer.Signature;
import org.griphyn.vdl.mapping.file.FileGarbageCollector;
import org.griphyn.vdl.mapping.nodes.AbstractDataNode;
import org.griphyn.vdl.mapping.nodes.ReadRefWrapper;

public class CleanDataset extends SwiftFunction {
    public static final Logger logger = Logger.getLogger(CleanDataset.class);
    
	private ArgRef<Object> var;
	private ArgRef<Boolean> shutdown;

	@Override
    protected Signature getSignature() {
        return new Signature(params("var", optional("shutdown", Boolean.FALSE)));
    }

    public Object function(Stack stack) {
    	boolean shutdown = this.shutdown.getValue(stack);
	    if (shutdown) {
	        // signals that everything is done and the main program should wait for the
	        // garbage collector to finish everything
	        try {
	            FileGarbageCollector.getDefault().clean();
                FileGarbageCollector.getDefault().waitFor();
            }
            catch (InterruptedException e) {
                // good time to quit now
            }
	    }
	    else {
	        // can probably do this check at compile-time
    		Object o = this.var.getValue(stack);
    		AbstractDataNode var;
    		if (o instanceof ReadRefWrapper) {
    		    var = ((ReadRefWrapper) o).getHandle();
    		    if (var == null) {
    		        // already cleaned
    		        return null;
    		    }
    		}
    		else {
    		    var = (AbstractDataNode) o;
    		}
    		if (var == null) {
    			return null;
    		}
    		if (!var.isCleaned()) {
        		if (logger.isDebugEnabled()) {
        		    logger.debug("Cleaning " + var);
        		}
        		var.clean();
    		}
	    }
	    return null;
	}
}
