// ----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Apr 26, 2005
 */
package org.globus.cog.karajan.compiled.nodes;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import k.rt.ConditionalYield;
import k.rt.ExecutionException;
import k.rt.FutureObject;
import k.rt.Stack;
import k.thr.LWThread;
import k.thr.Yield;

import org.globus.cog.karajan.analyzer.ArgRef;
import org.globus.cog.karajan.analyzer.CompilerSettings;
import org.globus.cog.karajan.analyzer.Signature;

public class Exclusive extends InternalFunction {
	private ArgRef<Object> on;
	private Node body;
	
	private static Map<Object, LinkedList<FutureObject>> locks = new HashMap<Object, LinkedList<FutureObject>>();

	@Override
	protected Signature getSignature() {
		return new Signature(params("on", block("body")));
	}

	@Override
	public void runBody(LWThread thr) {
		int i = thr.checkSliceAndPopState();
		int fc = thr.popIntState();
		Object on = thr.popState();
		Stack stack = thr.getStack();
		try {
			switch (i) {
				case 0:
					on = this.on.getValue(stack);
					if (on == null) {
						on = this;
					}
					monitorEnter(thr, on);
					fc = stack.frameCount();
					i++;
				case 1:
					if (CompilerSettings.PERFORMANCE_COUNTERS) {
						startCount++;
					}
					body.run(thr);
					monitorExit(thr, on);
			}
		}
		catch (ExecutionException e) {
			stack.dropToFrame(fc);
			monitorExit(thr, on);
			throw e;
		}
		catch (Yield y) {
			y.getState().push(on);
			y.getState().push(fc);
			y.getState().push(i);
			throw y;
		}
	}

	protected void monitorEnter(LWThread thr, Object on) {	
		int i = thr.checkSliceAndPopState();
		Stack stack = thr.getStack();

    	switch (i) {
    		case 0:
				synchronized(locks) {
					LinkedList<FutureObject> waiting = locks.get(on);
					if (waiting == null) {
						// first thread to get here
						locks.put(on, new LinkedList<FutureObject>());
					}
					else {
						// not the first thread; add a future object to the list and wait
						FutureObject fo = new FutureObject();
						waiting.add(fo);
						throw new ConditionalYield(1, fo);
					}
				}
    		default:
    			// awaken
    	}
	}

	protected void monitorExit(LWThread thr, Object on) {
		synchronized (locks) {
			LinkedList<FutureObject> waiting = locks.get(on);
			if (waiting.isEmpty()) {
				locks.remove(on);
			}
			else {
				FutureObject fo = waiting.removeFirst();
				fo.setValue(Boolean.TRUE);
			}
		}
	}
}
