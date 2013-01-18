
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

    
package org.globus.cog.gui.grapheditor.targets.swing.views;

import java.util.HashMap;
import java.util.Map;

import javax.swing.JTree;

import org.globus.cog.gui.grapheditor.canvas.CanvasRenderer;
import org.globus.cog.gui.grapheditor.canvas.views.CanvasView;
import org.globus.cog.gui.grapheditor.nodes.NodeComponent;
import org.globus.cog.gui.grapheditor.targets.swing.SwingComponentRenderer;
import org.globus.cog.gui.grapheditor.util.swing.GraphTreeModel;
import org.globus.cog.gui.grapheditor.util.tree.TreeNodeEditor;
import org.globus.cog.gui.grapheditor.util.tree.TreeNodeRenderer;
import org.globus.cog.util.graph.GraphChangedEvent;
import org.globus.cog.util.graph.GraphListener;

/**
 * Implements a tree view of the objects and their properties. It allows for filtering of
 * the nodes based on their class types.
 */
public class TreeView extends SwingView implements CanvasView, GraphListener {
    private GraphTreeModel model;
    private JTree tree;
    private Map renderers;

    public TreeView() {
        super();
        renderers = new HashMap();
        setName("Tree View");
        model = new GraphTreeModel(null);
        tree = new JTree(model);
        tree.setCellRenderer(new TreeNodeRenderer(this));
        tree.setCellEditor(new TreeNodeEditor(tree));
        tree.setRootVisible(false);
        tree.setShowsRootHandles(true);
        setComponent(tree);
    }

    public SwingComponentRenderer getRenderer(NodeComponent nc) {
    	if (renderers.containsKey(nc)) {
    		return (SwingComponentRenderer) renderers.get(nc);
    	}
    	else {
    		SwingComponentRenderer renderer = (SwingComponentRenderer) nc.newRenderer("swing");
    		renderers.put(nc, renderer);
    		return renderer;
    	}
    }
    
    public void invalidate() {
    	super.invalidate();
    }



	public void graphChanged(GraphChangedEvent e) {
	}

	public void setRenderer(CanvasRenderer renderer) {
		super.setRenderer(renderer);
		if (getCanvas() != null) {
			model.setRoot(getCanvas().getOwner());
		}
	}

}