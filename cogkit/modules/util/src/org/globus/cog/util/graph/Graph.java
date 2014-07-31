/*
 * Swift Parallel Scripting Language (http://swift-lang.org)
 * Code from Java CoG Kit Project (see notice below) with modifications.
 *
 * Copyright 2005-2014 University of Chicago
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.util.graph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 *  Implements the graph interface
 *
 *
 *@see    org.globus.ogce.util.Graph
 *@see    org.globus.ogce.util.GraphEdge
 *@see    org.globus.ogce.util.GraphNode
 */

public class Graph implements GraphInterface {

    private Set nodes;
    private Set edges;
    private List graphListeners;


    /**
     *  Constructs a new <code>Graph</code> object without any nodes or edges
     */
    public Graph() {
    	nodes = new LinkedHashSet();
        edges = new LinkedHashSet();
        graphListeners = Collections.synchronizedList(new ArrayList(0));
    }



    /* (non-Javadoc)
	 * @see org.globus.cog.util.graph.GraphInterface#addGraphListener(org.globus.cog.util.graph.GraphListener)
	 */
	public void addGraphListener(GraphListener l) {
        graphListeners.add(l);
    }

    /* (non-Javadoc)
	 * @see org.globus.cog.util.graph.GraphInterface#removeGraphListener(org.globus.cog.util.graph.GraphListener)
	 */
	public void removeGraphListener(GraphListener l) {
        graphListeners.remove(l);
    }

    /* (non-Javadoc)
	 * @see org.globus.cog.util.graph.GraphInterface#clear()
	 */
	public void clear() {
        edges.clear();
        nodes.clear();
    }

    private void fireGraphChangedEvent(GraphChangedEvent e) {
        Iterator i = graphListeners.listIterator();
        while (i.hasNext()) {
            ((GraphListener) i.next()).graphChanged(e);
        }
    }
    
    private void fireGraphChangedEvent(int type, Object o) {
    	if (graphListeners.size() == 0) {
    		return;
    	}
    	fireGraphChangedEvent(new GraphChangedEvent(this, type, o));
    }
    
    private void fireGraphChangedEvent(int type, Node n) {
    	if (graphListeners.size() == 0) {
    		return;
    	}
    	fireGraphChangedEvent(new GraphChangedEvent(this, type, n));
    }
    
    private void fireGraphChangedEvent(int type, Edge e) {
    	if (graphListeners.size() == 0) {
    		return;
    	}
    	fireGraphChangedEvent(new GraphChangedEvent(this, type, e));
    }

    /**
     *  Creates a new node in the graph
     *
     *@return    a reference to the newly created node
     */
    public Node addNode() {
    	Node newNode = addNode0();
        fireGraphChangedEvent(GraphChangedEvent.NODE_ADDED, newNode);
        return newNode;
    }
    
    private Node addNode0() {
    	Node newNode = new Node();
        nodes.add(newNode);
        return newNode;
    }


    /**
     *  Creates a new node storing <code>Contents</code> in it
     *
     *@param  Contents  The feature to be added to the Node attribute
     *@return           a reference to the newly created node
     */
    public Node addNode(Object Contents) {
        Node newNode = this.addNode0();
        newNode.setContents(Contents);
        fireGraphChangedEvent(GraphChangedEvent.NODE_ADDED, newNode);
        return newNode;
    }


    /**
     *  Adds an edge from <code>FromNode</code> to <code>ToNode</code>
     *
     *@param  FromNode                   the node the edge starts from
     *@param  ToNode                     the node the edge goes to
     *@return                            The newly created edge
     *@exception  NodeNotFoundException  If one of the specified nodes is not found
     */
    public Edge addEdge(Node FromNode, Node ToNode, Object contents) throws NodeNotFoundException {
        checkNode(FromNode);
        checkNode(ToNode);
        
        Edge newEdge = new Edge(contents);
        newEdge.setNodes(FromNode, ToNode);
        if (edges.contains(newEdge)) {
        	return null;
        }
        FromNode.addOutEdge(newEdge);
        ToNode.addInEdge(newEdge);
        edges.add(newEdge);
        fireGraphChangedEvent(GraphChangedEvent.EDGE_ADDED, newEdge);
        return newEdge;
    }


