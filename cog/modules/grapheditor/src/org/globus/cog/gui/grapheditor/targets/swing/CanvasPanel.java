// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.gui.grapheditor.targets.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.JViewport;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.log4j.Logger;
import org.globus.cog.gui.grapheditor.canvas.LogConsole;
import org.globus.cog.gui.grapheditor.canvas.views.CanvasView;
import org.globus.cog.gui.grapheditor.targets.swing.util.CanvasAction;
import org.globus.cog.gui.grapheditor.targets.swing.util.CanvasActionEvent;
import org.globus.cog.gui.grapheditor.targets.swing.util.CanvasActionListener;
import org.globus.cog.gui.grapheditor.targets.swing.views.SwingView;
import org.globus.cog.gui.grapheditor.util.EventConsumer;
import org.globus.cog.gui.grapheditor.util.EventDispatcher;
import org.globus.cog.gui.grapheditor.util.StatusRenderer;
import org.globus.cog.gui.grapheditor.util.swing.RepaintMonitoringContainer;

/**
 * This class represents a typical panel that contains a graph canvas. It adds
 * the menu from the canvas, the toolbar, implements the logic for switching
 * views, and adds the actual component of the canvas.
 */
public class CanvasPanel extends JPanel implements ChangeListener, ActionListener, StatusRenderer,
		CanvasActionListener, EventConsumer, ComponentListener {
	private static Logger logger = Logger.getLogger(CanvasPanel.class);
	private RepaintMonitoringContainer main;
	private Component viewComponent;
	private JMenuBar menuBar;
	private HashMap menuItems, toolBarItems;
	private JProgressBar progressBar;
	private SwingCanvasRenderer renderer;
	private JPanel sbPanel, progressPanel, top, notsotop;
	private JScrollPane sp;
	private JLabel statusBar;
	private JToolBar toolBar;
	private JButton log;
	private HashMap viewsItemMap;
	private LogConsole logConsole;

	/**
	 * Creates a new CanvasPanel
	 * 
	 * @param canvas
	 *            the canvas to create the panel for
	 * @param toolbarsandstuff
	 *            if this is set to true, the panel will also display a menubar,
	 *            toolbar, status bar, etc.
	 */
	public CanvasPanel(SwingCanvasRenderer renderer) {
		setLayout(new BorderLayout());
		setRenderer(renderer);
		top = new JPanel(new BorderLayout());
		notsotop = new JPanel(new BorderLayout());
		toolBar = new JToolBar();
		toolBar.setFloatable(false);
		toolBar.setPreferredSize(new Dimension(120, 32));
		top.add(notsotop, BorderLayout.CENTER);
		menuBar = new JMenuBar();
		top.add(menuBar, BorderLayout.NORTH);
		add(top, BorderLayout.NORTH);
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		main = new RepaintMonitoringContainer();
		main.setLayout(new BorderLayout());
		sp = new JScrollPane(main);
		sp.setAutoscrolls(true);
		sp.setDoubleBuffered(true);
		sp.getViewport().addChangeListener(this);
		panel.add(sp, BorderLayout.CENTER);
		add(panel, BorderLayout.CENTER);
		statusBar = new JLabel();
		statusBar.setPreferredSize(new Dimension(1200, 20));
		sbPanel = new JPanel(new BorderLayout());
		sbPanel.setPreferredSize(new Dimension(1400, 24));
		sbPanel.add(statusBar, BorderLayout.CENTER);
		progressPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 5));
		sbPanel.add(progressPanel, BorderLayout.EAST);
		log = new JButton("Log");
		JPanel logsp = new JPanel();
		logsp.setLayout(new BorderLayout());
		logsp.add(sbPanel, BorderLayout.CENTER);
		logsp.add(log, BorderLayout.EAST);
		log.setVisible(false);
		log.addActionListener(this);
		add(logsp, BorderLayout.SOUTH);
	}

	public void actionPerformed(ActionEvent e) {
		EventDispatcher.queue(this, e);
	}

	public void event(EventObject e) {
		if (menuItems.containsKey(e.getSource())) {
			CanvasAction menuItem = (CanvasAction) menuItems.get(e.getSource());
			menuItem.perform();
		}
		else if (toolBarItems.containsKey(e.getSource())) {
			CanvasAction toolBarItem = (CanvasAction) toolBarItems.get(e.getSource());
			toolBarItem.perform();
		}
		else if (e.getSource() == log) {
			if (logConsole != null) {
				logConsole.setVisible(!logConsole.isVisible());
			}
		}
	}

	public SwingCanvasRenderer getDRenderer() {
		return getRenderer();
	}

	public SwingCanvasRenderer getRenderer() {
		return renderer;
	}

	public JToolBar getToolBar() {
		return toolBar;
	}

	public SwingView getView() {
		return renderer.getSwingView();
	}

	/**
	 * Returns the current viewport
	 * 
	 * @return
	 */
	public JViewport getViewport() {
		return sp.getViewport();
	}

	/**
	 * Returns the visible rectangle in the current view.
	 * 
	 * @return
	 */
	public Rectangle getVisibleArea() {
		return sp.getViewport().getViewRect();
	}

	/**
	 * Advances one step in the progress indicator
	 */
	public void incrementProgress() {
		if (progressBar != null) {
			progressBar.setValue(progressBar.getValue() + 1);
			progressBar.repaint();
		}
	}

	/**
	 * Activates a status bar progress indicator which has <code>size</code>
	 * steps. If <code>steps</code> is set to 0 (zero) an indeterminate
	 * progress indicator will be used
	 * 
	 * @param size
	 */
	public void initializeProgress(int size) {
		if (progressBar != null) {
			removeProgress();
		}
		if (size != 0) {
			progressBar = new JProgressBar(0, size);
		}
		else {
			progressBar = new JProgressBar(0, 16);
			progressBar.setIndeterminate(true);
		}
		progressBar.setPreferredSize(new Dimension(200, 10));
		progressPanel.add(progressBar);
		progressPanel.revalidate();
		progressPanel.repaint();
	}

	public void listViewChanged(SwingView view) {
		getRenderer().setView(view);
	}

	/**
	 * Hides the current progress bar indicator
	 */
	public void removeProgress() {
		if (progressBar != null) {
			progressPanel.remove(progressBar);
			progressPanel.revalidate();
			progressPanel.repaint();
			progressBar = null;
		}
	}

	/**
	 * Sets the progress for an initialized progress indicator
	 * 
	 * @param progress
	 */
	public void setProgress(int progress) {
		if (progressBar != null) {
			progressBar.setValue(progress);
		}
	}

	public void setRenderer(SwingCanvasRenderer renderer) {
		this.renderer = renderer;
	}

	public void setSelectedView(CanvasView view) {
		Iterator i = viewsItemMap.keySet().iterator();
		while (i.hasNext()) {
			CanvasAction action = (CanvasAction) i.next();
			if (view == viewsItemMap.get(action)) {
				action.setSelected(true);
			}
		}
	}

	/**
	 * sets the icon to be displayed in the leftmost side of the status bar
	 * 
	 * @param icon
	 */
	public void setStatusIcon(Icon icon) {
		statusBar.setIcon(icon);
		sbPanel.repaint();
	}

	/**
	 * Sets the text for the status bar
	 * 
	 * @param text
	 *            the text to be displayed in the status bar
	 */
	public void setStatusText(String text) {
		statusBar.setText(text);
		sbPanel.repaint();
	}

	public void setToolBar(JToolBar toolBar) {
		this.toolBar = toolBar;
	}

	public void setView(SwingView view) {
		main.removeAll();
		if (viewComponent != null) {
			viewComponent.removeComponentListener(this);
		}
		getDRenderer().getView().setViewport(sp.getVisibleRect());
		viewComponent = getRenderer().getSwingView().getComponent();
		viewComponent.addComponentListener(this);
		viewComponent.doLayout();
		main.add(viewComponent, BorderLayout.CENTER);
		if (viewComponent instanceof JTable) {
			sp.setColumnHeaderView(((JTable) viewComponent).getTableHeader());
		}
		else {
			sp.setColumnHeaderView(null);
		}
		main.invalidate();
		main.validate();
		logger.debug("Adding view component " + getRenderer().getSwingView().getComponent());
		new Invalidator(getRenderer().getView()).start();
	}

	/**
	 * Sets the origin of the visible area in the current view, allowing
	 * programatic scrolling
	 * 
	 * @param p
	 */
	public void setVisibleAreaOrigin(Point p) {
		sp.getViewport().setViewPosition(p);
	}

	public void stateChanged(ChangeEvent e) {
		getRenderer().setViewport(sp.getViewport().getViewRect());
	}

	public void updateMenuBar() {
		if (getDRenderer().getMenuItems().size() == 0) {
			return;
		}
		ActionTree mt = new ActionTree();
		Iterator i = getDRenderer().getMenuItems().iterator();
		while (i.hasNext()) {
			CanvasAction mi = (CanvasAction) i.next();
			mt.addBranch(new RankedName(mi));
		}
		menuItems = new HashMap();
		List menus = new ArrayList();
		for (int j = 0; j < menuBar.getMenuCount(); j++) {
			menus.add(menuBar.getMenu(j).getText());
		}
		menuBar.removeAll();
		mt.buildMenu(menuBar, menuItems, this);
		boolean changed = false;
		if (menus.size() != menuBar.getMenuCount()) {
			changed = true;
		}
		else {
			for (int j = 0; j < menuBar.getMenuCount(); j++) {
				if (!menuBar.getMenu(j).getText().equals(menus.get(j))) {
					changed = true;
					break;
				}
			}
		}
		menuBar.validate();
	}

	public void updateToolBar() {
		if (getDRenderer().getToolBarItems().size() == 0) {
			notsotop.remove(toolBar);
			return;
		}
		ActionTree mt = new ActionTree();
		Iterator i = getDRenderer().getToolBarItems().iterator();
		while (i.hasNext()) {
			CanvasAction mi = (CanvasAction) i.next();
			mt.addBranch(new RankedName(mi));
		}
		if (toolBarItems != null) {
			Iterator j = toolBarItems.keySet().iterator();
			while (j.hasNext()) {
				((AbstractButton) j.next()).removeActionListener(this);
			}
		}
		toolBarItems = new HashMap();
		toolBar.removeAll();
		mt.buildToolBar(toolBar, toolBarItems, this, null);
		notsotop.add(toolBar, BorderLayout.CENTER);
		toolBar.validate();
		logger.debug("Toolbar updated");
	}

	public void updateViewList() {
		if (viewsItemMap != null) {
			Iterator i = viewsItemMap.keySet().iterator();
			while (i.hasNext()) {
				CanvasAction action = (CanvasAction) i.next();
				action.removeCanvasActionListener(this);
				getRenderer().removeMenuItem(action);
			}
		}
		viewsItemMap = new HashMap();
		Iterator i = getRenderer().getSupportedViews().iterator();
		int crtindex = 0;
		while (i.hasNext()) {
			CanvasView view = (CanvasView) i.next();
			CanvasAction action = new CanvasAction("10#View>1#Views>" + crtindex + "#"
					+ view.getName(), CanvasAction.ACTION_SELECTOR);
			if (view == getView()) {
				action.setSelectedQuiet(true);
			}
			action.addCanvasActionListener(this);
			if (getRenderer().getView() != null) {
				if (view.getName().equals(getRenderer().getView().getName())) {
					action.setSelected(true);
				}
			}
			this.getRenderer().addMenuItem(action);
			viewsItemMap.put(action, view);
			crtindex++;
		}
	}

	public RepaintMonitoringContainer getMain() {
		return main;
	}

	public Component getViewComponent() {
		return viewComponent;
	}

	public void canvasActionPerformed(CanvasActionEvent e) {
		if (viewsItemMap.containsKey(e.getSource())) {
			listViewChanged((SwingView) viewsItemMap.get(e.getSource()));
		}
	}

	public void componentResized(ComponentEvent e) {
	}

	public void componentMoved(ComponentEvent e) {
	}

	public void componentShown(ComponentEvent e) {
	}

	public void componentHidden(ComponentEvent e) {
	}

	public void setLogWindow(LogWindow log) {
		this.logConsole = log;
		if (log == null) {
			this.log.setVisible(false);
		}
		else {
			this.log.setVisible(true);
		}
	}

	public void log(Color color, String msg) {
		if (logConsole != null) {
			logConsole.output(color, msg);
		}
	}
}
