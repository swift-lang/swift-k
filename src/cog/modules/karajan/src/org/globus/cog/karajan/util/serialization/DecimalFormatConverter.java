// ----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Feb 22, 2005
 */
package org.globus.cog.karajan.util.serialization;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class DecimalFormatConverter extends AbstractKarajanConverter {
	private static final DecimalFormatSymbols DEFAULT_SYMBOLS = new DecimalFormatSymbols();

	public DecimalFormatConverter(KarajanSerializationContext kcontext) {
		super(kcontext);
	}

	public boolean canConvert(Class type) {
		return DecimalFormat.class.equals(type);
	}

	public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
		DecimalFormat df = (DecimalFormat) source;
		writer.addAttribute("pattern", df.toPattern());
		DecimalFormatSymbols symbols = df.getDecimalFormatSymbols();
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
		DecimalFormat df;
		reader.moveDown();
		if (reader.getNodeName().equals("default")) {
			df = new DecimalFormat(pattern, DEFAULT_SYMBOLS);
		}
		else {
			df = new DecimalFormat(pattern, (DecimalFormatSymbols) context.convertAnother(null,
					DecimalFormatSymbols.class));
		}
		reader.moveUp();
		return df;
	}
}