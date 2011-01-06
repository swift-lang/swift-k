
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/*
 *
 * Created on Mar 8, 2004
 *
 */
package org.globus.cog.gui.grapheditor.targets.swing;


import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeSet;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSeparator;
import javax.swing.JToolBar;

import org.apache.log4j.Logger;
import org.globus.cog.gui.grapheditor.targets.swing.util.CanvasAction;
import org.globus.cog.gui.grapheditor.util.swing.LinkedButton;
import org.globus.cog.gui.grapheditor.util.swing.LinkedCheckBoxMenuItem;
import org.globus.cog.gui.grapheditor.util.swing.LinkedMenuItem;
import org.globus.cog.gui.grapheditor.util.swing.LinkedRadioButtonMenuItem;
import org.globus.cog.gui.grapheditor.util.swing.LinkedToggleButton;

public class ActionTree {
	private static Logger logger = Logger.getLogger(ActionTree.class);
	
	private static Icon EMPTY_ICON = createEmptyIcon(16, 16);
	private static Icon GAP_NORMAL;
	private static Icon GAP_CHECK;
	private static Icon GAP_RADIO;
	
	static {
		JMenuItem test1 = new JMenuItem("-");
		JMenuItem test2 = new JMenuItem("-");
		test2.setIcon(EMPTY_ICON);
		JCheckBoxMenuItem test3 = new JCheckBoxMenuItem("-");
		JRadioButtonMenuItem test4 = new JRadioButtonMenuItem("-");
		int gapNormal = test2.getPreferredSize().width - test1.getPreferredSize().width - 16;
		logger.debug("gapNormal="+gapNormal);
		int gapCheck = test2.getPreferredSize().width - test3.getPreferredSize().width - gapNormal;
		logger.debug("gapCheck="+gapCheck);
		int gapRadio = test2.getPreferredSize().width - test4.getPreferredSize().width - gapNormal;
		logger.debug("gapRadio="+gapRadio);
		GAP_NORMAL = EMPTY_ICON;
		GAP_CHECK = createEmptyIcon(gapCheck, 16);
		GAP_RADIO = createEmptyIcon(gapRadio, 16);
	}
	
	private static Icon createEmptyIcon(int width, int height) {
		BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		return new ImageIcon(bi);
	}

	private CanvasAction action;

	private TreeSet children;

	private String name;

	public int rank;

	public ActionTree() {
		children = new TreeSet(new MenuTreeComparator());
	}

	public ActionTree(String name, int rank, CanvasAction menuItem) {
		this();
		this.name = name;
		this.rank = rank;
		this.action = menuItem;
	}

	public void addBranch(RankedName rn) {
		addBranch(rn, 0);
	}

	public void addBranch(RankedName rn, int start) {
		if (start >= rn.ranks.length) {
			return;
		}
		ActionTree mt = findNamed(rn.names[start]);
		if (mt == null) {
			mt = new ActionTree(rn.names[start], rn.ranks[start], rn.menuItem);
			children.add(mt);
		}
		else {
			if (rn.ranks[start] != -1) {
				children.remove(mt);
				mt.rank = rn.ranks[start];
				children.add(mt);
			}
		}
		mt.addBranch(rn, start + 1);
	}

	public void buildMenu(JComponent menu, Map itemMap, ActionListener l) {
		buildMenu(menu, itemMap, l, null);
	}

	public void buildMenu(JComponent menu, Map itemMap, ActionListener l, ButtonGroup group) {
		buildMenu(menu, itemMap, l, group, false);
	}

