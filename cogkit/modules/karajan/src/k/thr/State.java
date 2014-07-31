/*
 * Swift Parallel Scripting Language (http://swift-lang.org)
 * Code from Java CoG Kit Project (see notice below) with modifications.
 *
 * Copyright 2005-2014 University of Chicago
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Dec 29, 2007
 */
package k.thr;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

public class State {
	private List<Object> state;
	private List<Object> trace;
	
	public State() {
		state = new ArrayList<Object>();
	}
	
	public State(State s) {
		this();
		if (s != null) {
			state.addAll(s.getAll());
		}
	}

	public synchronized int popInt() {
		if (state.size() == 0) {
			return 0;
		}
		else {
			return (Integer) state.remove(state.size() - 1);
		}
	}
	
	public synchronized Object pop() {
		if (state.size() == 0) {
			return null;
		}
		else {
			return state.remove(state.size() - 1);
		}
	}
	
	public synchronized void replaceBottom(int i) {
	    state.set(state.size() - 1, i);
	}

	public synchronized void push(int i) {
		state.add(i);
	}
	
	public synchronized void push(Object o) {
		state.add(o);
	}
	
	private static final NumberFormat NF = new DecimalFormat("00000000");
		
	public String toString() {
		return NF.format(System.identityHashCode(this)) + ": " + state.toString();
	}
		
	public boolean isEmpty() {
	    return state.isEmpty();
	}
	
	protected List<Object> getAll() {
		return state;
	}
	
	public synchronized void addTraceElement(Object n) {
	    if (trace == null) {
	        trace = new ArrayList<Object>(3);
	    }
	    trace.add(n);
	}
	
	public synchronized List<Object> getTrace() {
	    if (trace == null) {
	        return null;
	    }
	    else {
	    	return new ArrayList<Object>(trace);
	    }
	}
}
