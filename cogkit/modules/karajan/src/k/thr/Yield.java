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
 * Created on Dec 13, 2007
 */
package k.thr;


public class Yield extends Error {
	private final State state;
	
	protected Yield(State state) {
		this.state = state;
	}
	
	public Yield() {
		state = new State();
	}
	
	public Yield(int pstate, int max) {
		state = new State();
		state.push(pstate, max);
	}
	
	public final State getState() {
		return state;
	}
	
	public synchronized Throwable fillInStackTrace() {
		return this;
	}
}
