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
    public void clean(Path paths) {
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
    public void setBaseDir(String baseDir) {
        mapper.setBaseDir(baseDir);
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
                PhysicalFormat f = root.getActualMapper().map(handle.getPathFromRoot());
                if (input) {
                    dmc.addRead(f, handle);
                }
                else {
                    dmc.addWrite(f, handle);
                }
            }
            for (String fieldName : type.getFieldNames()) {
                Path fieldPath = Path.parse(fieldName);
                try {
                    checkConsistency(root, handle.getField(fieldPath), dmc);
                }
                catch (InvalidPathException e) {
                    throw new RuntimeException("Data set initialization failed for " + handle
                            + ". Missing required field: " + fieldName);
                }
            }
        }
    }

}
