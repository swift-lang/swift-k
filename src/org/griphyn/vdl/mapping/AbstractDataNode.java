/*
 * Created on Jun 6, 2006
 */
package org.griphyn.vdl.mapping;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.griphyn.vdl.type.Field;
import org.griphyn.vdl.type.Type;
import org.griphyn.vdl.type.Types;

public abstract class AbstractDataNode implements DSHandle {
	public static final Logger logger = Logger.getLogger(AbstractDataNode.class);

	private Field field;
	private Map handles;
	private Object value;
	private boolean closed;
	private List listeners;

	protected AbstractDataNode(Field field) {
		this.field = field;
		handles = new HashMap();
	}

	public void init(Map params) {

	}

	public Type getType() {
		return field.getType();
	}

	public boolean isPrimitive() {
		return field.getType().isPrimitive();
	}

	protected Field getField() {
		return field;
	}

	/**
	 * create a String representation of this node. If the node has a value,
	 * then uses the String representation of that value. Otherwise, generates a
	 * text description.
	 */
	public String toString() {
		if (this.value != null && !(this.value instanceof Exception)) {
			// special handling for ints...
			if (this.getType().equals(Types.INT)) {
				try {
					Number n = (Number) this.getValue();
					return String.valueOf(n.intValue());
				}
				catch (ClassCastException e) {
					throw new RuntimeException("Internal type error. Value is not a Number for "
							+ getDisplayableName() + getPathFromRoot());
				}
			}
			else {
				return this.value.toString();
			}
		}

		String prefix = this.getClass().getName() + " with no value at ";
		prefix = prefix + getDisplayableName();

		if (!Path.EMPTY_PATH.equals(getPathFromRoot())) {
			prefix = prefix + getPathFromRoot().toString();
		}

		return prefix;
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
		}
		try {
			DSHandle handle = getField(path.getFirst());
			if (path.size() > 1) {
				return handle.getField(path.butFirst());
			}
			else {
				return handle;
			}
		}
		catch (NoSuchFieldException e) {
			throw new InvalidPathException(path, this);
		}
	}

	public Collection getFields(Path path) throws InvalidPathException, HandleOpenException {
		List fields = new ArrayList();
		getFields(fields, path);
		return fields;
	}

	private void getFields(List fields, Path path) throws InvalidPathException, HandleOpenException {
		if (path.isEmpty()) {
			fields.add(this);
		}
		else {
			Path rest = path.butFirst();
			if (path.isWildcard(0)) {
				if (isArray() && !closed) {
					throw new HandleOpenException(this);
				}
				Iterator i = this.handles.entrySet().iterator();
				while (i.hasNext()) {
					Map.Entry e = (Map.Entry) i.next();
					((AbstractDataNode) e.getValue()).getFields(fields, rest);
				}
			}
			else {
				try {
					((AbstractDataNode) getField(path.getFirst())).getFields(fields, rest);
				}
				catch (NoSuchFieldException e) {
					throw new InvalidPathException(path, this);
				}
			}
		}
	}

	public void set(DSHandle handle) {
		// TODO check type
		if (closed) {
			throw new IllegalArgumentException(this.getDisplayableName() + " is already assigned");
		}
		if (getParent() == null) {
			/*
			 * AbstractDataNode node = (AbstractDataNode)handle; field =
			 * node.getField(); handles = node.getHandles(); closed =
			 * node.isClosed(); value = node.getValue();
			 */
			throw new RuntimeException("Can't set root data node!");
		}
		else {
			((AbstractDataNode) getParent()).setField(field.getName(), handle);
		}
	}

	protected void setField(String name, DSHandle handle) {
		synchronized (handles) {
			handles.put(name, handle);
		}
	}

	protected synchronized DSHandle getField(String name) throws NoSuchFieldException {
		DSHandle handle = getHandle(name);
		if (handle == null) {
			if (closed) {
				throw new NoSuchFieldException(name);
			}
			else {
				handle = createDSHandle(name);
			}

		}
		return handle;
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

	public DSHandle createDSHandle(String fieldName) throws NoSuchFieldException {
		if (closed) {
			throw new RuntimeException("Cannot write to closed handle: " + this + " (" + fieldName
					+ ")");
		}

		AbstractDataNode child;
		Field childField = getChildField(fieldName);
		if (childField.getType().isArray()) {
			child = new ArrayDataNode(getChildField(fieldName), getRoot(), this);
		}
		else {
			child = new DataNode(getChildField(fieldName), getRoot(), this);
		}

		synchronized (handles) {
			Object o = handles.put(fieldName, child);
			if (o != null) {
				throw new RuntimeException("Trying to create a handle that already exists ("
						+ fieldName + ") in " + this);
			}
		}
		return child;
	}

	protected Field getChildField(String fieldName) throws NoSuchFieldException {
		return Field.Factory.createField(fieldName, field.getType().getField(fieldName).getType());
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
		checkDataException();
		if (field.getType().isArray()) {
			return handles;
		}
		else {
			return value;
		}
	}

	public Map getArrayValue() {
		checkDataException();
		if (!field.getType().isArray()) {
			throw new RuntimeException("getArrayValue called on a struct: " + this);
		}
		return handles;
	}

	public boolean isArray() {
		return false;
	}

	public void setValue(Object value) {
		if (this.value != null) {
			throw new IllegalArgumentException(this.getDisplayableName()
					+ " is already assigned with a value of " + this.value);
		}
		this.value = value;
	}

	public Collection getFringePaths() throws HandleOpenException {
		ArrayList list = new ArrayList();
		getFringePaths(list, Path.EMPTY_PATH);
		return list;
	}

	public void getFringePaths(List list, Path parentPath) throws HandleOpenException {
		checkMappingException();
		if (getField().getType().getBaseType() != null) {
			list.add(Path.EMPTY_PATH.toString());
		}
		else {
			Iterator i = getField().getType().getFields().iterator();
			while (i.hasNext()) {
				Field field = (Field) i.next();
				AbstractDataNode mapper;
				try {
					mapper = (AbstractDataNode) this.getField(field.getName());
				}
				catch (NoSuchFieldException e) {
					throw new RuntimeException(
							"Inconsistency between type declaration and handle for field '"
									+ field.getName() + "'");
				}
				Path fullPath = parentPath.addLast(mapper.getField().getName());
				if (!mapper.field.getType().isPrimitive() && !mapper.isArray()) {
					list.add(fullPath.toString());
				}
				else {
					mapper.getFringePaths(list, fullPath);
				}
			}
		}
	}

	public synchronized void closeShallow() {
		this.closed = true;
		notifyListeners();
	}

	public boolean isClosed() {
		return closed;
	}

	public void closeDeep() {
		if (!this.closed) {
			setValue(Boolean.TRUE);
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
		AbstractDataNode parent = (AbstractDataNode) this.getParent();
		Path myPath;
		if (parent != null) {
			myPath = parent.getPathFromRoot();
			return myPath.addLast(getField().getName(), parent.getField().getType().isArray());
		}
		else {
			return Path.EMPTY_PATH;
		}
	}

	public Mapper getMapper() {
		return ((AbstractDataNode) getRoot()).getMapper();
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
}
