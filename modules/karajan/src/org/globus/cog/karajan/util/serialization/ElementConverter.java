// ----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Dec 14, 2004
 */
package org.globus.cog.karajan.util.serialization;

import org.globus.cog.karajan.parser.ParsingException;
import org.globus.cog.karajan.util.ElementProperty;
import org.globus.cog.karajan.workflow.FlowElementWrapper;
import org.globus.cog.karajan.workflow.nodes.FlowElement;
import org.globus.cog.karajan.workflow.nodes.UnknownElement;

import com.thoughtworks.xstream.converters.ConversionException;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.ext.MXppReader;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class ElementConverter extends AbstractKarajanConverter {

	public ElementConverter(KarajanSerializationContext kcontext) {
		super(kcontext);
	}

	public boolean canConvert(Class cls) {
		return FlowElement.class.isAssignableFrom(cls);
	}

	public void marshal(Object elem, HierarchicalStreamWriter wr, MarshallingContext context) {
		getKContext().getElementMarshallingPolicy().marshal(elem, wr, context);
	}

	public Object unmarshal(HierarchicalStreamReader rd, UnmarshallingContext context) {
		FlowElement result = null;
		String name = rd.getNodeName().toLowerCase().intern();
		if (getKContext().isSource()) {
			FlowElement parent = getKContext().getParent();
			FlowElementWrapper wrapper = new FlowElementWrapper();

			if (wrapper == null) {
				throw new ConversionException("Unexpected element: " + name);
			}
			wrapper.setElementType(name);
			wrapper.setParent(parent);
			setProperties(wrapper, rd, context);
			readChildren(wrapper, rd, context);
			if (parent != null) {
				parent.addElement(wrapper);
			}
			result = wrapper;
		}
		else {
			String id = rd.getAttribute(FlowElement.UID);
			if (id == null) {
				throw new ConversionException("_uid is null");
			}
			if (id.equals("-1") || rd.getAttribute("_source") != null) {
				getKContext().setSource(true);
				rd.moveDown();
				result = (FlowElement) context.convertAnother(null, FlowElement.class);
				rd.moveUp();
				getKContext().setSource(false);
				if (result == null) {
					result = new UnknownElement();
				}
			}
			else {
				result = getKContext().getTree().getUIDMap().get(new Integer(id));
				if (result == null) {
					result = new UnknownElement();
					result.setProperty(FlowElement.UID, new Integer(id));
				}
			}
		}
		return result;
	}

	protected void setProperties(FlowElement wrapper, HierarchicalStreamReader rd,
			UnmarshallingContext context) {
		MXppReader mrd = (MXppReader) rd;
		for (int i = 0; i < mrd.getAttributeCount(); i++) {
			String name = mrd.getAttributeName(i).toLowerCase().intern();
			if (name.equalsIgnoreCase(FlowElement.UID)) {
				Integer uid = new Integer(mrd.getAttributeValue(i));
				getKContext().getTree().getUIDMap().put(uid, wrapper);
				wrapper.setProperty(name, uid);
			}
			else if (name.equals(FlowElement.LINE)) {
				wrapper.setProperty(name, Integer.valueOf(mrd.getAttributeValue(i)));
			}
			else if (name.startsWith("__")) {
				// Do not set non-serializable properties. This shouldn't even
				// be here
				continue;
			}
			else if (name.startsWith("_")) {
				wrapper.setProperty(name, mrd.getAttributeValue(i));
			}
			else if (name.equals("__id_")) {
				// unescape the id attribute to avoid clashes with XStream
				wrapper.addStaticArgument("id", mrd.getAttributeValue(i));
			}
			else if (name.equals("id")) {
				// this one belongs to XStream and should be skipped
				continue;
			}
			else {
				try {
					wrapper.addStaticArgument(name,
							ElementProperty.parse(mrd.getAttributeValue(i)));
				}
				catch (ParsingException e) {
					throw new ConversionException("Parsing error: " + e.getMessage(), e);
				}
			}
		}
		if (wrapper.acceptsInlineText() && rd.getValue() != null) {
			try {
				wrapper.setProperty(FlowElement.TEXT, ElementProperty.parse(rd.getValue()));
			}
			catch (ParsingException e) {
				throw new ConversionException("Parsing error: " + e.getMessage(), e);
			}
		}
		if (getKContext().isKmode() && !wrapper.hasProperty(FlowElement.LINE)) {
			wrapper.setProperty(FlowElement.LINE, new Integer(mrd.getLineNumber()));
		}
		if (!wrapper.hasProperty(FlowElement.UID)) {
			Integer uid = getKContext().getTree().getUIDMap().nextUID();
			wrapper.setProperty(FlowElement.UID, uid);
			getKContext().getTree().getUIDMap().put(uid, wrapper);
		}
		else {
			getKContext().getTree().getUIDMap().put((Integer) wrapper.getProperty(FlowElement.UID),
					wrapper);
		}
	}

	protected void readChildren(FlowElement node, HierarchicalStreamReader rd,
			UnmarshallingContext context) {
		while (rd.hasMoreChildren()) {
			getKContext().setParent(node);
			rd.moveDown();
			readChild(node, rd, context);
			rd.moveUp();
		}
	}

	protected FlowElement readChild(FlowElement wrapper, HierarchicalStreamReader rd,
			UnmarshallingContext context) {
		return (FlowElement) context.convertAnother(wrapper, FlowElement.class);
	}

}