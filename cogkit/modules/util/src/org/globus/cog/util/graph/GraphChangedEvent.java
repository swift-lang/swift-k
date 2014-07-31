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
 * Describes a graph event
 */
public class GraphChangedEvent extends java.util.EventObject {

    private int type;
    private Node node;
    private Edge edge;

    public static final int NODE_ADDED = 0;
    public static final int EDGE_ADDED = 1;
    public static final int NODE_REMOVED = 2;
    public static final int EDGE_REMOVED = 3;

    /**
     * Constructs a GraphChangedEvent
	 * @param source the source graph
	 * @param type the type of the change
	 * @param element the node that was added/removed
	 */
	public GraphChangedEvent(Object source, int type, Node element) {
        super(source);
        this.type = type;
        this.node = element;
    }
	
	/**
     * Constructs a GraphChangedEvent
	 * @param source the source graph
	 * @param type the type of the change
	 * @param element the edge that was added/removed
	 */
	public GraphChangedEvent(Object source, int type, Edge element) {
        super(source);
        this.type = type;
        this.edge = element;
    }
	
    /**
     * Constructs a GraphChangedEvent
	 * @param source the source graph
	 * @param type the type of the change
	 * @param element the element that was added/removed
	 */
	public GraphChangedEvent(Object source, int type, Object element) {
        super(source);
        this.type = type;
        if (element instanceof Edge) {
            this.edge = (Edge) element;
        }
        else if (element instanceof Node) {
            this.node = (Node) element;
        }
    }

    /**
     * Constructs a GraphChangedEvent
	 * @param source the source graph
	 * @param type the type of the event
	 */
	public GraphChangedEvent(Object source, int type) {
        this(source, type, (Node) null);
    }

    protected void setType(int type) {
        this.type = type;
    }

    /**
     * Returns the type of this event
	 * @return
	 */
	public int getType() {
        return type;
    }

    /**
     * Returns the node involved in this event
	 * @return
	 */
	public Node getNode() {
        return node;
    }

    protected void setNode(Node node) {
        this.node = node;
    }

    /**
     * Returns the edge involved in this event
	 * @return
	 */
	public Edge getEdge() {
        return edge;
    }

    protected void setEdge(Edge edge) {
        this.edge = edge;
    }
}