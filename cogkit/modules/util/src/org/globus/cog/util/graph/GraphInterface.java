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

import java.util.Set;

/**
 * Defines an interface for a directed graph structure. A graph can contain nodes and edges.
 * Operations on the graph are done using references to the nodes and edges.
 *
 *@see    org.globus.ogce.util.Graph
 *@see    org.globus.ogce.util.GraphEdge
 *@see    org.globus.ogce.util.GraphNode
 */

public interface GraphInterface extends Cloneable {

    /**
     *  Creates a new node in the graph
     *
     *@return    a reference to the newly created node
     */
    public Node addNode();


    /**
     *  Creates a new node storing <code>Contents</code> in it
     *
     *@param  contents  The object to be added to the Node
     *@return           a reference to the newly created node
     */
    public Node addNode(Object contents);


    /**
     *  Adds an edge from <code>FromNode</code> to <code>ToNode</code>.
     * A <code>NodeNotFoundException</code> is thrown if any of the two nodes does not
     * exist in this graph.
     *
     *@param  fromNode  The source node for the edge
     *@param  toNode    The destination node for the edge
     *@param  contents  The object to be stored in the edge
     *@return           A reference to the created edge
     */
    public Edge addEdge(Node fromNode, Node toNode, Object contents) throws NodeNotFoundException;


    /**
     *  Returns No of edges in the graph
     *
     *@return    Number of edges in the graph
     */
    public int edgeCount();


    /**
     *  Returns No of nodes in the graph
     *
     *@return    Number fo nodes in the graph
     */
    public int nodeCount();


    /**
     *  Returns an iterator with all the nodes in the graph
     *
     *@return    The nodesIterator value
     */
    public NodeIterator getNodesIterator();

    /**
     * Returns a list with the nodes in the graph. The order of nodes is a given.
     * New nodes are added at the end of the list. This method allows manipulation of
     * the graph based on the indexes of nodes.
     * @return the list of nodes in this graph
     */
    public Set getNodesSet();


    /**
     *  Returns an iterator with the edges in the graph
     *
     *@return    The edgesIterator value
     */
    public EdgeIterator getEdgesIterator();

    /**
     * Returns a list with the edges in the graph. The order of edges should not change between
     * calls to this method, unless changes are made to the graph.
     * New edges are added at the end of the list. This method allows manipulation of
     * the graph based on the indexes of edges.
     * @return the list of edges in this graph
     */
    public Set getEdgesSet();


    /**
     * Finds the node that has the specified contents
     */
    public Node findNode(Object contents);
    
    public Edge findEdge(Object contents);
    /**
     *  Removes the specified node from the graph
     *  It will also remove all the edges connected to the node
     *
     *@param  node  The node to be removed
     *@throws NodeNotFoundException thrown if the node was not found in this graph
     */
    public void removeNode(Node node) throws NodeNotFoundException;


    /**
     *  Removes an edge going from <code>FromNode</code> to <code>ToNode</code>
     *  If multiple edges exist between the two nodes, multiple calls to this method
     *  should be made, until an <code>EdgeNotFoundException</code> is thrown.
     *
     *@param  fromNode  Description of the Parameter
     *@param  toNode    Description of the Parameter
     *@throws EdgeNotFoundException this exception is thrown if no edge exists between the two nodes
     */
    public void removeEdge(Node fromNode, Node toNode) throws EdgeNotFoundException;

    /**
     *  Removes an edge from the graph.
     *
     *@param  edge  The edge to be removed.
     *@throws EdgeNotFoundException ... if the specified edge does not exist in this graph
     */
    public void removeEdge(Edge edge) throws EdgeNotFoundException;

    /**
     * Adds a listener to listen for structural changes in the graph
     * @param l the listener to be added
     */
    public void addGraphListener(GraphListener l);

    /**
     * Removes a listener
     * @param l
     */
    public void removeGraphListener(GraphListener l);

    /**
     * Removes all nodes and edges from this graph
     */
    public void clear();

    /**
     * Constructs a shallow copy of this graph. In the new graph the <code>Node</code> and
     * <code>Edge</code> objects will not be the same as in the old graph, but their contents will.
     * @return
     */
    public Object clone();
}

