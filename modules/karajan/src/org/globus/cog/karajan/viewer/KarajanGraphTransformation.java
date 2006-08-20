// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/*
 * 
 * Created on Jan 19, 2004
 */
package org.globus.cog.karajan.viewer;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.globus.cog.gui.grapheditor.ant.JoinNode;
import org.globus.cog.gui.grapheditor.canvas.GraphCanvas;
import org.globus.cog.gui.grapheditor.canvas.transformation.GraphTransformation;
import org.globus.cog.gui.grapheditor.canvas.views.layouts.FlowLayout;
import org.globus.cog.gui.grapheditor.edges.EdgeComponent;
import org.globus.cog.gui.grapheditor.edges.EdgeWithLabel;
import org.globus.cog.gui.grapheditor.edges.LoopEdge;
import org.globus.cog.gui.grapheditor.generic.GenericEdge;
import org.globus.cog.gui.grapheditor.generic.GenericNode;
import org.globus.cog.gui.grapheditor.nodes.NodeComponent;
import org.globus.cog.gui.grapheditor.properties.OverlayedProperty;
import org.globus.cog.gui.grapheditor.properties.Property;
import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.util.ThreadingContext;
import org.globus.cog.karajan.workflow.ElementTree;
import org.globus.cog.karajan.workflow.FlowElementWrapper;
import org.globus.cog.karajan.workflow.nodes.FlowElement;
import org.globus.cog.karajan.workflow.nodes.Parallel;
import org.globus.cog.karajan.workflow.nodes.ProjectNode;
import org.globus.cog.karajan.workflow.nodes.Sequential;
import org.globus.cog.karajan.workflow.nodes.user.UserDefinedElement;
import org.globus.cog.util.graph.Graph;
import org.globus.cog.util.graph.GraphInterface;
import org.globus.cog.util.graph.Node;

public class KarajanGraphTransformation implements GraphTransformation {
	private static Logger logger = Logger.getLogger(KarajanGraphTransformation.class);

	private static Map conditions = new Hashtable();
	private static Set ignored = new HashSet();

	static {
		conditions.put(new NamingWrapper("greaterThan"), ">?");
		conditions.put(new NamingWrapper("greaterOrEqual"), ">=?");
		conditions.put(new NamingWrapper("lessThan"), "<?");
		conditions.put(new NamingWrapper("lessOrEqual"), "<=?");
		conditions.put(new NamingWrapper("eq"), "=?");
		conditions.put(new NamingWrapper("equals"), "==?");

		ignored.add(new NamingWrapper("sys:include"));
		ignored.add(new NamingWrapper("sys:import"));
	}

	private Hashtable map;
	private Hashtable rmap;
	private Hashtable templates;
	private HashSet collapsedIterators;
	private FlowElement fe;
	private Graph graph;
	private KarajanFrame root;
	private Hook hook;
	private Set recursion;
	private GenericNode start, end;

	public KarajanGraphTransformation(KarajanFrame root, FlowElement fe, Hook hook) {
		this.fe = fe;
		this.root = root;
		this.hook = hook;
		this.collapsedIterators = new HashSet();
		this.recursion = new HashSet();
		createGraph();
	}

	public GraphInterface transform(GraphInterface graph) {
		return this.graph;
	}

	private void createGraph() {
		this.graph = new Graph();
		this.map = new Hashtable();
		this.rmap = new Hashtable();
		this.templates = new Hashtable();
		start = new KarajanNode();
		start.setName("START");
		start.setComponentType("start");
		start.setParent(root.getRootNode());
		List last = new ArrayList();
		last.add(graph.addNode(start));
		State finalState = createGraph(this.graph, fe, last);
		end = new KarajanNode();
		end.setName("END");
		end.setComponentType("end");
		end.setParent(root.getRootNode());
		Node en = this.graph.addNode(end);
		addEdges(finalState, en);
	}

	private State createGraph(Graph graph, FlowElement root, List last) {
		State initial = new State(graph, last, root, null, new ThreadingContext(), false,
				Color.BLACK, null);
		return seq(initial);
	}

