package org.griphyn.vdl.mapping;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.griphyn.vdl.karajan.Loader;
import org.griphyn.vdl.type.Types;
import org.griphyn.vdl.type.impl.FieldImpl;

public class ExternalDataNode extends AbstractDataNode {

	static final String DATASET_URI_PREFIX = "dataset:external:";

	public static final Logger logger = Logger.getLogger(ExternalDataNode.class);
	
	public static final MappingParam PARAM_PREFIX = new MappingParam("prefix", null);

	private static long datasetIDCounter = 850000000000l;

	private static final String datasetIDPartialID = Loader.getUUID();
	
	private Map<String, Object> params;

	public ExternalDataNode() {
	    super(new FieldImpl("", Types.EXTERNAL));
	}

	@Override
    public void init(Map<String, Object> params) {
        this.params = params;
    }

    public boolean isRestartable() {
		return true;
	}

	public DSHandle getRoot() {
		return this;
	}

	public DSHandle getField(Path path) throws InvalidPathException {
		if (path.isEmpty()) {
			return this;
		} 
		else {
			throw new InvalidPathException(path, this);
		}
	}
	
	protected void getFields(List<DSHandle> fields, Path path) throws InvalidPathException {
	    // nothing
	}

	public void set(DSHandle handle) {
		throw new UnsupportedOperationException(this.getDisplayableName() + " is an external dataset and cannot be set");
	}

	public Object getValue() {
	    logger.warn("getValue called on an external dataset");
	    return null;
	}

	public Map<Comparable<?>, DSHandle> getArrayValue() {
	    throw new UnsupportedOperationException("cannot get value of external dataset");
	}

	public boolean isArray() {
		return false;
	}

	public Collection<Path> getFringePaths() throws HandleOpenException {
	    return Collections.singletonList(Path.EMPTY_PATH);
	}

	public Path getPathFromRoot() {
		return Path.EMPTY_PATH;
	}

	public Mapper getMapper() {
		return null;
	}

	protected String makeIdentifierURIString() {
		datasetIDCounter++;
		return DATASET_URI_PREFIX + datasetIDPartialID + ":" + datasetIDCounter; 
	}

	public DSHandle createDSHandle(String fieldName) {
	    throw new UnsupportedOperationException("cannot create new field in external dataset");
	}

	public DSHandle getParent() {
	    return null;
	}

    public String getParam(String name) {
        if (params == null) {
            return null;
        }
        return (String) params.get(name);
    }
}
