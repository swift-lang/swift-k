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
