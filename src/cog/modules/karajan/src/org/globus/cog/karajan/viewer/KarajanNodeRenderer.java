// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/*
 * 
 * Created on Mar 22, 2004
 *  
 */
package org.globus.cog.karajan.viewer;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.util.EventObject;
import java.util.LinkedList;
import java.util.List;

import javax.swing.ImageIcon;

import org.globus.cog.gui.grapheditor.GraphComponent;
import org.globus.cog.gui.grapheditor.properties.PropertyHolder;
import org.globus.cog.gui.grapheditor.targets.swing.SwingNodeRenderer;
import org.globus.cog.gui.grapheditor.util.swing.ComponentAction;
import org.globus.cog.gui.grapheditor.util.tables.NodePropertiesEditor;

public class KarajanNodeRenderer extends SwingNodeRenderer {
	private static Color PROGRESS_COLOR = new Color(90, 160, 255);

	private ComponentAction resume, breakpoint;

	private List overlays;

	private long total;

	private JLabelWithProgress label;
	
	private ImageIcon aborted;

	public KarajanNodeRenderer() {
		overlays = new LinkedList();
		breakpoint = new ComponentAction("Set Breakpoint");
		breakpoint.addActionListener(this);
		addAction(breakpoint);
		resume = new ComponentAction("Resume Execution");
		resume.addActionListener(this);
	}

	public void setComponent(GraphComponent component) {
		if (component.hasProperty("show progress indicator")) {
			total = -1;
			label = new JLabelWithProgress();
			label.setProgressColor(PROGRESS_COLOR);
			label.setProgressBorderColor(Color.DARK_GRAY);
			label.setProgressHorizontalSize(0.4);
			label.setProgressVerticalSize(0.15);
			label.setProgressRange(100);
			label.setSize(label.getPreferredSize());
			setVisualComponent(label);
			setLabel(label);
			if ((component.getPropertyValue(KarajanNode.TOTAL) != null)
					&& (component.getPropertyValue(KarajanNode.CURRENT) != null)) {
				label.setProgressVisible(true);
				updateProgress(component);
			}
		}
		super.setComponent(component);
	}

	public void event(EventObject e) {
		if (e instanceof ActionEvent) {
			ActionEvent ae = (ActionEvent) e;
			if (ae.getSource() == breakpoint) {
				if (getKarajanNode().hasBreakpoint()) {
					getComponent().setPropertyValue("breakpoint", Boolean.FALSE);
				}
				else {
					getComponent().setPropertyValue("breakpoint", Boolean.TRUE);
				}
				return;
			}
			if (ae.getSource() == getAction("Properties")) {
				NodePropertiesEditor pe = new NodePropertiesEditor(getComponent());
				pe.setFilter("karajan.([^_].*)|status");
				pe.show();
				return;
			}
			if (ae.getSource() == resume) {
				removeAction(resume);
			}
		}
		super.event(e);
	}

	public KarajanNode getKarajanNode() {
		return (KarajanNode) getComponent();
	}

	public void propertyChange(PropertyChangeEvent e) {
		if (e.getPropertyName().equals("breakpoint")) {
			if (Boolean.TRUE.equals(e.getNewValue())) {
				breakpoint.setName("Remove Breakpoint");
			}
			else {
				breakpoint.setName("Set Breakpoint");
			}
		}
		else if (e.getPropertyName().equals("status")) {
			Integer status = (Integer) e.getNewValue();
			if (status.intValue() == KarajanNode.STATUS_SUSPENDED) {
				addAction(resume);
			}
			Integer oldStatus = (Integer) e.getOldValue();
			if (oldStatus.intValue() == KarajanNode.STATUS_SUSPENDED) {
				removeAction(resume);
			}
			if (status.intValue() == KarajanNode.STATUS_COMPLETED) {
				if (label != null) {
					updateProgress(getComponent());
				}
			}
		}
		if (e.getPropertyName().equals(KarajanNode.TOTAL)) {
			if (e.getNewValue() != null) {
				total = ((Long) e.getNewValue()).longValue();
				label.setProgressVisible(true);
			}
		}
		else if (e.getPropertyName().equals(KarajanNode.CURRENT)) {
			if (e.getNewValue() != null) {
				updateProgress(getComponent());
			}
		}
		super.propertyChange(e);
	}

	protected void updateProgress(PropertyHolder p) {
		if (new Integer(KarajanNode.STATUS_COMPLETED).equals(p.getPropertyValue("status"))) {
			label.setProgressColor(Color.GREEN);
			label.setProgressValue(label.getProgressRange());
		}
		else {
			total = ((Long) p.getPropertyValue(KarajanNode.TOTAL)).longValue();
			long crt = ((Long) p.getPropertyValue(KarajanNode.CURRENT)).longValue();
			crt = crt * 100 / total;
			label.setProgressColor(Color.getHSBColor((float) crt / 300, (float) 1.0, (float) 1.0));
			label.setProgressValue((int) crt);
		}
	}
}