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
 * Created on Jun 9, 2006
 */
package org.griphyn.vdl.karajan;

import java.util.AbstractCollection;
import java.util.AbstractList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.griphyn.vdl.mapping.DSHandle;

public class PairSet extends AbstractCollection<List<?>> {
	private Set<Map.Entry<Comparable<?>, DSHandle>> set;
	
	public PairSet(Map<Comparable<?>, DSHandle> map) {
		this.set = map.entrySet();
	}

    @Override
    public Iterator<List<?>> iterator() {
        final Iterator<Map.Entry<Comparable<?>, DSHandle>> it = set.iterator();
        return new Iterator<List<?>>() {
            @Override
            public boolean hasNext() {
                return it.hasNext();
            }

            @Override
            public List<?> next() {
                final Map.Entry<Comparable<?>, DSHandle> e = it.next();
                return new AbstractList<Object>() {
                    @Override
                    public Object get(int index) {
                        switch (index) {
                            case 0:
                                return e.getKey();
                            case 1:
                                return e.getValue();
                            default:
                                throw new IndexOutOfBoundsException(String.valueOf(index));
                        }
                    }

                    @Override
                    public int size() {
                        return 2;
                    }
                    
                };
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    @Override
    public int size() {
        return set.size();
    }
}
