//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------
package org.globus.cog.gridface.impl.desktop.listener;
/*
 * Listener for all events on Desktop pane
 */

//Local imports
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.SwingUtilities;

import org.globus.cog.gridface.impl.desktop.interfaces.AccessPopup;
import org.globus.cog.gridface.impl.desktop.interfaces.CoGTop;
//Other imports

public class DesktopListener implements MouseListener {
	/**
	 * Deselect all icons on desktop when the mouse button has
	 * been clicked (pressed and released) on the desktop.
	 */
	public void mouseClicked(MouseEvent e) {	
		CoGTop desktop = ((CoGTop) e.getSource());
	
		if (SwingUtilities.isLeftMouseButton(e)) {
			desktop.deselectAllIcons();
			desktop.getToolBar().deselectAllIcons();
			
		} else if (SwingUtilities.isRightMouseButton(e)) {
			desktop.deselectAllIcons();
			desktop.getToolBar().deselectAllIcons();
			if(e.getSource() instanceof AccessPopup){
				((AccessPopup)e.getSource()).showPopupAt(e.getX(), e.getY());
			}
		}
	}
	public void mouseReleased(MouseEvent e) {
	}
	public void mouseEntered(MouseEvent e) {
	}
	public void mouseExited(MouseEvent e) {
	}
	public void mousePressed(MouseEvent e) {
	}
}
