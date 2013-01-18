
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

    
package org.globus.cog.gui.grapheditor.canvas.views.layouts;
import java.awt.Point;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.globus.cog.util.graph.Edge;
import org.globus.cog.util.graph.GraphInterface;
import org.globus.cog.util.graph.Node;

/**
 * Adds distribution of edges around the node so they tend to have equal angles
 * between them. Incoming edges will be coming from above, while outgoing edges
 * will go downwards. The behaviour can be easily reversed by turning your monitor upside-down
 */
public class ExtendedSpringLayout implements GraphLayoutEngine {
	private static Logger logger = Logger.getLogger(ExtendedSpringLayout.class);

	private GraphInterface graph;
	private Hashtable coords, fixedNodes;
	private Hashtable indices;
	private int[] cx, cy, deltax, deltay;
	private boolean[] visited, set;
	private double k = 0.02;
	private double rk = 0.2;
	private double ak = 0.2; //0.2
	private double visc = 150;
	private double springLen = 40;
	private double repelLen = 2400;
	private Node[] nodes;
	private List traverseQ;
	private int gl = 1;

	public void setPreferredEdgeLength(double len) {
		this.springLen = len;
	}

	public void setRepulsionDistance(double c) {
		this.repelLen = c * c;
	}

	public void setSpringConstant(double k) {
		this.k = k;
	}

	public void setRepulsionConstant(double rk) {
		this.rk = rk;
	}

	public void setAngularSpringConstant(double ak) {
		this.ak = ak;
	}

	public void setDampeningFactor(double visc) {
		this.visc = visc;
	}

	public Hashtable layoutGraph(GraphInterface graph, Hashtable fixedNodes) {
		if (graph == null) {
			return null;
		}
		if (graph.nodeCount() == 0) {
			return null;
		}
		this.graph = graph;
		gl = (int) Math.log(graph.nodeCount())+1;
		this.fixedNodes = fixedNodes;
		cx = new int[graph.nodeCount()];
		cy = new int[graph.nodeCount()];
		deltax = new int[graph.nodeCount()];
		deltay = new int[graph.nodeCount()];
		visited = new boolean[graph.nodeCount()];
		set = new boolean[graph.nodeCount()];
		indices = new Hashtable();
		int sq = (int) Math.sqrt(graph.nodeCount());
		int x = 0, y = 0;
		nodes = new Node[graph.nodeCount()];
		Iterator ni = graph.getNodesIterator();
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
		if (graph.nodeCount() < 30000){
		    for (int j = 0; j < 20; j++) {
				springEm();
		    }
		}
		else{
		    logger.info("Graph too big; skipping the full layouting");
		}
		coords = new Hashtable();
		ni = graph.getNodesIterator();
		for (int i = 0; i < graph.nodeCount(); i++) {
			coords.put(ni.next(), new Point(cx[i], cy[i]));
		}
		indices = null;
		cx = null;
		cy = null;
		deltax = null;
		deltay = null;
		set = null;
		nodes = null;
		visited = null;
		Hashtable ret = coords;
		coords = null;
		this.fixedNodes = null;
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
		traverseQ.add(new TQ(maxi, 0.0, 0, 0, 0, -1));
		while (traverseQ.size() > 0) {
			TQ tq = (TQ) traverseQ.remove(0);
			lWalk(tq.index, tq.shift, tq.x, tq.y, tq.dr, tq.in);
		}
	}

	private void lWalk(int ni, double shift, int x, int y, int dr, int inn) {
		if (visited[ni]) {
			return;
		}
		if (dr < 0) {
			dr = 0;
		}
		visited[ni] = true;
		Node n = nodes[ni];
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

		if (n.outDegree() == 0) {
			if (n.inDegree() == 0) {
				angleAbove = 0;
			}
			else {
				angleAbove = 2 * Math.PI / (n.inDegree()+dr);
			}
		}
		else {
			angleAbove = Math.PI / (n.inDegree() + 1+dr);
		}
		double angleBelow;
		if (n.inDegree() == 0) {
			if (n.outDegree() == 0) {
				angleBelow = 0;
			}
			else {
				angleBelow = 2 * Math.PI / (n.outDegree()+dr);
			}
		}
		else {
			angleBelow = Math.PI / (n.outDegree() + 1+dr);
		}
		shift = shift - angleAbove * ic + angleBelow * oc;
		double angleAboveCrt = -Math.PI + (dr/2+1)*angleAbove + shift;
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

		double angleBelowCrt = Math.PI - (dr/2+1)*angleBelow + shift;
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

		angleAboveCrt = -Math.PI + (dr/2 +1)*angleAbove + shift;
		in = n.getInEdgesIterator();
		while (in.hasNext()) {
			Edge e = (Edge) in.next();
			Node m = e.getFromNode();
			int mi = indexOf(m);
			traverseQ.add(new TQ(mi, angleAboveCrt + Math.PI / 2, cx[mi], cy[mi], dr + 100/gl/n.degree(), ni));
			angleAboveCrt += angleAbove;
		}
		angleBelowCrt = Math.PI - (dr/2 +1)*angleBelow + shift;
		on = n.getOutEdgesIterator();
		while (on.hasNext()) {
			Edge e = (Edge) on.next();
			Node m = e.getToNode();
			int mi = indexOf(m);
			traverseQ.add(new TQ(mi, angleBelowCrt - Math.PI / 2, cx[mi], cy[mi], dr + 100/gl/n.degree(), ni));
			angleBelowCrt -= angleBelow;
		}
	}

