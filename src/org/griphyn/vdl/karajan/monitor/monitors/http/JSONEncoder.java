//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jul 30, 2013
 */
package org.griphyn.vdl.karajan.monitor.monitors.http;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Stack;


public class JSONEncoder {
    private StringBuilder sb;
    private Stack<Boolean> firstElement;
    private Stack<Boolean> nesting;
    
    public JSONEncoder() {
        this.sb = new StringBuilder();
        firstElement = new Stack<Boolean>();
        nesting = new Stack<Boolean>();
    }
    
    public void write(int value) {
        sb.append(value);
    }
    
    public void write(float value) {
        sb.append(value);
    }
    
    public void write(long value) {
        sb.append(value);
    }
    
    public void write(double value) {
        sb.append(value);
    }
    
    public void write(String value) {
        if (value == null) {
            sb.append("null");
        }
        else {
            sb.append('"');
            escape(sb, value);
            sb.append('"');
        }
    }
    
    private void escape(StringBuilder sb, String value) {
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '"':
                    sb.append('\\');
                default:
                    sb.append(c);
            }
        }
    }

    public void write(boolean value) {
        sb.append(value);
    }
    
    public void write(Object value) {
        if (value == null) {
            sb.append("null");
        }
        else if (value instanceof Integer) {
            write(((Integer) value).intValue());
        }
        else if (value instanceof String) {
            write((String) value);
        }
        else if (value instanceof Boolean) {
            write(((Boolean) value).booleanValue());
        }
        else if (value instanceof Float) {
            write(((Float) value).floatValue());
        }
        else if (value instanceof Double) {
            write(((Double) value).doubleValue());
        }
        else if (value instanceof Long) {
            write(((Long) value).longValue());
        }
        else if (value instanceof Collection) {
            writeArray((Collection<?>) value);
        }
        else if (value instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<? extends Object, ? extends Object> m = (Map<? extends Object, ? extends Object>) value;
            writeMap(m);
        }
        else if (value instanceof int[]) {
            writeArray((int[]) value);
        }
        else {
            write(value.toString());
        }
    }
    
    public void beginArray() {
        sb.append('[');
        firstElement.push(true);
        nesting.push(false);
    }
    
    public void endArray() {
        sb.append("]\n");
        firstElement.pop();
        nesting.pop();
    }
    
    public void writeArrayItem(int value) {
        arraySeparator();
        write(value);
    }
    
    public void writeArrayItem(boolean value) {
        arraySeparator();
        write(value);
    }
    
    public void writeArrayItem(float value) {
        arraySeparator();
        write(value);
    }
    
    public void writeArrayItem(long value) {
        arraySeparator();
        write(value);
    }
    
    public void writeArrayItem(double value) {
        arraySeparator();
        write(value);
    }
    
    public void writeArrayItem(String value) {
        arraySeparator();
        write(value);
    }
    
    public void writeArrayItem(Object value) {
        arraySeparator();
        write(value);
    }
    
    public void beginArrayItem() {
        arraySeparator();
    }
    
    public void endArrayItem() {
    }

    private void arraySeparator() {
        if (nesting.isEmpty()) {
            throw new IllegalStateException("Not in an array");
        }
        if (nesting.peek()) {
            throw new IllegalStateException("In map");
        }
        if (firstElement.peek()) {
            firstElement.pop();
            firstElement.push(false);
        }
        else {
            sb.append(", ");
        }
    }
    
    public void beginMap() {
        sb.append('{');
        firstElement.push(true);
        nesting.push(true);
    }
    
    public void endMap() {
        sb.append("}\n");
        firstElement.pop();
        nesting.pop();
    }
    
    public void writeMapKey(String key) {
        mapSeparator();
        write(key);
        sb.append(": ");
    }
    
    public void writeMapItem(String key, int value) {
        mapSeparator();
        write(key);
        sb.append(": ");
        write(value);
    }
    
    public void writeMapItem(String key, boolean value) {
        mapSeparator();
        write(key);
        sb.append(": ");
        write(value);
    }
    
    public void writeMapItem(String key, float value) {
        mapSeparator();
        write(key);
        sb.append(": ");
        write(value);
    }
    
    public void writeMapItem(String key, long value) {
        mapSeparator();
        write(key);
        sb.append(": ");
        write(value);
    }
    
    public void writeMapItem(String key, double value) {
        mapSeparator();
        write(key);
        sb.append(": ");
        write(value);
    }
    
    public void writeMapItem(String key, String value) {
        mapSeparator();
        write(key);
        sb.append(": ");
        write(value);
    }
    
    public void writeMapItem(String key, Object value) {
        mapSeparator();
        write(key);
        sb.append(": ");
        write(value);
    }

    private void mapSeparator() {
        if (nesting.isEmpty()) {
            throw new IllegalStateException("Not in a map");
        }
        if (!nesting.peek()) {
            throw new IllegalStateException("In array");
        }
        if (firstElement.peek()) {
            firstElement.pop();
            firstElement.push(false);
        }
        else {
            sb.append(", ");
        }
    }
    
    public void writeArray(Collection<?> a) {
        beginArray();
        for (Object v : a) {
            writeArrayItem(v);
        }
        endArray();
    }
    
    public void writeArray(int[] a) {
        beginArray();
        for (int v : a) {
            writeArrayItem(v);
        }
        endArray();
    }
    
    public void writeArray(boolean[] a) {
        beginArray();
        for (boolean v : a) {
            writeArrayItem(v);
        }
        endArray();
    }
    
    public void writeArray(float[] a) {
        beginArray();
        for (float v : a) {
            writeArrayItem(v);
        }
        endArray();
    }
    
    public void writeArray(double[] a) {
        beginArray();
        for (double v : a) {
            writeArrayItem(v);
        }
        endArray();
    }
    
    public void writeArray(String[] a) {
        beginArray();
        for (String v : a) {
            writeArrayItem(v);
        }
        endArray();
    }
    
    public void writeArray(long[] a) {
        beginArray();
        for (long v : a) {
            writeArrayItem(v);
        }
        endArray();
    }
    
    public void writeArray(Object[] a) {
        beginArray();
        for (Object v : a) {
            writeArrayItem(v);
        }
        endArray();
    }
    
    public void writeMap(Map<? extends Object, ? extends Object> m) {
        beginMap();
        for (Map.Entry<? extends Object, ? extends Object> e : m.entrySet()) {
            writeMapItem(String.valueOf(e.getKey()), e.getValue());
        }
        endMap();
    }
    
    public String toString() {
        return sb.toString();
    }
}
