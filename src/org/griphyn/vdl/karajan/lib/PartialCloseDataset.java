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

import org.apache.log4j.Logger;
import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.griphyn.vdl.mapping.DSHandle;

import java.util.StringTokenizer;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

public class PartialCloseDataset extends VDLFunction {
	public static final Logger logger = Logger.getLogger(CloseDataset.class);

	public static final Arg OA_CLOSE_ID = new Arg.Optional("closeID", null);

	static {
		setArguments(PartialCloseDataset.class, new Arg[] { PA_VAR, OA_CLOSE_ID });
	}


	/** Map from DSHandles (as keys) to lists of what we have seen
	    already. TODO this may end up growing too much when a program
	    has lots of objects. Consider alternative ways of doing this. */
	static Map<DSHandle,List<String>> pendingDatasets = 
	    new HashMap<DSHandle,List<String>>();

	public Object function(VariableStack stack) throws ExecutionException {
		boolean hasUnseenToken = false;
		DSHandle var = (DSHandle) PA_VAR.getValue(stack);
		String closeID = (String) OA_CLOSE_ID.getValue(stack);
		if (logger.isDebugEnabled()) {
			logger.debug("Partially closing " + var +
                                     " hash: " + var.hashCode() +
                                     " for statement " + closeID);
		}

		if(var.isClosed()) {
			logger.debug("variable already closed - skipping partial close processing");
			return null;
		}

		synchronized(pendingDatasets) {

			List<String> c = pendingDatasets.get(var);
			if (c == null) {
				c = new ArrayList<String>();
				pendingDatasets.put(var, c);
			}

			c.add(closeID);
			if (logger.isDebugEnabled()) {
			    logger.debug("Adding token "+closeID+" with hash "+closeID.hashCode());
			}

			String needToWaitFor = var.getParam("waitfor");
			logger.debug("need to wait for " + needToWaitFor);
			StringTokenizer stok = new StringTokenizer(needToWaitFor, " ");
			while(stok.hasMoreTokens()) {
				String s = stok.nextToken();
				// do we have this token in our list of already seen
				// statement IDs?
				if(! c.contains(s)) {
					// then we have a required element that we have not
					// seen yet, so...
					hasUnseenToken = true;
					if (logger.isDebugEnabled()) {
					    logger.debug("Container does not contain token " + s);
					}
				} else {
				    if (logger.isDebugEnabled()) {
				        logger.debug("Container does contain token " + s);
				    }
				}
			}
		}
		if (logger.isDebugEnabled()) {
		    logger.debug("hasUnseenToken = "+hasUnseenToken);
		}
		if(!hasUnseenToken) {
			if(logger.isInfoEnabled()) {
				logger.info("All partial closes for " + var + 
				             " have happened. Closing fully.");
			}
			var.closeDeep();
			pendingDatasets.remove(var);
		}
		return null;
	}
}
