/*
 * Created on Jun 15, 2006
 */
package org.griphyn.vdl.mapping;

import java.util.Iterator;
import java.util.Map;

import org.griphyn.vdl.type.Field;
import org.griphyn.vdl.type.NoSuchTypeException;
import org.griphyn.vdl.type.Type;
import org.griphyn.vdl.type.TypeDefinitions;

public class RootDataNode extends AbstractDataNode {
	private Mapper mapper;
	private Map params;

	public RootDataNode(String type) throws NoSuchTypeException {
		super(Field.Factory.newInstance());
		getField().setType(TypeDefinitions.getType(type));
	}

	public void init(Map params) {
		this.params = params;
		getField().setName((String) params.get("prefix"));
		String desc = (String) params.get("descriptor");
		if (desc == null)
			return;
		try {
			mapper = MapperFactory.getMapper(desc, params);
			checkInputs(params, mapper, this);
		}
		catch (InvalidMapperException e) {
			throw new IllegalArgumentException(e.getMessage());
		}
	}

	public static void checkInputs(Map params, Mapper mapper, DSHandle root) {
		if (Boolean.valueOf((String) params.get("input")).booleanValue()) {
			Iterator i = mapper.existing().iterator();
			while (i.hasNext()) {
				Path p = (Path) i.next();
				try {
					DSHandle field = root.getField(p);
					field.setValue(Boolean.TRUE);
					field.closeShallow();
					System.out.println("Found data " + root + "." + p);
				}
				catch (InvalidPathException e) {
					// it's ok.
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
					System.out.println("Found mapped data " + root + "." + p);
				}
				catch (InvalidPathException e) {
					// it's ok.
				}
			}
			root.closeShallow();
			checkConsistency(root);
		}
	}

	protected void checkConsistency() {
		checkConsistency(this);
	}

	public static void checkConsistency(DSHandle handle) {
		if (handle.isArray()) {
			// any number of indices is ok
			try {
				Iterator i = handle.getFields(Path.CHILDREN).iterator();
				while (i.hasNext()) {
					checkConsistency((DSHandle) i.next());
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
			try {
				Type type = TypeDefinitions.getType(handle.getType());
				Iterator i = type.getFieldNames().iterator();
				while (i.hasNext()) {
					String fieldName = (String) i.next();
					Path fieldPath = Path.parse(fieldName);
					try {
						checkConsistency(handle.getField(fieldPath));
					}
					catch (InvalidPathException e) {
						throw new RuntimeException("Data set initialization failed for " + handle
								+ ". Missing required field: " + fieldName + " mapped to "
								+ handle.getFilename());
					}
				}
			}
			catch (NoSuchTypeException e) {
				e.printStackTrace();
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
