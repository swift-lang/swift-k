//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Feb 22, 2005
 */
package org.globus.cog.karajan.util.serialization;

import org.globus.cog.karajan.workflow.ErrorHandler;
import org.globus.cog.karajan.workflow.nodes.FlowElement;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class ErrorHandlerConverter extends AbstractKarajanConverter {

	public ErrorHandlerConverter(KarajanSerializationContext kcontext) {
		super(kcontext);
	}

	public boolean canConvert(Class type) {
		return ErrorHandler.class.equals(type);
	}

	public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
		ErrorHandler eh = (ErrorHandler) source;
		marshallObject(writer, context, "type", eh.getType());
		marshallObject(writer, context, "handler", eh.getHandler());
	}

	public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
		ErrorHandler eh = new ErrorHandler();
		eh.setType((String) unmarshallObject(reader, context, String.class, eh));
		reader.moveDown();
		eh.setHandler((FlowElement) unmarshallObject(reader, context, FlowElement.class, eh));
		reader.moveUp();
		return eh;
	}

}
