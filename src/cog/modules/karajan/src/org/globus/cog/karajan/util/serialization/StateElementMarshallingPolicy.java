//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Mar 7, 2006
 */
package org.globus.cog.karajan.util.serialization;

import org.globus.cog.karajan.workflow.nodes.FlowElement;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class StateElementMarshallingPolicy extends AbstractElementMarshallingPolicy {
	public StateElementMarshallingPolicy() {
		super();
	}

	public StateElementMarshallingPolicy(KarajanSerializationContext kcontext) {
		super(kcontext);
	}

	public void marshal(Object elem, HierarchicalStreamWriter wr, MarshallingContext context) {
		Object uid = ((FlowElement) elem).getProperty(FlowElement.UID);
		if (getKContext().getTree().getUIDMap().contains((Integer) uid)
				|| getKContext().getDetachedSource()) {
			wr.addAttribute("_uid", uid.toString());
		}
		else {
			wr.addAttribute("_uid", "-1");
			writeNode((FlowElement) elem, wr, context);
		}
	}
}
