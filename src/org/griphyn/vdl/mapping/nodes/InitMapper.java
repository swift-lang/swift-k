/*
 * Swift Parallel Scripting Language (http://swift-lang.org)
 * Code from Java CoG Kit Project (see notice below) with modifications.
 *
 * Copyright 2005-2014 University of Chicago
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Mar 30, 2014
 */
package org.griphyn.vdl.mapping.nodes;

import java.util.Collection;
import java.util.Set;

import k.rt.Future;
import k.rt.FutureListener;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.futures.FutureNotYetAvailable;
import org.griphyn.vdl.karajan.SwiftContext;
import org.griphyn.vdl.karajan.lib.Tracer;
import org.griphyn.vdl.mapping.AbsFile;
import org.griphyn.vdl.mapping.DSHandle;
import org.griphyn.vdl.mapping.DependentException;
import org.griphyn.vdl.mapping.DuplicateMappingChecker;
import org.griphyn.vdl.mapping.FileSystemLister;
import org.griphyn.vdl.mapping.GenericMappingParamSet;
import org.griphyn.vdl.mapping.HandleOpenException;
import org.griphyn.vdl.mapping.InvalidPathException;
import org.griphyn.vdl.mapping.Mapper;
import org.griphyn.vdl.mapping.MappingDependentException;
import org.griphyn.vdl.mapping.NotCompositeException;
import org.griphyn.vdl.mapping.Path;
import org.griphyn.vdl.mapping.PhysicalFormat;
import org.griphyn.vdl.mapping.RootHandle;
import org.griphyn.vdl.mapping.file.FileGarbageCollector;
import org.griphyn.vdl.type.Type;

public class InitMapper implements Mapper, FutureListener {
    public static final Logger logger = Logger.getLogger(InitMapper.class);
    
    protected static final Tracer variableTracer = Tracer.getTracer("VARIABLE");
    
    private Mapper mapper;
    private boolean input;
    private AbstractDataNode waitingMapperParam;
    private RootHandle node;
    private DuplicateMappingChecker dmc;
    
    public InitMapper(DuplicateMappingChecker dmc) {
        this.dmc = dmc;
    }
    
    @Override
    public void setContext(SwiftContext ctx) {
        mapper.setContext(ctx);
    }

    @Override
    public boolean supportsCleaning() {
        return mapper.supportsCleaning();
    }

    @Override
    public void fileCleaned(PhysicalFormat pf) {
        mapper.fileCleaned(pf);
    }

    @Override
    public PhysicalFormat map(Path path) {
        return null;
    }

    @Override
    public boolean exists(Path path) {
        return false;
    }

    @Override
    public Collection<Path> existing() {
        return null;
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
        return false;
    }

    @Override
    public void remap(Path path, Mapper sourceMapper, Path sourcePath) {
    }


    @Override
    public boolean isPersistent(Path path) {
        return false;
    }

    @Override
    public Set<String> getSupportedParamNames() {
        return null;
    }

    @Override
    public void setParameters(GenericMappingParamSet params) {
    }

    @Override
    public void initialize(RootHandle node) {
        synchronized(node) {
            this.node = node;
            waitingMapperParam = mapper.getFirstOpenParameter();
            if (waitingMapperParam != null) {
                waitingMapperParam.addListener(this, null);
                if (variableTracer.isEnabled()) {
                    variableTracer.trace(node.getThread(), node.getLine(), node.getName() + " WAIT " 
                        + Tracer.getVarName(waitingMapperParam));
                }
                return;
            }
            
            // initialized means that this data has its mapper initialized
            // this should be called before checkInputs because the latter
            // may trigger calls to things that try to access this data node's
            // mapper
            mapper.initialize(node);
            node.mapperInitialized(mapper);
            try {
                checkInputs(node, dmc);
            }
            catch (DependentException e) {
                node.setValue(new MappingDependentException(node, e));
            }
        }
    }

    @Override
    public void futureUpdated(Future fv) {
        initialize(node);
    }

    @Override
    public AbstractDataNode getFirstOpenParameter() {
        return null;
    }

    @Override
    public Collection<AbsFile> getPattern(Path path, Type type) {
        return null;
    }

    public boolean isInput() {
        return input;
    }

    public void setInput(boolean input) {
        this.input = input;
    }

    public Mapper getMapper() {
        synchronized(node) {
            if (waitingMapperParam != null) {
                throw new FutureNotYetAvailable(waitingMapperParam);
            }
            else {
                return mapper;
            }
        }
    }
    
    public void setMapper(Mapper mapper) {
        this.mapper = mapper;
    }
    
