// ----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jan 18, 2005
 */
package org.globus.cog.karajan.util.serialization;

import org.globus.cog.karajan.util.DefList;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class DefListConverter extends AbstractKarajanConverter {

	public DefListConverter(KarajanSerializationContext kcontext) {
		super(kcontext);
	}

	public boolean canConvert(Class type) {
		return DefList.class.equals(type);
	}

	public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
		DefList dl = (DefList) source;
		writer.addAttribute("defname", dl.getName());
		DefList prev = dl.getPrev();
		if (prev != null) {
			marshalObject(writer, context, "prev", prev);
		}
		else {
			writer.addAttribute("first", "true");
		}
		String[] prefixes = dl.currentPrefixes();
		Object[] defs = dl.currentObjects();
		marshalObject(writer, context, "prefixes", prefixes);
		marshalObject(writer, context, "defs", defs);
	}

	public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
		String defname = reader.getAttribute("defname");
		boolean first = Boolean.valueOf(reader.getAttribute("first")).booleanValue();
		DefList dl;
		if (!first) {
			DefList prev = (DefList) unmarshalObject(reader, context, DefList.class, null);
			dl = new DefList(prev);
		}
		else {
			dl = new DefList(defname);
		}
		
		String[] prefixes = (String[]) unmarshalObject(reader, context, String[].class, null);
		Object[] defs = (Object[]) unmarshalObject(reader, context, Object[].class, null);
		for (int i = 0; i < prefixes.length; i++) {
			dl.put(prefixes[i], defs[i]);
		}
		return dl;
	}

}