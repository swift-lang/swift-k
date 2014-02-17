//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Sep 29, 2012
 */
package k.rt;


public class FutureCounter extends AbstractFuture {
	private int n;
	
	public FutureCounter(int n) {
		this.n = n;
	}
	
	public synchronized void dec() {
		n--;
		if (n == 0) {
			notifyListeners();
		}
	}

	@Override
	protected boolean isClosed() {
		return n == 0;
	}
}
