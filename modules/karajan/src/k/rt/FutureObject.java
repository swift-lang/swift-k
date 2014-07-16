//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on May 12, 2012
 */
package k.rt;


public class FutureObject extends AbstractFuture implements FutureValue {
	public static final Object NULL = new Object();
	
	private Object obj;
	private RuntimeException exception;

	@Override
	public synchronized Object getValue() {
		if (exception != null) {
            throw exception;
        }
		if (obj == null) {
			throw new ConditionalYield(this);
		}
		return obj == NULL ? null : obj;
	}
	
	@Override
	protected boolean isClosed() {
		return obj != null || exception != null;
	}

	public synchronized void setValue(Object obj) {
		if (obj == null) {
			this.obj = NULL;
		}
		else {
			this.obj = obj;
		}
		notifyListeners();
	}
	
	public synchronized void fail(RuntimeException e) {
		this.exception = e;
		notifyListeners();
	}
	
	@Override
	public String toString() {
		return String.valueOf(obj);
	}
}
