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
 * Created on Jun 15, 2006
 */
package org.griphyn.vdl.mapping;

import k.rt.Future;
import k.rt.FutureListener;
import k.thr.LWThread;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.futures.FutureNotYetAvailable;
import org.griphyn.vdl.karajan.lib.Tracer;
import org.griphyn.vdl.type.Field;
import org.griphyn.vdl.type.Type;

public class RootDataNode extends AbstractDataNode implements FutureListener, RootHandle {

    static Logger logger = Logger.getLogger(RootDataNode.class);
    
	private boolean initialized = false;
	private Mapper mapper;
	private AbstractDataNode waitingMapperParam;
	private DuplicateMappingChecker dmc;
	
    // previously in mapper params
    private int line = -1;
    private LWThread thread;
    private boolean input;

	
	public RootDataNode(String name, Type type, DuplicateMappingChecker dmc) {
		super(Field.Factory.getImmutableField(name, type));
		this.dmc = dmc;
	}
	
	public RootDataNode(Type type, Object value) {
        this("?", type, value);
    }
	
	public RootDataNode(Type type) {
        this("?", type, null);
    }
	
	public RootDataNode(String name, Type type) {
	    this(name, type, null);
	}
	
	public RootDataNode(String name, Type type, Object value) {
	    this(name, type);
	    initialized();
	    setValue(value);
	}
	
	public int getLine() {
        return line;
    }

    public void setLine(int line) {
        this.line = line;
    }

    public boolean isInput() {
        return input;
    }

    public void setInput(boolean input) {
        this.input = input;
    }

    public void setThread(LWThread thread) {
        this.thread = thread;
    }

    public LWThread getThread() {
        return thread;
    }
	
	public String getName() {
	    return (String) getField().getId();
	}

	@Override
    public void setName(String name) {
	    getField().setId(name);
    }
	
    public void init(Mapper mapper) {
		this.mapper = mapper;
		if (mapper == null) { 
			initialized();
		} 
		else {
			innerInit();
		}
	}

	/** must have this.params set to the appropriate parameters before
	    being called. 
	 * @throws HandleOpenException */
	private synchronized void innerInit() {
	    waitingMapperParam = mapper.getFirstOpenParameter();
	    if (waitingMapperParam != null) {
            waitingMapperParam.addListener(this);
            if (variableTracer.isEnabled()) {
                variableTracer.trace(getThread(), getLine(), getDisplayableName() + " WAIT " 
                    + Tracer.getVarName(waitingMapperParam));
            }
            return;
	    }
	    
		// initialized means that this data has its mapper initialized
		// this should be called before checkInputs because the latter
		// may trigger calls to things that try to access this data node's
		// mapper
	    mapper.initialize(this);
        initialized();
		checkInputs();
		
		if (isClosed()) {
		    notifyListeners();
		}
	}

	private void checkInputs() {
		try {
			checkInputs(this, dmc);
		}
		catch (DependentException e) {
			setValue(new MappingDependentException(this, e));
		}
	}

	public void futureUpdated(Future f) {
		try {
            innerInit();
        }
        catch (Exception e) {
            this.setValue(new MappingException(this, e));
        }
	}


	static protected void checkInputs(RootHandle root, DuplicateMappingChecker dmc) {
	    Mapper mapper = root.getActualMapper();
		if (root.isInput()) {
			addExisting(mapper, root);
			checkConsistency(root, root, dmc);
		}
		else if (mapper.isStatic()) {
		    if (root.isClosed()) {
		        // this means that code that would have used this variable is already done
		        // which can happen in cases such as if(false) {a = ...}
		        return;
		    }
			// Static mappers are (array) mappers which know the size of
			// an array statically. A good example is the fixed array mapper
		    if (logger.isDebugEnabled()) {
		        logger.debug("mapper: " + mapper);
		    }
			for (Path p : mapper.existing()) {
				try {
					// Try to get the path in order to check that the 
				    // path is valid - we'll get an exception if not
					DSHandle h = root.getField(p);
					if (variableTracer.isEnabled()) {
					    variableTracer.trace(root.getThread(), root.getLine(), 
					        root.getName() + " MAPPING " + p + ", " + mapper.map(p));
					}
				}
				catch (InvalidPathException e) {
					throw new IllegalStateException
					("mapper.existing() returned a path " + 
					" that it cannot subsequently map: " + 
					" root: " + root + " path: " + p);
				}
			}
			if (root.isArray()) {
			    root.closeArraySizes();
			}
			checkConsistency(root, root, dmc);
		}
	}