    public void checkInputs(RootHandle root, DuplicateMappingChecker dmc) {
        Mapper mapper = root.getActualMapper();
        if (input) {
            addExisting(mapper.existing(), mapper, root, root);
            checkConsistency(root, root, dmc);
        }
        else if (mapper.isStatic()) {
            if (root.isClosed()) {
                // this means that code that would have used this variable is already done
                // which can happen in cases such as if(false) {a = ...}
                return;
            }
            if (mapper.supportsCleaning()) {
                FileGarbageCollector.getDefault().add(root);
            }
            if (!root.getType().isComposite()) {
                checkConsistency(root, root, dmc);
                return;
            }
            // Static mappers are (array) mappers which know the size of
            // an array statically. A good example is the fixed array mapper
            if (logger.isDebugEnabled()) {
                logger.debug("mapper: " + mapper);
            }
            for (Path p : mapper.existing()) {
                try {
                    // Try to get the path in order to check that the 
                    // path is valid - we'll get an exception if not
                    DSHandle h = root.getField(p);
                    if (variableTracer.isEnabled()) {
                        variableTracer.trace(root.getThread(), root.getLine(), 
                            root.getName() + " MAPPING " + p + ", " + mapper.map(p));
                    }
                }
                catch (InvalidPathException e) {
                    throw new IllegalStateException
                    ("mapper.existing() returned a path " + 
                    " that it cannot subsequently map: " + 
                    " root: " + root + " path: " + p);
                }
            }
            if (root.isArray()) {
                root.closeArraySizes();
            }
            checkConsistency(root, root, dmc);
        }
    }

    public static void addExisting(Collection<Path> existing, Mapper mapper, RootHandle root, DSHandle var) {
        boolean any = false;
        checkBasicMappingConstraints(existing, var);
        for (Path p : existing) {
            try {
                DSHandle field = var.getField(p);
                field.setValue(AbstractDataNode.FILE_VALUE);
                if (variableTracer.isEnabled()) {
                    variableTracer.trace(root.getThread(), root.getLine(), 
                        root.getName() + " MAPPING " + p + ", " + mapper.map(p));
                }
                any = true;
            }
            catch (InvalidPathException e) {
                throw new IllegalStateException("Structure of mapped data is " +
                        "incompatible with the mapped variable type: " + e.getMessage());
            }
            catch (NotCompositeException e) {
                throw new IllegalStateException("Cannot map multiple files to variable '" + 
                    e.getDataNode().getFullName() + "' of non composite type '" + 
                    e.getDataNode().getType() + "'");
            }
        }
        var.closeDeep();
        if (!any && variableTracer.isEnabled()) {
            variableTracer.trace(root.getThread(), root.getLine(), 
                root.getName() + " MAPPING no files found");
        }
    }

    private static void checkBasicMappingConstraints(Collection<Path> existing,
            DSHandle var) {
        Type t = var.getType();
        if (!t.isComposite()) {
            if (existing.size() > 1) {
                throw new RuntimeException("Invalid mapping for " + Tracer.getVarName(var) + 
                    ". Expected a single file but found " + existing.size());
            }
            else if (existing.size() == 0) {
                throw new RuntimeException("File not found for variable " + Tracer.getVarName(var) + 
                    ": " + var.map());
            }
        }
    }

    public void checkConsistency(RootHandle root, DSHandle handle, DuplicateMappingChecker dmc) {
        if (handle.getType().isArray()) {
            // any number of indices is ok
            try {
                for (DSHandle dh : handle.getAllFields()) {
                    checkConsistency(root, dh, dmc);
                }
            }
            catch (HandleOpenException e) {
                // TODO init() should throw some checked exception
                throw new RuntimeException("Mapper consistency check failed for " + handle
                        + ". A HandleOpenException was thrown during consistency checking for "+e.getSource(), e);
            }
            catch (InvalidPathException e) {
                e.printStackTrace();
            }
        }
        else {
            // all fields must be present
            Type type = handle.getType();
            if (!type.isPrimitive() && !type.isComposite()) {
                // mapped. Feed the DMC.
                try {
                    Mapper mapper = root.getActualMapper();
                    Path pathFromRoot = handle.getPathFromRoot();
                    PhysicalFormat f = mapper.map(pathFromRoot);
                    if (input) {
                        dmc.addRead(f, handle);
                    }
                    else {
                        dmc.addWrite(f, handle);
                    }
                }
                catch (InvalidPathException e) {
                    throw new RuntimeException("Mapper did properly map " + handle + ".", 
                        new InvalidPathException(handle));
                }
            }
            for (String fieldName : type.getFieldNames()) {
                Path fieldPath = Path.parse(fieldName);
                try {
                    checkConsistency(root, handle.getField(fieldPath), dmc);
                }
                catch (InvalidPathException e) {
                    throw new RuntimeException("Data set initialization failed for " + handle
                            + ". Missing required field: " + fieldName, e);
                }
            }
        }
    }

}
