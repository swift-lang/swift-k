// ----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jan 18, 2005
 */
package org.globus.cog.karajan.util.serialization;

import java.util.List;

import org.globus.cog.karajan.util.ListKarajanIterator;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class ListLoopIteratorConverter extends AbstractKarajanConverter {

	public ListLoopIteratorConverter(KarajanSerializationContext kcontext) {
		super(kcontext);
	}

	public boolean canConvert(Class type) {
		return ListKarajanIterator.class.equals(type);
	}

	public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
		ListKarajanIterator lli = (ListKarajanIterator) source;
		marshallObject(writer, context, "list", lli.getList());
		marshallObject(writer, context, "current", lli.current());
	}

	public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
		List list = (List) unmarshallObject(reader, context, List.class, null);
		Integer current = (Integer) unmarshallObject(reader, context, Integer.class, list);
		ListKarajanIterator lli = new ListKarajanIterator(list);
		lli.skipTo(current.intValue());
		return lli;
	}

}