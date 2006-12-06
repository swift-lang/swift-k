/*
 * Created on Jun 15, 2006
 */
package org.griphyn.vdl.mapping;

public class HandleOpenException extends Exception {
	private DSHandle source;

	public HandleOpenException(DSHandle source) {
		super("Handle open: " + source.getType() + " " + source.toString());
		this.source = source;
	}

	public DSHandle getSource() {
		return source;
	}
}
