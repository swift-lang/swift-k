//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Mar 28, 2014
 */
package org.griphyn.vdl.mapping.nodes;

import org.griphyn.vdl.mapping.DataDependentException;
import org.griphyn.vdl.mapping.DependentException;
import org.griphyn.vdl.type.Field;

public abstract class AbstractClosedNonCompositeDataNode extends AbstractClosedDataNode {
    private Object value;
    
    public AbstractClosedNonCompositeDataNode(Field field, Object value) {
        super(field);
        this.value = value;
    }
    
    public AbstractClosedNonCompositeDataNode(Field field, DependentException e) {
        super(field);
        this.value = new DataDependentException(this, e);
    }

    @Override
    public synchronized Object getValue() {
        return value;
    }
    
    @Override
    protected Object getRawValue() {
        return value;
    }
        
    public boolean isArray() {
        return false;
    }
    
    @Override
    public void closeArraySizes() {
    }
}
