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
 * Created on Sep 3, 2015
 */
package org.griphyn.vdl.engine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TopologicalSort<T> {
    public interface VisitorHelper<T> {
        Collection<T> getDependencies(T item);
    }
    
    private final Set<T> all;
    private final VisitorHelper<T> helper;
    private final boolean allowSelfCycle;
    

    public TopologicalSort(Set<T> all, boolean allowSelfCycle, VisitorHelper<T> helper) {
        this.all = all;
        this.helper = helper;
        this.allowSelfCycle = allowSelfCycle;
    }

    public List<T> sort() {
        List<T> sorted = new ArrayList<T>();
        Set<T> unmarked = new HashSet<T>(all);
        
        while (!unmarked.isEmpty()) {
            Set<T> tmp = new HashSet<T>();
            visit(null, unmarked.iterator().next(), unmarked, sorted, tmp, all);
        }
        return sorted;
    }

    private void visit(T self, T item, Set<T> unmarked, List<T> sorted, Set<T> tmp, Set<T> all) {
        if (tmp.contains(item)) {
            if (item.equals(self) && allowSelfCycle) {
                // immediate recursion allowed
                return;
            }
            else {
                throw new IllegalArgumentException("Circular dependency detected for '" + item + "'");
            }
        }
        if (unmarked.contains(item)) {
            tmp.add(item);
            Set<T> dupes = new HashSet<T>();
            for (T dependency : helper.getDependencies(item)) {
                if (dupes.contains(dependency)) {
                    continue;
                }
                dupes.add(dependency);
                if (all.contains(dependency)) {
                    visit(item, dependency, unmarked, sorted, tmp, all);
                }
                else {
                    // handled later
                }
            }
            unmarked.remove(item);
            sorted.add(item);
        }
    }
}
