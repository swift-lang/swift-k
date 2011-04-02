// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/*
 * Created on Oct 8, 2003
 */
package org.globus.cog.karajan.viewer;

import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Iterator;

import javax.swing.JOptionPane;

import org.apache.log4j.Logger;
import org.globus.cog.gui.grapheditor.Launcher;
import org.globus.cog.gui.grapheditor.canvas.GraphCanvas;
import org.globus.cog.gui.grapheditor.generic.GenericNode;
import org.globus.cog.gui.grapheditor.nodes.NodeComponent;
import org.globus.cog.gui.grapheditor.properties.OverlayedProperty;
import org.globus.cog.gui.grapheditor.properties.Property;
import org.globus.cog.gui.grapheditor.targets.swing.GraphFrame;
import org.globus.cog.karajan.Loader;
import org.globus.cog.karajan.util.ThreadingContext;
import org.globus.cog.karajan.util.serialization.XMLConverter;
import org.globus.cog.karajan.viewer.KarajanRootCanvas.KarajanRootCanvasRenderer;
import org.globus.cog.karajan.workflow.ElementTree;
import org.globus.cog.karajan.workflow.events.Event;
import org.globus.cog.karajan.workflow.events.EventBus;
import org.globus.cog.karajan.workflow.events.EventListener;
import org.globus.cog.karajan.workflow.events.FailureNotificationEvent;
import org.globus.cog.karajan.workflow.events.FlowEvent;
import org.globus.cog.karajan.workflow.events.ProgressMonitoringEvent;
import org.globus.cog.karajan.workflow.events.StatusMonitoringEvent;
import org.globus.cog.karajan.workflow.nodes.FlowElement;
import org.globus.cog.util.ArgumentParser;
import org.globus.cog.util.ArgumentParserException;
import org.globus.cog.util.graph.Graph;

public class KarajanFrame extends GraphFrame implements EventListener {
	private static final long serialVersionUID = -898074882447582424L;

	private static Logger logger = Logger.getLogger(KarajanFrame.class);

	private Hashtable actions;

	private Hook hook;

	private ElementTree tree;

	private KarajanGraphTransformation transformation;

	private ViewerExecutionContext ec;

	private boolean done;

	public KarajanFrame(NodeComponent node) {
		super(node);
		actions = new Hashtable();
		hook = new Hook();
		getRootNode().setPropertyValue("karajan.frame", this);
		setAutoTitle(false);
		setTitle("Java CoG Kit - Karajan Desktop");
	}

	public void breakpointReached(ThreadedUID el, FlowEvent event) {
		Hashtable threads = (Hashtable) transformation.getMap().get(el.getElement());
		KarajanNode gn = (KarajanNode) threads.get(el.getThread());
		gn.addOverlay("images/karajan/suspended-overlay.png");
		JOptionPane.showMessageDialog(null, "Breakpoint reached", "Breakpoint",
				JOptionPane.INFORMATION_MESSAGE);
		gn.setPropertyValue("status", new Integer(KarajanNode.STATUS_SUSPENDED));
	}

	public void resume(KarajanNode node) {
		node.removeOverlay("images/karajan/suspended-overlay.png");
		hook.resumeElement((ThreadedUID) transformation.getRmap().get(node));
	}

	private void createGraph() {
		getCanvas().getGraph().clear();
		KarajanNode node = makeNode(tree.getRoot());
		getCanvas().getGraph().addNode(node);
		subGraph(node, tree.getRoot());
	}

	public void event(Event e) {
		if (e instanceof StatusMonitoringEvent) {
			statusMonitoringEvent((StatusMonitoringEvent) e);
		}
		else if (e instanceof ProgressMonitoringEvent) {
			progressMonitoringEvent((ProgressMonitoringEvent) e);
		}
	}

	public void failed(FailureNotificationEvent e) {
		EventBus.setEventHook(null);
		hook.shutdown();
		JOptionPane.showMessageDialog(null, "Execution failed with the following message: "
				+ e.getMessage(), "Execution Failed", JOptionPane.ERROR_MESSAGE);
		reset();
	}

	public void completed() {
		done = true;
		EventBus.setEventHook(null);
		hook.shutdown();
		JOptionPane.showMessageDialog(null, "Execution completed successfully", "Completed",
				JOptionPane.INFORMATION_MESSAGE);
		reset();
	}

	public void statusMonitoringEvent(StatusMonitoringEvent e) {
		NodeComponent node = getNode(e.getFlowElement(), e.getThread());
		if (node != null) {
			if (logger.isDebugEnabled()) {
				logger.debug(e);
			}
			if (e.getType() == StatusMonitoringEvent.EXECUTION_STARTED) {
				node.setPropertyValue("status", new Integer(GenericNode.STATUS_RUNNING));
			}
			else if (e.getType() == StatusMonitoringEvent.EXECUTION_COMPLETED) {
				node.setPropertyValue("status", new Integer(GenericNode.STATUS_COMPLETED));
			}
			else if (e.getType() == StatusMonitoringEvent.EXECUTION_FAILED) {
				node.setPropertyValue("status", new Integer(GenericNode.STATUS_FAILED));
			}
			else if (e.getType() == StatusMonitoringEvent.EXECUTION_ABORTED) {
				node.setPropertyValue("status", new Integer(GenericNode.STATUS_FAILED));
			}
		}
	}

	private void reset() {
		hook.shutdown();
		((KarajanRootCanvasRenderer) getSwingCanvasRenderer()).reset();
	}