	public void buildMenu(JComponent menu, Map itemMap, ActionListener l, ButtonGroup group,
		boolean icons) {
		if (children.size() == 0) {
			JMenuItem jMenuItem = null;
			int type = action.getType() & 0x07;
			int modifiers = action.getType() & 0xf8;
			if ((modifiers & CanvasAction.SEPARATOR_BEFORE) != 0) {
				menu.add(new JSeparator());
			}
			if (type == CanvasAction.ACTION_SWITCH) {
				jMenuItem = new LinkedCheckBoxMenuItem(name, action);
				jMenuItem.setIcon(GAP_CHECK);
			}
			else if (type == CanvasAction.ACTION_SELECTOR) {
				jMenuItem = new LinkedRadioButtonMenuItem(name, action);
				if (group != null) {
					group.add(jMenuItem);
				}
				jMenuItem.setSelected(action.isSelected());
				jMenuItem.setIcon(GAP_RADIO);
			}
			else if (type == CanvasAction.SEPARATOR) {
				menu.add(new JSeparator());
			}
			else {
				jMenuItem = new LinkedMenuItem(name, action);
				if (icons) {
					if (action.getIcon() != null) {
						jMenuItem.setIcon(action.getIcon());
					}
					else {
						jMenuItem.setIcon(GAP_NORMAL);
					}
				}
			}
			if (type != CanvasAction.SEPARATOR) {
				jMenuItem.addActionListener(l);
				if (action.isSelected()) {
					jMenuItem.setSelected(true);
				}
				if (!action.isEnabled()) {
					jMenuItem.setEnabled(false);
				}
				menu.add(jMenuItem);
				jMenuItem.setPreferredSize(new Dimension(jMenuItem.getPreferredSize().width, 20));

				if ((modifiers & CanvasAction.SEPARATOR_AFTER) != 0) {
					menu.add(new JSeparator());
				}
				itemMap.put(jMenuItem, action);
			}
		}
		else {
			if (name != null) {
				JMenu sMenu = new JMenu(name);
				if (icons) {
					if (action.getIcon() != null) {
						sMenu.setIcon(action.getIcon());
					}
					else {
						sMenu.setIcon(GAP_NORMAL);
					}
				}
				menu.add(sMenu);
				menu = sMenu;
			}
			ButtonGroup grp = new ButtonGroup();
			boolean sicons = false;
			Iterator i = children.iterator();
			while (i.hasNext()) {
				ActionTree mt = (ActionTree) i.next();
				if (mt.children.size() == 0) {
					if ((mt.action.getIcon() != null)
						|| (mt.action.getType() == CanvasAction.ACTION_SELECTOR)
						|| (mt.action.getType() == CanvasAction.ACTION_SWITCH)) {
						sicons = true;
						break;
					}
				}
			}
			i = children.iterator();

			while (i.hasNext()) {
				ActionTree mt = (ActionTree) i.next();
				mt.buildMenu(menu, itemMap, l, grp, sicons);
			}
			if (!(menu instanceof JMenuBar)) {
				menu.setPreferredSize(new Dimension(menu.getPreferredSize().width, 20));
			}
		}
	}

	public void buildToolBar(Container toolBar, HashMap itemMap, ActionListener l, ButtonGroup grp) {
		if (children.size() == 0) {
			AbstractButton button;
			if (action.getType() == CanvasAction.SEPARATOR) {
				toolBar.add(new JToolBar.Separator());
			}
			else {
				if (action.getType() == CanvasAction.ACTION_SELECTOR) {
					button = new LinkedToggleButton(name, action);
					grp.add(button);
				}
				else {
					button = new LinkedButton(name, action);
				}
				if (action.getIcon() != null) {
					button.setIcon(action.getIcon());
				}
				if (!action.isEnabled()) {
					button.setEnabled(false);
				}
				button.addActionListener(l);
				toolBar.add(button);
				itemMap.put(button, action);
			}
		}
		else {
			ButtonGroup ngrp = new ButtonGroup();
			Iterator i = children.iterator();
			while (i.hasNext()) {
				ActionTree mt = (ActionTree) i.next();
				mt.buildToolBar(toolBar, itemMap, l, ngrp);
			}
			toolBar.add(new JToolBar.Separator());
		}
	}

	public ActionTree findNamed(String name) {
		Iterator i = children.iterator();
		while (i.hasNext()) {
			ActionTree mt = (ActionTree) i.next();
			if (mt.name == null) {
				continue;
			}
			if (mt.name.equals(name)) {
				return mt;
			}
		}
		return null;
	}
}