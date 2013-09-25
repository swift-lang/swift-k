
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/*
 * 
 * Created on Jan 23, 2004
 */
package org.globus.cog.gui.grapheditor.targets.swing;


import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.EventObject;
import java.util.Iterator;
import java.util.List;

import javax.swing.JMenu;

import org.apache.log4j.Logger;
import org.globus.cog.gui.grapheditor.AbstractRenderer;
import org.globus.cog.gui.grapheditor.util.ConservativeArrayList;
import org.globus.cog.gui.grapheditor.util.EventConsumer;
import org.globus.cog.gui.grapheditor.util.EventDispatcher;
import org.globus.cog.gui.grapheditor.util.swing.ComponentAction;
import org.globus.cog.gui.grapheditor.util.tables.NodePropertiesEditor;

public abstract class AbstractSwingRenderer extends AbstractRenderer
	implements
		MouseListener,
		SwingComponentRenderer,
		EventConsumer {
	private static Logger logger = Logger.getLogger(AbstractSwingRenderer.class);
	
	private Component visualComponent;
	private JMenu menu;
	private List actions;
	private ComponentAction properties;
	
	public AbstractSwingRenderer() {
		properties = new ComponentAction("Properties");
		properties.addActionListener(this);
		addAction(properties);
	}

	public Component getVisualComponent() {
		return visualComponent;
	}

	public void setVisualComponent(Component component) {
		if (visualComponent != null) {
			visualComponent.removeMouseListener(this);
		}
		visualComponent = component;
		component.addMouseListener(this);
	}

	public void mouseClicked(MouseEvent e) {
		if ((e.getModifiers() & InputEvent.BUTTON3_MASK) != 0) {
			JMenu m = getMenu();
			m.getPopupMenu().show(getVisualComponent(), e.getX(), e.getY());
		}
	}

	public void mousePressed(MouseEvent e) {
	}

	public void mouseReleased(MouseEvent e) {
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}

	public synchronized JMenu getMenu() {
		if (menu == null) {
			menu = new JMenu();
			Iterator actions = getActions().iterator();
			while (actions.hasNext()) {
				ComponentAction a = (ComponentAction) actions.next();
				menu.add(a.createComponent());
			}
		}
		return menu;
	}

	public synchronized void addAction(ComponentAction action) {
		if (actions == null) {
			actions = new ConservativeArrayList(1);
		}
		actions.add(action);
	}

	public synchronized List getActions() {
		return actions;
	}

	public synchronized ComponentAction getAction(String name) {
		if (actions == null) {
			return null;
		}
		Iterator i = actions.iterator();
		while (i.hasNext()) {
			ComponentAction action = (ComponentAction) i.next();
			if (action.getName().equals(name)) {
				return action;
			}
		}
		return null;
	}

	public synchronized void removeAction(ComponentAction action) {
		if (actions == null) {
			return;
		}
		actions.remove(action);
	}
	
	public void actionPerformed(ActionEvent e) {
		EventDispatcher.queue(this, e);
	}
	
	public void event(EventObject e) {
		if (e instanceof ActionEvent) {
			ActionEvent ee = (ActionEvent) e;
			if (ee.getSource() == properties) {
				NodePropertiesEditor pe = new NodePropertiesEditor(getComponent());
				pe.setVisible(true);
			}
		}
		else {
			logger.warn("Unhandled event "+e);
		}
	}
}
