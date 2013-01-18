
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/*
 * 
 * Created on Jan 24, 2004
 */
package org.globus.cog.gui.grapheditor.util;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.globus.cog.gui.grapheditor.targets.swing.util.CanvasActionEvent;
import org.globus.cog.gui.grapheditor.targets.swing.util.CanvasActionListener;

public class EventDispatchHelper {
	public static void firePropertyChangeEvent(List listeners, PropertyChangeEvent pce) {
		if (listeners == null) {
			return;
		}
		Iterator i = new ArrayList(listeners).iterator();
		while (i.hasNext()) {
			((PropertyChangeListener) i.next()).propertyChange(pce);
		}
	}
	
	public static void fireActionEvent(List listeners, ActionEvent pce) {
		if (listeners == null) {
			return;
		}
		Iterator i = listeners.iterator();
		while (i.hasNext()) {
			((ActionListener) i.next()).actionPerformed(pce);
		}
	}
	
	public static void fireCanvasActionEvent(List listeners, CanvasActionEvent mie) {
		if (listeners == null) {
			return;
		}
		Iterator i = listeners.iterator();
		while (i.hasNext()) {
			((CanvasActionListener) i.next()).canvasActionPerformed(mie);
		}
	}
}
