/*
 * Created on Jun 30, 2006
 */
package org.griphyn.vdl.mapping;

import java.util.Collection;
import java.util.Map;

public interface Mapper {
	void setParams(Map params);
    
    String map(Path path);
    
    boolean exists(Path path);
    
    Collection existing();
    
    /**
     * Returns true if data mapped by this mapper cannot
     * change in structure at run-time. Typically this applies
     * to all mappers which work on pre determined data.
     */
    
    /* A short explanation of why this is here:
     *   Since we don't yet deal with apps returning things with
     *   undetermined sizes, yet I2U2 needs to return arrays, this
     *   tells the system that it can close top-level arrays. This
     *   allows the system to know exactly what files need to be 
     *   staged out before a job is run if the return type of the
     *   app is an array. This is not a permanent or nice solution,
     *   but a necessary compromise.
     */
    boolean isStatic();
    
    void setParam(String name, Object value);
    
    Object getParam(String name);
}
