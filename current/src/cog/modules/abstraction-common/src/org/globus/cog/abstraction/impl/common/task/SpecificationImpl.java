// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------



package org.globus.cog.abstraction.impl.common.task;

import org.globus.cog.abstraction.interfaces.Specification;

public class SpecificationImpl implements Specification, Cloneable {

    private static final long serialVersionUID = 1L;
    private int type;
    private String specification;

    public SpecificationImpl(int type) {
        this.type = type;
    }

    public SpecificationImpl(int type, String specification) {
        this.type = type;
        this.specification = specification;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getType() {
        return this.type;
    }

    public void setSpecification(String specification) {
        this.specification = specification;
    }

    public String getSpecification() {
        return this.specification;
    }
    
    public Object clone() {
        SpecificationImpl result = null;
        try {
            result = (SpecificationImpl) super.clone();
        }
        catch (CloneNotSupportedException e) {
            e.printStackTrace();
        } 
        return result;
    }
}
