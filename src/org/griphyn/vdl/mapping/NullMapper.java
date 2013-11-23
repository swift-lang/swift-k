//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Nov 18, 2013
 */
package org.griphyn.vdl.mapping;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;


public class NullMapper implements Mapper {
    public NullMapper() {
    }

    @Override
    public PhysicalFormat map(Path path) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean exists(Path path) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<Path> existing() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isStatic() {
        return false;
    }

    @Override
    public boolean canBeRemapped(Path path) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void remap(Path path, Mapper sourceMapper, Path sourcePath) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clean(Path paths) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isPersistent(Path path) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<String> getSupportedParamNames() {
        return Collections.emptySet();
    }

    @Override
    public void setParameters(Map<String, Object> params) {
    }

    @Override
    public void initialize(RootHandle root) {
    }

    @Override
    public void setBaseDir(String baseDir) {
    }

    @Override
    public AbstractDataNode getFirstOpenParameter() {
        return null;
    }
}
