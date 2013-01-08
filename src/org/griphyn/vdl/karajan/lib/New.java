/*
 * Copyright 2012 University of Chicago
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


/*
 * Created on Dec 26, 2006
 */
package org.griphyn.vdl.karajan.lib;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.util.TypeUtil;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.griphyn.vdl.karajan.VDL2ExecutionContext;
import org.griphyn.vdl.mapping.AbstractDataNode;
import org.griphyn.vdl.mapping.DSHandle;
import org.griphyn.vdl.mapping.DuplicateMappingChecker;
import org.griphyn.vdl.mapping.ExternalDataNode;
import org.griphyn.vdl.mapping.MappingParam;
import org.griphyn.vdl.mapping.MappingParamSet;
import org.griphyn.vdl.mapping.Path;
import org.griphyn.vdl.mapping.RootArrayDataNode;
import org.griphyn.vdl.mapping.RootDataNode;
import org.griphyn.vdl.mapping.file.ConcurrentMapper;
import org.griphyn.vdl.type.Type;
import org.griphyn.vdl.type.Types;

public class New extends VDLFunction {

	public static final Logger logger = Logger.getLogger(New.class);

	public static final Arg OA_TYPE = new Arg.Optional("type", null);
	public static final Arg OA_MAPPING = new Arg.Optional("mapping", null);
	public static final Arg OA_VALUE = new Arg.Optional("value", null);
	public static final Arg OA_DBGNAME = new Arg.Optional("dbgname", null);
	public static final Arg OA_WAITCOUNT = new Arg.Optional("waitcount", null);
	public static final Arg OA_INPUT = new Arg.Optional("input", Boolean.FALSE);

	static {
		setArguments(New.class,
				new Arg[] { OA_TYPE, OA_MAPPING, OA_VALUE, OA_DBGNAME, OA_WAITCOUNT, OA_INPUT});
	}
	
	private Tracer tracer;

	@Override
    protected void initializeStatic() {
        super.initializeStatic();
        tracer = Tracer.getTracer(this);
    }

    public Object function(VariableStack stack) throws ExecutionException {
		String typename = TypeUtil.toString(OA_TYPE.getValue(stack));
		Object value = OA_VALUE.getValue(stack);
		@SuppressWarnings("unchecked")
        Map<String,Object> mapping = 
		    (Map<String,Object>) OA_MAPPING.getValue(stack);
		String dbgname = TypeUtil.toString(OA_DBGNAME.getValue(stack));
		String waitCount = (String) OA_WAITCOUNT.getValue(stack);
		boolean input = TypeUtil.toBoolean(OA_INPUT.getValue(stack));
		String line = (String) getProperty("_defline");
		
		MappingParamSet mps = new MappingParamSet();
		mps.setAll(mapping);

		if (dbgname != null) {
			mps.set(MappingParam.SWIFT_DBGNAME, dbgname);
		}
		
		if (input) {
		    mps.set(MappingParam.SWIFT_INPUT, true);
		}
		
		if (line != null) {
		    mps.set(MappingParam.SWIFT_LINE, line);
		}
		
		String threadPrefix = getThreadPrefix(stack);

		mps.set(MappingParam.SWIFT_RESTARTID, threadPrefix + ":" + dbgname);

		// input means never written to, but read at least once
		int initialWriteRefCount;
		boolean noWriters = input;
		if (waitCount != null) {
			initialWriteRefCount = Integer.parseInt(waitCount);
		}
		else {
		    initialWriteRefCount = 0;
		}

		if (typename == null && value == null) {
			throw new ExecutionException("You must specify either a type or a value");
		}
	
		String mapper = (String) mps.get(MappingParam.SWIFT_DESCRIPTOR);
		if ("concurrent_mapper".equals(mapper)) {
		    mps.set(ConcurrentMapper.PARAM_THREAD_PREFIX, threadPrefix);
		}
		mps.set(MappingParam.SWIFT_BASEDIR, stack.getExecutionContext().getBasedir());
		
		try {
			Type type;
			if (typename == null) {
				throw new ExecutionException("vdl:new requires a type specification for value: " + value);
			}
			else {
				type = Types.getType(typename);
			}
			DSHandle handle;
			if (typename.equals("external")) {
			    if (tracer.isEnabled()) {
			        tracer.trace(threadPrefix, dbgname + " = external");
			    }
				handle = new ExternalDataNode();
			}
			else if (type.isArray()) {
				// dealing with array variable
				handle = new RootArrayDataNode(type, 
				    (DuplicateMappingChecker) stack.getGlobal(VDL2ExecutionContext.DM_CHECKER));
				if (value != null) {
					if (value instanceof RootArrayDataNode) {
					    if (tracer.isEnabled()) {
					        tracer.trace(threadPrefix, dbgname + " = " + Tracer.getVarName((RootDataNode) value));
					    }
						handle = (RootArrayDataNode) value;
					}
					else {
						if (!(value instanceof List)) {
							throw new ExecutionException("An array variable can only be initialized with a list of values");
						}
						if (tracer.isEnabled()) {
                            tracer.trace(threadPrefix, dbgname + " = " + formatList((List<?>) value));
                        }
						int index = 0;
						Iterator<?> i = ((List<?>) value).iterator();
						while (i.hasNext()) {
							// TODO check type consistency of elements with
							// the type of the array
							Object n = i.next();
							Path p = Path.EMPTY_PATH.addLast(index, true);
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
				else {			    
				    if (tracer.isEnabled()) {
				        tracer.trace(threadPrefix, dbgname);
                    }
				}

				handle.init(mps);
			}
			else if (value instanceof DSHandle) {
			    if (tracer.isEnabled()) {
			        tracer.trace(threadPrefix, dbgname + " = " + Tracer.getVarName((DSHandle) value));
                }
				handle = (DSHandle) value;
			}
			else {
				handle = new RootDataNode(type, 
				    (DuplicateMappingChecker) stack.getGlobal(VDL2ExecutionContext.DM_CHECKER));
				handle.init(mps);
				if (value != null) {
				    if (tracer.isEnabled()) {
				        tracer.trace(threadPrefix, dbgname + " = " + value);
				    }
					handle.setValue(internalValue(type, value));
				}
				else {
				    if (tracer.isEnabled()) {
                        tracer.trace(threadPrefix, dbgname + " " + mps);
                    }
				}
			}
			
			if (AbstractDataNode.provenance && logger.isDebugEnabled()) {
			    logger.debug("NEW id=" + handle.getIdentifier());
			}
			handle.setWriteRefCount(initialWriteRefCount);
			return handle;
		}
		catch (Exception e) {
			throw new ExecutionException(e);
		}
	}

    private String formatList(List<?> value) {
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        unfoldList(value, sb);
        sb.append(']');
        return sb.toString();
    }

    private void unfoldList(List<?> value, StringBuilder sb) {
        boolean first = true;
        for (Object v : value) {
            if (first) {
                first = false;
            }
            else {
                sb.append(", ");
            }
            sb.append(v);
        }
    }
}