	protected void progressMonitoringEvent(ProgressMonitoringEvent e) {
		NodeComponent node = getNode(e.getFlowElement(), e.getThread());
		if (node == null) {
			return;
		}
		node.setPropertyValue(KarajanNode.TOTAL, new Long(e.getTotal()));
		node.setPropertyValue(KarajanNode.CURRENT, new Long(e.getCurrent()));
	}

	protected NodeComponent getNode(FlowElement element, ThreadingContext thread) {
		Object uid = element.getProperty(FlowElement.UID);
		if (!transformation.getMap().containsKey(uid)) {
			return null;
		}
		Hashtable threads = (Hashtable) transformation.getMap().get(uid);
		NodeComponent nc = (NodeComponent) threads.get(thread);
		if (logger.isDebugEnabled()) {
			logger.debug(element + " - " + thread + ": " + nc);
		}
		return nc;
	}

	public void load(String fileName) {
		try {
			tree = (ElementTree) Loader.load(fileName);
			tree.setName(fileName);
			createGraph();
			transformation = new KarajanGraphTransformation(this, tree.getRoot(), hook);
			((KarajanRootCanvasRenderer) getCanvasRenderer()).setKarajanTransformation(transformation);
			getCanvas().getStatusManager().setDefaultText(
					transformation.getGraph().nodeCount() + " nodes, "
							+ transformation.getGraph().edgeCount() + " edges");
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void save(String fileName) {
		tree = transformation.inverseTransform();
		try {
			XMLConverter.serializeTree(tree, new FileWriter(fileName));
		}
		catch (IOException e) {
			this.getCanvas().getStatusManager().error("Could not save workflow", e);
		}
	}

	protected KarajanNode makeNode(FlowElement el) {
		KarajanNode node = new KarajanNode();
		node.setName(el.getElementType());
		node.setComponentType(el.getElementType());
		Iterator i = el.propertyNames().iterator();
		while (i.hasNext()) {
			String propName = "karajan." + (String) i.next();
			if (!node.hasProperty(propName)) {
				node.addProperty(new OverlayedProperty(node, propName, Property.RW));
			}
			node.setPropertyValue(propName, el.getProperty(propName));
		}
		return node;
	}

	public void propertyChange(PropertyChangeEvent e) {
		if (e.getPropertyName().equals("breakpoint")) {
			ThreadedUID te = null;
			if (transformation.getRmap().containsKey(e.getSource())) {
				te = (ThreadedUID) transformation.getRmap().get(e.getSource());
			}
			else {
				logger.warn("Reverse lookup failed for " + e.getSource());
				return;
			}
			if (e.getNewValue().equals(Boolean.TRUE)) {
				hook.addBreakPoint(te);
			}
			else {
				hook.removeBreakPoint(te);
			}
			return;
		}
		super.propertyChange(e);
	}

	public void start() {
		logger.info("Starting...");
		if (tree != null) {
			done = false;
			new Thread(hook, "Event Hook").start();
			EventBus.setEventHook(this.hook);
			ec = new ViewerExecutionContext(tree, this);
			ec.setStdout(new OutLogger(getLog(), false, Color.BLACK));
			ec.setStderr(new OutLogger(getLog(), true, Color.RED));
			ec.addEventListener(this);
			ec.setMonitoringEnabled(true);
			ec.start();
		}
		else {
			getSwingCanvasRenderer().getToolBarItem("Karajan>Start").setEnabled(true);
		}
	}

	public void stop() {
		logger.info("Stopping...");
		if (tree != null) {
			done = true;
			ec.getStateManager().abortContext(new ThreadingContext());
			reset();
		}
	}

	protected void subGraph(KarajanNode node, FlowElement el) {
		if (el.elementCount() > 0) {
			GraphCanvas canvas = node.createCanvas();
			Graph graph = new Graph();
			for (int i = 0; i < el.elementCount(); i++) {
				FlowElement subEl = el.getElement(i);
				KarajanNode subNode = makeNode(el.getElement(i));
				graph.addNode(subNode);
				subGraph(subNode, subEl);
			}
			canvas.setGraph(graph);
		}
	}

	public static void main(String[] args) {
		ArgumentParser ap = new ArgumentParser();
		ap.setExecutableName("cog-workflow-gui");
		ap.addOption("load", "Loads the specified file", "file", ArgumentParser.OPTIONAL);
		ap.addFlag("run", "If a workflow specification was loaded, it starts executing it; "
				+ "otherwise, it does nothing.");
		ap.addFlag("help", "Displays a usage summary");
		try {
			ap.parse(args);
			if (ap.isPresent("help")) {
				ap.usage();
				System.exit(0);
			}
			ap.checkMandatory();
		}
		catch (ArgumentParserException e) {
			System.out.println("Error parsing arguments: " + e.getMessage());
			ap.usage();
			System.exit(1);
		}

		System.setProperty("swing.plaf.metal.controlFont", "Arial");
		System.setProperty("swing.plaf.metal.systemFont", "Arial");
		System.setProperty("swing.plaf.metal.userFont", "Arial");
		System.setProperty("swing.plaf.metal.smallFont", "Arial");
		KarajanRootNode root = new KarajanRootNode();
		KarajanFrame frame = new KarajanFrame(root);
		Launcher.parseProperties("target.properties", root);
		Launcher.parseProperties("grapheditor.properties", root);
		root.setPropertyValue("karajan.frame", frame);
		if (ap.hasValue("load")) {
			frame.load(ap.getStringValue("load"));
		}
		if (ap.isPresent("run")) {
			frame.start();
		}
		frame.activate();
		frame.run();
		System.exit(0);
	}

	public KarajanGraphTransformation getTransformation() {
		return transformation;
	}
}
