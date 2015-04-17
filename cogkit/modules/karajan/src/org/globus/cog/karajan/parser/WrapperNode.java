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
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Apr 16, 2005
 */
package org.globus.cog.karajan.parser;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.globus.cog.karajan.analyzer.CompilationException;
import org.globus.cog.karajan.analyzer.Scope;
import org.globus.cog.karajan.compiled.nodes.Node;
import org.globus.cog.karajan.util.AdaptiveArrayList;
import org.globus.cog.karajan.util.AdaptiveMap;

public final class WrapperNode {
	public static final String UID = "_uid";
	public static final String LINE = "_line";
	public static final String FILENAME = "_filename";
	public static final String ANNOTATION = "_annotation";

	public static final String TEXT = "_text_";

	
	private WrapperNode parent;
	private List<WrapperNode> nodes;
	private List<WrapperNode> blocks;
	private Map<String, Object> properties;
	private Map<String, Object> staticArguments;
	private String type;
	private boolean compiled;

	private static AdaptiveMap.Context pc = new AdaptiveMap.Context(),
			sac = new AdaptiveMap.Context();
	private static AdaptiveArrayList.Context ec = new AdaptiveArrayList.Context();

	public WrapperNode() {
		properties = new AdaptiveMap<String, Object>(pc);
		staticArguments = new AdaptiveMap<String, Object>(sac);
		nodes = Collections.emptyList();
	}

	public void addNode(WrapperNode node) {
		if (nodes.isEmpty()) {
			nodes = new AdaptiveArrayList<WrapperNode>(ec);
		}
		node.setParent(this);
		nodes.add(node);
	}
	
	public void removeNode(WrapperNode node) {
		nodes.remove(node);
	}
	
	public WrapperNode removeNode(int index) {
		return nodes.remove(index);
	}
	
	public void setNodes(List<WrapperNode> l) {
		nodes = l;
		for (WrapperNode n : l) {
			n.setParent(this);
		}
	}

	public WrapperNode getNode(int index) {
		return nodes.get(index);
	}

	public int nodeCount() {
		return nodes.size();
	}

	public List<WrapperNode> nodes() {
		return nodes;
	}

	public void setNodeType(String type) {
		this.type = type;
	}

	public String getNodeType() {
		return type;
	}

	public void setProperty(String name, Object value) {
		properties.put(name.toLowerCase(), value);
	}

	public void removeProperty(String name) {
		properties.remove(name.toLowerCase());
	}

	public void setProperty(final String name, final int value) {
		setProperty(name, new Integer(value));
	}

	public Object getProperty(final String name) {
		return properties.get(name.toLowerCase());
	}

	public synchronized boolean hasProperty(final String name) {
		return properties.containsKey(name.toLowerCase());
	}

	public Collection<String> propertyNames() {
		return properties.keySet();
	}

	public void setParent(WrapperNode parent) {
		this.parent = parent;
	}

	public WrapperNode getParent() {
		return parent;
	}

	public boolean acceptsInlineText() {
		return true;
	}
		
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(getNodeType());
		Object fileName = getTreeProperty(FILENAME, this);
		if (fileName instanceof String) {
			String fn = (String) fileName;
			fn = fn.substring(1 + fn.lastIndexOf('/'));
			sb.append(" @ ");
			sb.append(fn);

			if (hasProperty(LINE)) {
				sb.append(", line: ");
				sb.append(getProperty(LINE));
			}
		}
		return sb.toString();
	}
	
	public static Object getTreeProperty(final String name, final WrapperNode element) {
		if (element == null) {
			return null;
		}
		if (element.hasProperty(name)) {
			return element.getProperty(name);
		}
		else {
			if (element.getParent() != null) {
				return getTreeProperty(name, element.getParent());
			}
			else {
				return null;
			}
		}
	}

	public Node compile(Node parent, Scope scope) throws CompilationException {
		try {
			if (compiled) {
				throw new CompilationException(this, "Already compiled");
			}
			setPropertiesFromArgs();
			compiled = true;
			Node self = scope.resolve(this.getNodeType());
			self.setParent(parent);
			self = self.compile(this, scope);
			return self;
		}
		catch (RuntimeException e) {
			throw new CompilationException(this, e.getMessage(), e);
		}
	}
	
	private void setPropertiesFromArgs() throws CompilationException {
		Iterator<WrapperNode> i = nodes.iterator();
		while (i.hasNext()) {
			WrapperNode c = i.next();
			if (c.getNodeType().equals("k:named")) {
				if (checkProperty(c)) {
					i.remove();
				}
			}
		}
	}

	private boolean checkProperty(WrapperNode c) throws CompilationException {
    	WrapperNode name = c.getNode(0);
    	String k = (String) name.getProperty(WrapperNode.TEXT);
    	if (k != null && k.startsWith("#")) {
    		WrapperNode value = c.getNode(1);
    		setProperty(k.substring(1), getValue(value));
    		return true;
    	}
    	else {
    		return false;
    	}
	}
	
	private Object getValue(WrapperNode n) throws CompilationException {
		if (n.getNodeType().equals("k:num")) {
			return Integer.parseInt((String) n.getProperty(WrapperNode.TEXT));
		}
		else if (n.getNodeType().equals("k:str")) {
			return n.getProperty(WrapperNode.TEXT);
		}
		else {
			throw new CompilationException(n, "Can't handle property node " + n.getNodeType());
		}
	}

	public String getText() {
		return (String) getProperty(TEXT);
	}
}
