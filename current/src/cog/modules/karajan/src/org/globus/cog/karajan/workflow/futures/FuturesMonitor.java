//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Mar 5, 2005
 */
package org.globus.cog.karajan.workflow.futures;

import java.util.Hashtable;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.stack.VariableNotFoundException;
import org.globus.cog.karajan.util.ThreadingContext;
import org.globus.cog.karajan.workflow.events.EventTargetPair;
import org.globus.cog.karajan.workflow.nodes.FlowNode;

public class FuturesMonitor extends Hashtable {
	private static final long serialVersionUID = 8320902838623426852L;

	private static final Logger logger = Logger.getLogger(FuturesMonitor.class);
	
	public static final FuturesMonitor monitor = new FuturesMonitor();
	public static boolean debug = false;
	
	public void remove(EventTargetPair etp) {
		try {
			FuturesMonitor.monitor.remove(new FlowNode.FNTP(etp.getTarget(),
					ThreadingContext.get(etp.getEvent().getStack())));
		}
		catch (VariableNotFoundException e) {
			logger.warn("No thread on the stack", e);
		}
	}
	
	public void add(EventTargetPair etp, Future f) {
		try {
			FuturesMonitor.monitor.put(new FlowNode.FNTP(etp.getTarget(),
					ThreadingContext.get(etp.getEvent().getStack())), f);
		}
		catch (VariableNotFoundException e) {
			logger.warn("No thread on the stack", e);
		}
	}
}
