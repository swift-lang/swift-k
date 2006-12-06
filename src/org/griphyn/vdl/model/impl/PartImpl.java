/*
 * XML Type:  Part
 * Namespace: http://www.griphyn.org/2006/08/vdl
 * Java type: org.griphyn.vdl.model.Part
 *
 * Automatically generated - do not modify.
 */
package org.griphyn.vdl.model.impl;
/**
 * An XML Part(@http://www.griphyn.org/2006/08/vdl).
 *
 * This is a complex type.
 */
public class PartImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements org.griphyn.vdl.model.Part
{
    
    public PartImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName FUNCTION$0 = 
        new javax.xml.namespace.QName("http://www.griphyn.org/2006/08/vdl", "function");
    private static final javax.xml.namespace.QName NAME$2 = 
        new javax.xml.namespace.QName("", "name");
    
    
    /**
     * Gets the "function" element
     */
    public org.griphyn.vdl.model.Function getFunction()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.griphyn.vdl.model.Function target = null;
            target = (org.griphyn.vdl.model.Function)get_store().find_element_user(FUNCTION$0, 0);
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
            return get_store().count_elements(FUNCTION$0) != 0;
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
            target = (org.griphyn.vdl.model.Function)get_store().find_element_user(FUNCTION$0, 0);
            if (target == null)
            {
                target = (org.griphyn.vdl.model.Function)get_store().add_element_user(FUNCTION$0);
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
            target = (org.griphyn.vdl.model.Function)get_store().add_element_user(FUNCTION$0);
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
            get_store().remove_element(FUNCTION$0, 0);
        }
    }
    
    /**
     * Gets the "name" attribute
     */
    public java.lang.String getName()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(NAME$2);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "name" attribute
     */
    public org.apache.xmlbeans.XmlString xgetName()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_attribute_user(NAME$2);
            return target;
        }
    }
    
    /**
     * True if has "name" attribute
     */
    public boolean isSetName()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().find_attribute_user(NAME$2) != null;
        }
    }
    
    /**
     * Sets the "name" attribute
     */
    public void setName(java.lang.String name)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(NAME$2);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_attribute_user(NAME$2);
            }
            target.setStringValue(name);
        }
    }
    
    /**
     * Sets (as xml) the "name" attribute
     */
    public void xsetName(org.apache.xmlbeans.XmlString name)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_attribute_user(NAME$2);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlString)get_store().add_attribute_user(NAME$2);
            }
            target.set(name);
        }
    }
    
    /**
     * Unsets the "name" attribute
     */
    public void unsetName()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_attribute(NAME$2);
        }
    }
}
