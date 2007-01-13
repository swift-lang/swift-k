/*
 * Created on Jan 13, 2007
 */
package org.griphyn.vdl.mapping;

/**
 * Signals a problem with mapping dependencies. Both data and mapping
 * must not continue.
 */
public class MappingDependentException extends DependentException {

	public MappingDependentException(DSHandle handle, Exception prev) {
		super(handle, prev);
	}

	public MappingDependentException(DSHandle handle) {
		super(handle);	
	}

	public String getMessage() {
		return getHandle() + " not mapped due to error in mapping dependencies";
	}
}
