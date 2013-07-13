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
package org.griphyn.vdl.mapping;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import k.rt.FutureListener;
import k.rt.FutureValue;
import k.thr.Yield;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.compiled.nodes.Node;
import org.globus.cog.karajan.futures.FutureNotYetAvailable;
import org.griphyn.vdl.karajan.Loader;
import org.griphyn.vdl.karajan.WaitingThreadsMonitor;
import org.griphyn.vdl.type.Field;
import org.griphyn.vdl.type.Type;
import org.griphyn.vdl.util.VDL2Config;



public abstract class AbstractDataNode implements DSHandle, FutureValue {
    public static final Object FILE_VALUE = new Object();

    static final String DATASET_URI_PREFIX = "dataset:";

    public static final Logger logger = Logger.getLogger(AbstractDataNode.class);

    public static final MappingParam PARAM_PREFIX = new MappingParam("prefix", null);

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
        try {
        	provenance = VDL2Config.getConfig().getProvenanceLog();
        }
        catch (IOException e) {
        }
    }

    private Field field;
    private Map<Comparable<?>, DSHandle> handles;
    private Object value;
    private boolean closed;
    private String identifier;
    private Path pathFromRoot;
    
    private int writeRefCount;
    private List<FutureListener> listeners;

    protected AbstractDataNode(Field field) {
        this.field = field;
        if (field.getType().isComposite()) {
            handles = new HashMap<Comparable<?>, DSHandle>();
        }
        else {
            handles = Collections.emptyMap();
        }
    }

    protected void populateStructFields() {
        for (String name : getType().getFieldNames()) {
            try {
                createField(name);
            }
            catch (NoSuchFieldException e) {
                throw new RuntimeException("Internal inconsistency found: field '" + name 
                    + "' is listed by the type but createField() claims it is invalid");
            }
        }
    }

    public void init(MappingParamSet params) throws HandleOpenException {
        throw new UnsupportedOperationException();
    }
    
    public final void init(Map<String, Object> params) {
        throw new UnsupportedOperationException();
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

    protected Field getField() {
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
        if (type.isArray() && closed) {
            strtype = strtype.replace("[]", "[" + this.getHandles().size() + "]");
        }
        sb.append(strtype);
        if (value != null) {
            sb.append(" = ");
            if (value instanceof Throwable) {
                sb.append(value.getClass().getName());
            }
            else {
                sb.append(value);
            }
        }
        if (closed) {
            sb.append(" - Closed");
        }
        else {
            sb.append(" - Open");
        }
        return sb.toString();
    }

    public String getIdentifyingString() {
        StringBuffer sb = new StringBuffer();
        sb.append(this.getClass().getName());

        sb.append(" identifier ");
        sb.append(this.getIdentifier());

        sb.append(" type ");
        sb.append(getType());

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
            sb.append(this.value.toString());
            sb.append(" dataset=");
        }

        sb.append(getDisplayableName());

        if (!Path.EMPTY_PATH.equals(getPathFromRoot())) {
            sb.append(" path=");
            sb.append(getPathFromRoot().toString());
        }

        if (closed) {
            sb.append(" (closed)");
        }
        else {
            sb.append(" (not closed)");
        }

        return sb.toString();
    }

    public String getDisplayableName() {
        String prefix = getRoot().getParam(MappingParam.SWIFT_DBGNAME);
        if (prefix == null) {
            prefix = getRoot().getParam(PARAM_PREFIX);
        }
        if (prefix == null) {
            prefix = "?";
        }
        return prefix;
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
    
    public String getDeclarationLine() {
        String line = getRoot().getParam(MappingParam.SWIFT_LINE);
        if (line == null || line.length() == 0) {
        	return null;
        }
        else {
        	return line;
        }
    }
    
    public String getThread() {
        String restartId = getRoot().getParam(MappingParam.SWIFT_RESTARTID);
        if (restartId != null) {
            return restartId.substring(0, restartId.lastIndexOf(":"));
        }
        else {
            return null;
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

    public Collection<DSHandle> getFields(Path path) throws InvalidPathException {
        List<DSHandle> fields = new ArrayList<DSHandle>();
        getFields(fields, path);
        return fields;
    }

    protected void getFields(List<DSHandle> fields, Path path) throws InvalidPathException {
        if (path.isEmpty()) {
            fields.add(this);
        }
        else {
            if (path.isWildcard(0)) {
                throw new InvalidPathException("getFields([*]) only applies to arrays");
            }
            try {
                ((AbstractDataNode) getField(path.getFirst())).getFields(fields, path.butFirst());
            }
            catch (NoSuchFieldException e) {
                throw new InvalidPathException(path, this);
            }
        }
    }

    public void set(DSHandle handle) {
        // TODO check type
        if (closed) {
            throw new IllegalArgumentException(this.getDisplayableName() + " is already assigned");
        }
        if (getParent() == null) {
            /*
             * AbstractDataNode node = (AbstractDataNode)handle; field =
             * node.getField(); handles = node.getHandles(); closed =
             * node.isClosed(); value = node.getValue();
             */
            throw new RuntimeException("Can't set root data node!");
        }
        ((AbstractDataNode) getParent()).setField(field.getId(), handle);
    }

    protected synchronized void setField(Comparable<?> id, DSHandle handle) {
        handles.put(id, handle);
    }

    public synchronized DSHandle getField(Comparable<?> key) throws NoSuchFieldException {
        DSHandle handle = handles.get(key);
        if (handle == null) {
            if (closed) {
                throw new NoSuchFieldException(key.toString());
            }
            handle = createField(key);
        }
        return handle;
    }

    protected synchronized boolean isHandlesEmpty() {
        return handles.isEmpty();
    }

    public DSHandle createField(Comparable<?> key) throws NoSuchFieldException {
        if (closed) {
            throw new RuntimeException("Cannot write to closed handle: " + this + " (" + key + ")");
        }
        
        if (!getType().isComposite()) {
            throw new NotCompositeException(this);
        }
        
        return addHandle(key, newNode(getChildField(key)));
    }
    
    @Override
    public DSHandle createField(Path path) throws InvalidPathException {
        if (path.size() != 1) {
            throw new InvalidPathException("Expected a path of size 1: " + path);
        }
        try {
            return createField(path.getFirst());
        }
        catch (NoSuchFieldException e) {
            throw new InvalidPathException("Invalid path (" + path + ") for " + this);
        }
    }

    protected synchronized DSHandle addHandle(Comparable<?> id, DSHandle handle) {
        Object o = handles.put(id, handle);
        if (o != null) {
            throw new RuntimeException("Trying to create a handle that already exists (" + id + ") in " + this);
        }
        return handle;
    }
    
    protected AbstractDataNode newNode(Field f) {
        if (f.getType().isArray()) {
            return new ArrayDataNode(f, getRoot(), this);
        }
        else {
            DataNode dn = new DataNode(f, getRoot(), this);
            if (field.getType().isComposite()) {
                dn.populateStructFields();
            }
            return dn;
        }

    }
        
    protected Field getChildField(Comparable<?> key) throws NoSuchFieldException {
        if (getType().isArray()) {
            return Field.Factory.createField(key, getType().itemType());
        }
        else {
            return Field.Factory.createField(key, getType().getField((String) key).getType());
        }
    }
    
    protected Field getArrayChildField(Comparable<?> key) {
        return Field.Factory.createField(key, getType().itemType());
    }

    protected void checkDataException() {
        if (value instanceof DependentException) {
            throw (DependentException) value;
        }
    }

    protected void checkMappingException() {
        if (value instanceof MappingDependentException) {
            throw (MappingDependentException) value;
        }
    }

    public synchronized Object getValue() {
        checkNoValue();
        checkDataException();
        if (field.getType().isArray()) {
            return handles;
        }
        return value;
    }

    public Map<Comparable<?>, DSHandle> getArrayValue() {
        checkDataException();
        if (!field.getType().isArray()) {
            throw new RuntimeException("getArrayValue called on a non-array: " + this);
        }
        return handles;
    }

    public boolean isArray() {
        return false;
    }

    public void setValue(Object value) {
        synchronized(this) {
            if (this.closed) {
                throw new IllegalArgumentException(this.getFullName() 
                		+ " is closed with a value of " + this.value);
            }
            if (this.value != null) {
                throw new IllegalArgumentException(this.getFullName() 
                		+ " is already assigned with a value of " + this.value);
            }
        
            this.value = value;
            this.closed = true;
        }
        postCloseActions();
    }

    public Collection<Path> getFringePaths() throws HandleOpenException {
        List<Path> list = new ArrayList<Path>();
        getFringePaths(list, Path.EMPTY_PATH);
        return list;
    }
    
    public void getFringePaths(List<Path> list, Path parentPath)
    throws HandleOpenException {
        checkMappingException();
        if (getType().getBaseType() != null) {
            list.add(Path.EMPTY_PATH);
        }
        else {
            for (Field field : getType().getFields()) {
                AbstractDataNode child;
                String name = (String) field.getId();
                try {
                    child = (AbstractDataNode) this.getField(name);
                }
                catch (NoSuchFieldException e) {
                    throw new RuntimeException("Inconsistency between type declaration and " + 
                        "handle for field '" + name + "'");
                }
                Path fullPath = parentPath.addLast(name);
                Type type = child.getType(); 
                if (!type.isPrimitive() && !child.isArray() && type.getFields().size() == 0) {
                    list.add(fullPath);
                }
                else {
                    child.getFringePaths(list, fullPath);
                }
            }
        }
    }
        
    public void closeShallow() {
        synchronized(this) {
            if (this.closed) {
                return;
            }
            this.closed = true;
        }
        postCloseActions();
    }

    private void postCloseActions() {
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
                Type type = this.getType();
                if(type.getName().equals("external")) {
                    filemapped = false;
                }
                if(type.isPrimitive()) {
                    filemapped = false;
                }
                if(type.isArray()) {
                    filemapped = false;
                }
                if(handles.size()>0) {
                    filemapped = false;
                }

                try {
                    if(filemapped) {
                        Object path = m.map(pathFromRoot);
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

        synchronized (this) {
            //Iterator i = handles.entrySet().iterator();
            //while (i.hasNext()) {
            //    Map.Entry e = (Map.Entry) i.next();
            for (DSHandle handle : handles.values()) {
                AbstractDataNode node = (AbstractDataNode) handle;
                if (logger.isInfoEnabled()) {
                    logger.info("CONTAINMENT parent=" + identifier + " child=" + node.getIdentifier());
                }
                node.logContent();
            }
        }
    }
    
    public Mapper getActualMapper() {
        return null;
    }

    public boolean isClosed() {
        return closed;
    }

    public synchronized void closeDeep() {
        if (!this.closed) {
            closeShallow();
        }
        for (DSHandle handle : handles.values()) {
            AbstractDataNode mapper = (AbstractDataNode) handle;
            mapper.closeDeep();
        }
    }
	
	/**
     * Recursively closes arrays through a tree of arrays and complex types.
     */
    public void closeArraySizes() {
        if (!this.closed && this.getType().isArray()) {
            closeShallow();
        }
        synchronized (this) {
            for (DSHandle handle : handles.values()) {
                AbstractDataNode child = (AbstractDataNode) handle;
                if (child.getType().isArray() || child.getType().getFields().size() > 0) {
                    child.closeArraySizes();
                }
            }
        }
    }
    
    public Path getPathFromRoot() {
        if (pathFromRoot == null) {
            AbstractDataNode parent = (AbstractDataNode) this.getParent();
            Path myPath;
            if (parent != null) {
                myPath = parent.getPathFromRoot();
                pathFromRoot = myPath.addLast(getField().getId(), parent.getField().getType().isArray());
            }
            else {
                pathFromRoot = Path.EMPTY_PATH;
            }
        }
        return pathFromRoot;
    }

    public Mapper getMapper() {
        return ((AbstractDataNode) getRoot()).getMapper();
    }

    protected Map<Comparable<?>, DSHandle> getHandles() {
        return handles;
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
       
    public synchronized void waitFor(Node who) {
        if (!closed) {
            if (logger.isDebugEnabled()) {
                logger.debug("Waiting for " + this);
            }
            
            Yield y = new FutureNotYetAvailable(this);
            y.getState().addTraceElement(who);
            throw y;
        }
        else {
            if (logger.isDebugEnabled()) {
                logger.debug("Do not need to wait for " + this);
            }
            checkNoValue();
            if (value instanceof RuntimeException) {
                throw (RuntimeException) value;
            }
        }
    }
    
    public synchronized void waitFor() throws OOBYield {
        if (!closed) {
            if (logger.isDebugEnabled()) {
                logger.debug("Waiting for " + this);
            }
            
            throw new OOBYield(new FutureNotYetAvailable(this), this);
        }
        else {
            if (logger.isDebugEnabled()) {
                logger.debug("Do not need to wait for " + this);
            }
            checkNoValue();
            if (value instanceof RuntimeException) {
                throw (RuntimeException) value;
            }
        }
    }
    
    protected void checkNoValue() {
        if (value == null) {
            if (getType().isComposite()) {
                // composite types (arrays, structs) don't usually have a value
                return;
            }
            AbstractDataNode parent = getParentNode();
            if (parent != null && parent.getType().isArray()) {
                throw new IndexOutOfBoundsException("Invalid index [" + field.getId() + "] for " + parent.getFullName());
            }
            else if (getType().isPrimitive()) {
                throw new RuntimeException(getFullName() + " has no value");
            }
            else {
                throw new MissingDataException(this, getMapper().map(getPathFromRoot()));
            }
        }
    }

    public void addListener(DSHandleListener listener) {
        throw new UnsupportedOperationException();
    }
    
    public void addListener(FutureListener l) {
    	boolean closed;
    	WaitingThreadsMonitor.addThread(l, this);
    	synchronized(this) {
        	if (this.listeners == null) {
        		this.listeners = new ArrayList<FutureListener>();
        	}
        	this.listeners.add(l);
        	closed = this.closed;
    	}
    	if (closed) {
    		notifyListeners();
    	}
    }
        
    protected void notifyListeners() {
    	List<FutureListener> l;
    	synchronized(this) {
    		l = this.listeners;
    		this.listeners = null;
    	}
    	if (l != null) {
    		for (FutureListener ll : l) {
    			ll.futureUpdated(this);
    			WaitingThreadsMonitor.removeThread(ll);
    		}
    	}
    }

    public synchronized void clean() {
        if (!handles.isEmpty()) {
            for (DSHandle h : handles.values()) {
                ((AbstractDataNode) h).clean();
            }
        }
        else if (!getType().isArray() && !getType().isPrimitive()) {
            Mapper mapper = getRoot().getMapper();
            if (mapper != null) {
                mapper.clean(getPathFromRoot());
            }
        }
        field = null;
        handles = null;
        value = null;
        pathFromRoot = null;
    }

    @Override
    public synchronized void setWriteRefCount(int count) {
        this.writeRefCount = count;
    }

    @Override
    public synchronized int updateWriteRefCount(int delta) {
        this.writeRefCount += delta;
       
        if (this.writeRefCount < 0) {
            throw new IllegalArgumentException("Reference count mismatch for " + this + ". Count is " + this.writeRefCount);
        }
                        
        if (logger.isDebugEnabled()) {
            logger.debug(this + " writeRefCount " + this.writeRefCount);
        }
        if (this.writeRefCount == 0) {
            if(logger.isInfoEnabled()) {
                logger.info("All partial closes for " + this + 
                             " have happened. Closing fully.");
            }
            closeDeep();
        }
        return this.writeRefCount;
    }
}
