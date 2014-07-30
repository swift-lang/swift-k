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

/**
 *  This class represents an edge in a graph that can
 *	wrap arbitrary objects
 */
public class Edge {

    private Object contents;

    private Node from;
    private Node to;


    /**
     *  Constructs an <code>Edge</code> object without anything contained
     */
    public Edge() {
        contents = null;
        from = null;
        to = null;
    }

    /**
     * Constructs an <code>Edge</code> object and stores the <code>contents</code> inside
     * @param contents the object to be stored "into" the edge
     */
    public Edge(Object contents) {
        this();
        setContents(contents);
    }


    /**
     *  Sets the object contained by this edge
     *
     *@param  Contents  The object to be stored
     */
    public void setContents(Object Contents) {
        this.contents = Contents;
    }


    /**
     *  Gets the object stored on this edge
     *
     *@return    The object stored in this edge
     */
    public Object getContents() {
        return contents;
    }


    /**
     *  Sets the from and to nodes for this edge
     *
     *@param  From  The node this edge starts from
     *@param  To    The node this edge goes to
     */
    public void setNodes(Node From, Node To) {
        this.from = From;
        this.to = To;
    }


    /**
     *  Gets the source node for this edge
     *
     *@return    The fromNode value
     */
    public Node getFromNode() {
        return from;
    }


    /**
     *  Gets the destination node for this edge
     *
     *@return    The toNode value
     */
    public Node getToNode() {
        return to;
    }
    
    public int hashCode() {
    	return from.hashCode()+to.hashCode();
    }
    
    /**
     * Two edges are equal if their endpoints are equal
     */
    public boolean equals(Object o) {
    	if (o instanceof Edge) {
    		Edge e = (Edge) o;
    		return ((e.getFromNode() == from) && (e.getToNode() == to));
    	}
    	return false;
    }
}

