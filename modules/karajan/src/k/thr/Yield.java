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
	
	public Yield(int pstate) {
		state = new State();
		state.push(pstate);
	}
	
	public final State getState() {
		return state;
	}
	
	public synchronized Throwable fillInStackTrace() {
		return this;
	}
}
