// ----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jan 18, 2005
 */
package org.globus.cog.karajan.util.serialization;

import java.util.Iterator;
import java.util.List;

import org.globus.cog.karajan.workflow.events.EventTargetPair;
import org.globus.cog.karajan.workflow.futures.FutureListener;
import org.globus.cog.karajan.workflow.futures.FutureVariableArguments;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class FutureVariableArgumentsConverter extends AbstractKarajanConverter {

	public FutureVariableArgumentsConverter(KarajanSerializationContext kcontext) {
		super(kcontext);
	}

	public boolean canConvert(Class type) {
		return FutureVariableArguments.class.equals(type);
	}

	public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
		FutureVariableArguments fva = (FutureVariableArguments) source;
		marshalObject(writer, context, "list", fva.getBackingList());
		marshalObject(writer, context, "closed", fva.isClosed());
		marshalObject(writer, context, "actions", fva.getModificationActions());
		
	}

	public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
		FutureVariableArguments fva = new FutureVariableArguments();
		String name = reader.getNodeName();
		List list = (List) unmarshalObject(reader, context, List.class, fva);
		Boolean closed = (Boolean) unmarshalObject(reader, context, Boolean.class, fva);
		List actions = (List) unmarshalObject(reader, context, List.class, fva);
		fva.appendAll(list);
		if (closed.booleanValue()) {
			fva.close();
		}
		Iterator i = actions.iterator();
		while (i.hasNext()) {
			EventTargetPair etp = (EventTargetPair) i.next();
			fva.addModificationAction(etp.getTarget(), etp.getEvent());
		}
		return fva;
	}

}