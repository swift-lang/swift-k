
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/*
 * 
 * Created on Jan 19, 2004
 */
package org.globus.cog.gui.grapheditor.util.swing;

import java.util.Iterator;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import org.globus.cog.gui.grapheditor.canvas.GraphCanvas;
import org.globus.cog.gui.grapheditor.nodes.EditableNodeComponent;
import org.globus.cog.gui.grapheditor.nodes.NodeComponent;
import org.globus.cog.gui.grapheditor.util.ConservativeArrayList;
import org.globus.cog.util.graph.Node;

public class GraphTreeModel implements TreeModel {

	private NodeComponent root;
	private ConservativeArrayList listeners;
	
	public GraphTreeModel(NodeComponent root) {
		this.root = root;
	}
	
	public Object getRoot() {
		return root; 
	}
	
	public void setRoot(NodeComponent root) {
		this.root = root;
		fireTreeStructureChanged(new TreeModelEvent(this, (TreePath) null));
	}
	
	protected void fireTreeStructureChanged(TreeModelEvent e) {
		if (listeners != null) {
			Iterator i = listeners.iterator();
			while (i.hasNext()) {
				((TreeModelListener) i.next()).treeStructureChanged(e);
			}
		}
	}

	public int getChildCount(Object parent) {
		if (parent instanceof NodeComponent) {
			NodeComponent nc = (NodeComponent) parent;
			GraphCanvas canvas = nc.getCanvas();
			if (canvas == null) {
				return 0;
			}
			return canvas.getGraph().nodeCount();
		}
		return 0;
	}

	public boolean isLeaf(Object node) {
		if (node instanceof EditableNodeComponent) {
			if (((NodeComponent) node).getCanvas() == null) {
				return true;
			}
			return false;
		}
		else {
			return true;
		}
	}

	public void addTreeModelListener(TreeModelListener l) {
		if (listeners == null) {
			listeners = new ConservativeArrayList(1);
		}
		listeners.add(l);
	}

	public void removeTreeModelListener(TreeModelListener l) {
		listeners.remove(l);
	}

	public Object getChild(Object parent, int index) {
		if (parent instanceof NodeComponent) {
			NodeComponent nc = (NodeComponent) parent;
			GraphCanvas canvas = nc.getCanvas();
			if (canvas == null) {
				return null;
			}
			return ((Node) canvas.getGraph().getNodesSet().toArray()[index]).getContents();
		}
		return null;
	}

	public int getIndexOfChild(Object parent, Object child) {
		if (parent instanceof NodeComponent) {
			NodeComponent nc = (NodeComponent) parent;
			GraphCanvas canvas = nc.getCanvas();
			if (canvas == null) {
				return -1;
			}
			Iterator i = canvas.getGraph().getNodesSet().iterator();
			int index = 0;
			while (i.hasNext()) {
				if (child == i.next()) {
					return index;
				}
				index++;
			}
		}
		return -1;
	}

	public void valueForPathChanged(TreePath path, Object newValue) {

	}

}
