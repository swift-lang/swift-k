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

/*
 * Created on Jul 31, 2003
 */
package org.globus.cog.karajan.compiled.nodes;

import java.util.Iterator;
import java.util.List;

import k.rt.ExecutionException;
import k.rt.Stack;
import k.thr.LWThread;

import org.globus.cog.karajan.analyzer.ArgRef;
import org.globus.cog.karajan.analyzer.ChannelRef;
import org.globus.cog.karajan.analyzer.CompilationException;
import org.globus.cog.karajan.analyzer.Param;
import org.globus.cog.karajan.analyzer.Scope;
import org.globus.cog.karajan.compiled.nodes.functions.AbstractSingleValuedFunction;
import org.globus.cog.karajan.parser.WrapperNode;
import org.globus.cog.karajan.util.TypeUtil;

public class NewJavaObjectNode extends AbstractSingleValuedFunction {
	
	private ArgRef<String> classname;
	private ArgRef<Object> types;
	private ChannelRef<Object> c_vargs;

	@Override
	protected Param[] getParams() {
		return params("classname", optional("types", null), "...");
	}

	public Object function(Stack stack) {
		String className = classname.getValue(stack);
		Object[] args = c_vargs.get(stack).toArray();
		Class<?>[] argTypes = new Class[args.length];
		Object types = this.types.getValue(stack);
		if (types != null) {
			List<?> typesl = TypeUtil.toList(types);
			if (typesl.size() != args.length) {
				throw new ExecutionException(
						"The number of items in the types attribute does not match the number of arguments");
			}
			Iterator<?> i = typesl.iterator();
			for (int j = 0; j < argTypes.length; j++) {
				String type = (String) i.next();
				argTypes[j] = JavaMethodInvocationNode.getClass(this, type);
				if (JavaMethodInvocationNode.TYPES.containsKey(type)) {
					args[j] = JavaMethodInvocationNode.convert(argTypes[j], args[j],
							argTypes[j].isArray());
				}
				else {
					if (args[j] != null) {
						if (!argTypes[j].isAssignableFrom(args[j].getClass())) {
							args[j] = JavaMethodInvocationNode.convert(argTypes[j], args[j],
									argTypes[j].isArray());
						}
					}
				}
			}
		}
		else {
			for (int i = 0; i < args.length; i++) {
				if (args[i] == null) {
					argTypes[i] = null;
				}
				else {
					argTypes[i] = args[i].getClass();
				}
			}
		}
		try {
			Class<?> c = Class.forName(className);
			return c.getConstructor(argTypes).newInstance(args);
		}
		catch (Exception e) {
			throw new ExecutionException(this, "Could not instantiate " + className + " with arguments "
					+ JavaMethodInvocationNode.prettyPrintArray(args) + " ("
					+ e.getClass().getName() + ": " + e.getMessage() + ")", e);
		}
	}
}