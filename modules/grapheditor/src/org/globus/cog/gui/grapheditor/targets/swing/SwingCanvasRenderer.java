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

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Rectangle;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.EventObject;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JOptionPane;

import org.apache.log4j.Logger;
import org.globus.cog.gui.about.CoGAbout;
import org.globus.cog.gui.grapheditor.canvas.AbstractCanvasRenderer;
import org.globus.cog.gui.grapheditor.canvas.GraphCanvas;
import org.globus.cog.gui.grapheditor.canvas.StatusEvent;
import org.globus.cog.gui.grapheditor.canvas.StatusEventListener;
import org.globus.cog.gui.grapheditor.canvas.views.CanvasView;
import org.globus.cog.gui.grapheditor.targets.swing.util.CanvasAction;
import org.globus.cog.gui.grapheditor.targets.swing.util.CanvasActionEvent;
import org.globus.cog.gui.grapheditor.targets.swing.util.CanvasActionListener;
import org.globus.cog.gui.grapheditor.targets.swing.util.export.ExportDialog;
import org.globus.cog.gui.grapheditor.targets.swing.views.GraphView;
import org.globus.cog.gui.grapheditor.targets.swing.views.ListView;
import org.globus.cog.gui.grapheditor.targets.swing.views.SwingView;
import org.globus.cog.gui.grapheditor.targets.swing.views.TreeView;
import org.globus.cog.gui.grapheditor.util.EventConsumer;
import org.globus.cog.gui.grapheditor.util.EventDispatcher;
import org.globus.cog.gui.grapheditor.util.StatusManager;
import org.globus.cog.util.ImageLoader;

