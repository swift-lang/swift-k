//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Mar 7, 2006
 */
package org.globus.cog.karajan.util.serialization;

import java.util.Iterator;
import java.util.Map;

import org.globus.cog.karajan.util.ElementProperty;
import org.globus.cog.karajan.workflow.nodes.FlowElement;
import org.globus.cog.karajan.workflow.nodes.FlowNode;

import com.thoughtworks.xstream.converters.ConversionException;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public abstract class AbstractElementMarshallingPolicy implements ElementMarshallingPolicy {
	private KarajanSerializationContext kcontext;

	public AbstractElementMarshallingPolicy() {
	}

	public AbstractElementMarshallingPolicy(KarajanSerializationContext kcontext) {
		this.kcontext = kcontext;
	}

	public KarajanSerializationContext getKContext() {
		return kcontext;
	}

	public void setKContext(KarajanSerializationContext kcontext) {
		this.kcontext = kcontext;
	}

	protected void writeProperties(FlowElement node, HierarchicalStreamWriter wr,
			MarshallingContext context) {
		Iterator i = node.propertyNames().iterator();
		String text = null;
		while (i.hasNext()) {
			String name = (String) i.next();
			if (name.equals(FlowElement.TEXT)) {
				Object prop = node.getProperty(name);
				if (prop instanceof ElementProperty) {
					text = ((ElementProperty) prop).getUnparsed();
				}
				else if (prop instanceof String) {
					text = (String) prop;
				}
				else {
					throw new ConversionException("Unexpected property value: " + prop);
				}
			}
			else if (!name.startsWith("__")) {
				wr.addAttribute(name.toLowerCase(),
						XMLUtils.escape(node.getProperty(name).toString()));
			}
		}
		Map sa = node.getStaticArguments();
		i = sa.keySet().iterator();
		while (i.hasNext()) {
			String name = (String) i.next();
			wr.addAttribute(name.toLowerCase(), XMLUtils.escape(sa.get(name).toString()));
		}
		if (text != null) {
			wr.setValue(XMLUtils.escape(text));
		}
	}

	protected void writeChildren(FlowElement node, HierarchicalStreamWriter wr,
			MarshallingContext context) {
		if (node.hasProperty("__no_child_serialization")) {
			return;
		}
		Iterator j = node.elements().iterator();
		while (j.hasNext()) {
			FlowElement child = (FlowElement) j.next();
			writeNode(child, wr, context);
		}
	}

	protected void writeNode(FlowElement node, HierarchicalStreamWriter wr,
			MarshallingContext context) {

		boolean checkpointable = true;
		if (node instanceof FlowNode && !((FlowNode) node).isCheckpointable()) {
			checkpointable = false;
		}
		if (!checkpointable) {
			wr.startNode("nonCheckpointable");
			wr.endNode();
			return;
		}
		else {
			wr.startNode(node.getElementType());
		}

		context.convertAnother(node);
		wr.endNode();
	}
}
