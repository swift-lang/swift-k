
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

import java.awt.Component;
import java.awt.Container;
import java.util.Iterator;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import org.globus.cog.gui.grapheditor.util.ConservativeArrayList;

public class SwingInspectorTreeModel implements TreeModel {

	private Component root;
	private ConservativeArrayList listeners;
	
	public SwingInspectorTreeModel(Component root) {
		this.root = root;
	}
	
	public Object getRoot() {
		return root; 
	}
	
	public void setRoot(Component root) {
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
		if (parent instanceof EventTrappingContainer) {
			EventTrappingContainer nc = (EventTrappingContainer) parent;
			if (nc.getComponent() instanceof Container) {
				return ((Container) nc.getComponent()).getComponentCount();
			}
			else {
				return 0;
			}
		}
		if (parent instanceof Container) {
			Container nc = (Container) parent;
			return nc.getComponentCount();
		}
		return 0;
	}

	public boolean isLeaf(Object node) {
		if (node instanceof Container) {
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
		if (parent instanceof EventTrappingContainer) {
			EventTrappingContainer nc = (EventTrappingContainer) parent;
			if (nc.getComponent() instanceof Container) {
				return ((Container) nc.getComponent()).getComponent(index);
			}
			else {
				return null;
			}
		}
		else if (parent instanceof Container) {
			return ((Container) parent).getComponent(index);
		}
		return null;
	}

	public int getIndexOfChild(Object parent, Object child) {
		if (parent instanceof Container) {
			Component[] c = ((Container) parent).getComponents();
			for (int i = 0; i < c.length; i++) {
				if (c[i] == child) {
					return i;
				}
			}
		}
		return -1;
	}

	public void valueForPathChanged(TreePath path, Object newValue) {

	}

}
