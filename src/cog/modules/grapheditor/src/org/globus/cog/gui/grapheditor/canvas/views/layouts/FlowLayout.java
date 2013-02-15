
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/*
 *
 * Created on Mar 18, 2004
 *
 */
package org.globus.cog.gui.grapheditor.canvas.views.layouts;


import java.awt.Point;
import java.awt.geom.Point2D;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.apache.log4j.Logger;
import org.globus.cog.gui.grapheditor.canvas.StatusManager;
import org.globus.cog.util.graph.Edge;
import org.globus.cog.util.graph.EdgeIterator;
import org.globus.cog.util.graph.GraphInterface;
import org.globus.cog.util.graph.Node;
import org.globus.cog.util.graph.NodeIterator;

public class FlowLayout implements GraphLayoutEngine2, StatusReporter {
	private static Logger logger = Logger.getLogger(FlowLayout.class);
	private static float sx = 75;
	private static float sy = 50;
	private StatusManager statusManager;
	private boolean root;
	private Set ignoredEdges;

	public FlowLayout() {
		this(null);
	}

	public FlowLayout(StatusManager statusManager) {
		this.statusManager = statusManager;
	}

	public Hashtable layoutGraph(GraphInterface graph, Hashtable fixedNodes) {
		GraphInterface myGraph = (GraphInterface) graph.clone();
		removeIgnoredEdges(myGraph);
		Node entry = prepare(myGraph, graph);
		removeCycles(myGraph, entry);
		reduce(myGraph);
		return layout(myGraph);
	}
	
	public Component getDecomposition(GraphInterface graph) {
		GraphInterface myGraph = (GraphInterface) graph.clone();
		removeIgnoredEdges(myGraph);
		Node entry = prepare(myGraph, graph);
		removeCycles(myGraph, entry);
		reduce(myGraph);
		Node n = myGraph.getNodesIterator().nextNode();
		return (Component) n.getContents();
	}
	
	private void removeIgnoredEdges(GraphInterface graph) {
		if (ignoredEdges == null) {
			return;
		}
		Iterator i = ignoredEdges.iterator();
		while (i.hasNext()) {
			Edge e = (Edge) i.next();
			graph.removeEdge(graph.findEdge(e.getContents()));
		}
	}

	private Hashtable layout(GraphInterface graph) {
		Node n = graph.getNodesIterator().nextNode();
		Component c = (Component) n.getContents();
		c.x = 0;
		c.y = 0;
		c.w = c.width * sx;
		c.h = c.height * sy;
		Map map = new Hashtable();
		layout(c, map, 0);
		Hashtable coords = new Hashtable();
		Iterator i = map.keySet().iterator();
		while (i.hasNext()) {
			Node node = (Node) i.next();
			List l = (List) map.get(node);
			float x = 0;
			float y = 0;
			Iterator j = l.iterator();
			while (j.hasNext()) {
				Point2D p = (Point2D) j.next();
				x += p.getX();
				y += p.getY();
			}
			x /= l.size();
			y /= l.size();
			coords.put(node, new Point((int) x, (int) y));
		}
		return coords;
	}

	private void layout(Component c, Map map, int depth) {
		if (c instanceof Sequential) {
			int sum = 0;
			float y = c.y;
			boolean filler = false;
			Iterator i = ((Container) c).iterator();
			while (i.hasNext()) {
				Component s = (Component) i.next();
				sum += s.height;
				if (s instanceof Filler) {
					filler = true;
				}
			}
			float yratio = c.h / sum;
			if (filler) {
				yratio = sy;
			}
			i = ((Container) c).iterator();
			while (i.hasNext()) {
				Component s = (Component) i.next();
				s.h = s.height * yratio;
				s.w = s.width * sx;
				s.x = c.x + (c.w - s.w) / 2;
				s.y = y;
				layout(s, map, depth + 1);
				y += s.h;
			}
		}
		else if (c instanceof Parallel) {
			int sum = 0;
			float x = c.x;
			Iterator i = ((Container) c).iterator();
			while (i.hasNext()) {
				Component s = (Component) i.next();
				sum += s.width;
			}
			float xratio = c.w / sum;
			i = ((Container) c).iterator();
			while (i.hasNext()) {
				Component s = (Component) i.next();
				s.w = s.width * xratio;
				s.y = c.y + (c.height - s.height) / 2 * sy;
				s.h = c.h - (c.height - s.height) * sy;
				s.x = x;
				layout(s, map, depth + 1);
				x += s.w;
			}
		}
		else {
			if (c.isEmpty()) {
				return;
			}
			Node n = (Node) c.get();
			if (n == null) {
				//the entry
				return;
			}
			List l = (List) map.get(n);
			if (l == null) {
				l = new LinkedList();
				map.put(n, l);
			}
			if (c instanceof Filler) {
				l.add(new Point2D.Float(c.x + c.w / 2, c.y + sy / 2));
			}
			else {
				l.add(new Point2D.Float(c.x + c.w / 2, c.y + c.h / 2));
			}
		}
	}

