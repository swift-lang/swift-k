/*
 * Created on Jun 17, 2006
 */
package org.griphyn.vdl.karajan;


public class ClosedHandle extends DSHandleFutureWrapper {

	public ClosedHandle() {
		super(null);
	}
	
	public String toString() {
		return "Closed";
	}

}
