//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Apr 14, 2005
 */
package org.globus.cog.karajan.translator;

import java.io.IOException;
import java.io.Writer;

public class IndentationLevel {
	private int level;
	
	public void inc() {
		level++;
	}
	
	public void dec() {
		level--;
	}
	
	public void write(Writer wr) throws IOException {
		for (int i = 0; i < level; i++) {
			wr.write("  ");
		}
	}
}
