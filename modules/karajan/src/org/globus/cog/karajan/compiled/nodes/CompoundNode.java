//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Dec 16, 2012
 */
package org.globus.cog.karajan.compiled.nodes;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import k.thr.LWThread;

import org.globus.cog.karajan.analyzer.CompilationException;
import org.globus.cog.karajan.analyzer.CompilerSettings;
import org.globus.cog.karajan.analyzer.Scope;
import org.globus.cog.karajan.parser.WrapperNode;

public class CompoundNode extends Node {	
	private List<Node> children;
	
	public CompoundNode() {
		children = Collections.emptyList();
	}
	
	public Node compile(WrapperNode w, Scope scope) throws CompilationException {
		Scope cs = newScope(w, scope);
		Integer l = (Integer) WrapperNode.getTreeProperty(WrapperNode.LINE, w);
		if (l != null) {
			setLine(l);
		}
		setType(w.getNodeType());
		Node fn = compileChildren(w, cs);
		cs.close();
		return fn;
	}
	
	protected Scope newScope(WrapperNode w, Scope scope) {
		return new Scope(w, scope);
	}

	protected Node compileChildren(WrapperNode w, Scope scope) throws CompilationException {
		for (WrapperNode c : w.nodes()) {
			Scope.Checkpoint sc = scope.checkpoint();
			Node n = compileChild(c, scope);
			if (n != null) {
				addChild(n);
			}
			else {
				scope.restore(sc);
			}
		}
		if (childCount() == 0) {
			return null;
		}
		else {
			return this;
		}
	}

	protected Node compileChild(WrapperNode c, Scope scope) throws CompilationException {
		return c.compile(this, scope);
	}
	
	public List<Node> children() {
		return children;
	}

	public synchronized void addChild(Node child) {
		if (children.isEmpty()) {
			children = new ArrayList<Node>();
		}
		children.add(child);
	}

	public Node getChild(int index) {
		return children.get(index);
	}
	
	protected void runChild(int index, LWThread thr) {
		if (CompilerSettings.PERFORMANCE_COUNTERS) {
			startCount++;
		}
	    Node fn = children.get(index);
	    fn.run(thr);
	}

	public int childCount() {
		return children.size();
	}

	@Override
	public void dump(PrintStream ps, int level) throws IOException {
		super.dump(ps, level);
		for (Node n : children()) {
			n.dump(ps, level + 1);
		}
	}
}
