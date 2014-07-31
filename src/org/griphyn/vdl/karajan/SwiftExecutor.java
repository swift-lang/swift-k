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

import k.rt.Context;
import k.rt.ExecutionException;
import k.rt.Executor;
import k.rt.Stack;
import k.thr.Scheduler.RootThread;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.compiled.nodes.Main;
import org.griphyn.vdl.karajan.functions.ProcessBulkErrors;

public class SwiftExecutor extends Executor {
	public static final Logger logger = Logger.getLogger(SwiftExecutor.class);
	
	public SwiftExecutor(Main root) {
		super(root);
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
			System.err.print("Execution failed:\n\t");
			String translation = VDL2ErrorTranslator.getDefault().translate(msg);
			if (translation != null) {
				System.err.print(translation);
			}
			else {
				System.err.print(ProcessBulkErrors.getMessageChain(e));
			}
			System.err.print("\n");
		}
		else {
			// lazy errors are on and they have already been printed
		}
	}
	
	public void start(Context context) {
		if (logger.isDebugEnabled()) {
            logger.debug(context);
        }
        logger.info("swift.home = " + 
                    System.getProperty("swift.home"));
        start(new RootThread(getRoot(), new Stack()), context);
    }
}