	protected Node prepare(GraphInterface graph, GraphInterface old) {
		Node[] nodes = (Node[]) graph.getNodesSet().toArray(new Node[0]);
		Node[] oldNodes = (Node[]) old.getNodesSet().toArray(new Node[0]);
		Node entry = graph.addNode();
		for (int i = 0; i < nodes.length; i++) {
			if (nodes[i].inDegree() == 0) {
				graph.addEdge(entry, nodes[i], null);
			}
			nodes[i].setContents(oldNodes[i]);
		}
		if (entry.outDegree() == 0) {
			graph.removeNode(entry);
			entry = graph.getNodesIterator().nextNode();
		}
		else if (entry.outDegree() < 2) {
			Node rem = entry;
			entry = entry.getOutEdgesIterator().nextEdge().getToNode();
			graph.removeNode(rem);
		}
		else {
			root = true;
		}
		return entry;
	}

	protected void removeCycles(GraphInterface graph, Node entry) {
		removeCycles(graph, entry, new Stack());
	}

	private void removeCycles(GraphInterface graph, Node entry, Stack stack) {
		if (entry.outDegree() == 0) {
			return;
		}
		Edge[] edges = (Edge[]) entry.getOutEdges().toArray(new Edge[0]);
		for (int i = 0; i < edges.length; i++) {
			Node t = edges[i].getToNode();
			if (stack.contains(t)) {
				graph.removeEdge(edges[i]);
			}
			else {
				stack.push(t);
				removeCycles(graph, t, stack);
				stack.pop();
			}
		}
	}

	protected boolean split(GraphInterface graph) {
		boolean changed = false;
		Node[] nodes = (Node[]) graph.getNodesSet().toArray(new Node[0]);
		for (int i = 0; i < nodes.length; i++) {
			if (nodes[i].inDegree() > 1) {
				split(graph, nodes[i]);
				changed = true;
				if (Runtime.getRuntime().totalMemory()
					- Runtime.getRuntime().maxMemory()
					+ Runtime.getRuntime().freeMemory() < graph.nodeCount() / 6 * 1024) {
					break;
				}
			}
		}
		return changed;
	}

	private void split(GraphInterface graph, Node head) {
		LinkedList froms = new LinkedList();
		Edge[] edges = (Edge[]) head.getInEdges().toArray(new Edge[0]);
		for (int i = 1; i < edges.length; i++) {
			froms.add(edges[i].getFromNode());
			graph.removeEdge(edges[i]);
		}
		LinkedList copies = new LinkedList();
		while (froms.size() > 0) {
			Node n = graph.addNode(head.getContents());
			copies.add(n);
			graph.addEdge((Node) froms.removeFirst(), n, null);
		}
		copy(graph, head, copies);
	}

	private void copy(GraphInterface graph, Node head, LinkedList copies) {
		if (head.inDegree() > 1) {
			if (Runtime.getRuntime().totalMemory()
				- Runtime.getRuntime().maxMemory()
				+ Runtime.getRuntime().freeMemory() > graph.nodeCount() / 6 * 1024) {
				split(graph, head);
			}
		}
		if (head.outDegree() == 0) {
			return;
		}
		Edge[] edges = (Edge[]) head.getOutEdges().toArray(new Edge[0]);
		for (int e = 0; e < edges.length; e++) {
			Node to = edges[e].getToNode();
			LinkedList toCopies = new LinkedList();
			Iterator i = copies.iterator();
			while (i.hasNext()) {
				Node copy = (Node) i.next();
				Node toCopy = graph.addNode(to.getContents());
				graph.addEdge(copy, toCopy, null);
				toCopies.add(toCopy);
			}
			copy(graph, to, toCopies);
		}
	}