	private State seq(State state) {
		Iterator i = state.flowNode.elements().iterator();
		State myState = state;
		while (i.hasNext()) {
			FlowElement n = (FlowElement) i.next();
			myState = one(myState.enter(n));
		}
		return myState;
	}

	private State unsynchronized(State state) {
		Iterator i = state.flowNode.elements().iterator();
		State myState = state;
		while (i.hasNext()) {
			FlowElement n = (FlowElement) i.next();
			myState = one(myState.enter(n));
		}
		return state;
	}

	private State seqChoice(State state) {
		KarajanNode choice = new KarajanNode();
		choice.setComponentType("sequentialChoice");
		Node start = addNode(state, choice);
		addEdges(state, start);
		State choiceState = state.addNode(start);

		Iterator i = state.flowNode.elements().iterator();
		State myState = null;
		while (i.hasNext()) {
			JoinNode dot = new JoinNode();
			Node branch = addNode(choiceState, dot);
			addEdges(choiceState, branch);
			if (myState != null) {
				addEdges(myState, branch);
			}
			State branchState = choiceState.addNode(branch);
			FlowElement n = (FlowElement) i.next();
			myState = one(branchState.enter(n));
		}
		return myState;
	}

	private State loop(State state) {
		KarajanNode loopStart = new KarajanNode();
		loopStart.setComponentType("loop");
		loopStart.setPropertyValue("show progress indicator", Boolean.TRUE);
		Node start = addNode(state, loopStart);
		addEdges(state, start);

		State myState = state.addNode(start);

		Iterator i = state.flowNode.elements().iterator();
		while (i.hasNext()) {
			FlowElement n = (FlowElement) i.next();
			myState = one(myState.enter(n));
		}

		JoinNode loopEnd = new JoinNode();
		Node end = addNode(state, loopEnd);
		addEdges(myState, end);

		myState = myState.addNode(end);

		LoopEdge loopEdge = new LoopEdge();
		loopEdge.updateControlPoint(2, (myState.loopDepth + 1) * 50, 50);
		myState.loopDepth++;
		loopEdge.setPropertyValue("layout.ignore", Boolean.TRUE);
		addEdge(state.g, loopEdge, start, end);
		return myState;
	}

	private void addEdge(Graph g, EdgeComponent ec, Node from, Node to) {
		ec.setParent(root.getRootNode());
		g.addEdge(from, to, ec);
	}

	private State par(State state) {
		Iterator i = state.flowNode.elements().iterator();
		State myState = state.copy();
		myState.clearLast();
		int j = 0;
		while (i.hasNext()) {
			FlowElement n = (FlowElement) i.next();
			myState.mergeNodes(one(state.enter(n, j++)));
		}
		return myState;
	}

	private State parChoice(State state) {
		KarajanNode choice = new KarajanNode();
		choice.setComponentType(state.flowNode.getElementType());
		Node start = addNode(state, choice);
		addEdges(state, start);
		State choiceState = state.addNode(start);

		Iterator i = state.flowNode.elements().iterator();
		State myState = choiceState.copy();
		myState.clearLast();
		int j = 0;
		while (i.hasNext()) {
			JoinNode dot = new JoinNode();
			Node branch = addNode(myState, dot);
			addEdges(choiceState, branch);
			State branchState = choiceState.addNode(branch);

			FlowElement n = (FlowElement) i.next();
			myState.mergeNodes(one(branchState.enter(n, j++)));
		}
		return myState;
	}

