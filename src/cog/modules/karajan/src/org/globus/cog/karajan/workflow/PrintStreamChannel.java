//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Apr 25, 2005
 */
package org.globus.cog.karajan.workflow;

import java.io.PrintStream;
import java.util.Iterator;
import java.util.List;

import org.globus.cog.karajan.arguments.AbstractWriteOnlyVariableArguments;
import org.globus.cog.karajan.arguments.VariableArguments;

public class PrintStreamChannel extends AbstractWriteOnlyVariableArguments {
	private PrintStream stream;
	private boolean commutative;

	public PrintStreamChannel() {
		this(System.out, false);
	}
	
	public PrintStreamChannel(PrintStream stream, boolean commutative) {
		this.stream = stream;
		this.commutative = commutative;
	}
	
	public void merge(VariableArguments args) {
		appendAll(args.getAll());
	}

	public void append(Object value) {
		stream.print(value);
	}

	public void appendAll(List args) {
		Iterator i = args.iterator();
		while (i.hasNext()) {
			append(i.next());
		}
	}

	public boolean isCommutative() {
		return commutative;
	}	
}
