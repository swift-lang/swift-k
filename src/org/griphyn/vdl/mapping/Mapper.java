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


package org.griphyn.vdl.mapping;

import java.util.Collection;
import java.util.Set;

import org.griphyn.vdl.mapping.nodes.AbstractDataNode;
import org.griphyn.vdl.type.Type;

/** This interface must be implemented by a Java class that represents
    a Swift mapper between SwiftScript variables and external data
    sources. */

public interface Mapper {

    /** Returns a filename for the specified variable path. If the path
     * is not valid for this mapper, it throws an InvalidPathException. The
     * returned object must not be null. 
     */
    PhysicalFormat map(Path path) throws InvalidPathException;

    /** Returns true if a (file?) backing the specified SwiftScript path
        exists. The path must be valid for the data object mapped by this
        mapper, otherwise an InvalidPathException will be thrown. 
     * @throws InvalidPathException */
    boolean exists(Path path) throws InvalidPathException;

    /** Returns a Collection of existing mappings, where each Collection
        entry is a Path object. The data files mapped to each Path object
        do not necessarily have to exist.
     */
    Collection<Path> existing();
    
    Collection<Path> existing(FileSystemLister l);

    /**
     * Returns true if data mapped by this mapper cannot
     * change in structure at run-time. Typically this applies
     * to all mappers which work on pre determined data.
     * <br />
     * A short explanation of why this is here:
     *   Since we don't yet deal with apps returning things with
     *   undetermined sizes, yet I2U2 needs to return arrays, this
     *   tells the system that it can close top-level arrays. This
     *   allows the system to know exactly what files need to be 
     *   staged out before a job is run if the return type of the
     *   app is an array. This is not a permanent or nice solution,
     *   but a necessary compromise.
     */
    boolean isStatic();
    
    /**
     * Specifies whether paths mapped by this mapper can be re-mapped
     * if needed (such as when aliasing another variable)
     */
    boolean canBeRemapped(Path path);
    
    /**
     * If this mapper supports remapping then remap the given path to
     * whatever the source mapper maps the sourcePath to
     * @throws InvalidPathException 
     */
    void remap(Path path, Mapper sourceMapper, Path sourcePath) throws InvalidPathException;
    
    boolean isPersistent(Path path);
    
    Set<String> getSupportedParamNames();
    
    void setParameters(GenericMappingParamSet params);
    
    /**
     * Called after all parameters have been closed
     */
    void initialize(RootHandle root);
    
    void setBaseDir(String baseDir);
    
    AbstractDataNode getFirstOpenParameter();

    /**
     * For dynamic mappers, this returns a glob pattern that can
     * be used to filter data that this mapper maps from a set of
     * files.
     * 
     * The type parameter specifies the swift type of the data
     * with the specified path. This is necessary in order to build
     * a sufficiently restrictive pattern in the case when multiple
     * levels of dynamic mappings exist (e.g. file[][]). 
     * 
     * Static mappers should return null.
     */
    Collection<AbsFile> getPattern(Path path, Type type);

    boolean supportsCleaning();

    void fileCleaned(PhysicalFormat pf);
}
