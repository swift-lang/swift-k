package org.griphyn.vdl.karajan.lib;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.griphyn.vdl.mapping.DSHandle;
import org.griphyn.vdl.mapping.InvalidPathException;
import org.griphyn.vdl.mapping.Path;

import java.util.StringTokenizer;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Iterator;

public class PartialCloseDataset extends VDLFunction {
	public static final Logger logger = Logger.getLogger(CloseDataset.class);

	public static final Arg OA_STATEMENTID = new Arg.Optional("closeID", null);

	static {
		setArguments(PartialCloseDataset.class, new Arg[] { PA_VAR, OA_PATH, OA_STATEMENTID });
	}


	/** Map from DSHandles (as keys) to lists of what we have seen
	    already. TODO this may end up growing too much when a program
	    has lots of objects. consider alternative ways of doing this. */
	static Map pendingDatasets = new HashMap();


	// TODO path is not used!
	public Object function(VariableStack stack) throws ExecutionException {
		boolean hasUnseenToken = false;
		Path path = parsePath(OA_PATH.getValue(stack), stack);
		DSHandle var = (DSHandle) PA_VAR.getValue(stack);
		String statementID = (String) OA_STATEMENTID.getValue(stack);
		try {
			if (logger.isInfoEnabled()) {
				logger.info("Partially closing " + var + " for statement " + statementID);
			}
			var = var.getField(path);

			logger.info("var is "+var);
			logger.info("var hash is "+var.hashCode());

			if(var.isClosed()) {
				logger.info("variable already closed - skipping partial close processing");
				return null;
			}

			synchronized(pendingDatasets) {

				List c = (List) pendingDatasets.get(var);
				if(c==null) {
					c=new ArrayList();
					pendingDatasets.put(var,c);
				}

				c.add(statementID);
				logger.info("Adding token "+statementID+" with hash "+statementID.hashCode());

				String needToWaitFor = (String) var.getParam("waitfor");
				logger.info("need to wait for "+needToWaitFor);
				StringTokenizer stok = new StringTokenizer(needToWaitFor, " ");
				while(stok.hasMoreTokens()) {
					String s = stok.nextToken();
					// do we have this token in our list of already seen
					// statement IDs?
					if(! c.contains(s)) {
						// then we have a required element that we have not
						// seen yet, so...
						hasUnseenToken = true;
						logger.info("Container does not contain token "+s);
					} else {

						logger.info("Container does contain token "+s);
					}
				}
			}
			logger.info("hasUnseenToken = "+hasUnseenToken);
			if(!hasUnseenToken) {
				if(logger.isInfoEnabled()) {
					logger.info("All partial closes for " + var + " have happened. Closing fully.");
				}
				var.closeDeep();
				pendingDatasets.remove(var);
			}
		}
		catch (InvalidPathException e) {
			throw new ExecutionException(e);
		}
		return null;
	}
}
