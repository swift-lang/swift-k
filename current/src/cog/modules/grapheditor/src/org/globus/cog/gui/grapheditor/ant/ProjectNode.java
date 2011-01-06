
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------


package org.globus.cog.gui.grapheditor.ant;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;

import javax.swing.JFileChooser;

import org.apache.log4j.Logger;
import org.apache.tools.ant.BuildEvent;
import org.apache.tools.ant.BuildListener;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Target;
import org.apache.tools.ant.Task;
import org.globus.cog.gui.grapheditor.ant.taskdefs.ETaskContainer;
import org.globus.cog.gui.grapheditor.canvas.StatusManager;
import org.globus.cog.gui.grapheditor.nodes.NodeComponent;
import org.globus.cog.gui.grapheditor.properties.OverlayedProperty;
import org.globus.cog.gui.grapheditor.properties.Property;
import org.globus.cog.gui.grapheditor.util.GraphToXML;
import org.globus.cog.gui.grapheditor.util.XMLToGraph;
import org.globus.cog.gui.grapheditor.util.swing.LogFrame;
import org.globus.cog.util.graph.Edge;
import org.globus.cog.util.graph.Graph;
import org.globus.cog.util.graph.GraphInterface;
import org.globus.cog.util.graph.Node;

/**
 * Renderer for an Ant project. It also handles execution,
 * saving/loading/importing of projects
 */
