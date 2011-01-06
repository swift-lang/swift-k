// ----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Feb 22, 2005
 */
package org.globus.cog.karajan.util.serialization;

import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class SimpleDateFormatConverter extends AbstractKarajanConverter {
	private static final DateFormatSymbols DEFAULT_SYMBOLS = new DateFormatSymbols();

	public SimpleDateFormatConverter(KarajanSerializationContext kcontext) {
		super(kcontext);
	}

	public boolean canConvert(Class type) {
		return SimpleDateFormat.class.equals(type);
	}

	public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
		SimpleDateFormat sdf = (SimpleDateFormat) source;
		writer.addAttribute("pattern", sdf.toPattern());
		DateFormatSymbols symbols = sdf.getDateFormatSymbols();
		if (DEFAULT_SYMBOLS.equals(symbols)) {
			writer.startNode("default");
			writer.endNode();
		}
		else {
			getKContext().getConverterLookup().lookupConverterForType(symbols.getClass()).marshal(
					symbols, writer, context);
		}
	}

	public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
		String pattern = reader.getAttribute("pattern");
		SimpleDateFormat sdf;
		reader.moveDown();
		if (reader.getNodeName().equals("default")) {
			sdf = new SimpleDateFormat(pattern, DEFAULT_SYMBOLS);
		}
		else {
			sdf = new SimpleDateFormat(pattern, (DateFormatSymbols) context.convertAnother(null,
					DateFormatSymbols.class));
		}
		reader.moveUp();
		return sdf;
	}
}