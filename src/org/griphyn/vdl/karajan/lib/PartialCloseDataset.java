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


// this is memory leak. better dshandle impl to hold this data, perhaps?

	/** Map from DSHandles (as keys) to lists of what we have seen
	    already */
	static Map cache = new HashMap();


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

			logger.info("dump of what's in cache: ");
			synchronized(cache) {
// TODO this locks the whole cache - probably don't actually need to 
// do this (nor do we necessarily even need to iterate over the cache
// to give that debugging info - perhaps the debugging info isn't
// actually necessary)
				Iterator cit = cache.keySet().iterator();
				while(cit.hasNext()) {
					logger.info(cit.next());
				}

				List c = (List) cache.get(var);
				if(c==null) {
					c=new ArrayList();
					cache.put(var,c);
				}

				c.add(statementID);
			logger.info("Adding token "+statementID);

			// TODO so remove the statement ID from the list of statements we
			// are waiting for. hmm. no way to write parameters in there.
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
			}
		}
		catch (InvalidPathException e) {
			throw new ExecutionException(e);
		}
		return null;
	}
}
