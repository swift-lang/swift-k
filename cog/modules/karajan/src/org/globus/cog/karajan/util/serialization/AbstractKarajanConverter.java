// ----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jan 12, 2005
 */
package org.globus.cog.karajan.util.serialization;

import com.thoughtworks.xstream.converters.ConversionException;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.ext.MXppReader;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public abstract class AbstractKarajanConverter implements Converter {
	private final KarajanSerializationContext kcontext;

	public AbstractKarajanConverter(KarajanSerializationContext kcontext) {
		this.kcontext = kcontext;
	}

	public KarajanSerializationContext getKContext() {
		return kcontext;
	}

	public void marshalObject(HierarchicalStreamWriter writer, MarshallingContext context,
			String tag, Object value) {
		writer.startNode(tag);
		if (value != null) {
			context.convertAnother(value);
		}
		writer.endNode();
	}

	public void marshalObjectCls(HierarchicalStreamWriter writer, MarshallingContext context,
			String tag, Object value) {
		writer.startNode(tag);
		if (value != null) {
			writer.addAttribute("class", getKContext().getClassMapper().serializedClass(
					value.getClass()));
			context.convertAnother(value);
		}
		writer.endNode();
	}

	public void marshalObject(HierarchicalStreamWriter writer, MarshallingContext context,
			String tag, int value) {
		marshalObject(writer, context, tag, new Integer(value));
	}

	public void marshalObject(HierarchicalStreamWriter writer, MarshallingContext context,
			String tag, boolean value) {
		marshalObject(writer, context, tag, Boolean.valueOf(value));
	}

	public Object unmarshalObject(HierarchicalStreamReader reader, UnmarshallingContext context,
			Class cls, Object current) {
		try {
			reader.moveDown();
			Class actualClass = cls;
			String scls = reader.getAttribute("class");
			if (scls != null) {
				actualClass = Class.forName(scls);
				if (!cls.isAssignableFrom(actualClass)) {
					throw new ConversionException("Invalid serialized class. Expected " + cls
							+ ", got " + actualClass);
				}
			}
			Object obj = context.convertAnother(current, actualClass);
			reader.moveUp();
			return obj;
		}
		catch (ConversionException e) {
			throw e;
		}
		catch (Exception e) {
			throw new ConversionException("Uncaught exception " + e.getMessage() + ": "
					+ reader.getNodeName() + ", line " + ((MXppReader) reader).getLineNumber());
		}
	}

	public Object unmarshalObject(HierarchicalStreamReader reader, UnmarshallingContext context) {
		reader.moveDown();
		reader.moveDown();
		String type = reader.getAttribute("class");
		Object obj = context.convertAnother(null, getKContext().getClassMapper().realClass(type));
		reader.moveUp();
		reader.moveUp();
		return obj;
	}
}