//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Mar 7, 2006
 */
package org.globus.cog.karajan.workflow.service.commands;

import org.globus.cog.karajan.util.serialization.AbstractElementMarshallingPolicy;
import org.globus.cog.karajan.util.serialization.KarajanSerializationContext;
import org.globus.cog.karajan.workflow.nodes.FlowElement;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class RemoteElementMarshallingPolicy extends AbstractElementMarshallingPolicy {

	public RemoteElementMarshallingPolicy() {
		super();
	}

	public RemoteElementMarshallingPolicy(KarajanSerializationContext kcontext) {
		super(kcontext);
	}

	public void marshal(Object elem, HierarchicalStreamWriter wr, MarshallingContext context) {
		FlowElement fe = (FlowElement) elem;
		boolean source = getKContext().isSource();
		if (!source) {
			getKContext().setSource(true);
			wr.addAttribute("_source", "true");
			wr.addAttribute("_uid", "-1");
			wr.startNode(fe.getElementType());
			this.writeProperties(fe, wr, context);
			this.writeChildren(fe, wr, context);
			wr.endNode();
			getKContext().setSource(false);
		}
		else {
			this.writeProperties(fe, wr, context);
			this.writeChildren(fe, wr, context);
		}
	}
}
