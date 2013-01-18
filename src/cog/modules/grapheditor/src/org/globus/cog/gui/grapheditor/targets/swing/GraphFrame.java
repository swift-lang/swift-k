
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------


package org.globus.cog.gui.grapheditor.targets.swing;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.apache.log4j.Logger;
import org.globus.cog.gui.grapheditor.RendererFactory;
import org.globus.cog.gui.grapheditor.RootContainer;
import org.globus.cog.gui.grapheditor.canvas.AbstractCanvas;
import org.globus.cog.gui.grapheditor.generic.RootCanvas;
import org.globus.cog.gui.grapheditor.generic.RootContainerHelper;
import org.globus.cog.gui.grapheditor.nodes.NodeComponent;

public class GraphFrame extends CanvasFrame implements RootContainer{
	private static Logger logger = Logger.getLogger(GraphFrame.class);
	
	private HashMap properties;

	private String fileName;
	private String lastDir;

	public GraphFrame() {
		init();
	}

	
	public GraphFrame(NodeComponent p) {
		super(p);
		init();
		getCanvasRenderer().setRootContainer(this);
	}
	
	private void init() {
		properties = new HashMap();
		System.setProperty("swing.plaf.metal.controlFont", "Arial");
		System.setProperty("swing.plaf.metal.systemFont", "Arial");
		System.setProperty("swing.plaf.metal.userFont", "Arial");
		System.setProperty("swing.plaf.metal.smallFont", "Arial");
		RendererFactory.setCurrentTarget("swing");
		RendererFactory.addClassRenderer(AbstractCanvas.class, "swing", SwingCanvasRenderer.class);
		RendererFactory.addClassRenderer(RootCanvas.class, "swing", SwingRootCanvasRenderer.class);
	}
	
	public void setRootNode(NodeComponent node) {
		super.setNode(node);
		getCanvasRenderer().setRootContainer(this);
	}
	
	public NodeComponent getRootNode() {
		return getNode();
	}
	
	public void load(String fname) {
		if (fname != null) {
			getCanvas().setEventsActive(false);
			getSwingCanvasRenderer().getSwingView().disable();
			getCanvas().getGraph().clear();
			this.fileName = fname;
			this.lastDir = new File(this.fileName).getAbsoluteFile().getParent();
			try {
				RootContainerHelper.load(this.fileName, getCanvas());
			}
			catch (FileNotFoundException e1) {
				getCanvas().getStatusManager().error("File not found: "+fileName, e1);
			}
			catch (Exception e) {
				getCanvas().getStatusManager().error("Error while loading graph: "+e.getMessage(), e);
			}
			getSwingCanvasRenderer().getSwingView().enable();
			getCanvas().setEventsActive(true);
			getCanvas().invalidate();
		}
	}

	public void load() {
		String f = fileChooser(true);
		if (f != null) {
			load(f);
		}
	}
	
	public void save(String fileName) {
		try {
			File file = new File(fileName);
			boolean overwrite = true;
			if (file.exists()) {
				int choice =
					JOptionPane.showConfirmDialog(
							null,
							"Overwrite existing " + fileName + " file?",
							"File exists",
							JOptionPane.YES_NO_OPTION,
							JOptionPane.WARNING_MESSAGE);
				if (choice == JOptionPane.NO_OPTION) {
					overwrite = false;
				}
			}
			if (overwrite) {
				RootContainerHelper.save(fileName, getCanvas());
			}
		}
		catch (Exception e) {
			logger.error("Exception caught while saving file:");
			logger.error(e);
		}
	}

	public void save() {
		if (fileName == null) {
			saveAs();
		}
		else {
			save(fileName);
		}
	}

	public void saveAs() {
		String f = fileChooser(false);
		if (f != null) {
			fileName = f;
			save(fileName);
		}
	}

	private String fileChooser(boolean open) {
		if (lastDir == null) {
			lastDir = new File(".").getAbsoluteFile().getParent();
		}
		JFileChooser JF = new JFileChooser(lastDir);
		JF.setFileSelectionMode(JFileChooser.FILES_ONLY);
		int ret;
		if (open) {
			ret = JF.showOpenDialog(null);
		}
		else {
			ret = JF.showSaveDialog(null);
		}
		if (ret == JFileChooser.APPROVE_OPTION) {
			return JF.getSelectedFile().getAbsolutePath();
		}
		return null;
	}
}
