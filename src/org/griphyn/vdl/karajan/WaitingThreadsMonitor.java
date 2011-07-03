/*
 * Created on Jun 17, 2006
 */
package org.griphyn.vdl.karajan;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.globus.cog.karajan.stack.VariableStack;

public class WaitingThreadsMonitor {
	private static Set threads;
	
	public synchronized static void addThread(VariableStack stack) {
	    if (stack != null) {
	        getThreads().add(stack);
	    }
	}
	
	private static synchronized Set getThreads() {
		if (threads == null) {
			threads = new HashSet();
		}
		return threads;
	}
	
	public synchronized static void removeThread(VariableStack stack) {
		getThreads().remove(stack);
	}
	
	public synchronized static Collection getAllThreads() {
		if (threads == null) {
			return Collections.EMPTY_SET;
		}
		else {
			return new HashSet(threads);
		}
	}
}
