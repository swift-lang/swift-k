
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.gui.grapheditor.targets.swing.util;


import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JLayeredPane;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;

import org.apache.log4j.Logger;
import org.globus.cog.gui.grapheditor.ComponentRenderer;
import org.globus.cog.gui.grapheditor.GraphComponent;
import org.globus.cog.gui.grapheditor.targets.swing.ActionTree;
import org.globus.cog.gui.grapheditor.targets.swing.RankedName;
import org.globus.cog.gui.grapheditor.targets.swing.SwingComponentRenderer;
import org.globus.cog.gui.grapheditor.util.ConservativeArrayList;
import org.globus.cog.gui.grapheditor.util.swing.ComponentAction;
import org.globus.cog.gui.grapheditor.util.swing.ExtendedMouseEvent;
import org.globus.cog.gui.grapheditor.util.swing.MouseEventDispatcher;
import org.globus.cog.gui.grapheditor.util.tables.NodePropertiesEditor;

/**
 * Implements some basic methods common to node and edge wrappers
 */
public class GraphComponentWrapper extends JComponent implements ActionListener,
	CanvasActionListener, MouseListener, MouseMotionListener {

	private static Logger logger = Logger.getLogger(GraphComponentWrapper.class);

	protected static final int ANTI_ALIASING = 0x0001;
	protected static final int USED_AS_RENDERER = 0x0002;
	protected static final int VALID = 0x0004;
	protected static final int SELECTED = 0x0008;
	protected static final int MOVABLE = 0x0010;

	private SwingComponentRenderer renderer;

	private Component comp;

	public GraphComponent c;

	private List graphComponentListeners;

	private int flags;

	private static Hashtable renderers = new Hashtable();

	private List actions;

	private Hashtable actionMap;

	private Hashtable componentActions;

	private CanvasAction delete;

	public GraphComponentWrapper() {
		super();
		unsetFlag(ANTI_ALIASING);
		enableEvents(AWTEvent.MOUSE_EVENT_MASK);
		addWrapperAction(delete = new CanvasAction("10#Delete", null,
			CanvasAction.ACTION_NORMAL));
	}

	protected void setFlag(int flag) {
		flags |= flag;
	}

	protected void unsetFlag(int flag) {
		flags &= ~flag;
	}

	protected void setFlag(int flag, boolean value) {
		if (value) {
			flags |= flag;
		}
		else {
			flags &= ~flag;
		}
	}

	protected boolean getFlag(int flag) {
		return (flags & flag) != 0;
	}

	public GraphComponentWrapper(GraphComponent e) {
		this();
		setGraphComponent(e);
	}

	public void setGraphComponent(GraphComponent e) {
		dispose();
		if (this.c != e) {
			invalidate();
		}
		this.c = e;
	}

	public void setRenderer(SwingComponentRenderer renderer) {
		this.renderer = renderer;
		//setComponent(renderer.getVisualComponent());
		this.comp = renderer.getVisualComponent();
	}

	public void validate() {
		if (getFlag(VALID)) {
			return;
		}
		if (getComponent() == null) {
			setUpComponent();
		}
		super.validate();
		setFlag(VALID);
	}

	public void invalidate() {
		unsetFlag(VALID);
	}

	public void setUpComponent() {

	}

	protected void addWrapperAction(CanvasAction action) {
		if (actions == null) {
			actions = new LinkedList();
		}
		actions.add(action);
		action.addCanvasActionListener(this);
	}

	public void removeWrapperAction(CanvasAction action) {
		action.removeCanvasActionListener(this);
		actions.remove(action);
	}

	public JMenu createMenuFromActions() {
		componentActions = new Hashtable();
		ActionTree mt = new ActionTree();
		int index = 0;
		if (comp != null) {
			Iterator i = getRenderer().getActions().iterator();
			while (i.hasNext()) {
				ComponentAction action = (ComponentAction) i.next();
				CanvasAction ca = new CanvasAction("" + (index++) + "#"
					+ action.getName(), null, CanvasAction.ACTION_NORMAL);
				componentActions.put(ca, action);
				mt.addBranch(new RankedName(ca));
			}
			mt.addBranch(new RankedName(new CanvasAction("" + (index++) + "#csep",
				CanvasAction.SEPARATOR)));
		}
		Iterator i = actions.iterator();
		while (i.hasNext()) {
			CanvasAction action = (CanvasAction) i.next();
			mt.addBranch(new RankedName(action));
		}
		JMenu menu = new JMenu();
		actionMap = new Hashtable();
		mt.buildMenu(menu, actionMap, this);
		return menu;
	}

	public boolean getAntiAliasing() {
		return getFlag(ANTI_ALIASING);
	}

	public void setAntiAliasing(boolean antiAliasing) {
		setFlag(ANTI_ALIASING, antiAliasing);
	}

	public Component getComponent() {
		return comp;
	}

	protected void setComponent(Component comp) {
		this.comp = comp;
	}

	public GraphComponent getGraphComponent() {
		return c;
	}

	public void setPosition(int position) {
		if (getParent() instanceof JLayeredPane) {
			((JLayeredPane) getParent()).setPosition(this, position);
		}
	}

	public int getPosition() {
		if (getParent() instanceof JLayeredPane) {
			return ((JLayeredPane) getParent()).getPosition(this);
		}
		return 0;
	}

	public void moveToFront() {
		if (getParent() instanceof JLayeredPane) {
			((JLayeredPane) getParent()).moveToFront(this);
		}
	}

	public void moveToBack() {
		if (getParent() instanceof JLayeredPane) {
			((JLayeredPane) getParent()).moveToBack(this);
		}
	}

	public boolean acceptsConnection(GraphComponent g) {
		return false;
	}

	public void addGraphComponentListener(GraphComponentWrapperListener l) {
		if (graphComponentListeners == null) {
			graphComponentListeners = new ConservativeArrayList(1);
		}
		if (!graphComponentListeners.contains(l)) {
			graphComponentListeners.add(l);
		}
	}

	public void removeGraphComponentListener(GraphComponentWrapperListener l) {
		if (graphComponentListeners == null) {
			return;
		}
		graphComponentListeners.remove(l);
	}

	public void fireGraphComponentEvent(GraphComponentWrapperEvent e) {
		if (graphComponentListeners == null) {
			return;
		}
		Iterator i = graphComponentListeners.iterator();
		while (i.hasNext()) {
			((GraphComponentWrapperListener) i.next()).graphComponentEvent(e);
		}
	}

	public ComponentRenderer getRenderer(GraphComponent component) {
		if (renderers.containsKey(component.getClass())) {
			return (ComponentRenderer) renderers.get(component.getClass());
		}
		else {
			ComponentRenderer renderer = component.newRenderer();
			renderers.put(component.getClass(), renderer);
			return renderer;
		}
	}

	public SwingComponentRenderer getRenderer() {
		return renderer;
	}

	public void mouseClicked(MouseEvent e) {
	}

	public void mousePressed(MouseEvent e) {
		if ((e.getModifiers() & InputEvent.BUTTON3_MASK) != 0) {
			JMenu menu = createMenuFromActions();
			JPopupMenu pm = menu.getPopupMenu();
			if (e instanceof ExtendedMouseEvent) {
				ExtendedMouseEvent ex = (ExtendedMouseEvent) e;
				pm.show(ex.getPopupInvoker(), ex.getInvokerX(), ex.getInvokerY());
			}
			else {
				pm.show(this, e.getX(), e.getY());
			}
		}
	}

	public void mouseReleased(MouseEvent e) {
	}

	public void dispose() {
		removeAll();
		if (renderer != null) {
			renderer.dispose();
		}
		renderer = null;
		comp = null;
	}

	public void actionPerformed(ActionEvent e) {
		CanvasAction ca = (CanvasAction) actionMap.get(e.getSource());
		if (ca == null) {
			return;
		}
		if (componentActions.containsKey(ca)) {
			if (e.getActionCommand().equals("Properties")) {
				NodePropertiesEditor pe = new NodePropertiesEditor(
					getRenderer().getComponent());
				pe.setLocation(getLocationOnScreen());
				pe.show();
				return;
			}
			ComponentAction action = (ComponentAction) componentActions.get(ca);
			action.actionPerformed(e);
		}
		else {
			ca.perform();
		}
	}

	protected void processMouseEvent(MouseEvent e) {
		super.processMouseEvent(e);
		MouseEventDispatcher.dispatchMouseEvent(e, this);
	}
	
	protected void processMouseMotionEvent(MouseEvent e) {
		super.processMouseEvent(e);
		MouseEventDispatcher.dispatchMouseMotionEvent(e, this);
	}

	public void canvasActionPerformed(CanvasActionEvent e) {
		if (e.getType() == CanvasActionEvent.PERFORM) {
			if (e.getCanvasAction() == delete) {
				GraphComponent gc = getGraphComponent();
				gc.getParent().getCanvas().removeComponent(getGraphComponent());
				return;
			}
			logger.debug("Unhandled event: " + e);
		}
	}

	public void setUsedAsRenderer(boolean usedAsRenderer) {
		setFlag(USED_AS_RENDERER, usedAsRenderer);
	}

	public boolean isUsedAsRenderer() {
		return getFlag(USED_AS_RENDERER);
	}

	public boolean isShowing() {
		return (getParent() != null) && (isUsedAsRenderer() || super.isShowing());
	}

	public boolean isSelected() {
		return getFlag(SELECTED);
	}

	public void setSelected(boolean selected) {
		setFlag(SELECTED, selected);
	}

	public void requestSelection() {
		fireGraphComponentEvent(new GraphComponentWrapperEvent(this,
			GraphComponentWrapperEvent.REQUEST_SELECTION));
	}

	public void requestUnselection() {
		fireGraphComponentEvent(new GraphComponentWrapperEvent(this,
			GraphComponentWrapperEvent.REQUEST_UNSELECTION));
	}

	public boolean isMovable() {
		return getFlag(MOVABLE);
	}

	public void setMovable(boolean movable) {
		setFlag(MOVABLE, movable);
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}

	public void mouseDragged(MouseEvent e) {
	}

	public void mouseMoved(MouseEvent e) {
	}
}