
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------
    
package org.globus.cog.gui.setup.events;

import java.util.EventListener;

/**
 *  Listener for navigation events (next, previous, cancel, finish, jump)
 */
public interface NavActionListener extends EventListener {

	public void navAction(NavEvent e);
}
