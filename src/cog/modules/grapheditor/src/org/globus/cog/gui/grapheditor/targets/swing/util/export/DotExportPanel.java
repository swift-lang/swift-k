
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
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import org.globus.cog.gui.grapheditor.RendererFactory;
import org.globus.cog.gui.grapheditor.RootContainer;
import org.globus.cog.gui.grapheditor.RootContainerInstantiationException;
import org.globus.cog.gui.grapheditor.nodes.NodeComponent;
import org.globus.cog.gui.grapheditor.util.swing.FileInputControl;

public class DotExportPanel extends ExportPanel {
	private FileInputControl dir;

	private JTextField indexName;

	public DotExportPanel() {
	}

	public void setup() {
		Container c0 = new Container();
		c0.setLayout(new GridLayout(0, 1));
		Container c1 = new Container();
		c1.setLayout(new FlowLayout(FlowLayout.LEFT));
		c1.add(new JLabel("Output file: "));
		String odir = (String) getRootNode().getPropertyValue("dot.outputdir");
		String ofile = (String) getRootNode().getPropertyValue("dot.outputfile");
		dir = new FileInputControl(JFileChooser.FILES_ONLY, odir+File.separator+ofile, "Choose Dot file");
		c1.add(dir);
		c0.add(c1);
		add(c0, BorderLayout.CENTER);
	}

	public void export() {
		try {
			RootContainer rootContainer = RendererFactory.newRootContainer("dot");
			File fdir = new File(dir.getPath());
			NodeComponent rootNode = (NodeComponent) getRootNode().clone();
			rootNode.createCanvas();
			rootNode.getCanvas().setGraph(getView().getGraph());
			rootNode.setPropertyValue("dot.outputdir", fdir.getParent());
			rootNode.setPropertyValue("dot.outputfile", fdir.getName());
			rootContainer
				.setRootNode(rootNode);
			rootNode.getCanvas().getStatusManager().push("Exporting graph");
			rootContainer.activate();
			rootContainer.run();
			rootNode.getCanvas().getStatusManager().pop();
		}
		catch (RootContainerInstantiationException e) {
			JOptionPane.showMessageDialog(this, "Cannot instantiate Dot root container", "Error",
				JOptionPane.ERROR_MESSAGE);
		}
	}
}
