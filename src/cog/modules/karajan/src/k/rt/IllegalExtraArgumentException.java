//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jan 31, 2013
 */
package k.rt;

public class IllegalExtraArgumentException extends IllegalArgumentException {
	private String message;
	
	public IllegalExtraArgumentException(Object value) {
		if (value instanceof Number || value instanceof String || value == null) {
			message = "Illegal extra argument: " + value;
		}
		else {
			message = "Illegal extra argument: <" + ExecutionException.translateType(value.getClass().getName()) + ">";
		}
	}

	@Override
	public String getMessage() {
		return message;
	}
}