	private void springEm() {
		for (int i = 0; i < graph.nodeCount(); i++) {
			deltax[i] = 0;
			deltay[i] = 0;
		}

		//distributeEdges();
		Iterator it = graph.getEdgesIterator();
		while (it.hasNext()) {
			Edge edge = (Edge) it.next();
			springEdge(edge);
		}

		repelNodes();

		for (int i = 0; i < graph.nodeCount(); i++) {
			cx[i] += deltax[i];
			cy[i] += deltay[i];
		}
	}

	private void distributeEdges() {

		for (int i = 0; i < graph.nodeCount(); i++) {
			Node n = nodes[i];
			double angleAbove = Math.PI / (n.inDegree() + 1);
			double angleBelow = Math.PI / (n.outDegree() + 1);
			double angleAboveCrt = -Math.PI + angleAbove;
			double angleBelowCrt = Math.PI - angleBelow;
			Iterator j = n.getInEdgesIterator();
			while (j.hasNext()) {
				Edge e = (Edge) j.next();
				Node m = e.getFromNode();
				int im = indexOf(m);
				int dx = cx[im] - cx[i];
				int dy = cy[im] - cy[i];
				double angle = Math.atan2(dy, dx);
				double r = Math.sqrt(dx * dx + dy * dy);
				double da = angle - angleAboveCrt;
				if (da > Math.PI){
					da -= 2 * Math.PI;
				}
				angle = angle - ak * da;
				int newdx = (int) (r * Math.cos(angle));
				int newdy = (int) (r * Math.sin(angle));
				Point d = new Point(newdx - dx, newdy - dy);
				dampen(d);
				deltax[im] += d.x;
				deltay[im] += d.y;
				angleAboveCrt += angleAbove;
			}
			j = n.getOutEdgesIterator();
			while (j.hasNext()) {
				Edge e = (Edge) j.next();
				Node m = e.getToNode();
				int im = indexOf(m);
				int dx = cx[im] - cx[i];
				int dy = cy[im] - cy[i];
				double angle = Math.atan2(dy, dx);
				double r = Math.sqrt(dx * dx + dy * dy);
				double da = angle - angleBelowCrt;
				if (da < -Math.PI){
					da += 2 * Math.PI;
				}
				angle = angle - ak * da;
				int newdx = (int) (r * Math.cos(angle));
				int newdy = (int) (r * Math.sin(angle));
				Point d = new Point(newdx - dx, newdy - dy);
				dampen(d);
				deltax[im] += d.x;
				deltay[im] += d.y;
				angleBelowCrt -= angleBelow;
			}
		}
	}

	private int indexOf(Node n) {
		return ((Integer) indices.get(n)).intValue();
	}

	private void springEdge(Edge e) {
		Node f = e.getFromNode();
		Node t = e.getToNode();
		if (f == t) {
			return;
		}
		int fi = indexOf(f);
		int ti = indexOf(t);
		int dx = cx[fi] - cx[ti];
		int dy = cy[fi] - cy[ti];
		double len = Math.sqrt(dx * dx + dy * dy);
		double force = -k*(springLen - len);
		Point d = new Point((int) (force * dx/len), (int) (force * dy/len));
		dampen(d);
		deltax[fi] -= d.x;
		deltay[fi] -= d.y;
		deltax[ti] += d.x;
		deltay[ti] += d.y;
	}

	private void repelNodes() {
		for (int i = 0; i < nodes.length - 1; i++) {
			for (int j = i + 1; j < nodes.length; j++) {
				int dx = cx[j] - cx[i];
				int dy = cy[j] - cy[i];
				int len = dx * dx + dy * dy;
				if (len > 4 * repelLen) {
					continue;
				}
				double force = (repelLen / (len + 1));
				Point d = new Point((int) (rk * force * dx/len), (int) (rk * force * dy/len));
				dampen(d);
				deltax[i] -= d.x;
				deltay[i] -= d.y;
				deltax[j] += d.x;
				deltay[j] += d.y;
			}
		}
	}

	public void dampen(Point p) {
		int abs = Math.abs(p.x * p.x + p.y * p.y);
		double factor = visc / (abs + visc);
		p.x = (int) (factor * p.x);
		p.y = (int) (factor * p.y);
	}

	private class TQ {
		int index;
		double shift;
		int x;
		int y;
		int dr;
		int in;

		public TQ(int index, double shift, int x, int y, int dr, int in) {
			this.index = index;
			this.shift = shift;
			this.x = x;
			this.y = y;
			this.dr = dr;
			this.in = in;
		}
	}
}
