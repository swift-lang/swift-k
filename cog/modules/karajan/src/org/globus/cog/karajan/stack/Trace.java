//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Aug 2, 2006
 */
package org.globus.cog.karajan.stack;

import java.util.ArrayList;
import java.util.List;

import org.globus.cog.karajan.workflow.nodes.FlowElement;
import org.globus.cog.karajan.workflow.nodes.FlowNode;


public class Trace {
	public static final String ELEMENT = "#trace:element";
	
	public static List<Object> getAsList(VariableStack stack) {
	    List<Object> l = new ArrayList<Object>();
	    boolean first = true;
        for (int li = stack.frameCount() - 1; li >= 0; li--) {
            StackFrame frame = stack.getFrame(li);
            Object caller = frame.getRegs().getCaller();
            if (first) {
                if (caller != null) {
                    l.add(caller);
                    first = false;
                }
            }
            else {
                if (frame.isDefined(ELEMENT)) {
                    l.add(frame.getVar(ELEMENT));
                }
                else if (caller != null) {
                    l.add(caller);
                }
            }
        }
        return l;
	}
	
	public static String get(VariableStack stack) {
		StringBuffer buf = new StringBuffer();
		List<Object> l = getAsList(stack);
		for (Object o : l) {
			buf.append('\t');
			buf.append(o);
			buf.append('\n');
		}
		return buf.toString();
	}

	public static String getUIDs(VariableStack stack) {
		StringBuffer buf = new StringBuffer();
		boolean first = true;
		for (int li = stack.frameCount() - 1; li >= 0; li--) {
			StackFrame frame = stack.getFrame(li);
			Object fe = null;
			if (frame.isDefined(ELEMENT)) {
				fe = frame.getVar(ELEMENT);
			}
			else if (frame.isDefined("#caller")) {
				fe = frame.getVar("#caller");
			}
			if (fe instanceof FlowElement) {
				buf.append(((FlowElement) fe).getProperty(FlowNode.UID));
				buf.append('-');
			}
		}
		return buf.toString();
	}
}
