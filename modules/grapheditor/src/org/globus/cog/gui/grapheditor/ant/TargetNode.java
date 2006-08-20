
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

    
package org.globus.cog.gui.grapheditor.ant;

import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

import javax.swing.JOptionPane;

import org.globus.cog.gui.grapheditor.nodes.NodeComponent;
import org.globus.cog.gui.grapheditor.properties.ComponentProperty;
import org.globus.cog.gui.grapheditor.properties.Property;
import org.globus.cog.gui.grapheditor.util.swing.LogFrame;

/**
 * A target node representing an Ant target. This is the most
 * complicated thing in this package.
 */
public class TargetNode extends TaskNode implements NodeComponent {
	private String deps;
	private boolean executing, top;
	private LogFrame log;
	
	static {
		setClassRendererClass(TargetNode.class, TargetNodeRenderer.class, "swing");
	}

	public TargetNode() {
		super();
		setComponentType("target");
		loadIcon("images/ant-target.png");
		executing = false;
		top = false;
		log = null;
		setCanvasType(TargetCanvas.class);
		addProperty(new ComponentProperty(this, "name"));
		addProperty(new ComponentProperty(this, "depends", Property.RW));
	}

	public void execute() {
		setStatus(new Integer(STATUS_STOPPED));
		ProjectNode project = (ProjectNode) getParent();
		project.executeTarget(getName());
	}

	public void buildFinished(Exception e) {
		top = false;
		if (e != null) {
			setStatus(new Integer(STATUS_FAILED));
			JOptionPane.showMessageDialog(
				null,
				e.getMessage(),
				"Build failed",
				JOptionPane.ERROR_MESSAGE);
		}
		else {
			setStatus(new Integer(STATUS_COMPLETED));
		}
	}

	public String getDepends() {
		return deps;
	}

	public void setDepends(String dependencies) {
		this.deps = dependencies;
	}

	public List getDependencies() {
		if (deps == null) {
			return new LinkedList();
		}
		StringTokenizer st = new StringTokenizer(deps, ",");
		LinkedList ls = new LinkedList();
		while (st.hasMoreTokens()) {
			ls.add(st.nextToken());
		}
		return ls;
	}

	public boolean supportsType(String type) {
		return type.equals("target");
	}
}
