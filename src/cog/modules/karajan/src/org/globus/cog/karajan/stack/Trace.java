//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Aug 2, 2006
 */
package org.globus.cog.karajan.stack;


public class Trace {
	public static final String ELEMENT = "#trace:element";
	
	public static String get(VariableStack stack) {
		StringBuffer buf = new StringBuffer();
		boolean first = true;
		for (int li = stack.frameCount() - 1; li >= 0; li--) {
			StackFrame frame = stack.getFrame(li);
			if (first) {
				if (frame.isDefined("#caller")) {
					buf.append('\t');
					buf.append(frame.getVar("#caller"));
					buf.append('\n');
					first = false;
				}
			}
			else {
				if (frame.isDefined(ELEMENT)) {
					buf.append('\t');
					buf.append(frame.getVar(ELEMENT));
					buf.append('\n');
				}
			}
		}
		return buf.toString();
	}
}
