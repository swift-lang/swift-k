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
 * Created on Dec 23, 2006
 */
package org.griphyn.vdl.karajan;

import java.util.List;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.workflow.ElementTree;
import org.globus.cog.karajan.workflow.ExecutionContext;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.griphyn.vdl.karajan.functions.ProcessBulkErrors;

public class VDL2ExecutionContext extends ExecutionContext {
	public static final Logger logger = Logger.getLogger(VDL2ExecutionContext.class);
	
	public static final String RUN_ID = "vdl:runid";
	public static final String SCRIPT_NAME = "vdl:scriptname";

	private String runID;
	private final String scriptName;

	public VDL2ExecutionContext(ElementTree tree, String scriptName) {
		super(tree);
		this.scriptName = scriptName;
	}

	protected void printFailure(ExecutionException e) {
		if (logger.isDebugEnabled()) {
			logger.debug(e.getMessage(), e);
		}
		String msg = e.getMessage();
		if (!"Execution completed with errors".equals(msg)) {
			if (msg == null) {
				msg = getMeaningfulMessage(e);
			}
			getStderr().append("Execution failed:\n\t");
			String translation = VDL2ErrorTranslator.getDefault().translate(msg);
			if (translation != null) {
				getStderr().append(translation);
			}
			else {
				getStderr().append(ProcessBulkErrors.getMessageChain(e));
			}
			if (e.getStack() != null) {
			    List<String> l = Monitor.getSwiftTrace(e.getStack());
			    for (String s : l) {
			        getStderr().append("\n\t");
			        getStderr().append(s);
			    }
			}
			getStderr().append("\n");
		}
		else {
			// lazy errors are on and they have already been printed
		}
	}

	protected void setGlobals(VariableStack stack) {
		super.setGlobals(stack);
		stack.setGlobal(RUN_ID, runID);
		stack.setGlobal(SCRIPT_NAME, scriptName);
	}

	public String getRunID() {
		return runID;
	}

	public void setRunID(String runID) {
		this.runID = runID;
	}

    public void start(VariableStack stack) {
        if (logger.isDebugEnabled()) {
            logger.debug(stack);
        }
        logger.info("swift.home = " + 
                    System.getProperty("swift.home"));
        super.start(stack);
    }
}
