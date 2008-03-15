/*
 * Created on Jun 15, 2006
 */
package org.griphyn.vdl.mapping;

import java.util.Iterator;
import java.util.Map;

import org.griphyn.vdl.karajan.VDL2FutureException;
import org.griphyn.vdl.type.Field;
import org.griphyn.vdl.type.Type;

public class RootDataNode extends AbstractDataNode implements DSHandleListener {

	private Mapper mapper;
	private Map params;
	
	public static DSHandle newNode(Type type, Object value) {
		DSHandle handle = new RootDataNode(type);
		handle.setValue(value);
		handle.closeShallow();
		return handle;
	}

	public RootDataNode(Type type) {
		super(Field.Factory.newInstance());
		getField().setType(type);
	}

	public void init(Map params) {
		this.params = params;

		String desc = (String) params.get("descriptor");
		if (desc == null)
			return;
		try {
			mapper = MapperFactory.getMapper(desc, params);
			checkInputs();
			getField().setName(PARAM_PREFIX.getStringValue(mapper));
		}
		catch (InvalidMapperException e) {
			throw new RuntimeException(e);
		}
	}

	private void checkInputs() {
		try {
			checkInputs(params, mapper, this);
		}
		catch (VDL2FutureException e) {
			e.printStackTrace();
			e.getHandle().addListener(this);
		}
		catch (DependentException e) {
			setValue(new MappingDependentException(this, e));
			closeShallow();
		}
	}

	public void handleClosed(DSHandle handle) {
		checkInputs();
	}

	public static void checkInputs(Map params, Mapper mapper, AbstractDataNode root) {
		String input = (String) params.get("input");
		if (input != null && Boolean.valueOf(input.trim()).booleanValue()) {
			Iterator i = mapper.existing().iterator();
			while (i.hasNext()) {
				Path p = (Path) i.next();
				try {
					DSHandle field = root.getField(p);
					field.closeShallow();
					if (logger.isInfoEnabled()) {
						logger.info("Found data " + root + "." + p);
					}
				}
				catch (InvalidPathException e) {
					throw new IllegalStateException("mapper.existing() returned a path "+p+" that it cannot subsequently map");
				}
			}
			root.closeDeep();
			checkConsistency(root);
		}
		else if (mapper.isStatic()) {
			Iterator i = mapper.existing().iterator();
			while (i.hasNext()) {
				Path p = (Path) i.next();
				try {
					DSHandle field = root.getField(p);
					if (logger.isInfoEnabled()) {
						logger.info("Found mapped data " + root + "." + p);
					}
				}
				catch (InvalidPathException e) {
					throw new IllegalStateException("mapper.existing() returned a path "+p+" that it cannot subsequently map");
				}
			}
			if (root.isArray()) {
				root.closeShallow();
			}
			checkConsistency(root);
		}
	}

	public static void checkConsistency(DSHandle handle) {
		if (handle.getType().isArray()) {
			// any number of indices is ok
			try {
				Iterator i = handle.getFields(Path.CHILDREN).iterator();
				while (i.hasNext()) {
					DSHandle dh = (DSHandle) i.next();
					Path path = dh.getPathFromRoot();
					String index = path.getElement(path.size()-1);
					try {
						Integer.parseInt(index);
					} catch(NumberFormatException nfe) {
						throw new RuntimeException("Array element has index '"+index+"' that does not parse as an integer.");
					}
					checkConsistency(dh);
				}
			}
			catch (HandleOpenException e) {
				// TODO init() should throw some checked exception
				throw new RuntimeException("Data set initialization failed for " + handle
						+ ". It should have been closed.", e);
			}
			catch (InvalidPathException e) {
				e.printStackTrace();
			}
		}
		else {
			// all fields must be present
			Type type = handle.getType();
			Iterator i = type.getFieldNames().iterator();
			while (i.hasNext()) {
				String fieldName = (String) i.next();
				Path fieldPath = Path.parse(fieldName);
				try {
					checkConsistency(handle.getField(fieldPath));
				}
				catch (InvalidPathException e) {
					throw new RuntimeException("Data set initialization failed for " + handle
							+ ". Missing required field: " + fieldName/*
																		 * + "
																		 * mapped
																		 * to " +
																		 * handle.getFilename()
																		 */);
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

	public Mapper getMapper() {
		return mapper;
	}

	public boolean isArray() {
		return false;
	}
}