	private State ifnode(State state) {
		KarajanNode ifn = new KarajanNode();
		ifn.setComponentType("if");
		Node gif = addNode(state, ifn);
		addEdges(state, gif);
		State myState = state.addNode(gif);
		State endState = state.copy();
		endState.clearLast();

		Iterator i = state.flowNode.elements().iterator();

		float hue = 0;
		String text = null;
		boolean condition = false;
		while (i.hasNext()) {
			condition = !condition;
			FlowElement n = (FlowElement) i.next();
			if (condition) {
				if (!i.hasNext()) {
					hue = (float) 0;
					if (n.hasProperty("_annotation")) {
						text = (String) n.getProperty("_annotation");
					}
					else {
						text = "else";
					}
				}
				else {
					hue = hue + (float) 0.2;
					if (n.hasProperty("annotation")) {
						text = (String) n.getProperty("_annotation");
					}
					else {
						text = n.getElementType();
						NamingWrapper w = new NamingWrapper(text);
						if (conditions.containsKey(w)) {
							text = (String) conditions.get(w);
						}
					}
					continue;
				}
			}

			State newState = myState.enter(n);
			newState.edgeColor = Color.getHSBColor(hue, (float) 1.0, (float) 0.75);
			newState.edgeLabel = text;
			endState.mergeNodes(one(newState));
		}
		return endState;
	}
	
	public static final Arg A_RANGE = new Arg.Optional("range", null);

	public State unfold(State state) {
		State myState = state.copy();
		myState.clearLast();
		if (isA(state.flowNode, "sys:for:")) {
			int f = 1, t = 0;
			if (A_RANGE.getStatic(state.flowNode) != null) {
				String[] s = ((String) A_RANGE.getStatic(state.flowNode)).split(",");
				if (s.length != 2) {
					return state;
				}
				else {
					f = new Integer(s[0]).intValue();
					t = new Integer(s[1]).intValue();
				}
			}
			else {
				f = ((Integer) state.flowNode.getProperty("from")).intValue();
				t = ((Integer) state.flowNode.getProperty("to")).intValue();
			}

			if (t >= f) {
				for (int i = f; i <= t; i++) {
					myState.mergeNodes(seq(state.enter(state.flowNode, i - f)));
				}
			}
		}
		else {
			this.collapsedIterators.add(state.flowNode);
			return seq(state);
		}
		return myState;
	}

	private NamingWrapper nw = new NamingWrapper();

	private State one(State state) {
		logger.debug("One " + state);
		nw.set(state.flowNode.getElementType());
		if (ignored.contains(nw)) {
			return state;
		}
		if (isA(state.flowNode, "sys:ignoreErrors") || isA(state.flowNode, "sys:restartOnError")) {
			state.ignoreErrors = true;
		}
		if (state.flowNode.getElementType().equals("element")) {
			templates.put(UserDefinedElement.A_NAME.getStatic(state.flowNode), state.flowNode);
			return state;
		}
		if (templates.containsKey(state.flowNode.getElementType())) {
			if (recursion.contains(state.flowNode.getElementType())) {
				logger.debug("Recursive call detected");
				return state;
			}
			FlowElement flowNode = state.flowNode;
			recursion.add(flowNode.getElementType());
			state = seq(state.copy());
			ThreadingContext thread = state.thread;
			state = seq(state.enter((FlowElement) templates.get(flowNode.getElementType()),
					flowNode));
			state.thread = thread;
			recursion.remove(flowNode.getElementType());
		}
		else {
			if (isA(state.flowNode, "sys:for")) {
				state = loop(state);
			}
			else if (isA(state.flowNode, "sys:parallelFor")) {
				state = unfold(state);
			}
			else if (isA(state.flowNode, "sys:while")) {
				state = loop(state);
			}
			else if (isA(state.flowNode, "sys:choice")) {
				state = seqChoice(state);
			}
			else if (isA(state.flowNode, "sys:parallelChoice") || isA(state.flowNode, "sys:race")) {
				state = parChoice(state);
			}
			else if (isA(state.flowNode, "sys:parallel")) {
				state = par(state);
			}
			else if (isA(state.flowNode, "sys:if")) {
				state = ifnode(state);
			}
			else if (isA(state.flowNode, "sys:unsynchronized")) {
				state = unsynchronized(state);
			}
			else if (isA(state.flowNode, "sys:ignoreErrors")) {
				state = seq(state);
			}
			else if (state.flowNode.elementCount() > 0 && !isA(state.flowNode, "vdl:execute")) {
				state = seq(state);
			}
			else {
				GenericNode gn = new KarajanNode();
				Node node = addNode(state, gn);
				addEdges(state, node);
				state = state.addNode(node);
				if (state.flowNode.elementCount() != 0) {
					Graph g = new Graph();
					createGraph(g, state.flowNode, new ArrayList());
					GraphCanvas gc = gn.createCanvas();
					gc.setGraph(g);
				}
			}
		}
		return state;
	}