	protected void reduce(GraphInterface graph) {
		int size = graph.nodeCount();
		if (statusManager != null) {
			statusManager.initializeProgress(size);
		}
		while (graph.nodeCount() > 1) {
			boolean changed;
			do {
				changed = false;
				changed = reduceSequential(graph) || changed;
				if (statusManager != null) {
					statusManager.setProgress(size - graph.nodeCount());
				}
				logger
					.debug("After sequential reduction: " + graph.nodeCount());
				changed = reduceParallel(graph) || changed;
				if (statusManager != null) {
					statusManager.setProgress(size - graph.nodeCount());
				}
				logger.debug("After parallel reduction: " + graph.nodeCount());

			}
			while (changed);
			if (graph.nodeCount() > 1) {
				changed = reduceParallelForced(graph) || changed;
				if (statusManager != null) {
					statusManager.setProgress(size - graph.nodeCount());
				}
				logger.debug("After forced parallel reduction: "
					+ graph.nodeCount());
				if (!changed) {
					if (statusManager != null) {
						statusManager.removeProgress();
					}
					logger
						.error("Graph has not changed after one sweep. Dumping to bug.xml");
					dumpGraph(graph);
					return;
				}
			}
		}
		if (graph.nodeCount() == 1) {
			Node node  = graph.getNodesIterator().nextNode();
			if (!(node.getContents() instanceof Component)) {
				node.setContents(new Component(node.getContents()));
			}
		}
		if (statusManager != null) {
			statusManager.removeProgress();
		}
	}

	protected void dumpGraph(GraphInterface graph) {
		try {
			Writer fw = new BufferedWriter(new FileWriter("bug.xml"));
			fw.write("<graph>\n");
			dumpGraph(graph, fw);
			fw.write("</graph>\n");
			fw.close();
		}
		catch (Exception e) {
			logger.error("Error dumping graph", e);
		}
	}

	protected void dumpGraph(GraphInterface graph, Writer fw)
		throws IOException {

		Hashtable ids = new Hashtable();
		int cnt = 0;
		NodeIterator n = graph.getNodesIterator();
		while (n.hasMoreNodes()) {
			Node node = n.nextNode();
			fw.write("  <node nodeid=\"" + cnt + "\" name=\"" + cnt + "\"/>\n");
			ids.put(node, new Integer(cnt));
			cnt++;
		}
		EdgeIterator e = graph.getEdgesIterator();
		while (e.hasMoreEdges()) {
			Edge edge = e.nextEdge();
			int f = ((Integer) ids.get(edge.getFromNode())).intValue();
			int t = ((Integer) ids.get(edge.getToNode())).intValue();
			fw.write("  <edge from=\"" + f + "\" to=\"" + t + "\"/>\n");
		}

	}

	protected boolean reduceSequential(GraphInterface graph) {
		LinkedList runs = new LinkedList();
		Set used = new HashSet();
		Node[] nodes = (Node[]) graph.getNodesSet().toArray(new Node[0]);
		for (int i = 0; i < nodes.length; i++) {
			if ((nodes[i].inDegree() <= 1) && (nodes[i].outDegree() <= 1)
				&& !used.contains(nodes[i])) {
				Node first = nodes[i];
				if (first.inDegree() > 0) {
					Node prev = prev(first);
					while ((prev.inDegree() == 1) && (prev.outDegree() == 1)) {
						first = prev;
						prev = prev(prev);
					}
					if (prev.outDegree() == 1) {
						first = prev;
					}
				}
				Node last = nodes[i];
				if (last.outDegree() > 0) {
					Node next = next(last);
					while ((next.outDegree() == 1) && (next.inDegree() == 1)) {
						last = next;
						next = next(next);
					}
					if (next.inDegree() == 1) {
						last = next;
					}
				}
				if (first != last) {
					LinkedList l = new LinkedList();
					l.add(first);
					used.add(first);
					while (first != last) {
						first = next(first);
						used.add(first);
						l.add(first);
					}
					runs.add(l);
				}
			}
		}
		if (runs.size() == 0) {
			return false;
		}
		else {
			Iterator r = runs.iterator();
			while (r.hasNext()) {
				LinkedList runNodes = (LinkedList) r.next();
				Node first = (Node) runNodes.getFirst();
				Node last = (Node) runNodes.getLast();
				Edge[] in;
				Edge[] out;
				if (first.inDegree() > 0) {
					in = (Edge[]) first.getInEdges().toArray(new Edge[0]);
				}
				else {
					in = new Edge[0];
				}
				if (last.outDegree() > 0) {
					out = (Edge[]) last.getOutEdges().toArray(new Edge[0]);
				}
				else {
					out = new Edge[0];
				}
				Sequential seq = new Sequential();
				Iterator n = runNodes.iterator();
				Node node = null;
				while (n.hasNext()) {
					node = (Node) n.next();
					Object contents = node.getContents();
					if (contents instanceof Component) {
						seq.addComponent((Component) contents);
					}
					else {
						seq.addComponent(new Component(contents));
					}
					graph.removeNode(node);
				}
				if (node.outDegree() == 0) {
					seq.addComponent(new Filler());
				}
				seq.adjustAll();
				Node s = graph.addNode(seq);
				int j;
				for (j = 0; j < in.length; j++) {
					graph.addEdge(in[j].getFromNode(), s, null);
				}
				for (j = 0; j < out.length; j++) {
					graph.addEdge(s, out[j].getToNode(), null);
				}
			}
			return true;
		}
	}

