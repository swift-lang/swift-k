//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Mar 7, 2006
 */
package org.globus.cog.karajan.util.serialization;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public interface ElementMarshallingPolicy {
	void marshal(Object elem, HierarchicalStreamWriter wr, MarshallingContext context);
	
	void setKContext(KarajanSerializationContext kcontext);
}
