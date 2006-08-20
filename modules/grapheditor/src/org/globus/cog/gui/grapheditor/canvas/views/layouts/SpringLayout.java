
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

    
package org.globus.cog.gui.grapheditor.canvas.views.layouts;

import java.awt.Point;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;

import org.globus.cog.util.graph.Edge;
import org.globus.cog.util.graph.GraphInterface;
import org.globus.cog.util.graph.Node;

/**
 * A basic spring layout 
 */
public class SpringLayout implements GraphLayoutEngine{
    private GraphInterface graph;
    private Hashtable coords, fixedNodes;
    private static double k = 0.1;
    private static double rk = 0.1;
    private static double springLen = 40;
    private static double repelLen = 50 * 50;

    public Hashtable layoutGraph(GraphInterface graph, Hashtable fixedNodes) {
        this.graph = graph;
        this.fixedNodes = fixedNodes;
        coords = new Hashtable();
		int sq = (int) Math.sqrt(graph.nodeCount());
		int x = 0, y = 0;
        Iterator i = graph.getNodesIterator();
        while (i.hasNext()){
            Node n = (Node) i.next();
            if (!fixedNodes.containsKey(n)){
				coords.put(n, new Point((x+1)*40, (y+1)*40));
            }
            else{
                coords.put(n, fixedNodes.get(n));
            }
			x++;
			if (x == sq) {
				x = 0;
				y++;
			}
        }
        for (int j = 0; j < 20; j++){
            springEm();
        }
        return coords;
    }

    private void springEm() {
            Hashtable deltas = new Hashtable();
            Iterator i = graph.getNodesIterator();
            while (i.hasNext()) {
                deltas.put(i.next(), new Point(0, 0));
            }

            i = graph.getEdgesIterator();
            while (i.hasNext()) {
                Edge edge = (Edge) i.next();
                springEdge(edge, deltas);
            }

            repelNodes(deltas);

            Enumeration e = deltas.keys();
            while (e.hasMoreElements()) {
                Node n = (Node) e.nextElement();
                if (!fixedNodes.containsKey(n)){
                    Point p = (Point) coords.get(n);
                    Point d = (Point) deltas.get(n);
                    p.x += d.x;
                    p.y += d.y;
                }
            }
        }
		
        private void springEdge(Edge e, Hashtable deltas){
                Node f = e.getToNode();
                Node t = e.getFromNode();
                if (f == t){
                    return;
                }

                Point up = (Point) coords.get(f);
                Point wp = (Point) coords.get(t);
                int dx = wp.x - up.x;
                int dy = wp.y - up.y;
                double len = Math.sqrt(dx * dx + dy * dy);
                double force = (springLen / (len + 1));
                int udx = (int) (force * dx);
                int udy = (int) (force * dy);
                Point delta = (Point) deltas.get(f);
                delta.x -= (int) (k * (udx - dx));
                delta.y -= (int) (k * (udy - dy));
                delta = (Point) deltas.get(t);
                delta.x += (int) (k * (udx - dx));
                delta.y += (int) (k * (udy - dy));
        }

        private void repelNodes(Hashtable deltas) {
            Iterator i = graph.getNodesIterator();
            while (i.hasNext()) {
                Node n = (Node) i.next();
                Iterator j = graph.getNodesIterator();
                while (j.hasNext()) {
                    Node m = (Node) j.next();
                    if (n == m) {
                        continue;
                    }
                    Point pn = (Point) coords.get(n);
                    Point pm = (Point) coords.get(m);
                    int dx = pm.x - pn.x;
                    int dy = pm.y - pn.y;
                    double len = dx * dx + dy * dy;
                    double force = (repelLen / (len + 1));
                    int udx = (int) (force * dx);
                    int udy = (int) (force * dy);
                    Point delta = (Point) deltas.get(n);
                    delta.x -= (int) (rk * udx);
                    delta.y -= (int) (rk * udy);
                    delta = (Point) deltas.get(m);
                    delta.x += (int) (rk * udx);
                    delta.y += (int) (rk * udy);
                }
            }
        }
}
