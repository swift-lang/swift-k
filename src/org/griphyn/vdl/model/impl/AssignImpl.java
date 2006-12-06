/*
 * XML Type:  Assign
 * Namespace: http://www.griphyn.org/2006/08/vdl
 * Java type: org.griphyn.vdl.model.Assign
 *
 * Automatically generated - do not modify.
 */
package org.griphyn.vdl.model.impl;
/**
 * An XML Assign(@http://www.griphyn.org/2006/08/vdl).
 *
 * This is a complex type.
 */
public class AssignImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements org.griphyn.vdl.model.Assign
{
    
    public AssignImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName ARRAY$0 = 
        new javax.xml.namespace.QName("http://www.griphyn.org/2006/08/vdl", "array");
    private static final javax.xml.namespace.QName RANGE$2 = 
        new javax.xml.namespace.QName("http://www.griphyn.org/2006/08/vdl", "range");
    private static final javax.xml.namespace.QName FUNCTION$4 = 
        new javax.xml.namespace.QName("http://www.griphyn.org/2006/08/vdl", "function");
    private static final javax.xml.namespace.QName TO$6 = 
        new javax.xml.namespace.QName("", "to");
    
    
    /**
     * Gets the "array" element
     */
    public org.griphyn.vdl.model.Array getArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.griphyn.vdl.model.Array target = null;
            target = (org.griphyn.vdl.model.Array)get_store().find_element_user(ARRAY$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * True if has "array" element
     */
    public boolean isSetArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(ARRAY$0) != 0;
        }
    }
    
    /**
     * Sets the "array" element
     */
    public void setArray(org.griphyn.vdl.model.Array array)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.griphyn.vdl.model.Array target = null;
            target = (org.griphyn.vdl.model.Array)get_store().find_element_user(ARRAY$0, 0);
            if (target == null)
            {
                target = (org.griphyn.vdl.model.Array)get_store().add_element_user(ARRAY$0);
            }
            target.set(array);
        }
    }
    
    /**
     * Appends and returns a new empty "array" element
     */
    public org.griphyn.vdl.model.Array addNewArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.griphyn.vdl.model.Array target = null;
            target = (org.griphyn.vdl.model.Array)get_store().add_element_user(ARRAY$0);
            return target;
        }
    }
    
    /**
     * Unsets the "array" element
     */
    public void unsetArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(ARRAY$0, 0);
        }
    }
    
    /**
     * Gets the "range" element
     */
    public org.griphyn.vdl.model.Range getRange()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.griphyn.vdl.model.Range target = null;
            target = (org.griphyn.vdl.model.Range)get_store().find_element_user(RANGE$2, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * True if has "range" element
     */
    public boolean isSetRange()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(RANGE$2) != 0;
        }
    }
    
    /**
     * Sets the "range" element
     */
    public void setRange(org.griphyn.vdl.model.Range range)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.griphyn.vdl.model.Range target = null;
            target = (org.griphyn.vdl.model.Range)get_store().find_element_user(RANGE$2, 0);
            if (target == null)
            {
                target = (org.griphyn.vdl.model.Range)get_store().add_element_user(RANGE$2);
            }
            target.set(range);
        }
    }
    
    /**
     * Appends and returns a new empty "range" element
     */
    public org.griphyn.vdl.model.Range addNewRange()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.griphyn.vdl.model.Range target = null;
            target = (org.griphyn.vdl.model.Range)get_store().add_element_user(RANGE$2);
            return target;
        }
    }
    
    /**
     * Unsets the "range" element
     */
    public void unsetRange()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(RANGE$2, 0);
        }
    }
    
    /**
     * Gets the "function" element
     */
    public org.griphyn.vdl.model.Function getFunction()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.griphyn.vdl.model.Function target = null;
            target = (org.griphyn.vdl.model.Function)get_store().find_element_user(FUNCTION$4, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * True if has "function" element
     */
    public boolean isSetFunction()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(FUNCTION$4) != 0;
        }
    }
    
    /**
     * Sets the "function" element
     */
    public void setFunction(org.griphyn.vdl.model.Function function)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.griphyn.vdl.model.Function target = null;
            target = (org.griphyn.vdl.model.Function)get_store().find_element_user(FUNCTION$4, 0);
            if (target == null)
            {
                target = (org.griphyn.vdl.model.Function)get_store().add_element_user(FUNCTION$4);
            }
            target.set(function);
        }
    }
    
    /**
     * Appends and returns a new empty "function" element
     */
    public org.griphyn.vdl.model.Function addNewFunction()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.griphyn.vdl.model.Function target = null;
            target = (org.griphyn.vdl.model.Function)get_store().add_element_user(FUNCTION$4);
            return target;
        }
    }
    
    /**
     * Unsets the "function" element
     */
    public void unsetFunction()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(FUNCTION$4, 0);
        }
    }
    
    /**
     * Gets the "to" attribute
     */
    public java.lang.String getTo()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(TO$6);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "to" attribute
     */
    public org.apache.xmlbeans.XmlString xgetTo()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_attribute_user(TO$6);
            return target;
        }
    }
    
    /**
     * True if has "to" attribute
     */
    public boolean isSetTo()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().find_attribute_user(TO$6) != null;
        }
    }
    
    /**
     * Sets the "to" attribute
     */
    public void setTo(java.lang.String to)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(TO$6);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_attribute_user(TO$6);
            }
            target.setStringValue(to);
        }
    }
    
    /**
     * Sets (as xml) the "to" attribute
     */
    public void xsetTo(org.apache.xmlbeans.XmlString to)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_attribute_user(TO$6);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlString)get_store().add_attribute_user(TO$6);
            }
            target.set(to);
        }
    }
    
    /**
     * Unsets the "to" attribute
     */
    public void unsetTo()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_attribute(TO$6);
        }
    }
}
