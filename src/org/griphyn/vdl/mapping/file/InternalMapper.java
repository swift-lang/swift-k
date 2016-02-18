/*
 * Copyright 2012 University of Chicago
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * Created on Jun 7, 2015
 */
package org.griphyn.vdl.mapping.file;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.griphyn.vdl.mapping.InvalidPathException;
import org.griphyn.vdl.mapping.Mapper;
import org.griphyn.vdl.mapping.Path;
import org.griphyn.vdl.mapping.PhysicalFormat;

public class InternalMapper extends ConcurrentMapper {
    private static class RemapEntry {
        public final PhysicalFormat pf;
        public final boolean persistent;
        
        public RemapEntry(PhysicalFormat pf, boolean persistent) {
            this.pf = pf;
            this.persistent = persistent;
        }
    }
    
    private Map<Path, RemapEntry> remappedPaths;
    
    @Override
    public synchronized Collection<Path> existing() {        
        Collection<Path> c = super.existing();
        if (remappedPaths != null) {
            Set<Path> s = new HashSet<Path>(c);
            s.addAll(remappedPaths.keySet());
            return s;
        }
        else {
            return c;
        }
    }
    
    @Override
    public PhysicalFormat map(Path path) {
        synchronized(this) {
            if (remappedPaths != null) {
                RemapEntry o = remappedPaths.get(path);
                if (o != null) {
                    return o.pf;
                }
            }
        }
        return super.map(path);
    }
    
    
    @Override
    public void remap(Path path, Mapper sourceMapper, Path sourcePath) throws InvalidPathException {
        PhysicalFormat pf = sourceMapper.map(sourcePath);
        boolean persistent = sourceMapper.isPersistent(sourcePath);
        synchronized(this) {
            if (remappedPaths == null) {
                remappedPaths = new HashMap<Path, RemapEntry>();
            }
            remappedPaths.put(path, new RemapEntry(pf, persistent));
        }
    }

    @Override
    public boolean isPersistent(Path path) {
        synchronized(this) {
            if (remappedPaths != null) {
                RemapEntry e = remappedPaths.get(path);
                if (e != null) {
                    return e.persistent;
                }
            }
        }
        return false;
    }
    
    @Override
    public boolean supportsCleaning() {
        return true;
    }

    @Override
    public boolean canBeRemapped(Path path) {
        return true;
    }
}
