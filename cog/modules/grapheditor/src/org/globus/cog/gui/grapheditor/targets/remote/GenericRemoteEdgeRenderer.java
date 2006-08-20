
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/*
 * 
 * Created on Jan 22, 2004
 */
package org.globus.cog.gui.grapheditor.targets.remote;

import java.beans.PropertyChangeEvent;

import org.apache.log4j.Logger;
import org.globus.cog.gui.grapheditor.edges.EdgeRenderer;
import org.globus.cog.gui.grapheditor.properties.Property;

public class GenericRemoteEdgeRenderer extends EdgeRenderer{
	private static Logger logger = Logger.getLogger(GenericRemoteEdgeRenderer.class);

	public void propertyChange(PropertyChangeEvent e) {
		String propName = e.getPropertyName();
		Property prop = getComponent().getProperty(propName);
		if (prop.hasAccess(Property.NONPERSISTENT)) {
			return;
		}
		RemoteContainer.getContainer().updateProperty(getComponent().get_ID(), propName, e.getNewValue());
	}
}
