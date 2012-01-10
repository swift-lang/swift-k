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
