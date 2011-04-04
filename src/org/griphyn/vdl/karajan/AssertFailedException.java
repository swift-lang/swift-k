
package org.griphyn.vdl.karajan;

import org.globus.cog.karajan.workflow.ExecutionException;

/** 
 * Generated only by SwiftScript @assert(). 
 * 
 * Created on September 27, 2010
 * @author wozniak
 */
public class AssertFailedException extends ExecutionException {

    private static final long serialVersionUID = 1L;
   
    String message = null;
    
	public AssertFailedException(String message) {
		super(message);
	}
}
