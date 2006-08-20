//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Feb 13, 2006
 */
package org.globus.cog.karajan.util.serialization;

import org.globus.cog.karajan.workflow.FlowElementWrapper;

import com.thoughtworks.xstream.alias.ClassMapper;
import com.thoughtworks.xstream.converters.DataHolder;
import com.thoughtworks.xstream.core.DefaultConverterLookup;
import com.thoughtworks.xstream.core.TreeMarshaller;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class SourceMarshaller extends TreeMarshaller {

	public SourceMarshaller(HierarchicalStreamWriter writer, DefaultConverterLookup converterLookup, ClassMapper classMapper) {
		super(writer, converterLookup, classMapper);
	}

	public void start(Object item, DataHolder dataHolder) {
		if (item instanceof FlowElementWrapper) {
			writer.startNode(((FlowElementWrapper) item).getElementType());
            convertAnother(item);
            writer.endNode();
		}
		else {
			super.start(item, dataHolder);
		}
	}
}
