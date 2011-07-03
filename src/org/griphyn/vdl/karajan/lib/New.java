/*
 * Created on Dec 26, 2006
 */
package org.griphyn.vdl.karajan.lib;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.util.TypeUtil;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.griphyn.vdl.mapping.DSHandle;
import org.griphyn.vdl.mapping.Path;
import org.griphyn.vdl.mapping.RootArrayDataNode;
import org.griphyn.vdl.mapping.RootDataNode;
import org.griphyn.vdl.mapping.ExternalDataNode;
import org.griphyn.vdl.mapping.file.ConcurrentMapper;
import org.griphyn.vdl.type.Type;
import org.griphyn.vdl.type.Types;

public class New extends VDLFunction {

	public static final Logger logger = Logger.getLogger(New.class);

	public static final Arg OA_TYPE = new Arg.Optional("type", null);
	public static final Arg OA_MAPPING = new Arg.Optional("mapping", null);
	public static final Arg OA_VALUE = new Arg.Optional("value", null);
	public static final Arg OA_DBGNAME = new Arg.Optional("dbgname", null);
	public static final Arg OA_WAITFOR = new Arg.Optional("waitfor", null);

	static {
		setArguments(New.class,
				new Arg[] { OA_TYPE, OA_MAPPING, OA_VALUE, OA_DBGNAME, OA_WAITFOR, });
	}

	public Object function(VariableStack stack) throws ExecutionException {
		String typename = TypeUtil.toString(OA_TYPE.getValue(stack));
		Object value = OA_VALUE.getValue(stack);
		@SuppressWarnings("unchecked")
        Map<String,Object> mapping = 
		    (Map<String,Object>) OA_MAPPING.getValue(stack);
		String dbgname = TypeUtil.toString(OA_DBGNAME.getValue(stack));
		String waitfor = (String) OA_WAITFOR.getValue(stack);

		if (mapping == null) {
			mapping = new HashMap<String,Object>();
		}

		if (dbgname != null) {
			mapping.put("dbgname", dbgname);
		}

		mapping.put("swift#restartid", getThreadPrefix(stack) + ":" + dbgname);

		if (waitfor != null) {
			mapping.put("waitfor", waitfor);
		}

		if (typename == null && value == null) {
			throw new ExecutionException("You must specify either a type or a value");
		}
	
		String mapper = (String) mapping.get("descriptor");
		if ("concurrent_mapper".equals(mapper)) {
		    String threadPrefix = getThreadPrefix(stack);
		    ConcurrentMapper.PARAM_THREAD_PREFIX.setValue(mapping, threadPrefix);
		}
		mapping.put("#basedir", stack.getExecutionContext().getBasedir());
		
		try {
			Type type;
			if (typename == null) {
				throw new ExecutionException
				("vdl:new requires a type specification for value: " + value);
			}
			else {
				type = Types.getType(typename);
			}
			DSHandle handle;
			if(typename.equals("external")) {
				handle = new ExternalDataNode();
			} else if (type.isArray()) {
				// dealing with array variable
				handle = new RootArrayDataNode(type);
				if (value != null) {
					if (value instanceof RootArrayDataNode) {
						handle = (RootArrayDataNode) value;
					}
					else {
						if (!(value instanceof List)) {
							throw new ExecutionException
							("An array variable can only be initialized " +
							 "with a list of values");
						}
						int index = 0;
						Iterator i = ((List) value).iterator();
						while (i.hasNext()) {
							// TODO check type consistency of elements with
							// the type of the array
							Object n = i.next();
							Path p = Path.EMPTY_PATH.addLast(String.valueOf(index), true);
							if (n instanceof DSHandle) {
								handle.getField(p).set((DSHandle) n);
							}
							else {
								throw new RuntimeException(
										"An array variable can only be initialized by a list of DSHandle values");
							}
							index++;
						}
					}
					handle.closeShallow();
				}

				handle.init(mapping);
			}
			else if (value instanceof DSHandle) {
				handle = (DSHandle) value;
			}
			else {
				handle = new RootDataNode(type);
				handle.init(mapping);
				if (value != null) {
					handle.setValue(internalValue(type, value));
				}
			}
			
			logger.debug("NEW id="+handle.getIdentifier());
			return handle;
		}
		catch (Exception e) {
			throw new ExecutionException(e);
		}
	}
}
