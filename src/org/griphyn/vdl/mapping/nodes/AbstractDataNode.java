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
 * Created on Jun 6, 2006
 */
package org.griphyn.vdl.mapping.nodes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import k.rt.ConditionalYield;
import k.rt.FutureListener;
import k.rt.FutureValue;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.compiled.nodes.Node;
import org.griphyn.vdl.karajan.Loader;
import org.griphyn.vdl.karajan.WaitingThreadsMonitor;
import org.griphyn.vdl.karajan.lib.Tracer;
import org.griphyn.vdl.mapping.DSHandle;
import org.griphyn.vdl.mapping.DSHandleListener;
import org.griphyn.vdl.mapping.HandleOpenException;
import org.griphyn.vdl.mapping.InvalidPathException;
import org.griphyn.vdl.mapping.Mapper;
import org.griphyn.vdl.mapping.OOBYield;
import org.griphyn.vdl.mapping.Path;
import org.griphyn.vdl.mapping.PhysicalFormat;
import org.griphyn.vdl.type.Field;
import org.griphyn.vdl.type.Type;
import org.griphyn.vdl.util.SwiftConfig;



public abstract class AbstractDataNode implements DSHandle, FutureValue {
    public static final Object FILE_VALUE = new Object() {
        public String toString() {
            return "<FILE>";
        }
    };

    static final String DATASET_URI_PREFIX = "dataset:";

    public static final Logger logger = Logger.getLogger(AbstractDataNode.class);

    /**
     * Datasets are identified within a run by this sequence number and the
     * partial ID field. The initial value is chosen to aid human recognition of
     * sequence numbers in the wild. There is no requirement that it start at
     * this (or any other) particular value. Note that this introduces a maximum
     * on the number of datasets which can be dealt with in any run to be about
     * 2^62.
     */
    private static long datasetIDCounter = 720000000000l;

    /**
     * This is used to provide a (hopefully) globally unique identifier for each
     * time the datasetIDCounter is reset (whenever this class is loaded, which
     * will usually happen once per JVM). No meaning should be inferred from
     * this value - it exists purely for making unique URIs.
     */
    private static final String datasetIDPartialID = Loader.getUUID();
    
    public static boolean provenance = false;
    static {
       	provenance = SwiftConfig.getDefault().isProvenanceEnabled();
    }
    
    protected Field field;
    private String identifier;
        
    protected static final Tracer variableTracer = Tracer.getTracer("VARIABLE");

    protected AbstractDataNode(Field field) {
        this.field = field;
    }
    
    public void initialize() {        
    }
    
    public String getName() {
        return getRoot().getName();
    }
    
    @Override
    public void setName(String name) {
        throw new UnsupportedOperationException("setName can only be called on a root variable");
    }
    
    public Type getType() {
        return field.getType();
    }

    public boolean isPrimitive() {
        return field.getType().isPrimitive();
    }

    public boolean isRestartable() {
        return !isPrimitive();
    }

    public Field getField() {
        return field;
    }
    
    protected abstract AbstractDataNode getParentNode();

    /**
     * create a String representation of this node. If the node has a value,
     * then uses the String representation of that value. Otherwise, generates a
     * text description.
     */
    public String toString() {
        return toDisplayableString();
    }

