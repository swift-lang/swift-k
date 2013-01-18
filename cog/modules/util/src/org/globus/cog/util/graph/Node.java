
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.util.graph;


import java.util.ArrayList;
import java.util.List;

/**
 * A generic node object that contains lists for edges connected to it and
 * wraps an object
 */
public class Node {

	private Object contents;

	private List outEdges;
	private List inEdges;

	/**
	 * Creates a new node object
	 */
	public Node() {
		contents = null;
	}

	/**
	 * Creates a new node object storing <code>contents</code> in it
	 * 
	 * @param contents
	 */
	public Node(Object contents) {
		this();
		setContents(contents);
	}

	/**
	 * Gets the contents of the node
	 * 
	 * @return the object stored in this node or <code>null</code>
	 */
	public Object getContents() {
		return contents;
	}

	/**
	 * Stores an object to this node
	 * 
	 * @param contents
	 *            the object to be added
	 */
	public void setContents(Object contents) {
		this.contents = contents;
	}

	/**
	 * Add an in-edge to this node
	 * 
	 * @param edge
	 *            The feature to be added to the InEdge attribute
	 */
	public void addInEdge(Edge edge) {
		if (inEdges == null) {
			inEdges = new ArrayList(1);
		}
		synchronized (inEdges) {
			inEdges.add(edge);
		}
	}

	/**
	 * Adds an out-edge to this node
	 * 
	 * @param edge
	 *            The feature to be added to the OutEdge attribute
	 */
	public void addOutEdge(Edge edge) {
		if (outEdges == null) {
			outEdges = new ArrayList(1);
		}
		synchronized (outEdges) {
			outEdges.add(edge);
		}
	}

	/**
	 * Removes an in-edge
	 * 
	 * @param edge
	 *            the edge to be removed
	 * @exception EdgeNotFoundException
	 *                in case the edge does not exist
	 */
	public void removeInEdge(Edge edge) throws EdgeNotFoundException {
		if (inEdges == null) {
			throw new EdgeNotFoundException();
		}
		if (inEdges.contains(edge)) {
			synchronized (inEdges) {
				inEdges.remove(edge);
			}
		}
		else {
			throw new EdgeNotFoundException();
		}
	}

	/**
	 * Removes an out-edge
	 * 
	 * @param edge
	 *            the edge to be removed
	 * @exception EdgeNotFoundException
	 *                if the edge is not found
	 */
	public void removeOutEdge(Edge edge) throws EdgeNotFoundException {
		if (outEdges == null) {
			throw new EdgeNotFoundException();
		}
		if (outEdges.contains(edge)) {
			synchronized (outEdges) {
				outEdges.remove(edge);
			}
		}
		else {
			throw new EdgeNotFoundException();
		}
	}

	/**
	 * Removes an edge regardless of its orientation
	 * 
	 * @param edge
	 *            The edge to be removed
	 */
	public void removeEdge(Edge edge) throws EdgeNotFoundException {
		try {
			removeInEdge(edge);
			return;
		}
		catch (EdgeNotFoundException e) {
			//Ignore the exception because the edge might be an OutEdge.
		}
		removeOutEdge(edge);
	}

	/**
	 * Determines the number of in-edges for this node
	 * 
	 * @return The number of in-edges
	 */
	public int inDegree() {
		if (inEdges == null) {
			return 0;
		}
		return inEdges.size();
	}

	/**
	 * Determines the number of out-edges for this node
	 * 
	 * @return The number of out-edges
	 */
	public int outDegree() {
		if (outEdges == null) {
			return 0;
		}
		return outEdges.size();
	}

	/**
	 * Determines the total number of edges connected to this node
	 * 
	 * @return The number of edges
	 */
	public int degree() {
		return inDegree() + outDegree();
	}

	/**
	 * Returns an iterator with the in-edges of this node It is advisable for
	 * multi-threaded applications to synchronize on this <code>Node</code>
	 * object when using the <code>Iterator</code>
	 * 
	 * @return An iterator with the in-edges
	 */
	public EdgeIterator getInEdgesIterator() {
		if (inEdges == null) {
			return new EdgeItr();
		}
		((ArrayList) inEdges).trimToSize();
		return new EdgeItr(inEdges.iterator());
	}

	/**
	 * Returns an iterator with the out-edges of this node It is advisable for
	 * multi-threaded applications to synchronize on this <code>Node</code>
	 * object when using the <code>Iterator</code>
	 * 
	 * @return An iterator with the out-edges
	 */
	public EdgeIterator getOutEdgesIterator() {
		if (outEdges == null) {
			return new EdgeItr();
		}
		((ArrayList) outEdges).trimToSize();
		return new EdgeItr(outEdges.iterator());
	}

	public List getOutEdges() {
		return outEdges;
	}

	public List getInEdges() {
		return inEdges;
	}
}