	private static void addExisting(Mapper mapper, RootHandle root) {
	    boolean any = false;
		for (Path p : mapper.existing()) {
            try {
                DSHandle field = root.getField(p);
                field.setValue(FILE_VALUE);
                if (variableTracer.isEnabled()) {
                    variableTracer.trace(root.getThread(), root.getLine(), 
                        root.getName() + " MAPPING " + p + ", " + mapper.map(p));
                }
                any = true;
            }
            catch (InvalidPathException e) {
                throw new IllegalStateException("Structure of mapped data is " +
                        "incompatible with the mapped variable type: " + e.getMessage());
            }
            catch (NotCompositeException e) {
                throw new IllegalStateException("Cannot map multiple files to variable '" + 
                    e.getDataNode().getFullName() + "' of non composite type '" + 
                    e.getDataNode().getType() + "'");
            }
        }
        root.closeDeep();
        if (!any && variableTracer.isEnabled()) {
            variableTracer.trace(root.getThread(), root.getLine(), 
                root.getName() + " MAPPING no files found");
        }
    }

    public static void checkConsistency(RootHandle root, DSHandle handle, DuplicateMappingChecker dmc) {
		if (handle.getType().isArray()) {
			// any number of indices is ok
			try {
			    for (DSHandle dh : handle.getFields(Path.CHILDREN)) {
					checkConsistency(root, dh, dmc);
				}
			}
			catch (HandleOpenException e) {
				// TODO init() should throw some checked exception
				throw new RuntimeException("Mapper consistency check failed for " + handle
						+ ". A HandleOpenException was thrown during consistency checking for "+e.getSource(), e);
			}
			catch (InvalidPathException e) {
				e.printStackTrace();
			}
		}
		else {
			// all fields must be present
			Type type = handle.getType();
			if (!type.isPrimitive() && !type.isComposite()) {
			    // mapped. Feed the DMC.
			    PhysicalFormat f = root.getActualMapper().map(handle.getPathFromRoot());
			    if (root.isInput()) {
			        dmc.addRead(f, handle);
			    }
			    else {
			        dmc.addWrite(f, handle);
			    }
			}
			for (String fieldName : type.getFieldNames()) {
				Path fieldPath = Path.parse(fieldName);
				try {
					checkConsistency(root, handle.getField(fieldPath), dmc);
				}
				catch (InvalidPathException e) {
					throw new RuntimeException("Data set initialization failed for " + handle
							+ ". Missing required field: " + fieldName);
				}
			}
		}
	}
	
	public RootHandle getRoot() {
		return this;
	}

	public DSHandle getParent() {
		return null;
	}

	@Override
    protected AbstractDataNode getParentNode() {
        return null;
    }

    public synchronized Mapper getMapper() {
		if (initialized) {
			return mapper;
		}
		if (waitingMapperParam == null) {
		    return null;
		}
		else {
		    throw new FutureNotYetAvailable(waitingMapperParam);
		}
	}
	
	public Mapper getActualMapper() {
        return mapper;
    }

	public boolean isArray() {
		return false;
	}

	public void setValue(Object value) {
		super.setValue(value);
		initialized();
	}

	private synchronized void initialized() {
		initialized = true;
		waitingMapperParam = null;
		if (variableTracer.isEnabled()) {
            variableTracer.trace(getThread(), getLine(), getName() + " INITIALIZED " + mapper);
        }
	}
}
