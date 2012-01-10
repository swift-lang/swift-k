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


package org.griphyn.vdl.mapping.file;

import java.util.Collection;
import java.util.Collections;

import org.griphyn.vdl.mapping.AbsFile;
import org.griphyn.vdl.mapping.AbstractMapper;
import org.griphyn.vdl.mapping.Mapper;
import org.griphyn.vdl.mapping.MappingParam;
import org.griphyn.vdl.mapping.Path;
import org.griphyn.vdl.mapping.PhysicalFormat;


public class TestMapper extends AbstractMapper {
    public static final MappingParam PARAM_FILE = new MappingParam("file");
    public static final MappingParam PARAM_TEMP = new MappingParam("temp", false);
    public static final MappingParam PARAM_REMAPPABLE = new MappingParam("remappable", false);
    public static final MappingParam PARAM_STATIC = new MappingParam("static", true);
    
    private PhysicalFormat remap, map;

    @Override
    public boolean canBeRemapped(Path path) {
        return PARAM_REMAPPABLE.getBooleanValue(this);
    }

    @Override
    public void remap(Path path, Mapper sourceMapper, Path sourcePath) {
        if (PARAM_REMAPPABLE.getBooleanValue(this)) {
            remap = sourceMapper.map(sourcePath);
            System.out.println("Remapping " + path + " -> " + remap);
            ensureCollectionConsistency(sourceMapper, sourcePath);
        }
        else {
            throw new UnsupportedOperationException("remap");
        }
    }

    @Override
    public void clean(Path path) {
        PhysicalFormat pf = map(path);
        if (PARAM_TEMP.getBooleanValue(this)) {
            System.out.println("Cleaning file " + pf);
            FileGarbageCollector.getDefault().decreaseUsageCount(pf);
        }
        else {
            System.out.println("Not cleaning " + pf + " (not temporary)");
        }
    }

    @Override
    public boolean isPersistent(Path path) {
        return !PARAM_TEMP.getBooleanValue(this);
    }

    @Override
    public PhysicalFormat map(Path path) {
        if (remap == null) {
            if (map == null) {
                map = new AbsFile(PARAM_FILE.getStringValue(this));
            }
            return map;
        }
        else {
            return remap;
        }
    }

    @Override
    public Collection<Path> existing() {
        return Collections.singletonList(Path.EMPTY_PATH);
    }

    @Override
    public boolean isStatic() {
        return PARAM_STATIC.getBooleanValue(this);
    }
}