	protected Node next(Node n) {
		return ((Edge) n.getOutEdges().get(0)).getToNode();
	}

	protected Node prev(Node n) {
		return ((Edge) n.getInEdges().get(0)).getFromNode();
	}

	protected boolean reduceParallel(GraphInterface graph) {
		LinkedList parallels = new LinkedList();
		Node[] nodes = (Node[]) graph.getNodesSet().toArray(new Node[0]);
		for (int i = 0; i < nodes.length; i++) {
			Map m = new Hashtable();
			if (nodes[i].outDegree() > 1) {
				EdgeIterator e = nodes[i].getOutEdgesIterator();
				while (e.hasMoreEdges()) {
					Edge edge = e.nextEdge();
					Node t = edge.getToNode();
					if (t.inDegree() == 1) {
						Object key;
						if (t.outDegree() == 0) {
							key = Boolean.TRUE;
						}
						else if (t.outDegree() == 1) {
							key = ((Edge) t.getOutEdges().get(0)).getToNode();
						}
						else {
							continue;
						}
						List par = (List) m.get(key);
						if (par == null) {
							par = new LinkedList();
							m.put(key, par);
						}
						par.add(t);
					}
				}
			}
			Iterator j = m.keySet().iterator();
			while (j.hasNext()) {
				Object key = j.next();
				List par = (List) m.get(key);
				if (par.size() > 1) {
					parallels.add(par);
				}
			}
		}
		if (parallels.size() == 0) {
			return false;
		}
		else {
			Iterator j = parallels.iterator();
			while (j.hasNext()) {
				Parallel par = new Parallel();
				List list = (List) j.next();
				Iterator l = list.iterator();
				Node from = null;
				Node to = null;
				while (l.hasNext()) {
					Node n = (Node) l.next();
					if (from == null) {
						from = prev(n);
						if (n.outDegree() == 1) {
							to = next(n);
						}
					}
					Object contents = n.getContents();
					if (contents instanceof Component) {
						par.addComponent((Component) contents);
					}
					else {
						if (n.outDegree() == 0) {
							par.addComponent(new Filler(contents));
						}
						else {
							par.addComponent(new Component(contents));
						}
					}
					graph.removeNode(n);
				}
				par.adjustAll();
				Node p = graph.addNode(par);
				graph.addEdge(from, p, null);
				if (to != null) {
					graph.addEdge(p, to, null);
				}
			}
			return true;
		}
	}

	protected boolean reduceParallelForced(GraphInterface graph) {
		LinkedList parallels = new LinkedList();
		Set processed = new HashSet();
		Node[] nodes = (Node[]) graph.getNodesSet().toArray(new Node[0]);
		for (int i = 0; i < nodes.length; i++) {
			if ((nodes[i].outDegree() > 1) && !processed.contains(nodes[i])) {
				boolean valid = true;
				List par = new LinkedList();
				par.add(nodes[i]);
				EdgeIterator e = nodes[i].getOutEdgesIterator();
				while (e.hasMoreEdges()) {
					Edge edge = e.nextEdge();
					Node t = edge.getToNode();
					if (processed.contains(t)) {
						valid = false;
						break;
					}
					processed.add(t);
					par.add(t);
				}
				if (valid) {
					parallels.add(par);
					processed.add(nodes[i]);
				}
			}
		}
		processed = null;
		if (parallels.size() == 0) {
			return false;
		}
		else {
			Iterator j = parallels.iterator();
			while (j.hasNext()) {
				Parallel par = new Parallel();
				List list = (List) j.next();
				Node from = (Node) list.remove(0);
				Iterator l = list.iterator();
				List to = new LinkedList();
				while (l.hasNext()) {
					Node n = (Node) l.next();
					EdgeIterator ei = n.getOutEdgesIterator();
					while (ei.hasMoreEdges()) {
						Node next = ei.nextEdge().getToNode();
						//if it's inside this parallel block, don't add it
						if (!list.contains(next)) {
							to.add(next);
						}
					}
					Object contents = n.getContents();
					if (contents instanceof Component) {
						par.addComponent((Component) contents);
					}
					else {
						if (to.isEmpty()) {
							par.addComponent(new Filler(contents));
						}
						else {
							par.addComponent(new Component(contents));
						}
					}
					graph.removeNode(n);
				}
				par.adjustAll();
				Node p = graph.addNode(par);
				graph.addEdge(from, p, null);
				Iterator tos = to.iterator();
				while (tos.hasNext()) {
					graph.addEdge(p, (Node) tos.next(), null);
				}
			}
			return true;
		}
	}
	
