
package org.globus.cog.gridshell.getopt.interfaces;

/**
 * Allows Strings to be turned into the objects that are mapped to their type. Also contains a listing of default validators
 * 
 */
public interface Validator {
	/**
	 * Ensures that Something is a valid type
	 * @param value - a String value that needs to be converted to the correct type
	 * @return - an object of the correct type for this object
	 * @throws Exception
	 */
	Object validate(String value) throws Exception;	
	/**
	 * Returns java.lang.Boolean
	 */
	Validator booleanValidator = new Validator() {
		public Object validate(String value)  throws Exception {			
			if(value == null || (!"true".equalsIgnoreCase(value) && !"false".equalsIgnoreCase(value)) ) {
				throw new Exception("Can't parse '"+value+"' as Boolean.");
			}			
			return Boolean.valueOf(value);
		}
	};
	/**
	 * Returns a java.lang.Double
	 */
	Validator doubleValidator = new Validator() {
		public Object validate(String value) throws Exception {
			Object result = Double.valueOf(value);
			if(result == null) {
				throw new Exception("Can't parse '"+value+"' as Double.");
			}
			return result;
		}
	};	
	/**
	 * Returns a java.lang.Integer
	 */
	Validator intValidator = new Validator() {
		public Object validate(String value) throws Exception {
			Object result = new Integer(Integer.parseInt(value));
			if(result == null) {
				throw new Exception("Can't parse '"+value+"' as Integer.");
			}
			return result;
		}
	};
	/**
	 * Doesn't really do anything since the input is a String
	 */
	Validator stringValidator = new Validator() {
		public Object validate(String value) {
			// don't need to do anything
			return value;
		}
	};
	/**
	 * Returns a java.net.URL
	 */
	Validator urlValidator = new Validator() {
		public Object validate(String value) throws Exception {
			return new java.net.URL(value);			
		}
	};	
	/**
	 * Returns a java.net.URI
	 */
	Validator uriValidator = new Validator() {
		public Object validate(String value) throws Exception {
			return new java.net.URI(value);			
		}
	};

}
