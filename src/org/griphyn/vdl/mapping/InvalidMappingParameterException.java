/*
 * Created on Mar 2, 2007
 */
package org.griphyn.vdl.mapping;

public class InvalidMappingParameterException extends RuntimeException {

	public InvalidMappingParameterException(String message, Throwable cause) {
		super(message, cause);		
	}

	public InvalidMappingParameterException(String message) {
		super(message);	
	}
}
