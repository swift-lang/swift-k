
package org.griphyn.vdl.karajan;

/** 
 * Generated only by SwiftScript @assert(). 
 * 
 * Created on September 27, 2010
 * @author wozniak
 */
public class AssertFailedException extends RuntimeException {

    private static final long serialVersionUID = 1L;
   
    String message = null;
    
	public AssertFailedException(String message) {
		super(message);
	}
}
