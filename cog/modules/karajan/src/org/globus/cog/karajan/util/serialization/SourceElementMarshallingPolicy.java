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

public class SourceElementMarshallingPolicy extends AbstractElementMarshallingPolicy {
	public SourceElementMarshallingPolicy() {
		super();
	}

	public SourceElementMarshallingPolicy(KarajanSerializationContext kcontext) {
		super(kcontext);
	}

	public void marshal(Object elem, HierarchicalStreamWriter wr, MarshallingContext context) {
		FlowElement node = (FlowElement) elem;
		writeProperties(node, wr, context);
		getKContext().getSourceElements().put(node.getProperty(FlowElement.UID), node);
		writeChildren(node, wr, context);
	}
}
