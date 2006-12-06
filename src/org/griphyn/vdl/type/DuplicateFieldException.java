package org.griphyn.vdl.type;

public class DuplicateFieldException extends Exception {
	public DuplicateFieldException(String name) {
		super("Field already exists: " + name);
	}
}
