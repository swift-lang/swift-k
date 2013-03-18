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
import java.util.Set;

import org.griphyn.vdl.mapping.AbsFile;
import org.griphyn.vdl.mapping.AbstractMapper;
import org.griphyn.vdl.mapping.HandleOpenException;
import org.griphyn.vdl.mapping.Mapper;
import org.griphyn.vdl.mapping.MappingParam;
import org.griphyn.vdl.mapping.MappingParamSet;
import org.griphyn.vdl.mapping.Path;
import org.griphyn.vdl.mapping.PhysicalFormat;


public class TestMapper extends AbstractMapper {
    public static final MappingParam PARAM_FILE = new MappingParam("file");
    public static final MappingParam PARAM_TEMP = new MappingParam("temp", false);
    public static final MappingParam PARAM_REMAPPABLE = new MappingParam("remappable", false);
    public static final MappingParam PARAM_STATIC = new MappingParam("static", true);
    
    @Override
    protected void getValidMappingParams(Set<String> s) {
        addParams(s, PARAM_FILE, PARAM_TEMP, PARAM_REMAPPABLE, PARAM_STATIC);
        super.getValidMappingParams(s);
    }
    
    private PhysicalFormat remap, map;
    
    private boolean remappable, temp, _static;
    private String file;

    @Override
    public void setParams(MappingParamSet params) throws HandleOpenException {
        super.setParams(params);
        remappable = PARAM_REMAPPABLE.getBooleanValue(this);
        temp = PARAM_TEMP.getBooleanValue(this);
        _static = PARAM_STATIC.getBooleanValue(this);
        file = PARAM_FILE.getStringValue(this);
    }

    @Override
    public boolean canBeRemapped(Path path) {
        return remappable;
    }

    @Override
    public void remap(Path path, Mapper sourceMapper, Path sourcePath) {
        if (remappable) {
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
        if (temp) {
            System.out.println("Cleaning file " + pf);
            FileGarbageCollector.getDefault().decreaseUsageCount(pf);
        }
        else {
            System.out.println("Not cleaning " + pf + " (not temporary)");
        }
    }

    @Override
    public boolean isPersistent(Path path) {
        return !temp;
    }

    public PhysicalFormat map(Path path) {
        if (remap == null) {
            if (map == null) {
                map = new AbsFile(file);
            }
            return map;
        }
        else {
            return remap;
        }
    }

    public Collection<Path> existing() {
        return Collections.singletonList(Path.EMPTY_PATH);
    }

    public boolean isStatic() {
        return _static;
    }
}
