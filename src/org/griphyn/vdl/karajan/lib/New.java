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

import k.rt.Context;
import k.rt.ExecutionException;
import k.rt.Stack;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.analyzer.ArgRef;
import org.globus.cog.karajan.analyzer.CompilationException;
import org.globus.cog.karajan.analyzer.Scope;
import org.globus.cog.karajan.analyzer.Signature;
import org.globus.cog.karajan.analyzer.VarRef;
import org.globus.cog.karajan.compiled.nodes.Node;
import org.globus.cog.karajan.parser.WrapperNode;
import org.griphyn.vdl.mapping.AbstractDataNode;
import org.griphyn.vdl.mapping.DSHandle;
import org.griphyn.vdl.mapping.DuplicateMappingChecker;
import org.griphyn.vdl.mapping.ExternalDataNode;
import org.griphyn.vdl.mapping.MappingParam;
import org.griphyn.vdl.mapping.MappingParamSet;
import org.griphyn.vdl.mapping.OOBYield;
import org.griphyn.vdl.mapping.RootArrayDataNode;
import org.griphyn.vdl.mapping.RootDataNode;
import org.griphyn.vdl.mapping.file.ConcurrentMapper;
import org.griphyn.vdl.type.Type;
import org.griphyn.vdl.type.Types;

public class New extends SwiftFunction {
	public static final Logger logger = Logger.getLogger(New.class);
	
	private ArgRef<String> type;
	private ArgRef<Map<String, Object>> mapping;
	private ArgRef<Object> value;
	private ArgRef<String> dbgname;
	private ArgRef<Number> waitCount;
	private ArgRef<Boolean> input;
	private ArgRef<String> _defline;
	
	private VarRef<Context> context;
	private VarRef<String> cwd;
	
	@Override
	protected Signature getSignature() {
	    return new Signature(params("type", optional("mapping", null), optional("value", null), 
	        optional("dbgname", null), optional("waitCount", null), optional("input", Boolean.FALSE), optional("_defline", null)));
	}
	
	private Tracer tracer;

    @Override
    protected void addLocals(Scope scope) {
        super.addLocals(scope);
        context = scope.getVarRef("#context");
        cwd = scope.getVarRef("CWD");
    }
   
    @Override
    public Node compile(WrapperNode w, Scope scope)
            throws CompilationException {
        Node fn = super.compile(w, scope);
        if (_defline.getValue() != null) {
            setLine(Integer.parseInt(_defline.getValue()));
        }
        tracer = Tracer.getTracer(this);
        return fn;
    }

    @Override
    public Object function(Stack stack) {
		String typename = this.type.getValue(stack);
		Object value = this.value.getValue(stack);
        Map<String,Object> mapping = this.mapping.getValue(stack);
		String dbgname = this.dbgname.getValue(stack);
		Number waitCount = this.waitCount.getValue(stack);
		boolean input = this.input.getValue(stack);
		String line = this._defline.getValue(stack);
		
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
		
		String threadPrefix = getThreadPrefix();

		mps.set(MappingParam.SWIFT_RESTARTID, threadPrefix + ":" + dbgname);

		// input means never written to, but read at least once
		int initialWriteRefCount;
		boolean noWriters = input;
		if (waitCount != null) {
			initialWriteRefCount = waitCount.intValue();
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
		mps.set(MappingParam.SWIFT_BASEDIR, cwd.getValue(stack));
		
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
				handle = new RootArrayDataNode(type, getDMChecker(stack));
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
							if (n instanceof DSHandle) {
								handle.getField(index).set((DSHandle) n);
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
				handle = new RootDataNode(type, getDMChecker(stack));
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
		catch (OOBYield y) {
		    throw y.wrapped(this);
		}
		catch (Exception e) {
			throw new ExecutionException(this, e);
		}
	}

    private DuplicateMappingChecker getDMChecker(Stack stack) {
        Context ctx = this.context.getValue(stack);
        return (DuplicateMappingChecker) ctx.getAttribute("SWIFT:DM_CHECKER");
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

    @Override
    public String getTextualName() {
        return "Variable declaration";
    }
}
