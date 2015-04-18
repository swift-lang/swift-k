//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Mar 13, 2015
 */
package org.griphyn.vdl.engine;

import org.griphyn.vdl.type.Type;
import org.griphyn.vdl.type.impl.TypeImpl;

public class TypeParameter extends TypeImpl {
    private Type binding;
    
    public TypeParameter(String name) {
        super(name);
    }
    
    public boolean tryBindOrMatch(Type type) {
        if (this.binding != null) {
            return this.binding.equals(type);
        }
        else {
            this.binding = type;
            return true;
        }
    }
    
    public void clearBinding() {
        this.binding = null;
    }

    @Override
    public boolean canBeAssignedTo(Type type) {
        return tryBindOrMatch(type);
    }

    @Override
    public boolean isAssignableFrom(Type type) {
        return tryBindOrMatch(type);
    }
}
