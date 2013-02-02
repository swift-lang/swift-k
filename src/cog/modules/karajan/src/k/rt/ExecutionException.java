// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/*
 * Created on Oct 8, 2003
 */
package k.rt;

import java.io.CharArrayWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;


import org.globus.cog.karajan.compiled.nodes.Node;

public class ExecutionException extends RuntimeException {
	private static final long serialVersionUID = 4975303013364072936L;

	private Stack stack;
	private LinkedList<Node> trace;

	public ExecutionException() {
		super();
	}

	public ExecutionException(Stack stack, String message) {
		this(stack, message, null);
	}
	
	public ExecutionException(Stack stack, Throwable cause) {
        this(stack, null, cause);
    }

	public ExecutionException(Stack stack, String message, Throwable cause) {
		this(message, cause);
		this.stack = stack;
	}

	public ExecutionException(String message) {
		super(message);
	}
	
	public ExecutionException(Node n, String message) {
		super(message);
		push(n);
	}

	public ExecutionException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public ExecutionException(Node n, String message, Throwable cause) {
        super(message, translateException(cause));
        push(n);
    }

	public ExecutionException(Node loc, Throwable cause) {
		super(translateException(cause));
		push(loc);
	}

	private static Throwable translateException(Throwable t) {
		if (t instanceof ClassCastException) {
			String msg = t.getMessage();
			int fti = msg.indexOf(' ');
			int lti = msg.lastIndexOf(' ');
			String t1 = msg.substring(0, fti);
			String t2 = msg.substring(lti + 1);
			ClassCastException e1 = new ClassCastException("Invalid type: expected a " 
					+ translateType(t2) + " but got a " + translateType(t1));
			e1.setStackTrace(t.getStackTrace());
			return e1;
		}
		return t;
	}
	
	private static final Map<String, String> types;
	
	static {
		types = new HashMap<String, String>();
		types.put("java.lang.String", "string");
		types.put("java.lang.Integer", "number");
		types.put("java.lang.Double", "number");
		types.put("java.lang.Long", "number");
		types.put("java.lang.Float", "number");
		types.put("java.lang.Number", "number");
		types.put("java.lang.Boolean", "boolean");
		types.put("java.util.ArrayList", "list");
		types.put("k.rt.ExecutionException", "exception");
	}

	public static String translateType(String t) {
		String tt = types.get(t);
		if (tt == null) {
			return t;
		}
		else {
			return tt;
		}
	}

	public Stack getStack() {
		return stack;
	}

	public void setStack(Stack stack) {
		this.stack = stack;
	}
	
	public void push(Node fe) {
		if (trace == null) {
			trace = new LinkedList<Node>();
		}
		trace.add(fe);
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		toString(sb);
		return sb.toString();
	}

	private void toString(StringBuffer sb) {
		sb.append(getMessage());
		sb.append('\n');
		getTrace(sb);

		Throwable cause = getCause();
		if (cause != null) {
			sb.append("Caused by: ");
			if (cause instanceof ExecutionException) {
				((ExecutionException) cause).toString(sb);
			}
			else {
				appendJavaException(sb, cause);
			}
		}
	}

	private void getTrace(StringBuffer sb) {
		if (trace != null) {
			for (Node fe : trace) {
				sb.append('\t');
				sb.append(fe);
				sb.append('\n');
			}
		}
	}

	private void appendJavaException(StringBuffer sb, Throwable cause) {
		if (cause instanceof RuntimeException) {
			CharArrayWriter caw = new CharArrayWriter();
			cause.printStackTrace(new PrintWriter(caw));
			sb.append(caw.toString());
		}
		else {
			sb.append(cause.toString());
		}
		if (cause.getCause() != null) {
			sb.append("\nCaused by: ");
			appendJavaException(sb, cause.getCause());
		}
	}
}
