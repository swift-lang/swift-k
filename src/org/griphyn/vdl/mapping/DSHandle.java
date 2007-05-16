package org.griphyn.vdl.mapping;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface DSHandle extends Serializable {

    /** get the type of the dataset, need to replace return type
     * with a Type interface.
     */
    public String getType();

    public void init(Map params);

    public DSHandle getRoot();

    public DSHandle getParent();

    /** returns the field below this node that is referred to by the
     *  supplied path. The path must have no wildcards.
     */
    public DSHandle getField(Path path) throws InvalidPathException;

    /** returns a collection of fields below this node that are referred to
     *  by the supplied path. The path may contain wildcards. If it does not,
     *  then the returned collection should contain a single member, which is
     *  the same as would be returned by getField().
     *
     *  @return a Collection of DSHandle objects
     */
    public Collection getFields(Path path) throws InvalidPathException, HandleOpenException;

    public Object getValue();

    public void setValue(Object value) throws InvalidPathException;

    /** create a new logical component */
    public DSHandle createDSHandle(String fieldName) throws NoSuchFieldException;

    /** write to the data source */
    public void commit();

    // special file oriented methods, not sure if these apply to 
    // all datasets

    /** get the filename of a specific field */
    public String getFilename();

    /** get all the leaf file names for a sub-component */
    public List getFileSet();

    /** close */
    public void closeShallow();

    public void closeDeep();

    public Collection getFringePaths() throws HandleOpenException;

    public Map getArrayValue();

    public Path getPathFromRoot();

    public void set(DSHandle svar);

    public String getParam(String name);

    public boolean isArray();

    public boolean isClosed();

    public void addListener(DSHandleListener listener);
}