	private Node addNode(State state, NodeComponent graphNode) {
		graphNode.setComponentType(state.flowNode.getElementType());
		if (graphNode instanceof GenericNode) {
			GenericNode genericNode = (GenericNode) graphNode;
			if (state.flowNode.hasProperty("_annotation")) {
				genericNode.setName((String) state.flowNode.getProperty("_annotation"));
			}
			else if (isA(state.flowNode, "kernel:string")) {
				genericNode.setName("\"" + state.flowNode.getProperty(FlowElement.TEXT) + "\"");
			}
			else if (isA(state.flowNode, "kernel:number")) {
				genericNode.setName((String) state.flowNode.getProperty(FlowElement.TEXT));
			}
			else {
				genericNode.setName(state.flowNode.getElementType());
			}
		}
		graphNode.setParent(root.getRootNode());
		Map sa = state.flowNode.getStaticArguments();
		Iterator i = sa.keySet().iterator();
		while (i.hasNext()) {
			String propName = (String) i.next();
			String propValue = sa.get(propName).toString();
			propName = "karajan." + propName;
			graphNode.addProperty(new OverlayedProperty(graphNode, propName, Property.RW));
			graphNode.setPropertyValue(propName, propValue);
		}
		graphNode.addPropertyChangeListener(root);

		Node node = state.g.addNode(graphNode);

		Object uid = state.flowNode.getProperty(FlowElement.UID);
		if (map.containsKey(uid)) {
			Hashtable threads = (Hashtable) map.get(uid);
			threads.put(state.thread, graphNode);
		}
		else {
			Hashtable threads = new Hashtable();
			threads.put(state.thread, graphNode);
			map.put(uid, threads);
		}
		ThreadedUID te = new ThreadedUID(state.flowNode, state.thread);
		rmap.put(graphNode, te);
		if (state.ignoreErrors) {
			hook.ignoreElement(te);
		}
		else {
			hook.addMonitoredElement(te);
		}
		return node;
	}

	private void addEdges(State state, Node node) {
		Iterator j = state.last.iterator();
		while (j.hasNext()) {
			EdgeComponent ge = getEdge(state);
			state.g.addEdge((Node) j.next(), node, ge);
		}
	}

	private EdgeComponent getEdge(State state) {
		EdgeComponent ge;
		if (state.edgeLabel == null) {
			ge = new GenericEdge();
		}
		else {
			ge = new EdgeWithLabel();
			ge.setPropertyValue("label", state.edgeLabel);
		}
		ge.setPropertyValue("color", state.edgeColor);
		ge.setParent(root.getRootNode());
		state.edgeLabel = null;
		state.edgeColor = Color.BLACK;
		return ge;
	}

	/**
	 * @return Returns the collapsedIterators.
	 */
	public HashSet getCollapsedIterators() {
		return collapsedIterators;
	}

	/**
	 * @return Returns the map.
	 */
	public Hashtable getMap() {
		return map;
	}

	/**
	 * @return Returns the rmap.
	 */
	public Hashtable getRmap() {
		return rmap;
	}

	/**
	 * @return Returns the templates.
	 */
	public Hashtable getTemplates() {
		return templates;
	}

	/**
	 * @return Returns the graph.
	 */
	public Graph getGraph() {
		return graph;
	}

	public static boolean isA(FlowElement element, String name) {
		String fen = element.getElementType().toLowerCase();
		name = name.toLowerCase();
		int i = name.indexOf(fen);
		if (i != -1) {
			return ((i == 0) && (name.length() == fen.length()))
					|| ((name.charAt(i - 1) == ':') && (i + fen.length() == name.length()));
		}
		else {
			return false;
		}
	}

	private class State {
		public Graph g;
		public FlowElement flowNode;
		public NodeComponent nodeComponent;
		public ThreadingContext thread;
		public List last;
		public Color edgeColor;
		public String edgeLabel;
		public boolean ignoreErrors;
		public int loopDepth;

