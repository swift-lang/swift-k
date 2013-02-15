// ----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Feb 15, 2005
 */
package org.globus.cog.karajan.workflow.futures;

import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.workflow.KarajanRuntimeException;


public class ForwardArgumentFuture implements Future, FutureListener {
	private static final Logger logger = Logger.getLogger(ForwardArgumentFuture.class);
	
	private final int index;
	private final FutureVariableArguments vargs;
	private boolean closed;
	private List<ListenerStackPair> actions;
	private FutureEvaluationException exception;

	public ForwardArgumentFuture(FutureVariableArguments vargs, int index) {
		this.vargs = vargs;
		this.index = index;
		vargs.addModificationAction(this, null);
	}

	public void close() {
		closed = true;
		actions();
	}

	public boolean isClosed() {
		return closed;
	}

	public Object getValue() {
		if (exception != null) {
			throw exception;
		}

		if (index <= vargs.available()) {
			return vargs.get(index);
		}
		else {
			if (vargs.isClosed()) {
				throw new KarajanRuntimeException("Variable not found");
			}
			throw new FutureNotYetAvailable(this);
		}
	}

	public boolean available() {
		return index <= vargs.available();
	}

	public synchronized void addModificationAction(FutureListener target, VariableStack event) {
		if (actions == null) {
			actions = new LinkedList<ListenerStackPair>();
		}
		
		ListenerStackPair etp = new ListenerStackPair(target, event);
		if (FuturesMonitor.debug) {
			FuturesMonitor.monitor.add(etp, this);
		}
		synchronized (actions) {
			actions.add(etp);
		}
		synchronized (vargs) {
			if (available() || closed) {
				actions();
			}
		}
	}

	private void actions() {
		List<ListenerStackPair> l;
		synchronized(this) {
			if (actions == null) {
				return;
			}
			
			l = actions;
			actions = null;
		}
		for (ListenerStackPair lsp : l) {
			if (FuturesMonitor.debug) {
				FuturesMonitor.monitor.remove(lsp);
			}
			lsp.listener.futureModified(this, lsp.stack);
		}
	}

	public int hashCode() {
		if (!available()) {
			return super.hashCode();
		}
		return getValue().hashCode();
	}

	public String toString() {
		if (!available()) {
			return "ForwardArgumentFuture(" + index + ")";
		}
		else {
			return "ForwardArgumentFuture(" + index + "): " + getValue();
		}
	}
	
	public void futureModified(Future f, VariableStack stack) {
		synchronized (vargs) {
			try {
				vargs.get(index);
				actions();
			}
			catch (FutureFault e) {
				if (((Future) vargs).isClosed()) {
					close();
				}
			}
			catch (IndexOutOfBoundsException e) {
				close();
			}
		}
	}

	public void fail(FutureEvaluationException e) {
		this.exception = e;
		actions();
	}
}
