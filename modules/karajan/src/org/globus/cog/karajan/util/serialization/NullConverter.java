//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Dec 14, 2004
 */
package org.globus.cog.karajan.util.serialization;

import org.globus.cog.karajan.util.StateManager;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class NullConverter implements Converter {

	public boolean canConvert(Class cls) {
		return StateManager.class.isAssignableFrom(cls);
	}

	public void marshal(Object arg0, HierarchicalStreamWriter arg1, MarshallingContext arg2) {
	}

	public Object unmarshal(HierarchicalStreamReader arg0, UnmarshallingContext arg1) {
		return null;
	}

}
