//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on May 28, 2016
 */
package org.globus.cog.abstraction.coaster.service.job.manager;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.globus.cog.abstraction.impl.common.task.ServiceContactImpl;
import org.globus.cog.abstraction.interfaces.ServiceContact;

public abstract class IntrospectiveMap extends AbstractMap<String, Object> {
    
    public abstract String[] getNames();
    
    public void setAttribute(String name, Object value) {
        throw new UnsupportedOperationException();
    }
    
    public Collection<String> getAttributeNames() {
        return Collections.emptyList();
    }
    
    public Object getAttribute(String name) {
        return null;
    }
    
    private static final Object[] NO_ARGS = new Object[0];
    
    public Object get(String name) throws IllegalArgumentException, IllegalAccessException,
            InvocationTargetException {
        Method[] ms = getClass().getMethods();
        String getterName = "get" + Character.toUpperCase(name.charAt(0)) + name.substring(1);
        for (int i = 0; i < ms.length; i++) {
            if (ms[i].getName().equals(getterName)) {
                return ms[i].invoke(this, NO_ARGS);
            }
        }
        return null;
    }
    
    @Override
    public Object put(String name, Object value) {
        if (name.length() == 0) {
            throw new IllegalArgumentException("Empty string Settings key "
                                            + "(value was \"" + value + "\"");
        }

        boolean complete = false;
        Method[] methods = getClass().getMethods();
        String setterName = "set" +
            Character.toUpperCase(name.charAt(0)) + name.substring(1);
        try {
            for (Method method : methods) {
                if (method.getName().equalsIgnoreCase(setterName)) {
                    set(method, value);
                    complete = true;
                    break;
                }
            }
        }
        catch (InvocationTargetException e) {
            throw new IllegalArgumentException
                ("Cannot set: " + name + " to: " + value);
        }
        catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        if (!complete) {
            setAttribute(name, value);
        }
        return null;
    }

    protected void set(Method method, Object value)
        throws InvocationTargetException, IllegalAccessException {
        Class<?> clazz = method.getParameterTypes()[0];
        Object[] args = null;
        if (clazz.equals(Object.class)) {
            args = new Object[] { value };
        }
        else if (clazz.equals(String.class)) {
            args = new Object[] { String.valueOf(value) };
        }
        else if (clazz.equals(int.class)) {
            args = new Object[] { toInt(value) };
        }
        else if (clazz.equals(double.class)) {
            args = new Object[] { toDouble(value) };
        }
        else if (clazz.equals(boolean.class)) {
            args = new Object[] { toBoolean(value) };
        }
        else if (clazz.equals(TimeInterval.class)) {
            args = new Object[] { TimeInterval.fromSeconds(toInt(value)) };
        }
        else if (clazz.equals(ServiceContact.class)) {
            args = new Object[] { new ServiceContactImpl(toString(value)) };
        }
        else {
            throw new IllegalArgumentException
                ("Don't know how to set option with type " + clazz);
        }
        method.invoke(this, args);
    }
    
    protected int toInt(Object value) {
        if (value instanceof Integer) {
            return (Integer) value;
        }
        else if (value instanceof String) {
            return Integer.parseInt((String) value);
        }
        else {
            throw new IllegalArgumentException("Illegal value: '" + value + "'");
        }
    }
    
    protected double toDouble(Object value) {
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        else if (value instanceof String) {
            return Double.parseDouble((String) value);
        }
        else {
            throw new IllegalArgumentException("Illegal value: '" + value + "'");
        }
    }
    
    protected boolean toBoolean(Object value) {
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        else if (value instanceof String) {
            return Boolean.parseBoolean((String) value);
        }
        else {
            throw new IllegalArgumentException("Illegal value: '" + value + "'");
        }
    }
    
    protected String toString(Object value) {
        return String.valueOf(value);
    }

    @Override
    public Set<Map.Entry<String, Object>> entrySet() {
        return new AbstractSet<Map.Entry<String, Object>>() {
            final Iterator<String> attrNamesIt = getAttributeNames().iterator();
            final String[] names = getNames();
            @Override
            public Iterator<Map.Entry<String, Object>> iterator() {
                return new Iterator<Map.Entry<String, Object>>() {
                    private int i = 0;
                    
                    @Override
                    public boolean hasNext() {
                        return i < names.length || attrNamesIt.hasNext();
                    }

                    @Override
                    public void remove() {
                        throw new UnsupportedOperationException();
                    }

                    @Override
                    public java.util.Map.Entry<String, Object> next() {
                        final String name;
                        final Object value;
                        if (i < names.length) {
                            name = names[i++];
                            try {
                                value = get(name);
                            }
                            catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        }
                        else {
                            name = attrNamesIt.next();
                            value = getAttribute(name);
                        }
                        
                        return new Map.Entry<String, Object>() {
                            @Override
                            public String getKey() {
                                return name;
                            }

                            @Override
                            public Object getValue() {
                                return value;
                            }

                            @Override
                            public Object setValue(Object value) {
                                throw new UnsupportedOperationException();
                            }
                        };
                    }
                };
            }

            @Override
            public int size() {
                return getNames().length + getAttributeNames().size();
            }
        };
    }
}
