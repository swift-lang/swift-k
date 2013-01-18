
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------


/*
 * Created on Aug 26, 2003
 */
package org.globus.cog.gui.grapheditor.canvas.views.layouts;


import java.awt.Point;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.globus.cog.util.graph.Edge;
import org.globus.cog.util.graph.EdgeIterator;
import org.globus.cog.util.graph.GraphInterface;
import org.globus.cog.util.graph.Node;

public class RadialHierarchicalLayout implements GraphLayoutEngine {

	private Hashtable coords, fixedNodes;
	private Hashtable indices;
	private int[] cx, cy;
	private boolean[] visited, set;
	private Node[] nodes;
	private List traverseQ;
	private List levels;
	private int minl, maxl;
	private static int defaultHeight = 80;
	private static final int ringSize = 100;
	private static final double stretch = 0.9;
	private Node root, tail;
	private boolean inverted, oneroot, onetail;

	public Hashtable layoutGraph(GraphInterface graph, Hashtable fixedNodes) {
		if (graph == null) {
			return null;
		}
		if (graph.nodeCount() == 0) {
			return null;
		}
		this.fixedNodes = fixedNodes;
		inverted = false;
		oneroot = false;
		onetail = false;
		List roots = new LinkedList();
		List tails = new LinkedList();
		Iterator ni = graph.getNodesIterator();
		for (int i = 0; i < graph.nodeCount(); i++) {
			Node n = (Node) ni.next();
			if (n.inDegree() == 0) {
				roots.add(n);
			}
			if (n.outDegree() == 0) {
				tails.add(n);
			}
		}
		if (roots.size() > 1) {
			root = graph.addNode();
			Iterator li = roots.iterator();
			while (li.hasNext()) {
				graph.addEdge(root, (Node) li.next(), null);
			}
		}
		else if (roots.size() == 1) {
			root = (Node) roots.get(0);
			oneroot = true;
		}

		if (tails.size() > 1) {
			tail = graph.addNode();
			Iterator li = tails.iterator();
			while (li.hasNext()) {
				graph.addEdge((Node) li.next(), tail, null);
			}
		}
		else if (tails.size() == 1) {
			tail = (Node) tails.get(0);
			onetail = true;
		}

		cx = new int[graph.nodeCount()];
		cy = new int[graph.nodeCount()];
		visited = new boolean[graph.nodeCount()];
		set = new boolean[graph.nodeCount()];
		indices = new Hashtable();
		int sq = (int) Math.sqrt(graph.nodeCount());
		int x = 0, y = 0;
		nodes = new Node[graph.nodeCount()];
		ni = graph.getNodesIterator();
		for (int i = 0; i < graph.nodeCount(); i++) {
			Node n = (Node) ni.next();
			nodes[i] = n;
			indices.put(n, new Integer(i));
			if (!fixedNodes.containsKey(n)) {
				cx[i] = (x + 1) * 40;
				cy[i] = (y + 1) * 40;
			}
			else {
				Point p = (Point) fixedNodes.get(n);
				cx[i] = p.x;
				cy[i] = p.y;
			}
			x++;
			if (x == sq) {
				x = 0;
				y++;
			}
			visited[i] = false;
			set[i] = false;
		}
		preLayout();
		coords = new Hashtable();
		for (int i = 0; i < nodes.length; i++) {
			coords.put(nodes[i], new Point(cx[i], cy[i]));
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
		this.fixedNodes = null;
		if (roots.size() > 1) {
			graph.removeNode(root);
			root = null;
		}
		if (tails.size() > 1) {
			graph.removeNode(tail);
			tail = null;
		}
		return ret;
	}

	private void preLayout() {
		int maxi = 0;
		int maxc = nodes[0].degree();
		visited[0] = false;
		boolean center = false;
		if (oneroot) {
			if (onetail) {
				if (tail.inDegree() > root.outDegree()) {
					inverted = true;
				}
			}
			center = true;
		}
		else if (onetail) {
			center = true;
			inverted = true;
		}
		else {
			if (root != null) {
				if (tail != null) {
					inverted = true;
				}
				center = true;
			}
			else if (tail != null) {
				inverted = true;
				center = true;
			}
			else {
				for (int i = 1; i < nodes.length; i++) {
					Node n = nodes[i];
					if (n.degree() > maxc) {
						maxc = n.degree();
						maxi = i;
					}
					visited[i] = false;
				}
			}
		}
		if (center) {
			if (inverted) {
				maxi = indexOf(tail);
				maxc = tail.degree();
			}
			else {
				maxi = indexOf(root);
				maxc = root.degree();
			}
		}
		if (!fixedNodes.containsKey(nodes[maxi])) {
			cx[maxi] = 0;
			cy[maxi] = 0;
			set[maxi] = true;
			if (nodes[maxi].inDegree() < nodes[maxi].outDegree()) {
				inverted = true;
			}
		}
		traverseQ = new LinkedList();
		traverseQ.add(new TQ(maxi, -minl));
		while (!traverseQ.isEmpty()) {
			TQ tq = (TQ) traverseQ.remove(0);
			countLevels(tq.index, tq.level);
		}
		int nlevels = maxl - minl + 1;
		levels = new ArrayList(nlevels);
		for (int i = 0; i < nlevels; i++) {
			levels.add(new LinkedList());
		}
		for (int i = 0; i < nodes.length; i++) {
			visited[i] = false;
		}
		traverseQ = new LinkedList();
		traverseQ.add(new TQ(maxi, -minl));
		while (!traverseQ.isEmpty()) {
			TQ tq = (TQ) traverseQ.remove(0);
			fillLevels(tq.index, tq.level);
		}
		for (int i = 0; i < levels.size(); i++) {
			List l = (List) levels.get(i);
			if (maxc < l.size()) {
				maxc = l.size();
			}
		}
		if (defaultHeight < maxc / 5) {
			defaultHeight = maxc / 5;
		}
		int netMax = ((-minl > maxl) ? -minl : maxl) + 1;
		int[] ringOccupancy = new int[netMax];
		int[] radii = new int[netMax];
		for (int i = 0; i < netMax; i++) {
			ringOccupancy[i] = 0;
			radii[i] = 0;
		}
		for (int i = -minl; i < levels.size(); i++) {
			List l = (List) levels.get(i);
			ringOccupancy[i + minl] = l.size();
		}
		for (int i = 0; i < -minl; i++) {
			List l = (List) levels.get(i);
			int ring = -minl - i;
			ringOccupancy[ring] += l.size();
		}
		for (int i = 1; i < netMax; i++) {
			radii[i] = radii[i - 1] + ringSize + 2 * ringOccupancy[i];
		}
		Set pl = new HashSet();
		double[] angles = new double[nodes.length];
		double spc = Math.PI * 2;
		for (int i = 0; i < levels.size(); i++) {
			List l = (List) levels.get(i);
			if (l.size() == 0) {
				continue;
			}

			int ring = i;
			double angleRange = 2 * Math.PI * (l.size()) / ringOccupancy[ring]
				* stretch;
			double angleSpacing = angleRange / l.size();
			double radius;
			radius = radii[ring];
			if (angleSpacing < spc) {
				spc = angleSpacing;
			}
			double startAngle;
			if (inverted) {
				startAngle = Math.PI / 2 - angleRange / 2 + angleSpacing / 2;
			}
			else {
				startAngle = Math.PI / 2 + angleRange / 2;
			}
			double[] xs = new double[l.size() + 1];
			int[] idx = new int[l.size() + 1];
			idx[0] = Integer.MAX_VALUE;
			Set newpl = new HashSet();
			for (int j = 0; j < l.size(); j++) {
				int index = ((Integer) l.get(j)).intValue();
				Node n = nodes[index];
				EdgeIterator ei = n.getInEdgesIterator();
				int count = 0;
				double score = 0;
				while (ei.hasNext()) {
					Node p = ei.nextEdge().getFromNode();
					if (pl.contains(p)) {
						int pindex = indexOf(p);
						count++;
						score += angles[pindex];
					}
				}
				newpl.add(n);
				double angle;
				if (count == 0) {
					angle = -j * angleSpacing - startAngle;
				}
				else {
					angle = score / count;
				}
				insert(idx, xs, index, angle);
			}
			for (int j = 0; j < xs.length - 1; j++) {
				if (xs[j + 1] - spc < xs[j]) {
					xs[j + 1] = xs[j] + spc;
				}
			}
			pl = newpl;

			for (int j = 0; j < l.size(); j++) {
				double angle = xs[j];
				int index = idx[j];
				angles[index] = angle;
				cx[index] = (int) (radius * Math.cos(angle));
				cy[index] = (int) (radius * Math.sin(angle));
			}
		}
	}

	private void insert(int[] indices, double[] scores, int index, double score) {
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
		l.add(new Integer(index));
		Iterator i = n.getInEdgesIterator();
		while (i.hasNext()) {
			Edge e = (Edge) i.next();
			Node m = e.getFromNode();
			int mi = indexOf(m);
			if (inverted) {
				traverseQ.add(new TQ(mi, crtlevel + 1));
			}
			else {
				traverseQ.add(new TQ(mi, crtlevel - 1));
			}
		}
		i = n.getOutEdgesIterator();
		while (i.hasNext()) {
			Edge e = (Edge) i.next();
			Node m = e.getToNode();
			int mi = indexOf(m);
			if (inverted) {
				traverseQ.add(new TQ(mi, crtlevel - 1));
			}
			else {
				traverseQ.add(new TQ(mi, crtlevel + 1));
			}
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
			if (inverted) {
				traverseQ.add(new TQ(mi, crtlevel + 1));
			}
			else {
				traverseQ.add(new TQ(mi, crtlevel - 1));
			}

		}
		i = n.getOutEdgesIterator();
		while (i.hasNext()) {
			Edge e = (Edge) i.next();
			Node m = e.getToNode();
			int mi = indexOf(m);
			if (inverted) {
				traverseQ.add(new TQ(mi, crtlevel - 1));
			}
			else {
				traverseQ.add(new TQ(mi, crtlevel + 1));
			}
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
