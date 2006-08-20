
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/*
 * 
 * Created on Jan 23, 2004
 */
package org.globus.cog.gui.grapheditor.ant;

import org.globus.cog.gui.grapheditor.targets.swing.SwingCanvasRenderer;
import org.globus.cog.gui.grapheditor.targets.swing.util.CanvasAction;
import org.globus.cog.gui.grapheditor.targets.swing.util.CanvasActionEvent;
import org.globus.cog.gui.grapheditor.targets.swing.views.ListView;

public class ProjectCanvasRenderer extends SwingCanvasRenderer {
	private CanvasAction open, save, saveAs, importAnt;
	
	public ProjectCanvasRenderer() {
		getSupportedViews().clear();
		addSupportedView(new AllListView());
		addSupportedView(new ProjectPropertiesView());
		addSupportedView(new TargetsListView());
		addSupportedView(new TargetsGraphView());
		addSupportedView(new FlowView());
		addMenuItem(open = new CanvasAction("File->Open...", CanvasAction.ACTION_NORMAL));
		addMenuItem(save = new CanvasAction("File->Save", CanvasAction.ACTION_NORMAL));
		addMenuItem(saveAs = new CanvasAction("File->Save As...", CanvasAction.ACTION_NORMAL));
		addMenuItem(importAnt = new CanvasAction("File->Import Ant Build File...", CanvasAction.ACTION_NORMAL));
		setView(new ListView());
	}
	
	
	public void menuItemEvent(CanvasActionEvent ee) {
		if (ee.getCanvasAction() == open) {
			((ProjectNode) getCanvas().getOwner()).open();
			return;
		}
		else if (ee.getCanvasAction() == importAnt) {
			((ProjectNode) getCanvas().getOwner()).importAntBuildfile();
			return;
		}
		else if (ee.getCanvasAction() == save) {
			((ProjectNode) getCanvas().getOwner()).save();
			return;
		}
		else {
			super.menuItemEvent(ee);
		}
	}

}
