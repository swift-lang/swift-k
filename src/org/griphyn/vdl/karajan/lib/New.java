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

import java.util.Collections;
import java.util.List;

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
import org.griphyn.vdl.mapping.DSHandle;
import org.griphyn.vdl.mapping.DuplicateMappingChecker;
import org.griphyn.vdl.mapping.GenericMappingParamSet;
import org.griphyn.vdl.mapping.InvalidMapperException;
import org.griphyn.vdl.mapping.Mapper;
import org.griphyn.vdl.mapping.MapperFactory;
import org.griphyn.vdl.mapping.NullMapper;
import org.griphyn.vdl.mapping.RootHandle;
import org.griphyn.vdl.mapping.nodes.AbstractDataNode;
import org.griphyn.vdl.mapping.nodes.ExternalDataNode;
import org.griphyn.vdl.mapping.nodes.NodeFactory;
import org.griphyn.vdl.mapping.nodes.RootClosedArrayDataNode;
import org.griphyn.vdl.mapping.nodes.RootClosedPrimitiveDataNode;
import org.griphyn.vdl.mapping.nodes.RootFutureArrayDataNode;
import org.griphyn.vdl.mapping.nodes.RootFuturePrimitiveDataNode;
import org.griphyn.vdl.type.Field;
import org.griphyn.vdl.type.NoSuchTypeException;
import org.griphyn.vdl.type.Type;
import org.griphyn.vdl.type.Types;

public class New extends SwiftFunction {
	public static final Logger logger = Logger.getLogger(New.class);
	
	private static final Mapper NULL_MAPPER = new NullMapper();
	private static final LWThread STATIC_THREAD = new LWThread("STATIC", null, null);
		
	private ArgRef<Field> field;
	private ArgRef<GenericMappingParamSet> mapping;
	private ArgRef<Object> value;
	private ArgRef<Number> waitCount;
	private ArgRef<Boolean> input;
	private ArgRef<Integer> _defline;
	
	private VarRef<Context> context;
	private VarRef<String> cwd; 
	
	@Override
	protected Signature getSignature() {
	    return new Signature(params("field", optional("mapping", null), optional("value", null), 
	        optional("waitCount", null), optional("input", Boolean.FALSE), optional("_defline", null)));
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
    protected Node compileBody(WrapperNode w, Scope argScope, Scope scope)
            throws CompilationException {
        DSHandle h = null;
        RootHandle r = null;
        int waitCount = 0;
        if (this.waitCount.getValue() != null) {
            waitCount = this.waitCount.getValue().intValue();
        }
        if (field.isStatic() && value.isStatic() && mapping.isStatic() && mapping.getValue() == null) {
            Field f = field.getValue();
            Type t = f.getType();
            Object v = value.getValue();
            if (v != null) {
                if (t.isPrimitive() && !Types.EXTERNAL.equals(t)) {
                    r = new RootClosedPrimitiveDataNode(f, internalValue(t, v)); 
                }
            }
            else {
                if (getParent().getType().equals("k:assign") && getParent().getParent().getType().equals("k:main")) {
                    // in the main scope, so it can be initialized at compile-time
                    if (t.isArray()) {
                    	if (input.getValue()) {
                    		r = new RootClosedArrayDataNode(f, Collections.emptyList(), null);
                    	}
                    	else {
                    	    r = new RootFutureArrayDataNode(f, null);
                    	}
                    }
                    else if (t.isPrimitive()) {
                        r = new RootFuturePrimitiveDataNode(f);
                    }
                    // TODO can add other types
                }
            }
        }
        if (r != null) {
            h = initHandle(r, NULL_MAPPER, STATIC_THREAD, _defline.getValue(), false);
            h.setWriteRefCount(waitCount);
        }
        if (h != null && staticReturn(scope, h)) {
            return null;
        }
        else {
            return super.compileBody(w, argScope, scope);
        }
    }

    @Override
    public Object function(Stack stack) {
		Field field = this.field.getValue(stack);
		Object value = this.value.getValue(stack);
        GenericMappingParamSet mapping = this.mapping.getValue(stack);
		Number waitCount = this.waitCount.getValue(stack);
		boolean input = this.input.getValue(stack);
		Integer line = this._defline.getValue(stack);
		
		if (field == null && value == null) {
            throw new ExecutionException("You must specify either a field or a value");
        }
				
		Mapper mapper;
		Type type = field.getType();
		String dbgname = (String) field.getId();
		
		if (type.hasMappedComponents()) {
		    try {
                mapper = MapperFactory.getMapper(mapping.getDescriptor());
            }
            catch (InvalidMapperException e) {
                throw new ExecutionException(this, "Invalid mapper '" + mapping.getDescriptor() + "'");
            }
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
			if (Types.EXTERNAL.equals(type)) {
			    if (tracer.isEnabled()) {
			        tracer.trace(thread, dbgname + " = external");
			    }
				handle = initHandle(new ExternalDataNode(field), mapper, thread, line, input);
			}
			else if (type.isArray()) {
				// dealing with array variable
				if (value != null) {
					if (value instanceof RootHandle) {
					    handle = (RootHandle) value;
					    if (tracer.isEnabled()) {
					        tracer.trace(thread, dbgname + " = " + Tracer.getVarName(handle));
					    }
						handle.closeShallow();
					}
					else {
					    handle = initHandle(createArrayFromList(stack, thread, field, value), 
					        mapper, thread, line, input); 		    
					}
				}
				else if (input && !type.hasMappedComponents()) {
				    handle = initHandle(createArrayFromList(stack, thread, field, Collections.emptyList()), 
                            mapper, thread, line, input);
				}
				else {			    
				    if (tracer.isEnabled()) {
				        tracer.trace(thread, dbgname);
                    }
				    handle = initHandle(createEmptyArray(stack, thread, field), 
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
			    if (value == null) {
			        handle = initHandle(NodeFactory.newOpenRoot(field, getDMChecker(stack)), 
			            mapper, thread, line, input);
			        if (tracer.isEnabled()) {
                        tracer.trace(thread, dbgname + " " + mapper);
                    }
			    }
			    else {
			        if (type.isPrimitive()) {
                        handle = initHandle(new RootClosedPrimitiveDataNode(field, internalValue(type, value)), 
                            mapper, thread, line, input);
                    }
                    else {
                        throw new ExecutionException("Cannot create non-primitive data node with value '" + value + "'");
                    }
			        if (tracer.isEnabled()) {
                        tracer.trace(thread, dbgname + " = " + value);
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
        if (mapper != NULL_MAPPER) {
            handle.setInput(input);
        }
        handle.init(mapper);
        return handle;
    }

    private RootHandle createArrayFromList(Stack stack, LWThread thread, Field field, Object value) 
            throws NoSuchFieldException {
        if (!(value instanceof List)) {
            throw new ExecutionException("An array variable can only be initialized with a list of values");
        }
        if (tracer.isEnabled()) {
            tracer.trace(thread, field.getId() + " = " + formatList((List<?>) value));
        }
        return new RootClosedArrayDataNode(field, (List<?>) value, getDMChecker(stack));
    }
    
    private RootHandle createEmptyArray(Stack stack, LWThread thread, Field field) 
            throws NoSuchFieldException {
        return new RootFutureArrayDataNode(field, getDMChecker(stack));
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
