//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Aug 14, 2010
 */
package org.globus.cog.karajan;

import java.util.HashMap;
import java.util.Map;

import org.globus.cog.karajan.workflow.nodes.FlowElement;
import org.globus.cog.karajan.workflow.nodes.Sequential;
import org.globus.cog.karajan.workflow.nodes.SequentialChoice;

public class Optimizer {
    
    private static Map<String, Integer> counts; 

	public static void optimize(FlowElement e) {
	    if (e == null) {
	        return;
	    }
	    counts = new HashMap<String, Integer>();
		optimize0(e);
	}

	private static FlowElement optimize0(FlowElement e) {
		if ((e instanceof Sequential || e.getElementType().equals("sequential") || e.getElementType().equals("parallel"))
				&& e.elementCount() == 1) {
			return optimize0(e.getElement(0));
		}
		if (e.getElementType().equals("try") || e.getClass().equals(SequentialChoice.class)) {
		    if (e.elementCount() == 2 && e.getElement(0).getElementType().equals("kernel:variable") && e.getElement(1).getElementType().startsWith("kernel:")) {
		    	e.getStaticArguments().put("buffer", Boolean.FALSE);
		    }
		}
		for (int i = 0; i < e.elementCount(); i++) {
			FlowElement c = optimize0(e.getElement(i));
			if (c == null) {
				e.removeElement(i);
				i--;
			}
			else {
				e.elements().set(i, c);
			}
		}
		return e;
	}
}