		public State(Graph g, List last, FlowElement flowNode, NodeComponent nodeComponent,
				ThreadingContext thread, boolean ignoreErrors, Color edgeColor, String edgeLabel) {
			this(g, last, flowNode, nodeComponent, thread, ignoreErrors, edgeColor, edgeLabel, 0);
		}

		public State(Graph g, List last, FlowElement flowNode, NodeComponent nodeComponent,
				ThreadingContext thread, boolean ignoreErrors, Color edgeColor, String edgeLabel,
				int loopDepth) {
			this.g = g;
			this.last = last;
			this.flowNode = flowNode;
			this.nodeComponent = nodeComponent;
			this.thread = thread;
			this.edgeColor = edgeColor;
			this.edgeLabel = edgeLabel;
			this.ignoreErrors = ignoreErrors;
			this.loopDepth = loopDepth;
		}

		public State enter(FlowElement node) {
			return new State(g, last, node, null, thread, ignoreErrors, edgeColor, edgeLabel,
					loopDepth);
		}

		public State enter(FlowElement node, int subThread) {
			return new State(g, last, node, null, thread.split(subThread), ignoreErrors, edgeColor,
					edgeLabel, loopDepth);
		}

		public State enter(FlowElement node, FlowElement caller) {
			return new State(g, last, node, null,
					thread.split(((Integer) caller.getProperty(FlowElement.UID)).intValue()), ignoreErrors,
					edgeColor, edgeLabel, loopDepth);
		}

		public State copy() {
			return new State(g, last, flowNode, null, thread, ignoreErrors, edgeColor, edgeLabel,
					loopDepth);
		}

		public State addNode(Node node) {
			State state = copy();
			state.last = new LinkedList();
			state.last.add(node);
			return state;
		}

		public void mergeNodes(State state) {
			last.addAll(state.last);
		}

		public void clearLast() {
			last = new LinkedList();
		}

		public String toString() {
			return flowNode.toString() + "#" + thread + ":" + last;
		}
	}

	public ElementTree inverseTransform() {
		FlowLayout fl = new FlowLayout();
		ElementTree tree = new ElementTree();
		ProjectNode pn = new ProjectNode();
		pn.setElementType("project");
		tree.setRoot(pn);
		Graph g2 = (Graph) graph.clone();
		g2.removeNode(g2.findNode(start));
		g2.removeNode(g2.findNode(end));
		inverseTransform(pn, fl.getDecomposition(g2));
		return tree;
	}

	private void inverseTransform(FlowElement fe, FlowLayout.Component c) {
		if (c instanceof FlowLayout.Sequential) {
			Sequential s = new Sequential();
			s.setElementType("sequential");
			fe.addElement(s);
			inverseTransformContainer(s, (FlowLayout.Container) c);
		}
		else if (c instanceof FlowLayout.Parallel) {
			Parallel p = new Parallel();
			p.setElementType("parallel");
			fe.addElement(p);
			inverseTransformContainer(p, (FlowLayout.Container) c);
		}
		else if (c instanceof FlowLayout.Filler) {
			return;
		}
		else {
			fe.addElement(buildElement((NodeComponent) ((Node) c.get()).getContents()));
		}
	}

	private void inverseTransformContainer(FlowElement fe, FlowLayout.Container container) {
		Iterator i = container.iterator();
		while (i.hasNext()) {
			FlowLayout.Component c = (FlowLayout.Component) i.next();
			inverseTransform(fe, c);
		}
	}

	private FlowElement buildElement(NodeComponent node) {
		FlowElement fe = new FlowElementWrapper();
		String name = (String) node.getPropertyValue("name");
		if (name == null) {
			throw new RuntimeException("No name set on node");
		}
		fe.setElementType(name);
		Iterator i = node.getProperties().iterator();
		while (i.hasNext()) {
			Property p = (Property) i.next();
			if (p.getName().startsWith("karajan.") && !p.getName().startsWith("karajan._")) {
				fe.setProperty(p.getName().substring(8), (String) p.getValue());
			}
		}
		return fe;
	}

}