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
import java.util.Set;

import org.griphyn.vdl.mapping.nodes.AbstractDataNode;
import org.griphyn.vdl.type.Type;


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
        return Collections.emptyList();
    }

    
    @Override
    public Collection<Path> existing(FileSystemLister l) {
        return null;
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
    public void setParameters(GenericMappingParamSet params) {
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

    @Override
    public Collection<AbsFile> getPattern(Path path, Type type) {
        return null;
    }

    @Override
    public String toString() {
        return "<>";
    }
}
