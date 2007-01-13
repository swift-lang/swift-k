/*
 * Created on Jan 13, 2007
 */
package org.griphyn.vdl.mapping;

/**
 * Used for problems with data dependencies. Mapping should be ok,
 * so mapping must continue (mainly to allow building the graph).
 */
public class DataDependentException extends DependentException {
	
	public DataDependentException(DSHandle handle, Exception prev) {
		super(handle, prev);
	}

	public DataDependentException(DSHandle handle) {
		super(handle);
	}

	public String getMessage() {
		return getHandle() + " not derived due to errors in data dependencies";
	}

}
