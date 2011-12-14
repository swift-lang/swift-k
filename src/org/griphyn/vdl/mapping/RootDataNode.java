/*
 * Created on Jun 15, 2006
 */
package org.griphyn.vdl.mapping;

import java.util.Map;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.workflow.futures.Future;
import org.globus.cog.karajan.workflow.futures.FutureListener;
import org.globus.cog.karajan.workflow.futures.FutureNotYetAvailable;
import org.griphyn.vdl.type.Field;
import org.griphyn.vdl.type.Type;

public class RootDataNode extends AbstractDataNode implements FutureListener {

    static Logger logger = Logger.getLogger(RootDataNode.class); 
    
	private boolean initialized=false;
	private Mapper mapper;
	private Map<String, Object> params;
	private AbstractDataNode waitingMapperParam;

	public static DSHandle newNode(Type type, Object value) {
		DSHandle handle = new RootDataNode(type);
		handle.setValue(value);
		handle.closeShallow();
		logger.debug("newNode");
		return handle;
	}

	public RootDataNode(Type type) {
		super(Field.Factory.createField(null, type));
	}

	public void init(Map<String,Object> params) {
		this.params = params;
		if(this.params == null) { 
			initialized();
		} else {
			innerInit();
		}
	}

	/** must have this.params set to the appropriate parameters before
	    being called. */
	private synchronized void innerInit() {
	    for (Object v : params.values()) {
			if(v instanceof AbstractDataNode && !((AbstractDataNode) v).isClosed()) {
			    if (logger.isDebugEnabled()) {
			        logger.debug("addListener: " + this + " " + v);
			    }
			    waitingMapperParam = (AbstractDataNode) v;
                waitingMapperParam.getFutureWrapper().addModificationAction(this, null);
				return;
			}
		}
		String desc = (String) params.get("descriptor");
		if (desc == null) {
			initialized();
			return;
		}
		try {
			mapper = MapperFactory.getMapper(desc, params);
			getField().setName(PARAM_PREFIX.getStringValue(mapper));
			// initialized means that this data has its mapper initialized
			// this should be called before checkInputs because the latter
			// may trigger calls to things that try to access this data node's
			// mapper
            initialized();
			checkInputs();
		}
		catch (InvalidMapperException e) {
			throw new RuntimeException
			("InvalidMapperException caught in mapper initialization", e);
		}
		if (isClosed()) {
		    notifyListeners();
		}
	}

	private void checkInputs() {
		try {
			checkInputs(params, mapper, this);
		}
		catch (DependentException e) {
			setValue(new MappingDependentException(this, e));
			closeShallow();
			return;
		}
	}

	public void futureModified(Future f, VariableStack stack) {
		innerInit();
	}


	static protected void checkInputs(Map<String, Object> params, Mapper mapper, AbstractDataNode root) {
		String input = (String) params.get("input");
		if (input != null && Boolean.valueOf(input.trim()).booleanValue()) {
		    for (Path p : mapper.existing()) {
				try {
					DSHandle field = root.getField(p);
					field.closeShallow();
					if (logger.isInfoEnabled()) {
						logger.info("Found data " + root + "." + p);
					}
				}
				catch (InvalidPathException e) {
				    throw new IllegalStateException("Structure of mapped data is " +
				    		"incompatible with the mapped variable type: " + e.getMessage());
				}
			}
			root.closeDeep();
			checkConsistency(root);
		}
		else if (mapper.isStatic()) {
		    if (root.isClosed()) {
		        // this means that code that would have used this variable is already done
		        // which can happen in cases such as if(false) {a = ...}
		        return;
		    }
			// Static mappers are (array) mappers which know the size of
			// an array statically. A good example is the fixed array mapper
		    logger.debug("mapper: " + mapper);
			for (Path p : mapper.existing()) {
				try {
					// Try to get the path in order to check that the 
				    // path is valid - we'll get an exception if not
					root.getField(p);
					if (logger.isInfoEnabled()) {
						logger.info("Found mapped data " + root + "." + p);
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
			checkConsistency(root);
		}
	}

	public static void checkConsistency(DSHandle handle) {
		if (handle.getType().isArray()) {
			// any number of indices is ok
			try {
			    for (DSHandle dh : handle.getFields(Path.CHILDREN)) {
					Path path = dh.getPathFromRoot();
					String index = path.getElement(path.size() - 1);
					try {
						Integer.parseInt(index);
					}
					catch (NumberFormatException nfe) {
						throw new RuntimeException("Array element has index '" + index
								+ "' that does not parse as an integer.");
					}
					checkConsistency(dh);
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
			for (String fieldName : type.getFieldNames()) {
				Path fieldPath = Path.parse(fieldName);
				try {
					checkConsistency(handle.getField(fieldPath));
				}
				catch (InvalidPathException e) {
					throw new RuntimeException("Data set initialization failed for " + handle
							+ ". Missing required field: " + fieldName);
				}
			}

		}
	}

	public String getParam(String name) {
		if (params == null) {
			return null;
		}
		return (String) params.get(name);
	}

	public DSHandle getRoot() {
		return this;
	}

	public DSHandle getParent() {
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
		    throw new FutureNotYetAvailable(waitingMapperParam.getFutureWrapper());
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
	}    
}
