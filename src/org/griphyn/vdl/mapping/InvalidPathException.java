/*
 * Created on Jun 6, 2006
 */
package org.griphyn.vdl.mapping;

public class InvalidPathException extends Exception {
	public InvalidPathException(String path, DSHandle source) {
		super("Invalid path (" + path + ") for "
			+ source.toString());
	}
	
	public InvalidPathException(Path path, DSHandle source) {
		this(path.toString(), source);
	}
}
