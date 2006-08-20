// ----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jan 18, 2005
 */
package org.globus.cog.karajan.util.serialization;

import org.globus.cog.karajan.workflow.futures.FutureVariableArguments;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class FutureVariableArgumentsIteratorConverter extends AbstractKarajanConverter {

	public FutureVariableArgumentsIteratorConverter(KarajanSerializationContext kcontext) {
		super(kcontext);
	}

	public boolean canConvert(Class type) {
		return FutureVariableArguments.Iterator.class.equals(type);
	}

	public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
		FutureVariableArguments.Iterator fvai = (FutureVariableArguments.Iterator) source;
		marshallObject(writer, context, "fvargs", fvai.getVargs());
		marshallObject(writer, context, "crt", fvai.current());
	}

	public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
		FutureVariableArguments vargs = (FutureVariableArguments) unmarshallObject(reader, context,
				FutureVariableArguments.class, null);
		Integer crt = (Integer) unmarshallObject(reader, context, Integer.class, null);
		FutureVariableArguments.Iterator fvai = new FutureVariableArguments.Iterator(vargs,
				crt.intValue());
		return fvai;
	}

}