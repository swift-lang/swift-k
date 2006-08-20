
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------


package org.globus.cog.gui.setup.util;

/**
 * A generic callback interface
 */
public interface Callback {
	/**
	 * The callback method
	 * @param source the source of the notification
	 * @param data optional data that may come with the notification
	 */
	public void callback(Object source, Object data);
}
