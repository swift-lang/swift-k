
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------


package org.globus.cog.gui.grapheditor.canvas.views.layouts;


import java.awt.Point;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.globus.cog.util.graph.Edge;
import org.globus.cog.util.graph.EdgeIterator;
import org.globus.cog.util.graph.GraphInterface;
import org.globus.cog.util.graph.Node;

/**
 * Adds distribution of edges around the node so they tend to have equal angles
 * between them. Incoming edges will be coming from above, while outgoing edges
 * will go downwards. The behaviour can be easily reversed by turning your
 * monitor upside-down
 */
public class HierarchicalLayout implements GraphLayoutEngine {
	private static Logger logger = Logger.getLogger(HierarchicalLayout.class);

	private GraphInterface graph;
	private Hashtable coords, fixedNodes;
	private Hashtable indices;
	private int[] cx, cy;
	private boolean[] visited, set;
	private int[] heights;
	private Node[] nodes;
	private List traverseQ;

	private List levels;
	private List longestPath;
	private int minl, maxl;
	private static int defaultWidth = 80;
	private static int defaultHeight = 80;
	private boolean dag;
	private int maxlen;

	public HierarchicalLayout() {

	}

	public Hashtable layoutGraph(GraphInterface graph, Hashtable fixedNodes) {
		if (graph == null) {
			return null;
		}
		if (graph.nodeCount() == 0) {
			return null;
		}
		this.graph = graph;
		this.fixedNodes = fixedNodes;

		List roots = new LinkedList();
		Iterator ni = graph.getNodesIterator();
		for (int i = 0; i < graph.nodeCount(); i++) {
			Node n = (Node) ni.next();
			if (n.inDegree() == 0) {
				roots.add(n);
			}
		}
		Node root = null;
		if (roots.size() > 1) {
			root = graph.addNode();
			Iterator li = roots.iterator();
			while (li.hasNext()) {
				graph.addEdge(root, (Node) li.next(), null);
			}
		}

		cx = new int[graph.nodeCount()];
		cy = new int[graph.nodeCount()];
		visited = new boolean[graph.nodeCount()];
		set = new boolean[graph.nodeCount()];
		indices = new Hashtable();
		nodes = new Node[graph.nodeCount()];
		ni = graph.getNodesIterator();
		for (int i = 0; i < graph.nodeCount(); i++) {
			Node n = (Node) ni.next();
			nodes[i] = n;
			if (n.inDegree() == 0) {
				roots.add(n);
			}
			indices.put(n, new Integer(i));
			visited[i] = false;
			set[i] = false;
		}

		maxlen = -1;
		for (int i = 0; i < graph.nodeCount(); i++) {
			if (nodes[i].inDegree() == 0) {
				findLongest(i);
			}
		}
		if (maxlen == -1) {
			layoutCyclic();
		}
		else {
			logger.info("DAG: " + maxlen);
			layoutDag();
		}
		coords = new Hashtable();
		for (int i = 0; i < nodes.length; i++) {
			Node n = nodes[i];
			if (!fixedNodes.containsKey(n)) {
				coords.put(n, new Point(cx[i], cy[i]));
			}
			else {
				coords.put(n, fixedNodes.get(n));
			}
		}
		indices = null;
		cx = null;
		cy = null;
		set = null;
		visited = null;
		Hashtable ret = coords;
		coords = null;
		nodes = null;
		levels = null;
		heights = null;
		longestPath = null;
		this.fixedNodes = null;
		if (root != null) {
			graph.removeNode(root);
		}
		return ret;
	}

	private int findLongest(int start) {
		boolean[] visited = new boolean[graph.nodeCount()];
		findLongest(start, visited, 0, new LinkedList());
		return maxlen;
	}

	private void findLongest(int start, boolean[] visited, int level,
		List indices) {
		visited[start] = true;
		indices.add(new Integer(start));
		if (nodes[start].outDegree() == 0) {
			if (level > maxlen) {
				maxlen = level;
				longestPath = new ArrayList(indices);
			}
			indices.remove(indices.size() - 1);
			visited[start] = false;
			return;
		}
		Iterator i = nodes[start].getOutEdgesIterator();
		while (i.hasNext()) {
			Edge out = (Edge) i.next();
			int next = indexOf(out.getToNode());
			if (!visited[next]) {
				findLongest(next, visited, level + 1, indices);
			}
		}
		indices.remove(indices.size() - 1);
		visited[start] = false;
		return;
	}

	private void layoutCyclic() {
		dag = false;
		layout();
	}

	private void layoutDag() {
		dag = true;
		layout();
	}

