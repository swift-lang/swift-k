
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

public class EPSExportPanel extends LayoutedExportPanel {
	private FileInputControl dir;

	private JTextField indexName;

	public EPSExportPanel() {
	}

	public void setup() {
		super.setup();
		Container c0 = new Container();
		c0.setLayout(new GridLayout(0, 1));
		Container c1 = new Container();
		c1.setLayout(new FlowLayout(FlowLayout.LEFT));
		c1.add(new JLabel("Output file: "));
		String odir = (String) getRootNode().getPropertyValue("postscript.outputdir");
		String ofile = (String) getRootNode().getPropertyValue("postscript.outputfile");
		dir = new FileInputControl(JFileChooser.FILES_ONLY, odir+File.separator+ofile, "Choose EPS file");
		c1.add(dir);
		c0.add(c1);
		add(c0, BorderLayout.CENTER);
	}

	public void export() {
		try {
			RootContainer rootContainer = RendererFactory.newRootContainer("postscript");
			File fdir = new File(dir.getPath());
			NodeComponent rootNode = (NodeComponent) getRootNode().clone();
			rootNode.createCanvas();
			rootNode.getCanvas().setGraph(getView().getGraph());
			rootNode.setPropertyValue("postscript.outputdir", fdir.getParent());
			rootNode.setPropertyValue("postscript.outputfile", fdir.getName());
			rootNode.setPropertyValue("postscript.graphview.layoutengine", getLayoutEngine().getName());
			rootContainer
				.setRootNode(rootNode);
			rootNode.getCanvas().getStatusManager().push("Exporting graph");
			rootContainer.activate();
			rootContainer.run();
			rootNode.getCanvas().getStatusManager().pop();
		}
		catch (RootContainerInstantiationException e) {
			JOptionPane.showMessageDialog(this, "Cannot instantiate PostScript root container", "Error",
				JOptionPane.ERROR_MESSAGE);
		}
	}
}
