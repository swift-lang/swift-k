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
 * Created on Jul 30, 2013
 */
package org.globus.cog.util.json;

import java.io.CharArrayWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;


public class JSONEncoder {
    private static final JsonFactory factory = new JsonFactory();
    
    private JsonGenerator g;
    private CharArrayWriter wr;
    
    public JSONEncoder() throws IOException {
        wr = new CharArrayWriter();
        g = factory.createGenerator(wr);
    }
    
    public void write(int value) throws IOException {
        g.writeNumber(value);
    }
    
    public void write(float value) throws IOException {
        g.writeNumber(value);
    }
    
    public void write(long value) throws IOException {
        g.writeNumber(value);
    }
    
    public void write(double value) throws IOException {
        g.writeNumber(value);
    }
    
    public void write(String value) throws IOException {
        if (value == null) {
            g.writeNull();
        }
        else {
            g.writeString(value);
        }
    }
    
    
    public void write(boolean value) throws IOException {
        g.writeBoolean(value);
    }
    
    public void write(Object value) throws IOException {
        if (value == null) {
            g.writeNull();
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
    
    public void beginArray() throws IOException {
        g.writeStartArray();
    }
    
    public void endArray() throws IOException {
        g.writeEndArray();
    }
    
    public void writeArrayItem(int value) throws IOException {
        write(value);
    }
    
    public void writeArrayItem(boolean value) throws IOException {
        write(value);
    }
    
    public void writeArrayItem(float value) throws IOException {
        write(value);
    }
    
    public void writeArrayItem(long value) throws IOException {
        write(value);
    }
    
    public void writeArrayItem(double value) throws IOException {
        write(value);
    }
    
    public void writeArrayItem(String value) throws IOException {
        write(value);
    }
    
    public void writeArrayItem(Object value) throws IOException {
        write(value);
    }
    
    public void beginArrayItem() {
    }
    
    public void endArrayItem() {
    }
    
    public void beginMap() throws IOException {
        g.writeStartObject();
    }
    
    public void endMap() throws IOException {
        g.writeEndObject();
    }
    
    public void writeMapKey(String key) throws IOException {
        g.writeFieldName(key);
    }
    
    public void writeMapItem(String key, int value) throws IOException {
        g.writeFieldName(key);
        write(value);
    }
    
    public void writeMapItem(int key, int value) throws IOException {
        g.writeFieldName(String.valueOf(key));
        write(value);
    }
    
    public void writeMapItem(String key, boolean value) throws IOException {
        g.writeFieldName(key);
        write(value);
    }
    
    public void writeMapItem(String key, float value) throws IOException {
        g.writeFieldName(key);
        write(value);
    }
    
    public void writeMapItem(String key, long value) throws IOException {
        g.writeFieldName(key);
        write(value);
    }
    
    public void writeMapItem(String key, double value) throws IOException {
        g.writeFieldName(key);
        write(value);
    }
    
    public void writeMapItem(String key, String value) throws IOException {
        g.writeFieldName(key);
        write(value);
    }
    
    public void writeMapItem(String key, Object value) throws IOException {
        g.writeFieldName(key);
        write(value);
    }
    
    public void writeArray(Collection<?> a) throws IOException {
        beginArray();
        for (Object v : a) {
            writeArrayItem(v);
        }
        endArray();
    }
    
    public void writeArray(int[] a) throws IOException {
        beginArray();
        for (int v : a) {
            writeArrayItem(v);
        }
        endArray();
    }
    
    public void writeArray(boolean[] a) throws IOException {
        beginArray();
        for (boolean v : a) {
            writeArrayItem(v);
        }
        endArray();
    }
    
    public void writeArray(float[] a) throws IOException {
        beginArray();
        for (float v : a) {
            writeArrayItem(v);
        }
        endArray();
    }
    
    public void writeArray(double[] a) throws IOException {
        beginArray();
        for (double v : a) {
            writeArrayItem(v);
        }
        endArray();
    }
    
    public void writeArray(String[] a) throws IOException {
        beginArray();
        for (String v : a) {
            writeArrayItem(v);
        }
        endArray();
    }
    
    public void writeArray(long[] a) throws IOException {
        beginArray();
        for (long v : a) {
            writeArrayItem(v);
        }
        endArray();
    }
    
    public void writeArray(Object[] a) throws IOException {
        beginArray();
        for (Object v : a) {
            writeArrayItem(v);
        }
        endArray();
    }
    
    public void writeMap(Map<? extends Object, ? extends Object> m) throws IOException {
        beginMap();
        for (Map.Entry<? extends Object, ? extends Object> e : m.entrySet()) {
            writeMapItem(String.valueOf(e.getKey()), e.getValue());
        }
        endMap();
    }
    
    public String toString() {
        try {
            g.flush();
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (wr != null) {
            return wr.toString();
        }
        else {
            return g.toString();
        }
    }
}
