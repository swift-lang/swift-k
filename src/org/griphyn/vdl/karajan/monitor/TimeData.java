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
 * Created on Jul 24, 2013
 */
package org.griphyn.vdl.karajan.monitor;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

public class TimeData<T> {
    private final int size;
    private Deque<T> data;
    private long startTime, endTime;
    private T initialValue;
    
    public TimeData(int size, T initialValue) {
        this.size = size;
        data = new LinkedList<T>();
        this.initialValue = initialValue;
    }
    
    public synchronized void add(T value, long time) {
        T prev;
        if (data.isEmpty()) {
            startTime = time;
            endTime = time - 1;
            prev = initialValue;
        }
        else {
            prev = data.getLast();
        }
        if (endTime == time) {
            data.removeLast();
            data.addLast(value);
        }
        else {
            while (endTime < time) {
                data.add(prev);
                endTime++;
            }
            data.add(value);
            while (data.size() > size) {
                data.removeFirst();
            }
        }
        
        System.out.println(System.identityHashCode(this) + " s: " + startTime + ", e: " + endTime + ", + (" + time + ", "+ value + ")");
    }
    
    public synchronized List<DataItem<T>> getAll() {
        return new AbstractList<DataItem<T>>() {
            private final ArrayList<T> value;
            private final long t0;
            
            {
                this.value = new ArrayList<T>(data);
                this.t0 = startTime;
            }

            @Override
            public DataItem<T> get(int index) {
                return new DataItem<T>(t0 + index, value.get(index));
            }

            @Override
            public int size() {
                return value.size();
            }
            
        };
    }
    
    public String toString() {
        return data.toString();
    }
    
    public static class DataItem<T> {
        public final T value;
        public final long time;
        
        public DataItem(long time, T value) {
            this.time = time;
            this.value = value;
        }
    }
    
}
