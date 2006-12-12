// ----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Feb 22, 2005
 */
package org.globus.cog.karajan.util.serialization;

import org.globus.cog.karajan.util.DefinitionEnvironment;
import org.globus.cog.karajan.workflow.nodes.FlowElement;
import org.globus.cog.karajan.workflow.nodes.user.UDEDefinition;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class UDEDefinitionConverter extends AbstractKarajanConverter {

	public UDEDefinitionConverter(KarajanSerializationContext kcontext) {
		super(kcontext);
	}

	public boolean canConvert(Class type) {
		return UDEDefinition.class.equals(type);
	}

	public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
		UDEDefinition uded = (UDEDefinition) source;
		marshalObject(writer, context, "env", uded.getEnv());
		marshalObject(writer, context, getKContext().getClassMapper().serializedClass(
				uded.getUde().getClass()), uded.getUde());
	}

	public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
		DefinitionEnvironment env = (DefinitionEnvironment) unmarshalObject(reader, context,
				DefinitionEnvironment.class, null);
		FlowElement ude = (FlowElement) unmarshalObject(reader, context,
				FlowElement.class, null);
		UDEDefinition uded = new UDEDefinition(ude, env);
		return uded;
	}
}