public class SwingCanvasRenderer extends AbstractCanvasRenderer implements ComponentListener,
		StatusEventListener, EventConsumer, CanvasActionListener {
	private static Logger logger = Logger.getLogger(SwingCanvasRenderer.class);

	private static Icon ICON_CLOSE = ImageLoader.loadIcon("images/16x16/co/fileclose.png");

	private CanvasAction reLayout, close, about, export;

	private CanvasPanel panel;

	private StatusManager statusManager;

	private List menuItems;

	private List toolBarItems;

	private boolean simplePanel;

	public SwingCanvasRenderer() {
		this(false);
	}

	public SwingCanvasRenderer(boolean simplePanel) {
		menuItems = new LinkedList();
		toolBarItems = new LinkedList();
		this.simplePanel = simplePanel;
		addMenuItem(close = new CanvasAction("1#File>999#Close", ICON_CLOSE,
				CanvasAction.ACTION_NORMAL + CanvasAction.SEPARATOR_BEFORE));
		addMenuItem(about = new CanvasAction("999#Help>999#About", CanvasAction.ACTION_NORMAL));
		addMenuItem(export = new CanvasAction("1#File>30#Export...", CanvasAction.ACTION_NORMAL));
		addSupportedView(new GraphView());
		addSupportedView(new ListView());
		addSupportedView(new TreeView());
		panel = new CanvasPanel(this);
		statusManager = new StatusManager(panel);
	}

	public synchronized void initialize() {
		if (getView() == null) {
			setView((CanvasView) getSupportedViews().get(0));
		}
		updatePanel();
	}

	public void updatePanel() {
		panel.updateViewList();
		panel.updateMenuBar();
		panel.updateToolBar();
	}

	public void menuItemEvent(CanvasActionEvent mie) {
	}

	public void setSize(Dimension dimension) {

	}

	public void componentHidden(ComponentEvent e) {
	}

	public void componentMoved(ComponentEvent e) {
	}

	public void componentResized(ComponentEvent e) {
		// panel.getViewport().setViewSize(getSwingView().getComponent().getSize());
	}

	public void componentShown(ComponentEvent e) {
	}

	public Component getComponent() {
		if (simplePanel) {
			return panel.getViewComponent();
		}
		return panel;
	}

	public CanvasPanel getPanel() {
		return panel;
	}

	public void setCanvas(GraphCanvas canvas) {
		if (getCanvas() != null) {
			logger.warn("Renderer already bound to a canvas.");
			return;
		}
		super.setCanvas(canvas);
		getCanvas().addStatusEventListener(this);
	}

	public void statusEvent(StatusEvent e) {
		switch (e.getType()) {
			case StatusEvent.PUSH:
				statusManager.push(e.getMsg(), e.getIcon());
				break;
			case StatusEvent.POP:
				statusManager.pop();
				break;
			case StatusEvent.SET_DEFAULT_TEXT:
				statusManager.setDefaultText(e.getMsg());
				break;
			case StatusEvent.INITIALIZE_PROGRESS:
				statusManager.initializeProgress(e.getValue());
				break;
			case StatusEvent.SET_PROGRESS:
				statusManager.setProgress(e.getValue());
				break;
			case StatusEvent.STEP_PROGRESS:
				statusManager.stepProgress();
				break;
			case StatusEvent.REMOVE_PROGRESS:
				statusManager.removeProgress();
				break;
			case StatusEvent.ERROR:
				JOptionPane.showMessageDialog(getPanel(), e.getMsg(), "Error",
						JOptionPane.ERROR_MESSAGE);
				break;
			case StatusEvent.WARNING:
				JOptionPane.showMessageDialog(getPanel(), e.getMsg(), "Warning",
						JOptionPane.WARNING_MESSAGE);
				break;
			case StatusEvent.INFO:
				panel.log(Color.DARK_GRAY, e.getMsg());
				break;
			case StatusEvent.DEBUG:
				panel.log(Color.GRAY, e.getMsg());
				break;
			case StatusEvent.OUT:
				panel.log(Color.BLACK, e.getMsg());
				break;
			case StatusEvent.ERR:
				panel.log(Color.RED, e.getMsg());
				break;
			default:
				logger.warn("Unknown status event: " + e);
		}
	}

	public void setView(CanvasView view) {
		super.setView(view);
		if (panel != null) {
			panel.setView((SwingView) view);
			updatePanel();
		}
		logger.debug("Setting view " + getView());
	}

	public SwingView getSwingView() {
		return (SwingView) getView();
	}

	public void dispose() {
		super.dispose();
		getCanvas().removeStatusEventListener(this);
	}

	public void event(EventObject e) {
		if (e instanceof CanvasActionEvent) {
			CanvasActionEvent mie = (CanvasActionEvent) e;
			logger.debug(mie);
			if (mie.getCanvasAction() == close) {
				Frame frame = (Frame) getRootContainer();
				WindowListener[] listeners = frame.getWindowListeners();
				for (int i = 0; i < listeners.length; i++) {
					listeners[i].windowClosing(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
				}
				return;
			}
			else if (mie.getCanvasAction() == about) {
				CoGAbout aboutBean = new CoGAbout(null, true);
				aboutBean.setTextResource("text/graphviewer-about.html");
				aboutBean.setImageResource("images/logos/about-small.png");
				aboutBean.show();
				return;
			}
			else if (mie.getCanvasAction() == export) {
				new ExportDialog(getView());
			}
		}

	}

	public void addMenuItem(CanvasAction item) {
		logger.debug("Adding menu item " + item.toString());
		if (menuItems.contains(item)) {
			logger.warn(item + " already in list");
		}
		menuItems.add(item);
		item.addCanvasActionListener(this);
	}

	public void removeMenuItem(CanvasAction item) {
		if (item == null) {
			logger.warn("Attempting to remove null item");
			return;
		}
		logger.debug("Removing menu item " + item.toString());
		item.removeCanvasActionListener(this);
		menuItems.remove(item);
	}

	public List getMenuItems() {
		return menuItems;
	}

	public CanvasAction getMenuItem(String actionName) {
		Iterator i = getMenuItems().iterator();
		while (i.hasNext()) {
			CanvasAction action = (CanvasAction) i.next();
			if (action.representsAction(actionName)) {
				return action;
			}
		}
		logger.warn("Menu item does not exist: " + actionName);
		return null;
	}

	public void addToolBarItem(CanvasAction item) {
		if (!toolBarItems.contains(item)) {
			logger.debug("Adding toolbar item " + item.toString());
			toolBarItems.add(item);
			item.addCanvasActionListener(this);
		}
	}

	public void removeToolBarItem(CanvasAction item) {
		logger.debug("Removing toolbar item " + item.toString());
		item.removeCanvasActionListener(this);
		toolBarItems.remove(item);
	}

	public List getToolBarItems() {
		return toolBarItems;
	}

	public CanvasAction getToolBarItem(String actionName) {
		Iterator i = getToolBarItems().iterator();
		while (i.hasNext()) {
			CanvasAction action = (CanvasAction) i.next();
			if (action.representsAction(actionName)) {
				return action;
			}
		}
		return null;
	}

	public void canvasActionPerformed(CanvasActionEvent e) {
		EventDispatcher.queue(this, e);
	}

	public void setViewport(Rectangle viewRect) {
		CanvasView view = getView();
		if (view != null) {
			view.setViewport(viewRect);
		}
	}

	public void setLogWindow(LogWindow log) {
		panel.setLogWindow(log);
	}

}
