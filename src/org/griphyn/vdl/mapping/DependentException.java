/*
 * Created on Jan 11, 2007
 */
package org.griphyn.vdl.mapping;

/**
 * Signifies a problem with data/mapping dependencies
 */
public abstract class DependentException extends RuntimeException {
	private DSHandle handle;

	public DependentException(DSHandle handle, Exception prev) {
		super(prev);
		this.handle = handle;
	}

	public DependentException(DSHandle handle) {
		super();
		this.handle = handle;
	}
	
	public abstract String getMessage();

	public DSHandle getHandle() {
		return handle;
	}

	public String toString() {
		return getMessage();
	}
}