    private String toDisplayableString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getDisplayableName());
        Path p = getPathFromRoot();
        if (!p.isEmpty()) {
            if (!p.isArrayIndex(0)) {
                sb.append(".");
            }
            sb.append(p.toString());
        }
        sb.append(":");
        Type type = getType();
        String strtype = type.toString();
        if (type.isArray() && isClosed()) {
            strtype = strtype.replace("[]", "[" + this.arraySize() + "]");
        }
        sb.append(strtype);
        Object value = getRawValue();
        
        if (value != null) {
            sb.append(" = ");
            if (value instanceof Throwable) {
                sb.append(value.getClass().getName());
            }
            else {
                sb.append(value);
            }
        }
        if (isClosed()) {
            sb.append(" - Closed");
        }
        else {
            sb.append(" - Open");
        }
        return sb.toString();
    }
    
    protected abstract Object getRawValue();
    
    protected int arraySize() {
        return -1;
    }

    public String getIdentifyingString() {
        StringBuffer sb = new StringBuffer();
        sb.append(this.getClass().getName());

        sb.append(" identifier ");
        sb.append(this.getIdentifier());

        sb.append(" type ");
        sb.append(getType());

        Object value = getRawValue();
        if (value == null) {
            sb.append(" with no value at dataset=");
        }
        else if (value instanceof Throwable) {
            sb.append(" containing throwable ");
            sb.append(value.getClass());
            sb.append(" dataset=");
        }
        else {
            sb.append(" value=");
            sb.append(value.toString());
            sb.append(" dataset=");
        }

        sb.append(getDisplayableName());

        if (!Path.EMPTY_PATH.equals(getPathFromRoot())) {
            sb.append(" path=");
            sb.append(getPathFromRoot().toString());
        }

        if (isClosed()) {
            sb.append(" (closed)");
        }
        else {
            sb.append(" (not closed)");
        }

        return sb.toString();
    }

    public String getDisplayableName() {
        return getName();
    }
    
    public String getFullName() {
        String name = getDisplayableName();
        Path p = getPathFromRoot();
        if (p.isEmpty()) {
            return name;
        }
        else {
            return name + "." + p;
        }
    }
        
    public DSHandle getField(Path path) throws InvalidPathException {
        if (path.isEmpty()) {
            return this;
        }
        try {
            DSHandle handle = getField(path.getFirst());
            
            if (path.size() > 1) {
                return handle.getField(path.butFirst());
            }
            else {
                return handle;
            }
        }
        catch (NoSuchFieldException e) {
            throw new InvalidPathException(path, this);
        }
    }

    @Override
    public Collection<DSHandle> getAllFields() throws InvalidPathException, HandleOpenException {
        throw new InvalidPathException("No fields");
    }
            
    @Override
    public DSHandle getField(Comparable<?> key) throws NoSuchFieldException {
        throw new NoSuchFieldException("No fields");
    }

    public Map<Comparable<?>, DSHandle> getArrayValue() {
        throw new RuntimeException("getArrayValue called on a non-array: " + this);
    }

    @Override
    public Collection<Path> getFringePaths() throws HandleOpenException {
        List<Path> list = new ArrayList<Path>();
        getFringePaths(list, Path.EMPTY_PATH);
        return list;
    }
    
    protected abstract void getFringePaths(List<Path> list, Path myPath) throws HandleOpenException;

    public Collection<DSHandle> getLeaves() throws HandleOpenException {
        List<DSHandle> list = new ArrayList<DSHandle>();
        getLeaves(list);
        return list;
    }
            
    protected abstract void getLeaves(List<DSHandle> list) throws HandleOpenException;

    protected void postCloseActions() {
        // closed
        notifyListeners();
        if (logger.isDebugEnabled()) {
            logger.debug("closed " + this.getIdentifyingString());
        }
        // so because its closed, we can dump the contents

        try {
            if(provenance) {
                logContent();
            }
        }
        catch (Exception e) {
            logger.warn("Exception whilst logging dataset content for " + this, e);
        }
        // TODO record retrospective provenance information for this dataset here
        // we should do it at closing time because that's the point at which we
        // know the dataset has its values (all the way down the tree) assigned.

        // provenance-id for this dataset should have been assigned at creation time,
        // though, so that we can refer to this dataset elsewhere before it is closed.

        // is this method the only one called to set this.closed? or do subclasses
        // or other methods ever change it?
    }

    public synchronized void logContent() {
        String identifier = this.getIdentifier();
        Path pathFromRoot = this.getPathFromRoot();
        Type type = this.getType();
        if (this.getPathFromRoot() != null) {
            if (logger.isInfoEnabled()) {
                logger.info("ROOTPATH dataset=" + identifier + " path=" + pathFromRoot);
                if (this.getType().isPrimitive()) {
                    logger.info("VALUE dataset=" + identifier + " VALUE=" + this.toString());
                }
            }

            Mapper m = getActualMapper();

            if (m != null) {
                // TODO proper type here
                // Not sure catching exception here is really the right thing to
                // do here
                // anyway - should perhaps only be trying to map leafnodes?
                // Mapping
                // non-leaf stuff is giving wierd paths anyway

                // TODO this is perhaps an unpleasant way of finding if this is a file-backed
                // leaf node or not
                boolean filemapped = true;
                
                if (type.getName().equals("external")) {
                    filemapped = false;
                }
                if (type.isPrimitive()) {
                    filemapped = false;
                }
                if (type.isComposite()) {
                    filemapped = false;
                }

                try {
                    if (filemapped) {
                        Object path = map();
                        if (logger.isInfoEnabled()) {
                            logger.info("FILENAME dataset=" + identifier + " filename=" + path);
                        }
                    }
                }
                catch (Exception e) {
                    if (logger.isInfoEnabled()) {
                        logger.info("NOFILENAME dataset=" + identifier);
                    }
                }
            }
        }

        if (type.isComposite()) {
            synchronized (this) {
                try {
                    for (DSHandle handle : this.getAllFields()) {
                        AbstractDataNode node = (AbstractDataNode) handle;
                        if (logger.isInfoEnabled()) {
                            logger.info("CONTAINMENT parent=" + identifier + " child=" + node.getIdentifier());
                        }
                        node.logContent();
                    }
                }
                catch (Exception e) {
                    logger.warn("Exception caught while trying to log provenance data", e);
                }
            }
        }
    }
	
	private Mapper getActualMapper() {
        return getRoot().getActualMapper();
    }
	
	protected Path calculatePathFromRoot() {
	    AbstractDataNode parent = (AbstractDataNode) this.getParent();
        Path myPath;
        if (parent != null) {
            myPath = parent.getPathFromRoot();
            return myPath.addLast(getField().getId(), parent.getField().getType().isArray());
        }
        else {
            return Path.EMPTY_PATH;
        }
	}
    
    public Mapper getMapper() {
        return ((AbstractDataNode) getRoot()).getMapper();
    }
    
    protected InitMapper getInitMapper() {
        Mapper m = getActualMapper();
        if (m instanceof InitMapper) {
            return (InitMapper) m;
        }
        else {
            throw new IllegalStateException("Cannot get initialization mapper on initialized node");
        }
    }
    
    public boolean isInput() {
        return getInitMapper().isInput();
    }

    public void setInput(boolean input) {
        getInitMapper().setInput(input);
    }
    
    @Override
    public PhysicalFormat map(Path path) {
        Mapper m = getMapper();
        if (m == null) {
            return null;
        }
        else {
            Path p = getPathFromRoot().append(path);
            return m.map(p);
        }
    }

    @Override
    public PhysicalFormat map() {
        return map(Path.EMPTY_PATH);
    }
    
    public synchronized String getIdentifier() {
    	if (identifier == null) {
   		    identifier = makeIdentifierURIString();
    	}
        return identifier;
    }

    String makeIdentifierURIString() {
        datasetIDCounter++;
        return DATASET_URI_PREFIX + datasetIDPartialID + ":" + datasetIDCounter;
    }

    public void addListener(DSHandleListener listener) {
        throw new UnsupportedOperationException();
    }
        
    public void addListener(FutureListener l, ConditionalYield y) {
    	boolean closed;
    	WaitingThreadsMonitor.addThread(l, this);
    	synchronized(this) {
    	    closed = addListener0(l);
    	}
    	if (closed) {
    		notifyListeners();
    	}
    }
    
    protected abstract boolean addListener0(FutureListener l);
    
    protected abstract void notifyListeners();

    public synchronized void clean() {
        clean0();
    }
    
    protected void clean0() {
        field = null;
    }
    
    public abstract void waitFor(Node who);
    
    public abstract void waitFor() throws OOBYield;
}
