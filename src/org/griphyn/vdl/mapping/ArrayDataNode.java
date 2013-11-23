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
 * Created on Jun 30, 2006
 */
package org.griphyn.vdl.mapping;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import k.rt.FutureListener;

import org.globus.cog.karajan.futures.FutureNotYetAvailable;
import org.griphyn.vdl.karajan.WaitingThreadsMonitor;
import org.griphyn.vdl.type.Field;

public class ArrayDataNode extends DataNode { 
    
	private List<Comparable<?>> keyList;
	
	protected ArrayDataNode(Field field, RootHandle root, AbstractDataNode parent) {
		super(field, root, parent);
	}
	
	public void getFringePaths(List<Path> list, Path parentPath) throws HandleOpenException {
		checkMappingException();
		if (!isClosed()) {
		    throw new FutureNotYetAvailable(this);
		}
		Map<Comparable<?>, DSHandle> handles = getHandles();
		synchronized (this) {
			for (Map.Entry<Comparable<?>, DSHandle> e : handles.entrySet()) {
				AbstractDataNode mapper = (AbstractDataNode) e.getValue();
				Path fullPath = parentPath.addLast(e.getKey(), getType().isArray());
				if (mapper.getType().isComposite()) {
					mapper.getFringePaths(list, fullPath);
				}
				else if (!mapper.getType().isPrimitive()) {
					list.add(fullPath);
				}
			}
		}
	}
	
	/** Recursively closes arrays through a tree of arrays and complex
        types. */
    public void closeDeep() {
        assert(this.getType().isArray());
        closeShallow();
        Map<Comparable<?>, DSHandle> handles = getHandles();
        synchronized (this) {
        	for (Map.Entry<Comparable<?>, DSHandle> e : handles.entrySet()) {
                AbstractDataNode child = (AbstractDataNode) e.getValue();
                child.closeDeep();
            }
        }
    }
	
	public boolean isArray() {
		return true;
	}
	
	public int size() {
		return getHandles().size();
	}
	
    @Override
    protected void setField(Comparable<?> id, DSHandle handle) {
        synchronized(this) {
            super.setField(id, handle);
            // Operations on the handles and the wrapper keys need to be synchronized.
            // When a wrapper is created, it populates its list of keys with the handles
            // available. If this happens concurrently with a call being exactly at this
            // point in the code, then it's possible for a key to have both been added
            // as part of the creation of the wrapper as well as due to the next call,
            // causing duplicate iterations.
            addKey(id);
        }
    }
    
    private void addKey(Comparable<?> key) {
    	synchronized(this) {
    		if (keyList != null) {
    			keyList.add(key);
    		}
    	}
        notifyListeners();
    }
    
    @Override
    public void addListener(FutureListener l) {
        boolean shouldNotify;
        WaitingThreadsMonitor.addThread(l, this);
        synchronized(this) {
            shouldNotify = addListener0(l);
            if (keyList != null) {
                shouldNotify = true;
            }
        }
        if (shouldNotify) {
            notifyListeners();
        }
    }

    @Override
    public synchronized DSHandle createField(Comparable<?> key) throws NoSuchFieldException {
        DSHandle h = super.createField(key);
        addKey(key);
        return h;
    }
    
    public Iterable<List<?>> entryList() {
    	synchronized(this) {
    		if (isClosed()) {
    			return new ClosedArrayEntries(getArrayValue());
    		}
    		else {
    			keyList = new ArrayList<Comparable<?>>(getArrayValue().keySet());
    			return new OpenArrayEntries(keyList, getArrayValue(), this);
    		}
    	}
    }
    
    @Override
    public void closeShallow() {
        super.closeShallow();
        synchronized(this) {
        	keyList = null;
        }
    }

    protected void getFields(List<DSHandle> fields, Path path) throws InvalidPathException {
        if (path.isEmpty()) {
            fields.add(this);
        }
        else {
            Path rest = path.butFirst();
            if (path.isWildcard(0)) {
                if (!isClosed()) {
                    throw new FutureNotYetAvailable(this);
                }
                for (DSHandle handle : getHandles().values()) {
                    ((AbstractDataNode) handle).getFields(fields, rest);
                }
            }
            else {
                try {
                    ((AbstractDataNode) getField(path.getKey(0))).getFields(
                        fields, path.butFirst());
                }
                catch (NoSuchFieldException e) {
                    throw new InvalidPathException(path.getKey(0), this);
                }
            }
        }
    }

    @Override
    protected void checkNoValue() {
        // lack of a value in an array node does not indicate an error
    }
    
    
}
