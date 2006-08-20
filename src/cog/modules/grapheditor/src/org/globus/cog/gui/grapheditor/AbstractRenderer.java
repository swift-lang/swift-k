
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

    
/*
 * Created on Jun 24, 2003
 */
package org.globus.cog.gui.grapheditor;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;

import org.globus.cog.gui.grapheditor.nodes.NodeComponent;

public abstract class AbstractRenderer
	implements ComponentRenderer, GraphComponentListener{
	private GraphComponent component;

	public GraphComponent getComponent() {
		return component;
	}

	public void setComponent(GraphComponent component) {
		if (this.component != null) {
			this.component.removePropertyChangeListener(this);
		}
		this.component = component;
		if (component != null) {
			component.addPropertyChangeListener(this);
		}
	}

	public void propertyChange(PropertyChangeEvent evt) {

	}

	
	public void dispose() {
		if (component != null) {
			component.removePropertyChangeListener(this);
		}
	}

	public void actionPerformed(ActionEvent e) {
	}
	
	protected NodeComponent getRootNode() {
		return component.getRootNode();
	}
}
