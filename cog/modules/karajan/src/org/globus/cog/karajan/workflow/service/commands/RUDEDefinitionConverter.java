//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Mar 10, 2006
 */
package org.globus.cog.karajan.workflow.service.commands;

import org.globus.cog.karajan.util.DefinitionEnvironment;
import org.globus.cog.karajan.util.serialization.KarajanSerializationContext;
import org.globus.cog.karajan.util.serialization.UDEDefinitionConverter;
import org.globus.cog.karajan.workflow.nodes.user.UDEDefinition;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class RUDEDefinitionConverter extends UDEDefinitionConverter {
	
	private final DefinitionEnvironment env;

	public RUDEDefinitionConverter(KarajanSerializationContext kcontext, DefinitionEnvironment env) {
		super(kcontext);
		this.env = env;
	}

	public boolean canConvert(Class type) {
		return UDEDefinition.class.equals(type);
	}

	public void marshal(Object original, HierarchicalStreamWriter writer, MarshallingContext context) {
		UDEDefinition odef = (UDEDefinition) original;
		UDEDefinition ndef = new UDEDefinition(odef.getUde(), env);
		super.marshal(ndef, writer, context);
	}
}
