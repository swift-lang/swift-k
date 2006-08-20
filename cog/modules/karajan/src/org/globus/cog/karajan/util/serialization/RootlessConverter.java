//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jan 12, 2005
 */
package org.globus.cog.karajan.util.serialization;

import org.globus.cog.karajan.workflow.nodes.FlowElement;
import org.globus.cog.karajan.workflow.nodes.ProjectNode;

import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.ext.MXppReader;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;

public class RootlessConverter extends ElementConverter {
	private final FlowElement parent;

	public RootlessConverter(KarajanSerializationContext kcontext, FlowElement parent) {
		super(kcontext);
		this.parent = parent;
	}
	
	public boolean canConvert(Class cls) {
		return ProjectNode.class.isAssignableFrom(cls);
	}

	public Object unmarshal(HierarchicalStreamReader rd, UnmarshallingContext context) {
		MXppReader mrd = (MXppReader) rd;
		String name = rd.getNodeName();
		for(int i = 0; i < mrd.getAttributeCount(); i++) {
			String pname = mrd.getAttributeName(i).toLowerCase().intern();
			if (pname.startsWith("_")) {
				parent.setProperty(pname, mrd.getAttributeValue(i));
			}
			else {
				parent.addStaticArgument(pname, mrd.getAttributeValue(i));
			}
		}
		readChildren(parent, rd, context);
		return parent;
	}
	
	protected FlowElement readChild(FlowElement node, HierarchicalStreamReader rd,
			UnmarshallingContext context) {
		return super.readChild(node, rd, context);
	}
}
