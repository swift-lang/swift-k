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
import k.thr.LWThread;

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
import org.griphyn.vdl.mapping.InvalidMapperException;
import org.griphyn.vdl.mapping.Mapper;
import org.griphyn.vdl.mapping.MapperFactory;
import org.griphyn.vdl.mapping.NullMapper;
import org.griphyn.vdl.mapping.RootArrayDataNode;
import org.griphyn.vdl.mapping.RootDataNode;
import org.griphyn.vdl.mapping.RootHandle;
import org.griphyn.vdl.type.NoSuchTypeException;
import org.griphyn.vdl.type.Type;
import org.griphyn.vdl.type.Types;

public class New extends SwiftFunction {
	public static final Logger logger = Logger.getLogger(New.class);
	
	private static final Mapper NULL_MAPPER = new NullMapper();
	
	private ArgRef<String> type;
	private ArgRef<Map<String, Object>> mapping;
	private ArgRef<Object> value;
	private ArgRef<String> dbgname;
	private ArgRef<Number> waitCount;
	private ArgRef<Boolean> input;
	private ArgRef<Integer> _defline;
	
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
            setLine(_defline.getValue());
        }
        tracer = Tracer.getTracer(this);
        return fn;
    }

    @Override
    public Object function(Stack stack) {
		String typename = this.type.getValue(stack);
		Object value = this.value.getValue(stack);
        Map<String, Object> mapping = this.mapping.getValue(stack);
		String dbgname = this.dbgname.getValue(stack);
		Number waitCount = this.waitCount.getValue(stack);
		boolean input = this.input.getValue(stack);
		Integer line = this._defline.getValue(stack);
		
		if (typename == null && value == null) {
            throw new ExecutionException("You must specify either a type or a value");
        }
				
		Mapper mapper;
		
		Type type = getType(typename);
		if (type.hasNonPrimitiveComponents()) {
		    String desc = (String) mapping.get("swift#descriptor");
		    try {
                mapper = MapperFactory.getMapper(desc);
            }
            catch (InvalidMapperException e) {
                throw new ExecutionException(this, "Invalid mapper '" + desc + "'");
            }
		    mapping.remove("descriptor");
		    mapper.setParameters(mapping);
		    mapper.setBaseDir(cwd.getValue(stack));
		}
		else {
		    mapper = NULL_MAPPER;
		}
		
		LWThread thread = LWThread.currentThread();

		// input means never written to, but read at least once
		int initialWriteRefCount;
		boolean noWriters = input;
		if (waitCount != null) {
			initialWriteRefCount = waitCount.intValue();
		}
		else {
		    initialWriteRefCount = 0;
		}
	
		boolean initialize = true;
		
		try {
			DSHandle handle;
			if (typename.equals("external")) {
			    if (tracer.isEnabled()) {
			        tracer.trace(thread, dbgname + " = external");
			    }
				handle = initHandle(new ExternalDataNode(dbgname), mapper, thread, line, input);
			}
			else if (type.isArray()) {
				// dealing with array variable
				if (value != null) {
					if (value instanceof RootArrayDataNode) {
					    if (tracer.isEnabled()) {
					        tracer.trace(thread, dbgname + " = " + Tracer.getVarName((RootDataNode) value));
					    }
						handle = (RootArrayDataNode) value;
					}
					else {
					    handle = initHandle(createArrayFromList(stack, thread, dbgname, type), 
					        mapper, thread, line, input); 		    
					}
					handle.closeShallow();
				}
				else {			    
				    if (tracer.isEnabled()) {
				        tracer.trace(thread, dbgname);
                    }
				    handle = initHandle(createEmptyArray(stack, thread, dbgname, type), 
                            mapper, thread, line, input);
				}				
			}
			else if (value instanceof DSHandle) {
			    // TODO Check this. Seems suspicious.
			    if (tracer.isEnabled()) {
			        tracer.trace(thread, dbgname + " = " + Tracer.getVarName((DSHandle) value));
                }
				handle = (DSHandle) value;
			}
			else {
				handle = initHandle(new RootDataNode(dbgname, type, getDMChecker(stack)), 
				    mapper, thread, line, input);
				if (value != null) {
				    if (tracer.isEnabled()) {
				        tracer.trace(thread, dbgname + " = " + value);
				    }
					handle.setValue(internalValue(type, value));
				}
				else {
				    if (tracer.isEnabled()) {
                        tracer.trace(thread, dbgname + " " + mapper);
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
			throw new ExecutionException(this, e);
		}
	}

    private DSHandle initHandle(RootHandle handle, Mapper mapper, LWThread thread, Integer line, boolean input) {
        handle.setThread(thread);
        if (line != null) {
            handle.setLine(line);
        }
        handle.setInput(input);
        handle.init(mapper);
        return handle;
    }

    private RootHandle createArrayFromList(Stack stack, LWThread thread, String dbgname, Type type) 
            throws NoSuchFieldException {
        RootHandle handle = new RootArrayDataNode(dbgname, type, getDMChecker(stack));
        if (!(value instanceof List)) {
            throw new ExecutionException("An array variable can only be initialized with a list of values");
        }
        if (tracer.isEnabled()) {
            tracer.trace(thread, dbgname + " = " + formatList((List<?>) value));
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
        return handle;
    }
    
    private RootHandle createEmptyArray(Stack stack, LWThread thread, String dbgname, Type type) 
            throws NoSuchFieldException {
        return new RootArrayDataNode(dbgname, type, getDMChecker(stack));
    }

    private Type getType(String typename) {
        if (typename == null) {
            throw new ExecutionException("vdl:new requires a type specification for value: " + value);
        }
        else {
            try {
                return Types.getType(typename);
            }
            catch (NoSuchTypeException e) {
                throw new ExecutionException("Unknown type: " + typename, e);
            }
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
