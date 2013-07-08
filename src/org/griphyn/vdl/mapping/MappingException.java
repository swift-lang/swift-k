//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Mar 16, 2013
 */
package org.griphyn.vdl.mapping;

public class MappingException extends DependentException {
   
    public MappingException(DSHandle handle, Exception prev) {
        super(handle, prev);
    }

    public MappingException(DSHandle handle) {
        super(handle);
    }

    @Override
    public String getMessage() {
        return getVariableInfo() + " had mapping errors";
    }

}
