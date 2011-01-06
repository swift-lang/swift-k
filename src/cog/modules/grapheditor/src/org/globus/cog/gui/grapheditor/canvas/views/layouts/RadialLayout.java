
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
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.globus.cog.util.graph.Edge;
import org.globus.cog.util.graph.GraphInterface;
import org.globus.cog.util.graph.Node;

public class RadialLayout implements GraphLayoutEngine {

	private Hashtable coords, fixedNodes;
	private Hashtable indices;
	private int[] cx, cy;
	private boolean[] visited, set;
	private Node[] nodes;
	private List traverseQ;
	private int gl = 1;
	private static final int springLen = 60;

	public RadialLayout() {

	}

	public Hashtable layoutGraph(GraphInterface graph, Hashtable fixedNodes) {
		if (graph == null) {
			return null;
		}
		if (graph.nodeCount() == 0) {
			return null;
		}
		gl = (int) Math.log(graph.nodeCount());
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
		this.fixedNodes = null;
		if (root != null) {
			graph.removeNode(root);
		}
		return ret;
	}

	private void preLayout() {

		int maxi = 0;
		int maxc = nodes[0].degree();
		for (int i = 1; i < nodes.length; i++) {
			Node n = nodes[i];
			if (n.degree() > maxc) {
				maxc = n.degree();
				maxi = i;
			}
		}
		if (!fixedNodes.containsKey(nodes[maxi])) {
			cx[maxi] = 0;
			cy[maxi] = 0;
			set[maxi] = true;
		}
		traverseQ = new LinkedList();
		traverseQ.add(new TQ(maxi, 0.0, 0, 0, 0, -1, Math.PI * 2));
		while (traverseQ.size() > 0) {
			TQ tq = (TQ) traverseQ.remove(0);
			lWalk(tq.index, tq.shift, tq.x, tq.y, tq.dr, tq.in, tq.angleRestrict);
		}
	}

	private void lWalk(int ni, double shift, int x, int y, int dr, int inn, double angleRestrict) {
		if (visited[ni]) {
			return;
		}
		if (dr < 0) {
			dr = 0;
		}
		double ar = dr * 5;
		visited[ni] = true;
		Node n = nodes[ni];
		double rangeAbove = (2 * Math.PI + angleRestrict) / 2 * n.inDegree() / n.degree();
		double rangeBelow = (2 * Math.PI + angleRestrict) / 2 * n.outDegree() / n.degree();
		double angleAbove;
		Iterator i = n.getInEdgesIterator();
		int k = 0;
		int ic = 0;
		while (i.hasNext()) {
			if (indexOf(((Edge) i.next()).getFromNode()) == inn) {
				ic = k;
				break;
			}
			k++;
		}
		i = n.getOutEdgesIterator();
		k = 0;
		int oc = 0;
		while (i.hasNext()) {
			if (indexOf(((Edge) i.next()).getToNode()) == inn) {
				oc = k;
				break;
			}
			k++;
		}

		angleAbove = rangeAbove / (n.inDegree() + 1 + ar);
		double angleBelow;
		angleBelow = rangeBelow / (n.outDegree() + 1 + ar);
		shift = shift - angleAbove * ic + angleBelow * oc;
		double angleAboveCrt = -rangeAbove / 2 - Math.PI / 2 + (ar / 2 + 1) * angleAbove + shift;
		Iterator in = n.getInEdgesIterator();
		while (in.hasNext()) {
			Edge e = (Edge) in.next();
			Node m = e.getFromNode();
			int mi = indexOf(m);
			int dx = (int) (Math.cos(angleAboveCrt) * springLen * (2 + gl / (dr + 1)));
			int dy = (int) (Math.sin(angleAboveCrt) * springLen * (2 + gl / (dr + 1)));
			if ((!fixedNodes.containsKey(m)) && (!set[mi])) {
				cx[mi] = x + dx;
				cy[mi] = y + dy;
				set[mi] = true;
			}
			angleAboveCrt += angleAbove;
		}

		double angleBelowCrt = Math.PI / 2 + rangeBelow / 2 - (ar / 2 + 1) * angleBelow + shift;
		Iterator on = n.getOutEdgesIterator();
		while (on.hasNext()) {
			Edge e = (Edge) on.next();
			Node m = e.getToNode();
			int mi = indexOf(m);
			int dx = (int) (Math.cos(angleBelowCrt) * springLen * (2 + (gl / (dr + 1))));
			int dy = (int) (Math.sin(angleBelowCrt) * springLen * (2 + (gl / (dr + 1))));
			if ((!fixedNodes.containsKey(m)) && (!set[mi])) {
				cx[mi] = x + dx;
				cy[mi] = y + dy;
				set[mi] = true;
			}
			angleBelowCrt -= angleBelow;
		}

		angleAboveCrt = -rangeAbove / 2 - Math.PI / 2 + (ar / 2 + 1) * angleAbove + shift;
		in = n.getInEdgesIterator();
		while (in.hasNext()) {
			Edge e = (Edge) in.next();
			Node m = e.getFromNode();
			int mi = indexOf(m);
			traverseQ.add(
				new TQ(
					mi,
					angleAboveCrt + Math.PI / 2,
					cx[mi],
					cy[mi],
					dr + 100 / gl / n.degree(),
					ni,
					angleAbove));
			angleAboveCrt += angleAbove;
		}
		angleBelowCrt = Math.PI / 2 + rangeBelow / 2 - (ar / 2 + 1) * angleBelow + shift;
		on = n.getOutEdgesIterator();
		while (on.hasNext()) {
			Edge e = (Edge) on.next();
			Node m = e.getToNode();
			int mi = indexOf(m);
			traverseQ.add(
				new TQ(
					mi,
					angleBelowCrt - Math.PI / 2,
					cx[mi],
					cy[mi],
					dr + 100 / gl / n.degree(),
					ni,
					angleBelow));
			angleBelowCrt -= angleBelow;
		}
	}

	private int indexOf(Node n) {
		return ((Integer) indices.get(n)).intValue();
	}

	private class TQ {
		int index;
		double shift, angleRestrict;
		int x;
		int y;
		int dr;
		int in;

		public TQ(int index, double shift, int x, int y, int dr, int in, double angleRestrict) {
			this.index = index;
			this.shift = shift;
			this.x = x;
			this.y = y;
			this.dr = dr;
			this.in = in;
			this.angleRestrict = angleRestrict;
		}
	}
}
