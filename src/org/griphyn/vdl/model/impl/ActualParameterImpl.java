/*
 * XML Type:  ActualParameter
 * Namespace: http://www.griphyn.org/2006/08/vdl
 * Java type: org.griphyn.vdl.model.ActualParameter
 *
 * Automatically generated - do not modify.
 */
package org.griphyn.vdl.model.impl;
/**
 * An XML ActualParameter(@http://www.griphyn.org/2006/08/vdl).
 *
 * This is a complex type.
 */
public class ActualParameterImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements org.griphyn.vdl.model.ActualParameter
{
    
    public ActualParameterImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName ARRAY$0 = 
        new javax.xml.namespace.QName("http://www.griphyn.org/2006/08/vdl", "array");
    private static final javax.xml.namespace.QName RANGE$2 = 
        new javax.xml.namespace.QName("http://www.griphyn.org/2006/08/vdl", "range");
    private static final javax.xml.namespace.QName BIND$4 = 
        new javax.xml.namespace.QName("", "bind");
    
    
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
     * Gets the "bind" attribute
     */
    public java.lang.String getBind()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(BIND$4);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "bind" attribute
     */
    public org.apache.xmlbeans.XmlNMTOKEN xgetBind()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlNMTOKEN target = null;
            target = (org.apache.xmlbeans.XmlNMTOKEN)get_store().find_attribute_user(BIND$4);
            return target;
        }
    }
    
    /**
     * True if has "bind" attribute
     */
    public boolean isSetBind()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().find_attribute_user(BIND$4) != null;
        }
    }
    
    /**
     * Sets the "bind" attribute
     */
    public void setBind(java.lang.String bind)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(BIND$4);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_attribute_user(BIND$4);
            }
            target.setStringValue(bind);
        }
    }
    
    /**
     * Sets (as xml) the "bind" attribute
     */
    public void xsetBind(org.apache.xmlbeans.XmlNMTOKEN bind)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlNMTOKEN target = null;
            target = (org.apache.xmlbeans.XmlNMTOKEN)get_store().find_attribute_user(BIND$4);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlNMTOKEN)get_store().add_attribute_user(BIND$4);
            }
            target.set(bind);
        }
    }
    
    /**
     * Unsets the "bind" attribute
     */
    public void unsetBind()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_attribute(BIND$4);
        }
    }
}
