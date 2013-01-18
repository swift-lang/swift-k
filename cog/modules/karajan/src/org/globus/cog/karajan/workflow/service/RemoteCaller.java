//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Aug 24, 2005
 */
package org.globus.cog.karajan.workflow.service;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.globus.cog.karajan.workflow.futures.Future;
import org.globus.cog.karajan.workflow.nodes.FlowElement;
import org.globus.cog.karajan.workflow.nodes.ProjectNode;

public class RemoteCaller implements FlowElement {
	private final InstanceContext instanceContext;
	private final int uid;

	public RemoteCaller(InstanceContext instanceContext, int uid) {
		this.instanceContext = instanceContext;
		this.uid = uid;
	}

	public void addElement(FlowElement element) {
		throw new UnsupportedOperationException();
	}
	
	public void replaceElement(int index, FlowElement element) {
		throw new UnsupportedOperationException();
	}
	
	public void removeElement(int index) {
		throw new UnsupportedOperationException();
	}

	public FlowElement getElement(int index) {
		throw new UnsupportedOperationException();
	}

	public int elementCount() {
		throw new UnsupportedOperationException();
	}

	public List elements() {
		throw new UnsupportedOperationException();
	}

	public void setElementType(String type) {
		throw new UnsupportedOperationException();
	}

	public String getElementType() {
		throw new UnsupportedOperationException();
	}

	public void setProperty(String name, Object value) {
		throw new UnsupportedOperationException();
	}

	public void removeProperty(String name) {
		throw new UnsupportedOperationException();
	}

	public Object getProperty(String name) {
		throw new UnsupportedOperationException();
	}

	public boolean hasProperty(String name) {
		throw new UnsupportedOperationException();
	}

	public Collection propertyNames() {
		throw new UnsupportedOperationException();
	}

	public void setParent(FlowElement parent) {
		throw new UnsupportedOperationException();
	}

	public FlowElement getParent() {
		throw new UnsupportedOperationException();
	}

	public void failImmediately(VariableStack stack, String message) throws ExecutionException {
		//TODO
	}

	public ProjectNode getProjectNode() {
		throw new UnsupportedOperationException();
	}

	public boolean acceptsInlineText() {
		throw new UnsupportedOperationException();
	}
	
	public void abort(VariableStack stack) throws ExecutionException {
	}

	public void failImmediately(VariableStack stack, ExecutionException e)
			throws ExecutionException {
	}

	public void restart(VariableStack stack) throws ExecutionException {
	}

	public void start(VariableStack stack) throws ExecutionException {
	}

	public void completed(VariableStack stack) throws ExecutionException {
	}

	public void failed(VariableStack stack, ExecutionException e) throws ExecutionException {
	}

	public Map getStaticArguments() {
		throw new UnsupportedOperationException();
	}

	public void setElements(List<FlowElement> elements) {
	}

	public void setProperties(Map<String, Object> properties) {
	}

	public void setStaticArguments(Map<String, Object> args) {
	}

	public void futureModified(Future f, VariableStack stack) {
	}

	public void addStaticArgument(String name, Object value) {
	}

	public void executeSimple(VariableStack stack) throws ExecutionException {
	}

	public boolean isSimple() {
		return false;
	}
	
	
}
