//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Nov 18, 2013
 */
package org.griphyn.vdl.type.impl;

import org.griphyn.vdl.type.Type;

public class ImmutableField extends FieldImpl {

    public ImmutableField(Comparable<?> id, Type type) {
        super(id, type);
    }

    @Override
    public void setId(Comparable<?> id) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setType(Type type) {
        throw new UnsupportedOperationException();
    }
}
