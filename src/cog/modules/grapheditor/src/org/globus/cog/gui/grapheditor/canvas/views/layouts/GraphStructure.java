
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------


/*
 * Created on Jun 25, 2003
 */
package org.globus.cog.gui.grapheditor.canvas.views.layouts;


import java.awt.Point;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;
import org.globus.cog.gui.grapheditor.GraphComponent;
import org.globus.cog.gui.grapheditor.targets.swing.views.GraphView;
import org.globus.cog.util.graph.Edge;
import org.globus.cog.util.graph.EdgeIterator;
import org.globus.cog.util.graph.GraphChangedEvent;
import org.globus.cog.util.graph.GraphInterface;
import org.globus.cog.util.graph.GraphListener;
import org.globus.cog.util.graph.Node;
import org.globus.cog.util.graph.NodeIterator;

public class GraphStructure implements GraphListener {
	private static Logger logger = Logger.getLogger(GraphStructure.class);

	private int[] pts;
	private Point[] points;
	private String hash;
	private GraphInterface graph;
	public volatile boolean dirty;
	private long pcount;
	public static final long maxperm = 50000;

	public GraphStructure(String hash, String pts) {
		dirty = false;
		initialize(hash, pts);
	}

	public GraphStructure(GraphInterface graph) {
		dirty = false;
		this.graph = graph;
		initialize(graph);
	}

	public void updateCoords() {
		if (graph != null) {
			int[] rpts = new int[graph.nodeCount()];
			for (int i = 0; i < graph.nodeCount(); i++) {
				rpts[pts[i]] = i;
			}
			Iterator nodes = graph.getNodesIterator();
			int i = 0;
			while (nodes.hasNext()) {
				Node n = (Node) nodes.next();
				if (n.getContents() instanceof GraphComponent) {
					GraphComponent gc = (GraphComponent) n.getContents();
					if (gc.hasProperty(GraphView.LOCATION)) {
						points[rpts[i++]] = (Point) gc
							.getPropertyValue(GraphView.LOCATION);
					}
				}
			}
		}
	}

	private void initialize(String hash, String pts) {
		StringTokenizer st = new StringTokenizer(hash, ".");
		int l = atoi(st.nextToken());
		points = new Point[l];
		st = new StringTokenizer(pts, " ");
		int s = 0;
		int i = 0;
		int x = 0, y = 0;
		while (st.hasMoreTokens()) {
			if (s == 0) {
				x = atoi(st.nextToken());
			}
			else {
				y = atoi(st.nextToken());
				points[i++] = new Point(x, y);
			}
			s = 1 - s;
		}
		if (i != l) {
			logger.warn("Error in graph database: " + "\n" + hash + "\n" + pts);
		}
	}

	private int atoi(String a) {
		return new Integer(a).intValue();
	}

	private void initialize(GraphInterface graph) {
		initializeGeneric(graph);
	}

	public static boolean isDAG(GraphInterface graph) {
		HashSet traversed = new HashSet();
		//find all nodes with no incoming edges and start
		//traversing the graph from there to find if there
		//are any cycles
		NodeIterator ni = graph.getNodesIterator();
		while (ni.hasMoreNodes()) {
			Node n = ni.nextNode();
			if (n.inDegree() == 0) {
				if (hasCycle(n, traversed)) {
					return false;
				}
			}
		}
		return true;
	}

	private static boolean hasCycle(Node n, HashSet traversed) {
		if (traversed.contains(n)) {
			return true;
		}
		else {
			traversed.add(n);
			EdgeIterator ei = n.getOutEdgesIterator();
			while (ei.hasMoreEdges()) {
				if (hasCycle(ei.nextEdge().getToNode(), traversed)) {
					traversed.remove(n);
					return true;
				}
			}
			traversed.remove(n);
			return false;
		}
	}

	private void initializeGeneric(GraphInterface graph) {
		this.graph = graph;
		//incidence matrix
		int nn = graph.nodeCount();
		short[][] adj = new short[nn][nn];
		points = new Point[nn];
		for (int i = 0; i < nn; i++) {
			Arrays.fill(adj[i], (short) 1);
		}
		Hashtable indices = new Hashtable();
		Iterator nodes = graph.getNodesIterator();
		int count = 0;
		while (nodes.hasNext()) {
			indices.put(nodes.next(), new Integer(count++));
		}
		Iterator i = graph.getEdgesIterator();
		while (i.hasNext()) {
			Edge e = (Edge) i.next();
			int ifrom = ((Integer) indices.get(e.getFromNode())).intValue();
			int ito = ((Integer) indices.get(e.getToNode())).intValue();
			adj[ifrom][ito] = 2;
			adj[ito][ifrom] = 0;
		}

		int[] p = new int[nn];
		for (int j = 0; j < nn; j++) {
			p[j] = j;
		}
		pts = copy(p);
		if (graph.nodeCount() < 14) {
			pcount = 1;
			perm(p, 0, nn - 1, adj);
			long hash = 0;
			for (int k = 0; k < nn; k++) {
				for (int l = 0; l < nn; l++) {
					hash = hash * 3 + adj[pts[l]][pts[k]];
				}
			}
		}
		this.hash = nn + "." + graph.edgeCount() + "." + hash;
	}

	private void perm(int[] a, int index, int n, short[][] m) {
		if (pcount++ >= maxperm) {
			return;
		}
		if (index == n) {
			if (larger(a, pts, m)) {
				pts = copy(a);
			}
		}
		else {
			for (int i = index; i <= n; i++) {
				swap(a, index, i);
				perm(a, index + 1, n, m);
				swap(a, index, i);
				if (pcount > maxperm) {
					break;
				}
			}
		}
	}

	private boolean larger(int[] a, int[] b, short[][] m) {
		for (int i = 0; i < a.length; i++) {
			for (int j = 0; j < a.length; j++) {
				if (m[a[i]][a[j]] > m[b[i]][b[j]]) {
					return true;
				}
				if (m[a[i]][a[j]] < m[b[i]][b[j]]) {
					return false;
				}
			}
		}
		return false;
	}

	private int[] copy(int[] a) {
		int[] r = new int[a.length];
		for (int i = 0; i < a.length; i++) {
			r[i] = a[i];
		}
		return r;
	}

	private void swap(int[] array, int i, int j) {
		int t = array[i];
		array[i] = array[j];
		array[j] = t;
	}

	public int getCanonicalIndex(int i) {
		return pts[i];
	}

	public Point getPoint(int i) {
		return points[i];
	}

	public String getHash() {
		return hash;
	}

	public String getCoords() {
		String space = " ";
		StringBuffer b = new StringBuffer();
		for (int i = 0; i < points.length; i++) {
			b.append(points[i].x);
			b.append(space);
			b.append(points[i].y);
			b.append(space);
		}
		return b.toString();
	}

	public void graphChanged(GraphChangedEvent e) {
		//initialize(graph);
	}
}
