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
    boolean isStatic();
}
