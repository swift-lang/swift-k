//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Mar 22, 2006
 */
package org.globus.cog.karajan.compiled.nodes.restartLog;

import java.util.Map;

import k.rt.ExecutionException;
import k.rt.Stack;
import k.thr.LWThread;
import k.thr.Yield;

import org.globus.cog.karajan.analyzer.ChannelRef;
import org.globus.cog.karajan.analyzer.Signature;
import org.globus.cog.karajan.analyzer.VariableNotFoundException;
import org.globus.cog.karajan.compiled.nodes.InternalFunction;

public class Logged extends InternalFunction {
	private ChannelRef<String> cr_restartLog;
	
	@Override
	protected Signature getSignature() {
		return new Signature(params(), returns(channel("restartLog")));
	}

	@Override
	public void run(LWThread thr) {
		int i = thr.checkSliceAndPopState();
		try {
			Stack stack = thr.getStack();
			switch (i) {
				case 0:
					if (checkLogged(thr, stack)) {
						return;
					}
					i++;
				case 1:
					super.run(thr);
					cr_restartLog.append(stack, LogEntry.build(thr, this).toString());
			}
		}
		catch (Yield y) {
			y.getState().push(i);
			throw y;
		}
	}

	protected boolean checkLogged(LWThread thr, Stack stack) {
		try {
			Map<LogEntry, Object> map = ((LogChannelOperator) cr_restartLog.get(stack)).getLogData();
			LogEntry entry = LogEntry.build(thr, this);
			boolean found = false;
			synchronized(map) {
				MutableInteger count = (MutableInteger) map.get(entry);
				if (count != null && count.getValue() > 0) {
					count.dec();
					found = true;
				}
			}
			return found;
		}
		catch (VariableNotFoundException e) {
			throw new ExecutionException("No restart log environment found");
		}
	}
}
