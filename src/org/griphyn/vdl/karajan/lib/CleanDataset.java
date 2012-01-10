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

import org.apache.log4j.Logger;
import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.griphyn.vdl.mapping.AbstractDataNode;
import org.griphyn.vdl.mapping.file.FileGarbageCollector;

public class CleanDataset extends VDLFunction {
	public static final Logger logger = Logger.getLogger(CleanDataset.class);
	
	public static final Arg.Optional OA_SHUTDOWN = new Arg.Optional("shutdown");
	
	static {
		setArguments(CleanDataset.class, new Arg[] { PA_VAR, OA_SHUTDOWN });
	}

	public Object function(VariableStack stack) throws ExecutionException {
	    if (OA_SHUTDOWN.isPresent(stack)) {
	        // signals that everything is done and the main program should wait for the
	        // garbage collector to finish everything
	        try {
                FileGarbageCollector.getDefault().waitFor();
            }
            catch (InterruptedException e) {
                // good time to quit now
            }
	    }
	    else {
    		AbstractDataNode var = (AbstractDataNode) PA_VAR.getValue(stack);
    		if (logger.isInfoEnabled()) {
    		    logger.info("Cleaning " + var);
    		}
    		var.clean();
	    }
	    return null;
	}
}
