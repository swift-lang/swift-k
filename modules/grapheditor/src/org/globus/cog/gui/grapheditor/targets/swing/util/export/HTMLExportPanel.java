
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/*
 *
 * Created on Mar 4, 2004
 *
 */
package org.globus.cog.gui.grapheditor.targets.swing.util.export;


import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.GridLayout;

import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import org.globus.cog.gui.grapheditor.RendererFactory;
import org.globus.cog.gui.grapheditor.RootContainer;
import org.globus.cog.gui.grapheditor.RootContainerInstantiationException;
import org.globus.cog.gui.grapheditor.nodes.NodeComponent;
import org.globus.cog.gui.grapheditor.util.swing.FileInputControl;

public class HTMLExportPanel extends LayoutedExportPanel {
	private FileInputControl dir;

	private JTextField indexName;

	public HTMLExportPanel() {
	}

	public void setup() {
		super.setup();
		Container c0 = new Container();
		c0.setLayout(new GridLayout(0, 1));
		Container c1 = new Container();
		c1.setLayout(new FlowLayout(FlowLayout.LEFT));
		c1.add(new JLabel("Output directory: "));
		dir = new FileInputControl(JFileChooser.DIRECTORIES_ONLY, (String) getRootNode().getPropertyValue("html.outputdir"), "Choose HTML output directory");
		c1.add(dir);
		c0.add(c1);
		add(c0, BorderLayout.CENTER);
	}

	public void export() {
		try {
			RootContainer rootContainer = RendererFactory.newRootContainer("html");
			NodeComponent rootNode = (NodeComponent) getRootNode().clone();
			rootNode.createCanvas();
			rootNode.getCanvas().setGraph(getView().getGraph());
			rootNode.setPropertyValue("html.outputdir", dir.getPath());
			rootNode.setPropertyValue("html.graphview.layoutengine", getLayoutEngine().getName());
			rootContainer
				.setRootNode(rootNode);
			rootNode.getCanvas().getStatusManager().push("Exporting graph");
			rootContainer.activate();
			rootContainer.run();
			rootNode.getCanvas().getStatusManager().pop();
		}
		catch (RootContainerInstantiationException e) {
			JOptionPane.showMessageDialog(this, "Cannot instantiate HTML root container", "Error",
				JOptionPane.ERROR_MESSAGE);
		}
	}
}
