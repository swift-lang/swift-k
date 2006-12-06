/*
 * XML Type:  Variable
 * Namespace: http://www.griphyn.org/2006/08/vdl
 * Java type: org.griphyn.vdl.model.Variable
 *
 * Automatically generated - do not modify.
 */
package org.griphyn.vdl.model.impl;
/**
 * An XML Variable(@http://www.griphyn.org/2006/08/vdl).
 *
 * This is a complex type.
 */
public class VariableImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements org.griphyn.vdl.model.Variable
{
    
    public VariableImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName ARRAY$0 = 
        new javax.xml.namespace.QName("http://www.griphyn.org/2006/08/vdl", "array");
    private static final javax.xml.namespace.QName RANGE$2 = 
        new javax.xml.namespace.QName("http://www.griphyn.org/2006/08/vdl", "range");
    private static final javax.xml.namespace.QName FUNCTION$4 = 
        new javax.xml.namespace.QName("http://www.griphyn.org/2006/08/vdl", "function");
    private static final javax.xml.namespace.QName NAME$6 = 
        new javax.xml.namespace.QName("", "name");
    private static final javax.xml.namespace.QName TYPE$8 = 
        new javax.xml.namespace.QName("", "type");
    private static final javax.xml.namespace.QName ISARRAY1$10 = 
        new javax.xml.namespace.QName("", "isArray");
    
    
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
     * Gets the "name" attribute
     */
    public java.lang.String getName()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(NAME$6);
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
    public org.apache.xmlbeans.XmlNCName xgetName()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlNCName target = null;
            target = (org.apache.xmlbeans.XmlNCName)get_store().find_attribute_user(NAME$6);
            return target;
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
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(NAME$6);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_attribute_user(NAME$6);
            }
            target.setStringValue(name);
        }
    }
    
    /**
     * Sets (as xml) the "name" attribute
     */
    public void xsetName(org.apache.xmlbeans.XmlNCName name)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlNCName target = null;
            target = (org.apache.xmlbeans.XmlNCName)get_store().find_attribute_user(NAME$6);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlNCName)get_store().add_attribute_user(NAME$6);
            }
            target.set(name);
        }
    }
    
    /**
     * Gets the "type" attribute
     */
    public javax.xml.namespace.QName getType()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(TYPE$8);
            if (target == null)
            {
                return null;
            }
            return target.getQNameValue();
        }
    }
    
    /**
     * Gets (as xml) the "type" attribute
     */
    public org.apache.xmlbeans.XmlQName xgetType()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlQName target = null;
            target = (org.apache.xmlbeans.XmlQName)get_store().find_attribute_user(TYPE$8);
            return target;
        }
    }
    
    /**
     * Sets the "type" attribute
     */
    public void setType(javax.xml.namespace.QName type)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(TYPE$8);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_attribute_user(TYPE$8);
            }
            target.setQNameValue(type);
        }
    }
    
    /**
     * Sets (as xml) the "type" attribute
     */
    public void xsetType(org.apache.xmlbeans.XmlQName type)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlQName target = null;
            target = (org.apache.xmlbeans.XmlQName)get_store().find_attribute_user(TYPE$8);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlQName)get_store().add_attribute_user(TYPE$8);
            }
            target.set(type);
        }
    }
    
    /**
     * Gets the "isArray" attribute
     */
    public boolean getIsArray1()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(ISARRAY1$10);
            if (target == null)
            {
                return false;
            }
            return target.getBooleanValue();
        }
    }
    
    /**
     * Gets (as xml) the "isArray" attribute
     */
    public org.apache.xmlbeans.XmlBoolean xgetIsArray1()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlBoolean target = null;
            target = (org.apache.xmlbeans.XmlBoolean)get_store().find_attribute_user(ISARRAY1$10);
            return target;
        }
    }
    
    /**
     * True if has "isArray" attribute
     */
    public boolean isSetIsArray1()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().find_attribute_user(ISARRAY1$10) != null;
        }
    }
    
    /**
     * Sets the "isArray" attribute
     */
    public void setIsArray1(boolean isArray1)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(ISARRAY1$10);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_attribute_user(ISARRAY1$10);
            }
            target.setBooleanValue(isArray1);
        }
    }
    
    /**
     * Sets (as xml) the "isArray" attribute
     */
    public void xsetIsArray1(org.apache.xmlbeans.XmlBoolean isArray1)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlBoolean target = null;
            target = (org.apache.xmlbeans.XmlBoolean)get_store().find_attribute_user(ISARRAY1$10);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlBoolean)get_store().add_attribute_user(ISARRAY1$10);
            }
            target.set(isArray1);
        }
    }
    
    /**
     * Unsets the "isArray" attribute
     */
    public void unsetIsArray1()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_attribute(ISARRAY1$10);
        }
    }
}
