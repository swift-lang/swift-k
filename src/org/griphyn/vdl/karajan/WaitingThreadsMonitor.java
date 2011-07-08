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
	private static Set<VariableStack> threads;
	
	public synchronized static void addThread(VariableStack stack) {
	    if (stack != null) {
	        getThreads().add(stack);
	    }
	}
	
	private static synchronized Set<VariableStack> getThreads() {
		if (threads == null) {
			threads = new HashSet<VariableStack>();
		}
		return threads;
	}
	
	public synchronized static void removeThread(VariableStack stack) {
		getThreads().remove(stack);
	}
	
	public synchronized static Collection<VariableStack> getAllThreads() {
		if (threads == null) {
			return Collections.emptySet();
		}
		else {
			return new HashSet<VariableStack>(threads);
		}
	}
}
