/*
 * Swift Parallel Scripting Language (http://swift-lang.org)
 * Code from Java CoG Kit Project (see notice below) with modifications.
 *
 * Copyright 2005-2014 University of Chicago
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.karajan.compiled.nodes.grid;

import java.util.HashMap;
import java.util.Map;

import k.rt.ExecutionException;
import k.rt.Stack;

import org.globus.cog.abstraction.impl.common.task.ServiceContactImpl;
import org.globus.cog.abstraction.interfaces.Service;
import org.globus.cog.karajan.analyzer.ArgRef;
import org.globus.cog.karajan.analyzer.ChannelRef;
import org.globus.cog.karajan.analyzer.Param;
import org.globus.cog.karajan.compiled.nodes.functions.AbstractSingleValuedFunction;
import org.globus.cog.karajan.util.BoundContact;
import org.globus.cog.karajan.util.Property;

public class HostNode extends AbstractSingleValuedFunction {
	private ArgRef<String> name;
	private ArgRef<Number> cpus;
	private ChannelRef<Property> c_properties;
	private ChannelRef<Object> c_vargs;

	@Override
	protected Param[] getParams() {
		return params("name", optional("cpus", 1), channel("properties"), "...");
	}

	public Object function(Stack stack) {
		try {
			BoundContact contact = new BoundContact();
			String host = this.name.getValue(stack);
			contact.setName(host);
			contact.setCpus(this.cpus.getValue(stack).intValue());
			for (Object o : c_vargs.get(stack)) {
				if (!(o instanceof Service)) {
					throw new ExecutionException("Unexpected argument " + o);
				}
				Service s = (Service) o;
				if (s.getServiceContact() == null) {
					s.setServiceContact(new ServiceContactImpl(host));
				}
				contact.addService(s);
			}
			Map<String, Object> properties = contact.getProperties();
			if (properties == null) {
				properties = new HashMap<String, Object>();
				contact.setProperties(properties);
			}
			for (Property p : c_properties.get(stack)) {
				properties.put(p.getKey(), p.getValue());
			}
			return contact;
		}
		catch (Exception e) {
			throw new ExecutionException(this, e);
		}
	}
}