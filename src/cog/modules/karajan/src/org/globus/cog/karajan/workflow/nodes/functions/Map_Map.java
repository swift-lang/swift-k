// ----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Feb 19, 2005
 */
package org.globus.cog.karajan.workflow.nodes.functions;

import java.util.HashMap;

import org.globus.cog.karajan.arguments.ArgUtil;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.globus.cog.karajan.workflow.KarajanRuntimeException;
import org.globus.cog.karajan.workflow.nodes.Sequential;

public class Map_Map extends Sequential {

	public void pre(VariableStack stack) throws ExecutionException {
		super.pre(stack);
		VariableArgumentsOperator op = new VariableArgumentsOperator() {
			public Object initialValue() {
				return new HashMap();
			}

			public Object update(Object old, Object value)  {
				java.util.Map map = (java.util.Map) old;
				if (value instanceof java.util.Map.Entry) {
					java.util.Map.Entry entry = (java.util.Map.Entry) value; 
					map.put(entry.getKey(), entry.getValue());
				}
				else if (value instanceof java.util.Map) {
					map.putAll((java.util.Map) value);
				}
				else {
					throw new KarajanRuntimeException("Invalid argument (must be map:entry or map): "
							+ value + "(class " + value.getClass() + ")");
				}
				return map;
			}

			public boolean isCommutative() {
				return true;
			}
		};
		ArgUtil.setVariableArguments(stack, op);
	}

	public void post(VariableStack stack) throws ExecutionException {
		VariableArgumentsOperator vao = (VariableArgumentsOperator) ArgUtil.getVariableArguments(stack);
		ret(stack, vao.getValue());
		super.post(stack);
	}
}