public class ProjectNode
	extends AntNode
	implements NodeComponent, BuildListener, ThreadedBuildListener, ComponentListener {
	private static Logger logger = Logger.getLogger(ProjectNode.class);

	private org.apache.tools.ant.Project antProject;
	private String fileName;
	private String defaultTarget;
	private String baseDir;
	protected Hashtable tasks;
	protected Hashtable targets;
	private LogFrame log;

	public ProjectNode() {
		super();
		setCanvasType(ProjectCanvas.class);
		setComponentType("project");
		tasks = new Hashtable();
		targets = new Hashtable();
		fileName = null;
		baseDir = null;
	}

	public org.apache.tools.ant.Project getAntProject() {
		return antProject;
	}

	protected boolean isTarget(Object o) {
		if (o instanceof TargetNode) {
			return true;
		}
		return false;
	}

	/**
	 * Loads a build file through Ant. It allows Ant to construct its objects...
	 * 
	 * @param buildfile
	 */
	public void loadAntBuildFile(String buildfile) {
		getStatusManager().push("Initializing Ant...");
		org.apache.tools.ant.Project antProject = new org.apache.tools.ant.Project();
		antProject.init();
		org.apache.tools.ant.ProjectHelper projectHelper =
			new org.apache.tools.ant.helper.ProjectHelperImpl();
		projectHelper.parse(antProject, new File(buildfile));
		this.antProject = antProject;
		String baseDir = new File(buildfile).getAbsoluteFile().getParent();
		logger.debug("Basedir: " + baseDir);
		antProject.setBasedir(baseDir);
		getStatusManager().pop();
	}

	protected String fileChooser() {
		JFileChooser JF = new JFileChooser("");
		JF.setFileSelectionMode(JFileChooser.FILES_ONLY);
		int ret = JF.showOpenDialog(null);
		if (ret == JFileChooser.APPROVE_OPTION) {
			return JF.getSelectedFile().getAbsolutePath();
		}
		return null;
	}

	public void open() {
		String s = fileChooser();
		if (s != null) {
			load(s);
			fileName = s;
		}
	}

	public void load(String fname) {
		getStatusManager().push("Loading " + fname + "...");
		getCanvas().getGraph().clear();
		this.fileName = fname;
		try {
			XMLToGraph.read(this, fname, true, null);
			baseDir = new File(fname).getParentFile().getAbsolutePath();
		}
		catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		getStatusManager().pop();
		getCanvas().invalidate();
	}

	public void save() {
		if (fileName == null) {
			saveAs();
		}
		else {
			getStatusManager().push("Saving file...");
			try {
				logger.debug("Writing to " + fileName);
				BufferedWriter bw = new BufferedWriter(new FileWriter(fileName));
				GraphToXML.write(this, bw, 0, true);
				bw.close();
			}
			catch (IOException e) {
				e.printStackTrace();
			}
			getStatusManager().pop();
		}
	}

	public void saveAs() {
		String s = fileChooser();
		if (s != null) {
			fileName = s;
			save();
		}
	}

	public void setDefault(String defaultTarget) {
		this.defaultTarget = defaultTarget;
	}

	public String getDefault() {
		return defaultTarget;
	}

	public String getBaseDir() {
		return baseDir;
	}

	public void setBaseDir(String baseDir) {
		this.baseDir = baseDir;
		antProject.setBasedir(baseDir);
	}

	public void importAntBuildfile() {
		String s = fileChooser();
		if (s != null) {
			importAntBuildfile(s);
		}
	}

	/**
	 * Loads a buildfile and adds edges to represent flow and dependencies
	 * 
	 * @param fileName
	 */
	public void importAntBuildfile(String fileName) {
		getStatusManager().push("Loading Ant buildfile...");
		load(fileName);
		//add the dependencies as edges
		Iterator i = getCanvas().getGraph().getNodesIterator();
		while (i.hasNext()) {
			Node srcNode = (Node) i.next();
			if (!isTarget(srcNode.getContents())) {
				continue;
			}
			TargetNode srcTarget = (TargetNode) srcNode.getContents();
			Iterator deps = srcTarget.getDependencies().listIterator();
			while (deps.hasNext()) {
				String dependency = (String) deps.next();
				Iterator j = getCanvas().getGraph().getNodesIterator();
				while (j.hasNext()) {
					Node destNode = (Node) j.next();
					if (!isTarget(destNode.getContents())) {
						continue;
					}
					TargetNode destTarget = (TargetNode) destNode.getContents();
					if (destTarget.getName().equals(dependency)) {
						//add both the dependency edges and the flow edges, but reversed
						getCanvas().getGraph().addEdge(srcNode, destNode, new TargetDependency());
						getCanvas().getGraph().addEdge(destNode, srcNode, new FlowEdge());
					}
				}
			}
		}
		getStatusManager().pop();
		getCanvas().invalidate();
	}

	public void exportAntBuildFile(String name) {
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(name));
			GraphToXML.write(this, bw, 0, false);
			bw.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	protected NodeComponent taskdef(String name, String classname) {
		GraphInterface g = getCanvas().getGraph();
		TaskNode taskdef = new TaskNode();
		taskdef.setComponentType("taskdef");
		OverlayedProperty pname = new OverlayedProperty(taskdef, "name");
		taskdef.addProperty(pname);
		pname.setValue(name);
		OverlayedProperty pclass = new OverlayedProperty(taskdef, "classname");
		taskdef.addProperty(pclass);
		pclass.setValue(classname);
		Graph ng = new Graph();
		ng.addNode(taskdef);
		Iterator i = g.getNodesIterator();
		Hashtable map = new Hashtable();
		while (i.hasNext()) {
			Node n = (Node) i.next();
			map.put(n, ng.addNode(n.getContents()));
		}
		i = g.getEdgesIterator();
		while (i.hasNext()) {
			Edge e = (Edge) i.next();
			ng.addEdge(
				(Node) map.get(e.getFromNode()),
				(Node) map.get(e.getToNode()),
				e.getContents());
		}
		getCanvas().setGraph(ng);
		return taskdef;
	}

	public void executeTarget(String targetName) {
		getStatusManager().push("Initializing execution environment...");
		if (log == null) {
			log = new LogFrame();
			log.addComponentListener(this);
			if (hasProperty("logsize")) {
				log.setSize((Dimension) getPropertyValue("logsize"));
			}
			if (hasProperty("loglocation")) {
				Point l = (Point) getPropertyValue("loglocation");
				log.setLocation(l.x, l.y);
			}
		}
		else {
			log.clear();
			log.setVisible(true);
		}
		NodeComponent seq =
			taskdef("sequential", "org.globus.cog.gui.grapheditor.ant.taskdefs.ESequential");
		NodeComponent par =
			taskdef("parallel", "org.globus.cog.gui.grapheditor.ant.taskdefs.EParallel");
		exportAntBuildFile(new File(getBaseDir(), "somefile.xml").getAbsolutePath());
		loadAntBuildFile(new File(getBaseDir(), "somefile.xml").getAbsolutePath());
		GraphInterface graph = getCanvas().getGraph();
		graph.removeNode(graph.findNode(seq));
		graph.removeNode(graph.findNode(par));
		tasks = new Hashtable();
		targets = new Hashtable();
		setUpListeners();
		resetAllTargets();
		antProject.addBuildListener(this);
		ThreadedBuilder builder = new ThreadedBuilder(getAntProject(), targetName, this);
		getStatusManager().pop();
		builder.start();
	}

	public void setUpListeners() {
		Iterator i = getCanvas().getGraph().getNodesIterator();
		while (i.hasNext()) {
			Node node = (Node) i.next();
			NodeComponent nodeComponent = (NodeComponent) node.getContents();
			if (nodeComponent instanceof TargetNode) {
				String targetName = (String) nodeComponent.getPropertyValue("name");
				if (targets == null) {
					targets = new Hashtable();
				}
				targets.put(targetName, nodeComponent);
				Object c = antProject.getTargets().get(targetName);
				if (nodeComponent.getCanvas() != null) {
					recurse(c, nodeComponent.getCanvas().getGraph());
				}
			}
		}
	}

	protected void recurse(Object c, GraphInterface graph) {
		//there should be a one to one mapping between the two
		if (c instanceof Target) {
			Task[] t = ((Target) c).getTasks();
			if (t.length != graph.nodeCount()) {
				throw new RuntimeException("Inconsistency between stored graph and buildfile structure");
			}
			Iterator ni = graph.getNodesIterator();
			for (int i = 0; i < t.length; i++) {
				Node n = (Node) ni.next();
				NodeComponent nc = (NodeComponent) n.getContents();
				tasks.put(t[i], nc);
				if (t[i] instanceof ETaskContainer) {
					recurse(t[i], nc.getCanvas().getGraph());
				}
			}
		}
		else if (c instanceof ETaskContainer) {
			ETaskContainer t = (ETaskContainer) c;
			if (t.getTasks().size() != graph.nodeCount()) {
				throw new RuntimeException("Inconsistency between stored graph and buildfile structure");
			}
			Iterator ni = graph.getNodesIterator();
			for (int i = 0; i < t.getTasks().size(); i++) {
				Task tk = (Task) t.getTasks().get(i);
				Node n = (Node) ni.next();
				NodeComponent nc = (NodeComponent) n.getContents();
				tasks.put(tk, nc);
				if (nc instanceof ForNode) {
					((ForNode) nc).setAntProject(tk.getProject());
				}
				if (tk instanceof ETaskContainer) {
					recurse(tk, nc.getCanvas().getGraph());
				}
			}
		}
	}

	//for some reason the build started event is not sent by Ant properly.
	//this is needed to reset the state of all the targets
	public void resetAllTargets() {
		Iterator i = getCanvas().getGraph().getNodesIterator();
		while (i.hasNext()) {
			Node node = (Node) i.next();
			NodeComponent nodeComponent = (NodeComponent) node.getContents();
			if (nodeComponent instanceof TargetNode) {
				nodeComponent.setPropertyValue("status", new Integer(STATUS_STOPPED));
			}
		}
		Enumeration e = tasks.elements();
		while (e.hasMoreElements()) {
			Object task = e.nextElement();
			if (task instanceof TaskNode) {
				((TaskNode) task).setPropertyValue("status", new Integer(STATUS_STOPPED));
			}
		}
	}

	public void targetStarted(BuildEvent p0) {
		TargetNode tn = (TargetNode) targets.get(p0.getTarget().getName());
		tn.setPropertyValue("status", new Integer(STATUS_RUNNING));
	}

	public void targetFinished(BuildEvent p0) {
		TargetNode tn = (TargetNode) targets.get(p0.getTarget().getName());
		if (p0.getException() == null) {
			tn.setPropertyValue("status", new Integer(STATUS_COMPLETED));
		}
		else {
			p0.getException().printStackTrace();
			tn.setPropertyValue("status", new Integer(STATUS_FAILED));
		}
	}

	public void taskFinished(BuildEvent p0) {
		Task task = p0.getTask();
		AntNode node = (AntNode) tasks.get(task);
		if (node instanceof TaskNode) {
			TaskNode tn = (TaskNode) node;
			if (p0.getException() == null) {
				tn.setPropertyValue("status", new Integer(STATUS_COMPLETED));
			}
			else {
				tn.setPropertyValue("status", new Integer(STATUS_FAILED));
			}
		}
	}

	public void messageLogged(BuildEvent p0) {
		if (p0.getPriority() > Project.MSG_INFO) {
			return;
		}
		if (log != null) {
			log.append(p0.getMessage());
		}
		else {
			logger.info("LOG: " + p0.getMessage());
		}
	}

	public void taskStarted(BuildEvent p0) {
		Task task = p0.getTask();
		if (!tasks.containsKey(task)) {
			logger.error("Task not found: " + task.toString());
		}
		AntNode node = (AntNode) tasks.get(task);
		if (node instanceof TaskNode) {
			TaskNode tn = (TaskNode) node;
			tn.setPropertyValue("status", new Integer(STATUS_RUNNING));
		}
	}

	public void buildStarted(BuildEvent p0) {
	}

	public void buildFinished(BuildEvent p0) {
	}

	public void buildFinished(Exception e) {
		if (e != null) {
			e.printStackTrace();
		}
	}

	public void componentShown(ComponentEvent e) {

	}

	public void componentMoved(ComponentEvent e) {
		if (e.getSource() == log) {
			Point p = log.getLocation();
			setPropertyValue("loglocation", p);
		}
	}

	public void componentHidden(ComponentEvent e) {

	}

	public void componentResized(ComponentEvent e) {
		if (e.getSource() == log) {
			Dimension d = log.getSize();
			if (!hasProperty("logsize")) {
				addProperty(new OverlayedProperty(this, "logsize", Property.HIDDEN));
			}
			setPropertyValue("logsize", d);
		}
	}

	public ProjectCanvas getProjectCanvas() {
		return (ProjectCanvas) getCanvas();
	}

	public StatusManager getStatusManager() {
		return getProjectCanvas().getStatusManager();
	}
	public void setAntProject(org.apache.tools.ant.Project project) {
		this.antProject = project;
	}

	public LogFrame getLog() {
		return log;
	}

	public void setLog(LogFrame frame) {
		this.log = frame;
	}

	public String getFileName() {
		return fileName;
	}

}
