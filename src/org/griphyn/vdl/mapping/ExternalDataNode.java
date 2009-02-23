package org.griphyn.vdl.mapping;

import org.griphyn.vdl.karajan.Loader;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.griphyn.vdl.type.Field;
import org.griphyn.vdl.type.NoSuchTypeException;
import org.griphyn.vdl.type.Type;
import org.griphyn.vdl.type.Types;

public class ExternalDataNode implements DSHandle {

        private Map params;

        public void init(Map params) {
                this.params = params;
	}

	static final String DATASET_URI_PREFIX = "tag:benc@ci.uchicago.edu,2008:swift:dataset:external:";

	public static final Logger logger = Logger.getLogger(ExternalDataNode.class);
	
	public static final MappingParam PARAM_PREFIX = new MappingParam("prefix", null);

	/** Datasets are identified within a run by this sequence number and the
	    partial ID field.
	    The initial value is chosen to aid human recognition of sequence
	    numbers in the wild. There is no requirement that it start at this
	    (or any other) particular value. Note that this introduces a
	    maximum on the number of datasets which can be dealt with in any
	    run to be about 2^62. */
	private static long datasetIDCounter = 720000000000l;

	/** This is used to provide a (hopefully) globally unique identifier for
	    each time the datasetIDCounter is reset (whenever this class is
	    loaded, which will usually happen once per JVM). No meaning should be
	    inferred from this value - it exists purely for making unique URIs. */
	private static final String datasetIDPartialID = Loader.getUUID();

	private Map handles;
	private Object value;
	private boolean closed;
	private List listeners;
	final String identifierURI = makeIdentifierURIString();

	public ExternalDataNode() {
	}

	public Type getType() {
		try {
			return Types.getType("external");
		} catch(NoSuchTypeException te) {
			throw new RuntimeException(te);
		}
	}

	public boolean isPrimitive() {
		return false;
	}

	public boolean isRestartable() {
		return true;
	}

	/**
	 * create a String representation of this node. If the node has a value,
	 * then uses the String representation of that value. Otherwise, generates a
	 * text description.
	 */
	public String toString() {
		String prefix = this.getClass().getName();

		prefix = prefix + " identifier "+this.getIdentifier(); 

 		prefix = prefix + " with no value at dataset=";

		prefix = prefix + getDisplayableName();

		if (!Path.EMPTY_PATH.equals(getPathFromRoot())) {
			prefix = prefix + " path="+ getPathFromRoot().toString();
		}

		if(closed) {
			prefix = prefix + " (closed)";
		}
		else {
			prefix = prefix + " (not closed)";
		}

		return prefix;
	}

	public String getIdentifyingString() {
		return toString();
	}

	public DSHandle getRoot() {
		return this;
	}

	protected String getDisplayableName() {
		String prefix = getRoot().getParam("dbgname");
		if (prefix == null) {
			prefix = getRoot().getParam("prefix");
		}
		if (prefix == null) {
			prefix = "unnamed SwiftScript value";
		}
		return prefix;
	}

	public DSHandle getField(Path path) throws InvalidPathException {
		if (path.isEmpty()) {
			return this;
		} else {
			throw new InvalidPathException(path, this);
		}
	}

	public Collection getFields(Path path) throws InvalidPathException, HandleOpenException {
		List fields = new ArrayList();
		return fields;
	}

	public void set(DSHandle handle) {
		throw new IllegalArgumentException(this.getDisplayableName() + " is an external dataset and cannot be set");
	}

	protected void setField(String name, DSHandle handle) {
		synchronized (handles) {
			handles.put(name, handle);
		}
	}

	protected DSHandle getHandle(String name) {
		synchronized (handles) {
			return (DSHandle) handles.get(name);
		}
	}

	protected boolean isHandlesEmpty() {
		synchronized (handles) {
			return handles.isEmpty();
		}
	}

	protected void checkDataException() {
		if (value instanceof DependentException) {
			throw (DependentException) value;
		}
	}

	protected void checkMappingException() {
		if (value instanceof MappingDependentException) {
			throw (MappingDependentException) value;
		}
	}

	public Object getValue() {
logger.warn("getValue called in an external dataset");
return value;
// throw new RuntimeException("cannot get value of external dataset");
	}

	public Map getArrayValue() {
throw new RuntimeException("cannot get value of external dataset");
	}

	public boolean isArray() {
		return false;
	}

	public void setValue(Object value) {
		if (this.closed) {
			throw new IllegalArgumentException(this.getDisplayableName()
					+ " is closed with a value of "+this.value);
		}
		if (this.value != null) {
			throw new IllegalArgumentException(this.getDisplayableName()
					+ " is already assigned with a value of " + this.value);
		}
		this.value = value;
	}

	public Collection getFringePaths() throws HandleOpenException {
		ArrayList list = new ArrayList();
		list.add(Path.EMPTY_PATH);
		return list;
	}

	public synchronized void closeShallow() {
		this.closed = true;
		notifyListeners();
		logger.info("closed "+this.getIdentifier());
	}

	public boolean isClosed() {
		return closed;
	}

	public void closeDeep() {
		if (!this.closed) {
			closeShallow();
		}
		synchronized (handles) {
			Iterator i = handles.entrySet().iterator();
			while (i.hasNext()) {
				Map.Entry e = (Map.Entry) i.next();
				AbstractDataNode mapper = (AbstractDataNode) e.getValue();
				mapper.closeDeep();
			}
		}
	}

	public Path getPathFromRoot() {
		return Path.EMPTY_PATH;
	}

	public Mapper getMapper() {
		return null;
	}

	protected Map getHandles() {
		return handles;
	}

	public synchronized void addListener(DSHandleListener listener) {
		if (logger.isInfoEnabled()) {
			logger.info("Adding handle listener \"" + listener + "\" to \"" + this + "\"");
		}
		if (listeners == null) {
			listeners = new LinkedList();
		}
		listeners.add(listener);
		if (closed) {
			notifyListeners();
		}
	}

	protected synchronized void notifyListeners() {
		if (listeners != null) {
			Iterator i = listeners.iterator();
			while (i.hasNext()) {
				DSHandleListener listener = (DSHandleListener) i.next();
				i.remove();
				if (logger.isInfoEnabled()) {
					logger.info("Notifying listener \"" + listener + "\" about \"" + this + "\"");
				}
				listener.handleClosed(this);
			}
			listeners = null;
		}
	}

	public String getIdentifier() {
		return identifierURI;
	}

	String makeIdentifierURIString() {
		datasetIDCounter++;
		return DATASET_URI_PREFIX + datasetIDPartialID + ":" + datasetIDCounter; 
	}

        public String getParam(String name) {
                if (params == null) {
                        return null;
                }
                return (String) params.get(name);
        }

  public DSHandle createDSHandle(String fieldName) {
throw new RuntimeException("cannot create new field in external dataset");
                }

        public DSHandle getParent() {
                return null;
        }
}
