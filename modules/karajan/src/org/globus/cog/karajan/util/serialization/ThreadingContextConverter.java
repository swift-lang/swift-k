//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Feb 22, 2005
 */
package org.globus.cog.karajan.util.serialization;

import java.util.StringTokenizer;

import org.globus.cog.karajan.util.ThreadingContext;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class ThreadingContextConverter extends AbstractKarajanConverter {
	public ThreadingContextConverter(KarajanSerializationContext kcontext) {
		super(kcontext);
	}

	public boolean canConvert(Class type) {
		return ThreadingContext.class.equals(type);
	}

	public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
		writer.setValue(((ThreadingContext) source).toString());
	}

	public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
		String value = reader.getValue();
		StringTokenizer st = new StringTokenizer(value, "-");
		ThreadingContext tc = new ThreadingContext();
		while (st.hasMoreTokens()) {
			tc.split(Integer.parseInt(st.nextToken()));
		}
		return tc;
	}
}
