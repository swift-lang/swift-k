// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/*
 * Created on Oct 9, 2003
 */
package org.globus.cog.karajan.viewer;

import org.apache.log4j.Logger;
import org.globus.cog.gui.grapheditor.canvas.transformation.GraphTransformation;
import org.globus.cog.gui.grapheditor.canvas.views.layouts.FlowLayout;
import org.globus.cog.gui.grapheditor.canvas.views.layouts.PersistentLayoutEngine2;
import org.globus.cog.gui.grapheditor.generic.RootCanvas;
import org.globus.cog.gui.grapheditor.targets.swing.SwingRootCanvasRenderer;
import org.globus.cog.gui.grapheditor.targets.swing.util.CanvasAction;
import org.globus.cog.gui.grapheditor.targets.swing.util.CanvasActionEvent;
import org.globus.cog.gui.grapheditor.targets.swing.util.CanvasActionListener;
import org.globus.cog.gui.grapheditor.targets.swing.views.GraphView;
import org.globus.cog.gui.grapheditor.targets.swing.views.ListView;
import org.globus.cog.gui.grapheditor.targets.swing.views.TreeView;
import org.globus.cog.karajan.workflow.nodes.ProjectNode;
import org.globus.cog.util.ImageLoader;
import org.globus.cog.util.graph.GraphInterface;
import org.globus.cog.util.graph.Node;
import org.globus.cog.util.graph.NodeIterator;

public class KarajanRootCanvas extends RootCanvas {
	private static Logger logger = Logger.getLogger(KarajanRootCanvas.class);

	public KarajanRootCanvas() {
		setInstanceRendererClass(KarajanRootCanvasRenderer.class);
	}

	public static class KarajanRootCanvasRenderer extends SwingRootCanvasRenderer implements
			CanvasActionListener {
		private CanvasAction run, stop;

		private ProjectNode projectNode;

		private GraphTransformation karajanTransformation;

		public KarajanRootCanvasRenderer() {
			run = new CanvasAction("300#Karajan>10#Start",
					ImageLoader.loadIcon("images/16x16/co/exec.png"), CanvasAction.ACTION_NORMAL);
			stop = new CanvasAction("300#Karajan>11#Stop",
					ImageLoader.loadIcon("images/16x16/co/stop.png"), CanvasAction.ACTION_NORMAL);
			stop.setEnabled(false);
			run.addCanvasActionListener(this);
			stop.addCanvasActionListener(this);
			addToolBarItem(run);
			addToolBarItem(stop);

			getSupportedViews().clear();
			addSupportedView(new GraphView(new PersistentLayoutEngine2(new FlowLayout()),
					"Graph View - Flow Layout"));
			addSupportedView(new ListView());
			addSupportedView(new TreeView());
			// removeMenuItem(getMenuItem("File>Save"));
			// removeMenuItem(getMenuItem("File>Save As..."));
		}
		
		

		public KarajanRootCanvas getKarajanRootCanvas() {
			return (KarajanRootCanvas) getCanvas();
		}

		public void canvasActionPerformed(CanvasActionEvent e) {

			if (e.getCanvasAction().representsAction("File>Load...")) {
				getKarajanFrame().load();
				return;
			}
			else if ((e.getSource() == run) && (e.getType() == CanvasActionEvent.PERFORM)) {
				run.setEnabled(false);
				stop.setEnabled(true);
				getKarajanFrame().start();
			}
			else if ((e.getSource() == stop) && (e.getType() == CanvasActionEvent.PERFORM)) {
				reset();
				getKarajanFrame().stop();
			}
			else {
				super.canvasActionPerformed(e);
			}
		}

		public void reset() {
			getToolBarItem("Karajan>Start").setEnabled(true);
			getToolBarItem("Karajan>Stop").setEnabled(false);
			resetIcons();
		}

		protected void resetIcons() {
			GraphInterface graph = getKarajanFrame().getTransformation().getGraph();
			NodeIterator ni = graph.getNodesIterator();
			while (ni.hasMoreNodes()) {
				Node n = ni.nextNode();
				if (n.getContents() instanceof KarajanNode) {
					KarajanNode kn = (KarajanNode) n.getContents();
					kn.setPropertyValue("status", KarajanNode.STOPPED);
				}
			}
		}

		private KarajanFrame getKarajanFrame() {
			return (KarajanFrame) getCanvas().getOwner().getRootNode().getPropertyValue(
					"karajan.frame");
		}

		public GraphTransformation getKarajanTransformation() {
			return karajanTransformation;
		}

		public void setKarajanTransformation(GraphTransformation karajanTransformation) {
			this.karajanTransformation = karajanTransformation;
			getCanvas().setGraph(karajanTransformation.transform(null));
		}
	}
}