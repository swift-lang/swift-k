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

import java.util.Comparator;

public class PathElementComparator implements Comparator<Comparable<?>> {

    @SuppressWarnings("unchecked")
    public int compare(Comparable<?> p1, Comparable<?> p2) {
        Comparable<Object> e1 = (Comparable<Object>) p1;
        Comparable<Object> e2 = (Comparable<Object>) p2;
        int d = 0;
        if (e1.getClass() != e2.getClass()) {
            d = e1.getClass().getName().compareTo(e2.getClass().getName());
        }
        if (d == 0) {
            d = e1.compareTo(e2);
        }
        return d;
    }
}
