// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.karajan.compiled.nodes;

import java.io.IOException;
import java.io.PrintStream;

import k.rt.KRunnable;
import k.thr.LWThread;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.analyzer.CompilationException;
import org.globus.cog.karajan.analyzer.Scope;
import org.globus.cog.karajan.parser.WrapperNode;

public class Node implements KRunnable {	
	public static final Logger logger = Logger.getLogger(Node.class);

	private int line;
	
	private Integer uid;
	
	private String type;

	private Node parent;

	public static long startCount = 0;

	public static boolean debug = false;

	public Node() {
	}
	
	@Override
	public void run(LWThread thr) {
	}
	
	public Node compile(WrapperNode w, Scope scope) throws CompilationException {
		Integer l = (Integer) WrapperNode.getTreeProperty(WrapperNode.LINE, w);
		if (l != null) {
			setLine(l);
		}
		setType(w.getNodeType());
		return this;
	}
			
	public String getFileName() {
		if (parent != null) {
			return parent.getFileName();
		}
		else {
			return null;
		}
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(getTextualName());
		String fn = getFileName();
		if (fn != null) {
			fn = fn.substring(1 + fn.lastIndexOf('/'));
			sb.append(" @ ");
			sb.append(fn);

			if (line != 0) {
				sb.append(", line: ");
				sb.append(line);
			}
		}
		return sb.toString();
	}

	public String getTextualName() {
		String tmp = getType();
		if (tmp == null) {
			tmp = this.getClass().getName();
		}
		return tmp;
	}

	public Object getCanonicalType() {
		return getClass();
	}

	public Node getParent() {
		return parent;
	}

	public void setParent(Node element) {
		parent = element;
	}

	public Integer getUID() {
		return uid;
	}
	
	public int getLine() {
		return line;
	}

	public void setLine(int line) {
		this.line = line;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public void dump(PrintStream ps, int level) throws IOException {
		for (int i = 0; i < level; i++) {
			ps.print("\t");
		}
		ps.println(getType() + " - " + line);
	}
}