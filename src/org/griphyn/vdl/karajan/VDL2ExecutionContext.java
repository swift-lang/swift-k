/*
 * Created on Dec 23, 2006
 */
package org.griphyn.vdl.karajan;

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
        if (logger.isInfoEnabled()) {
            logger.info(stack);
        }
        super.start(stack);
    }
}