    /**
     *  Returns No of edges in the graph
     *
     *@return    Description of the Return Value
     */
    public int edgeCount() {
        return edges.size();
    }


    /**
     *  Returns No of nodes in the graph
     *
     *@return    Description of the Return Value
     */
    public int nodeCount() {
        return nodes.size();
    }


    /**
     *  Returns an iterator with all the nodes in the graph
     *
     *@return    The nodesIterator value
     */
    public NodeIterator getNodesIterator() {
        return new NodeItr(nodes.iterator());
    }

    public Set getNodesSet() {
        return nodes;
    }


    /**
     *  Returns an iterator with the edges in the graph
     *
     *@return    The edgesIterator value
     */
    public EdgeIterator getEdgesIterator() {
        return new EdgeItr(edges.iterator());
    }

    public Set getEdgesSet() {
        return edges;
    }


    /**
     *  Removes the specified node from the graph It will also remove all the edges
     *  connected to the node
     *
     *@param  node  Description of the Parameter
     */
    public void removeNode(Node node) throws NodeNotFoundException {
        checkNode(node);

        synchronized (node) {
            Iterator inEdges = node.getInEdgesIterator();

            while (inEdges.hasNext()) {
                Edge crtEdge = (Edge) inEdges.next();
                crtEdge.getFromNode().removeOutEdge(crtEdge);
                edges.remove(crtEdge);
                fireGraphChangedEvent(new GraphChangedEvent(this, GraphChangedEvent.EDGE_REMOVED, crtEdge));
            }

            Iterator outEdges = node.getOutEdgesIterator();

            while (outEdges.hasNext()) {
                Edge crtEdge = (Edge) outEdges.next();
                crtEdge.getToNode().removeInEdge(crtEdge);
                edges.remove(crtEdge);
                fireGraphChangedEvent(new GraphChangedEvent(this, GraphChangedEvent.EDGE_REMOVED, crtEdge));
            }
        }
        nodes.remove(node);
        fireGraphChangedEvent(new GraphChangedEvent(this, GraphChangedEvent.NODE_REMOVED, node));
    }


    /**
     *  Removes an edge going from <code>FromNode</code> to <code>ToNode</code>
     *
     *@param  fromNode                   Description of the Parameter
     *@param  toNode                     Description of the Parameter
     *@exception  NodeNotFoundException  Description of the Exception
     *@exception  EdgeNotFoundException  Description of the Exception
     */
    public void removeEdge(Node fromNode, Node toNode) throws NodeNotFoundException, EdgeNotFoundException {
        checkNode(fromNode);
        checkNode(toNode);

        synchronized (fromNode) {
            Iterator outEdges = fromNode.getOutEdgesIterator();

            while (outEdges.hasNext()) {
                Edge crtEdge = (Edge) outEdges.next();

                if (crtEdge.getToNode() == toNode) {
                    fromNode.removeOutEdge(crtEdge);
                    toNode.removeInEdge(crtEdge);
                    edges.remove(crtEdge);
                    fireGraphChangedEvent(new GraphChangedEvent(this, GraphChangedEvent.EDGE_REMOVED, crtEdge));
                    return;
                }
            }
        }
        throw new EdgeNotFoundException();
    }


    /**
     *  Removes an edge from the graph
     *
     *@param  edge                       Description of the Parameter
     *@exception  EdgeNotFoundException  Description of the Exception
     */
    public void removeEdge(Edge edge) throws EdgeNotFoundException {
        checkEdge(edge);
        edge.getFromNode().removeOutEdge(edge);
        edge.getToNode().removeInEdge(edge);
        edges.remove(edge);
        fireGraphChangedEvent(new GraphChangedEvent(this, GraphChangedEvent.EDGE_REMOVED, edge));
    }


    /**
     *  Checks if the specified node exists in this graph and
     * 	throws a <code>NodeNotFoundException</code> if the node
     *	is not there
     *
     *@param  node                       The node to be checked
     *@exception  NodeNotFoundException  Thrown if the node is not found
     */
    private void checkNode(Node node) throws NodeNotFoundException {
        if (!nodes.contains(node)) {
            throw new NodeNotFoundException();
        }
    }