	private void layout() {
		int maxi = 0;
		int maxc = nodes[0].degree();
		visited[0] = false;
		for (int i = 1; i < nodes.length; i++) {
			Node n = nodes[i];
			if (n.degree() > maxc) {
				maxc = n.degree();
				maxi = i;
			}
			visited[i] = false;
		}
		if (dag) {
			maxi = ((Integer) longestPath.get(0)).intValue();
			minl = 0;
			maxl = maxlen;
		}
		if (!fixedNodes.containsKey(nodes[maxi])) {
			cx[maxi] = 0;
			cy[maxi] = 0;
			set[maxi] = true;
		}
		if (!dag) {
			traverseQ = new LinkedList();
			traverseQ.add(new TQ(maxi, 0));
			while (!traverseQ.isEmpty()) {
				TQ tq = (TQ) traverseQ.remove(0);
				countLevels(tq.index, tq.level);
			}
		}
		int nlevels = maxl - minl + 1;
		levels = new ArrayList(nlevels);
		heights = new int[nlevels];
		for (int i = 0; i < nlevels; i++) {
			levels.add(new LinkedList());
		}
		for (int i = 0; i < nodes.length; i++) {
			visited[i] = false;
		}
		traverseQ = new LinkedList();
		if (!dag) {
			traverseQ.add(new TQ(maxi, -minl));
		}
		else {
			for (int i = 0; i < longestPath.size(); i++) {
				traverseQ.add(new TQ(((Integer) longestPath.get(i)).intValue(),
					i));
			}
		}
		while (!traverseQ.isEmpty()) {
			TQ tq = (TQ) traverseQ.remove(0);
			fillLevels(tq.index, tq.level);
		}
		for (int i = 0; i < levels.size(); i++) {
			List l = (List) levels.get(i);
			if (maxc < l.size()) {
				maxc = l.size();
			}
			heights[i] = (int) (Math.log(heights[i]) * 100);
			if (heights[i] < 40) {
				heights[i] = 40;
			}
		}
		if (defaultHeight < maxc / 5) {
			defaultHeight = maxc / 5;
		}
		int crth = 0;
		Set pl = new HashSet();
		for (int i = 0; i < levels.size(); i++) {
			List l = (List) levels.get(i);
			if (l.size() == 0) {
				continue;
			}
			int spacing = defaultWidth * maxc / l.size();
			int[] xs = new int[l.size() + 1];
			int[] idx = new int[l.size() + 1];
			idx[0] = Integer.MAX_VALUE;
			Set newpl = new HashSet();
			for (int j = 0; j < l.size(); j++) {
				int index = ((Integer) l.get(j)).intValue();
				Node n = nodes[index];
				EdgeIterator ei = n.getInEdgesIterator();
				int count = 0;
				int score = 0;
				while (ei.hasNext()) {
					Node p = ei.nextEdge().getFromNode();
					if (pl.contains(p)) {
						int pindex = indexOf(p);
						count++;
						score += cx[pindex];
					}
				}
				newpl.add(n);
				int x;
				if (count == 0) {
					x = (spacing - defaultWidth) / 2 + spacing * j;
				}
				else {
					x = score / count;
				}
				insert(idx, xs, index, x);
			}
			int min = xs[0]-spacing/2;
			for (int j = 0; j < xs.length - 1; j++) {
				xs[j] -= min;
			}
			for (int j = 0; j < xs.length - 1; j++) {
				if (xs[j + 1] - spacing < xs[j]) {
					xs[j + 1] = xs[j] + spacing;
				}
			}
			pl = newpl;
			for (int j = 0; j < l.size(); j++) {
				int index = idx[j];
				cx[index] = xs[j];
				cy[index] = crth;
			}
			crth += heights[i];
		}
	}

	private void insert(int[] indices, int[] scores, int index, int score) {
		int i = 0;
		while ((indices[i] != Integer.MAX_VALUE) && (scores[i] <= score)) {
			i++;
		}
		int j = indices.length - 1;
		while (j > i) {
			indices[j] = indices[j - 1];
			scores[j] = scores[j - 1];
			j--;
		}
		indices[i] = index;
		scores[i] = score;
	}

	private void fillLevels(int index, int crtlevel) {
		if (visited[index]) {
			return;
		}
		visited[index] = true;
		List l = (List) levels.get(crtlevel);
		Node n = nodes[index];
		if (crtlevel > 0) {
			if (heights[crtlevel - 1] < n.inDegree()) {
				heights[crtlevel - 1] = n.inDegree();
			}
		}
		if (heights[crtlevel] < n.outDegree()) {
			heights[crtlevel] = n.outDegree();
		}
		Integer iIndex = new Integer(index);
		l.add(iIndex);
		Iterator i = n.getInEdgesIterator();
		while (i.hasNext()) {
			Edge e = (Edge) i.next();
			Node m = e.getFromNode();
			int mi = indexOf(m);
			traverseQ.add(new TQ(mi, crtlevel - 1));
		}

		i = n.getOutEdgesIterator();
		while (i.hasNext()) {
			Edge e = (Edge) i.next();
			Node m = e.getToNode();
			int mi = indexOf(m);
			traverseQ.add(new TQ(mi, crtlevel + 1));
		}
	}

	private void countLevels(int index, int crtlevel) {
		if (visited[index]) {
			return;
		}
		visited[index] = true;
		if (crtlevel < minl) {
			minl = crtlevel;
		}
		if (crtlevel > maxl) {
			maxl = crtlevel;
		}
		Node n = nodes[index];
		Iterator i = n.getInEdgesIterator();
		while (i.hasNext()) {
			Edge e = (Edge) i.next();
			Node m = e.getFromNode();
			int mi = indexOf(m);
			traverseQ.add(new TQ(mi, crtlevel - 1));
		}
		i = n.getOutEdgesIterator();
		while (i.hasNext()) {
			Edge e = (Edge) i.next();
			Node m = e.getToNode();
			int mi = indexOf(m);
			traverseQ.add(new TQ(mi, crtlevel + 1));
		}
	}

	private int indexOf(Node n) {
		return ((Integer) indices.get(n)).intValue();
	}

	private class TQ {
		int index;
		int level;

		public TQ(int index, int level) {
			this.index = index;
			this.level = level;
		}
	}
}