	public void setIgnoredEdges(Set ignoredEdges) {
		this.ignoredEdges = ignoredEdges;
	}

	public static class Component {
		public int width;
		public int height;
		public float x, y, w, h;
		private Object obj;

		private Component() {
		}

		public Component(Object o) {
			set(o);
			width = 1;
			height = 1;
		}

		public Object get() {
			return obj;
		}

		public void set(Object o) {
			this.obj = o;
		}

		public boolean isEmpty() {
			return (obj == null);
		}
		
		protected String indent(int count) {
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < count; i++) {
				sb.append(' ');
			}
			return sb.toString();
		}
		
		public String toString() {
			return toString(0);
		}
		
		public String toString(int indent) {
			return "C";
		}
	}

	public static class Filler extends Component {
		public Filler() {
			super();
			width = 0;
			height = 0;
		}

		public Filler(Object o) {
			super(o);
		}
		
		public String toString(int indent) {
			return "F";
		}
	}

	public static class Container extends Component {
		protected LinkedList list;

		public Container() {
			list = new LinkedList();
		}

		public boolean addComponent(Component c) {
			adjustSize(c.width, c.height);
			return list.add(c);
		}

		public void adjustSize(int width, int height) {
		}

		public void adjustAll() {
		}

		public boolean isEmpty() {
			return list.isEmpty();
		}

		public Iterator iterator() {
			return list.iterator();
		}
		
		public int childCount() {
			return list.size();
		}
		
		public String toString(int indent) {
			StringBuffer sb = new StringBuffer();
			indent+=4;
			sb.append("[\n");
			sb.append(indent(indent));
			Iterator i = iterator();
			while (i.hasNext()) {
				sb.append(((Component) i.next()).toString(indent));
				if (i.hasNext()) {
					sb.append(", ");
				}
			}
			sb.append('\n');
			sb.append(indent(indent-4));
			sb.append(']');
			return sb.toString();
		}
	}

	public static class Sequential extends Container {
		public void adjustSize(int width, int height) {
			if (width > this.width) {
				this.width = width;
			}
			this.height += height;
		}

		public void adjustAll() {
		}
		
		public String toString(int indent) {
			return "\n"+indent(indent)+"S"+super.toString(indent);
		}
	}

	public static class Parallel extends Container {
		private boolean left;

		public void adjustSize(int width, int height) {
			if (height > this.height) {
				this.height = height;
			}
			this.width += width;
		}

		public void adjustAll() {
			Iterator i = list.iterator();
			while (i.hasNext()) {
				((Component) i.next()).height = this.height;
			}
			height += Math.log(width);
		}

		public boolean addComponent(Component c) {
			if (list.size() > 0) {
				adjustSize(c.width, c.height);
				if (left) {
					ListIterator li = list.listIterator(list.size());
					Component s = (Component) li.previous();
					while ((s.height > c.height) && (li.hasPrevious())) {
						s = (Component) li.previous();
					}
					int index = li.previousIndex() + 2;
					list.add(index, c);
				}
				else {
					ListIterator li = list.listIterator(0);
					Component s = (Component) li.next();
					while ((s.height > c.height) && (li.hasNext())) {
						s = (Component) li.next();
					}
					int index = li.nextIndex() - 1;
					list.add(index, c);
				}
				left = !left;
				return true;
			}
			else {
				return super.addComponent(c);
			}
		}
		
		public String toString(int indent) {
			return "\n"+indent(indent)+"P"+super.toString(indent);
		}
	}

	public void setStatusManager(StatusManager statusManager) {
		this.statusManager = statusManager;
	}

	public StatusManager getStatusManager() {
		return this.statusManager;
	}
}
