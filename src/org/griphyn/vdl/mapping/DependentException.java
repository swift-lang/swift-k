/*
 * Created on Jan 11, 2007
 */
package org.griphyn.vdl.mapping;

public class DependentException extends RuntimeException {
	private DSHandle handle;
	
	public DependentException(DSHandle handle, String message, Exception prev) {
		super(message, prev);
		this.handle = handle;
	}
	
	public DependentException(DSHandle handle, Exception prev) {
		super(prev);
		this.handle = handle;
	}
	
	public DependentException(DSHandle handle, String message) {
		super(message);
		this.handle = handle;
	}

	public DSHandle getHandle() {
		return handle;
	}

	public String toString() {
		return super.getMessage();
	}
}