    /**
     *  Checks if the specified edge exists in the graph and throws
     *	a <code>EdgeNotFoundException</code> if the edge is not
     *	found
     *
     *@param  edge                       Description of the Parameter
     *@exception  EdgeNotFoundException  Description of the Exception
     */
    private void checkEdge(Edge edge) throws EdgeNotFoundException {
        if (!edges.contains(edge)) {
            throw new EdgeNotFoundException();
        }
    }

    public Edge findEdge(Object contents) {
        Iterator i = edges.iterator();
        while (i.hasNext()) {
            Edge e = (Edge) i.next();
            if (e.getContents().equals(contents)) {
                return e;
            }
        }
        return null;
    }

    public Node findNode(Object contents) {
        Iterator i = nodes.iterator();
        while (i.hasNext()) {
            Node e = (Node) i.next();
            if (e.getContents().equals(contents)) {
                return e;
            }
        }
        return null;
    }

    /**
     * Returns a shallow copy of the graph. The Node
     * and Edge objects will be different, but their contained
     * objects will remain the same
     * @return the cloned graph
     */
    public Object clone() {
        //we can count on the order of the edges and nodes lists, so the indexes
        //in the lists can be used to preserve the structure
        Graph newGraph = new Graph();
        Set nodes = getNodesSet();
        //Set edges = getEdgesSet();
        Iterator i = getNodesIterator();
        Hashtable indices = new Hashtable();
        Node[] newNodes = new Node[nodes.size()];
        int j = 0;
        while (i.hasNext()){
        	Node n = (Node) i.next();
        	indices.put(n, new Integer(j));
			newNodes[j] = newGraph.addNode(n.getContents());
			j++;
        }
       	
       	Iterator k = getEdgesIterator();
       	while (k.hasNext()){
       		Edge e = (Edge) k.next();
       		int ifrom = ((Integer) indices.get(e.getFromNode())).intValue();
			int ito = ((Integer) indices.get(e.getToNode())).intValue();
			newGraph.addEdge(newNodes[ifrom], newNodes[ito], e.getContents());
       	}
        return newGraph;
    }

    /**
     * Determines if the specified graph is structurally equivalen to the specified graph.
     * Structural equivalence, in this case, means:
     * Given graph A and B:
     * <ul>
     * <li>The number of nodes and edges are the same for A and B
     * <li>Nodes from A and B having the same index in the nodes list contain the same objects
     * <li>Edges from A and B having the same index in the edges list contain the same objects
     * <li>Edges from A and B having the same index in the edges list point to (and from) nodes
     * with the same indices
     * </li>
     * The objects contained within nodes and edges are compare using equals()
     * @param o The object to be compared to this graph
     * @return True if the graphs are equivalent
     */
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof GraphInterface)) {
            return false;
        }
        GraphInterface graph = (GraphInterface) o;
        //compare list sizes
        if ((graph.nodeCount() != nodeCount()) || (graph.edgeCount() != edgeCount())) {
            return false;
        }
        //compare nodes
        Iterator i = getNodesIterator();
        Iterator j = graph.getNodesIterator();
        Hashtable map = new Hashtable();
        while (i.hasNext()){
			Node n = (Node) i.next();
			Node m = (Node) j.next();
			map.put(n, m);
			if (!n.getContents().equals(m.getContents())) {
				return false;
			}
        }
        //compare edges
        i = getEdgesIterator();
        j = graph.getEdgesIterator();
        while (i.hasNext()){
            Edge n = (Edge) i.next();
            Edge m = (Edge) j.next();
            if (!n.getContents().equals(m.getContents())) {
                return false;
            }
            if (map.get(n.getFromNode()) != m.getFromNode()){
            	return false;
            }
			if (map.get(n.getToNode()) != m.getToNode()){
				return false;
			}
            
        }
        return true;
    }

    public int hashCode() {
        int result = 0;
        Iterator i = getNodesIterator();
        while (i.hasNext()) {
            result += ((Node) i.next()).getContents().hashCode();
        }
        i = getEdgesIterator();
        while (i.hasNext()) {
            result += ((Edge) i.next()).getContents().hashCode();
        }
        return result;
    }
}

