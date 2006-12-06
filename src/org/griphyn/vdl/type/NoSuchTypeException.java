/*
 * Created on Jun 6, 2006
 */
package org.griphyn.vdl.type;

public class NoSuchTypeException extends Exception {
	public NoSuchTypeException(String type) {
		super("No such type: " + type);
	}
}
