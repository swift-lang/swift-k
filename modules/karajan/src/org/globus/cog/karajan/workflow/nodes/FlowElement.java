
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.karajan.workflow.nodes;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.globus.cog.karajan.workflow.events.EventListener;


public interface FlowElement extends EventListener {
	public static final String UID = "_uid";
	public static final String LINE = "_line";
	public static final String FILENAME = "_filename";
	public static final String ANNOTATION = "_annotation";

	public static final String TEXT = "_text_";
	
	public static final String CALLER = "#caller";

	
	/**
	 * Adds a child element to this element. The child element
	 * is added at the end of the list of child elements
	 */
	void addElement(FlowElement element);
	
	/**
	 * Returns the child element at @param index
	 */
	FlowElement getElement(int index);
	
	/**
	 * Replaces the child element at @param index with
	 * @param element
	 */
	void replaceElement(int index, FlowElement element);
	
	/**
	 * Removes the child element at @param index. The elements
	 * with indexes > @param index will be shifted down in the list
	 */
	void removeElement(int index);
	
	void setElements(List elements);
	
	/**
	 * Returns the number of child elements for this element
	 */
	int elementCount();
	
	/**
	 * Returns the list of child elements 
	 */
	List elements();
	
	/**
	 * Sets the element type. The element type is a string reflecting
	 * the actual name used in source files.
	 */
	void setElementType(String type);
	
	/**
	 * Returns the type of this element
	 */
	String getElementType();
	
	/**
	 * Sets a property on this element. The value can be
	 * <code>null</code>
	 */
	void setProperty(String name, Object value);
	
	void setProperties(Map properties);
	
	/**
	 * Completely removes a property previously set on this
	 * element. If no property with the given name exists,
	 * <code>removeProperty</code> will have no effect.
	 */
	void removeProperty(String name);
	
	/**
	 * Retrieves the value of a property. If no property with
	 * the given name exists, <code>getProperty</code> will
	 * return <code>null</code>. In order to distinguish between
	 * a property with a value of <code>null</code> and a property
	 * not set, <code>hasProperty</code> can be used.
	 */
	Object getProperty(String name);
	
	/**
	 * Returns <code>true</code> if a property with the given name
	 * was set on this element.
	 */
	boolean hasProperty(String name);
	
	/**
	 * Returns a collection of all the property names that are
	 * set on this element. 
	 */
	Collection propertyNames();
	
	void addStaticArgument(String name, Object value);
	
	void setStaticArguments(Map args);
	
	Map getStaticArguments();
	
	/**
	 * Sets the lexical parent of this element. When adding a child
	 * element with <code>addElement</code>, <code>setParent</code> is
	 * automatically called on the child element with this element as
	 * the argument.
	 */
	void setParent(FlowElement parent);
	
	/**
	 * Retrieves the parent previously set with <code>setParent</code>
	 */
	FlowElement getParent();

	/**
	 * Provides means to cause the execution of this element under the
	 * given context (<code>stack</code>) to fail
	 */
	void failImmediately(VariableStack stack, String string) throws ExecutionException;

	/**
	 * Returns <code>true</code> if the implementation of this element makes use
	 * of inline XML text. XML unfortunately does not provide means to separate
	 * relevant inline text from formatting whitespace (which in itself may or may
	 * not be relevant, depending on the context).
	 */
	boolean acceptsInlineText();
}
