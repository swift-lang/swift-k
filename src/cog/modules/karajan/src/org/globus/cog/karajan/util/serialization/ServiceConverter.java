// ----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jan 18, 2005
 */
package org.globus.cog.karajan.util.serialization;

import org.globus.cog.abstraction.impl.common.task.ServiceImpl;
import org.globus.cog.abstraction.interfaces.SecurityContext;
import org.globus.cog.abstraction.interfaces.ServiceContact;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class ServiceConverter extends AbstractKarajanConverter {

	public ServiceConverter(KarajanSerializationContext kcontext) {
		super(kcontext);
	}

	public boolean canConvert(Class type) {
		return ServiceImpl.class.equals(type);
	}

	public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
		ServiceImpl si = (ServiceImpl) source;
		marshallObject(writer, context, "type", si.getType());
		marshallObject(writer, context, "provider", si.getProvider());
		marshallObjectCls(writer, context, "serviceContact", si.getServiceContact());
		marshallObjectCls(writer, context, "securityContext", si.getSecurityContext());
	}

	public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
		Integer type = (Integer) unmarshallObject(reader, context, Integer.class, null);
		String provider = (String) unmarshallObject(reader, context, String.class, null);
		ServiceContact svc = (ServiceContact) unmarshallObject(reader, context,
				ServiceContact.class, null);
		SecurityContext sc = (SecurityContext) unmarshallObject(reader, context,
				SecurityContext.class, null);
		ServiceImpl s = new ServiceImpl();
		s.setType(type.intValue());
		s.setProvider(provider);
		s.setServiceContact(svc);
		s.setSecurityContext(sc);
		return s;
	}

}