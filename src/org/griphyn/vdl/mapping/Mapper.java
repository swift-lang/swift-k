package org.griphyn.vdl.mapping;

import java.util.Collection;
import java.util.Map;

/** This interface must be implemented by a Java class that represents
    a Swift mapper between SwiftScript variables and external data
    sources. */

public interface Mapper {

    /** Returns a (?)filename for the specified SwiftScript path. */
    String map(Path path);

    /** Returns true if a (file?) backing the specified SwiftScript path
        already exists. */
    boolean exists(Path path);

    /** Returns a Collection of existing mappings, where each Collection
        entry is a Path object. The data files mapped to each Path object
        do not necessarily have to exist.
     */
    Collection existing();

    /**
     * Returns true if data mapped by this mapper cannot
     * change in structure at run-time. Typically this applies
     * to all mappers which work on pre determined data.
     * <br />
     * A short explanation of why this is here:
     *   Since we don't yet deal with apps returning things with
     *   undetermined sizes, yet I2U2 needs to return arrays, this
     *   tells the system that it can close top-level arrays. This
     *   allows the system to know exactly what files need to be 
     *   staged out before a job is run if the return type of the
     *   app is an array. This is not a permanent or nice solution,
     *   but a necessary compromise.
     */
    boolean isStatic();

    void setParams(Map params);

    void setParam(String name, Object value);

    Object getParam(String name);

}
