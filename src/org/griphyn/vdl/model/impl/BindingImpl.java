/*
 * XML Type:  Binding
 * Namespace: http://www.griphyn.org/2006/08/vdl
 * Java type: org.griphyn.vdl.model.Binding
 *
 * Automatically generated - do not modify.
 */
package org.griphyn.vdl.model.impl;
/**
 * An XML Binding(@http://www.griphyn.org/2006/08/vdl).
 *
 * This is a complex type.
 */
public class BindingImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements org.griphyn.vdl.model.Binding
{
    
    public BindingImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName APPLICATION$0 = 
        new javax.xml.namespace.QName("http://www.griphyn.org/2006/08/vdl", "application");
    private static final javax.xml.namespace.QName SERVICE$2 = 
        new javax.xml.namespace.QName("http://www.griphyn.org/2006/08/vdl", "service");
    private static final javax.xml.namespace.QName TYPE$4 = 
        new javax.xml.namespace.QName("", "type");
    
    
    /**
     * Gets the "application" element
     */
    public org.griphyn.vdl.model.ApplicationBinding getApplication()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.griphyn.vdl.model.ApplicationBinding target = null;
            target = (org.griphyn.vdl.model.ApplicationBinding)get_store().find_element_user(APPLICATION$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * True if has "application" element
     */
    public boolean isSetApplication()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(APPLICATION$0) != 0;
        }
    }
    
    /**
     * Sets the "application" element
     */
    public void setApplication(org.griphyn.vdl.model.ApplicationBinding application)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.griphyn.vdl.model.ApplicationBinding target = null;
            target = (org.griphyn.vdl.model.ApplicationBinding)get_store().find_element_user(APPLICATION$0, 0);
            if (target == null)
            {
                target = (org.griphyn.vdl.model.ApplicationBinding)get_store().add_element_user(APPLICATION$0);
            }
            target.set(application);
        }
    }
    
    /**
     * Appends and returns a new empty "application" element
     */
    public org.griphyn.vdl.model.ApplicationBinding addNewApplication()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.griphyn.vdl.model.ApplicationBinding target = null;
            target = (org.griphyn.vdl.model.ApplicationBinding)get_store().add_element_user(APPLICATION$0);
            return target;
        }
    }
    
    /**
     * Unsets the "application" element
     */
    public void unsetApplication()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(APPLICATION$0, 0);
        }
    }
    
    /**
     * Gets the "service" element
     */
    public org.griphyn.vdl.model.ServiceBinding getService()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.griphyn.vdl.model.ServiceBinding target = null;
            target = (org.griphyn.vdl.model.ServiceBinding)get_store().find_element_user(SERVICE$2, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * True if has "service" element
     */
    public boolean isSetService()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(SERVICE$2) != 0;
        }
    }
    
    /**
     * Sets the "service" element
     */
    public void setService(org.griphyn.vdl.model.ServiceBinding service)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.griphyn.vdl.model.ServiceBinding target = null;
            target = (org.griphyn.vdl.model.ServiceBinding)get_store().find_element_user(SERVICE$2, 0);
            if (target == null)
            {
                target = (org.griphyn.vdl.model.ServiceBinding)get_store().add_element_user(SERVICE$2);
            }
            target.set(service);
        }
    }
    
    /**
     * Appends and returns a new empty "service" element
     */
    public org.griphyn.vdl.model.ServiceBinding addNewService()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.griphyn.vdl.model.ServiceBinding target = null;
            target = (org.griphyn.vdl.model.ServiceBinding)get_store().add_element_user(SERVICE$2);
            return target;
        }
    }
    
    /**
     * Unsets the "service" element
     */
    public void unsetService()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(SERVICE$2, 0);
        }
    }
    
    /**
     * Gets the "type" attribute
     */
    public java.lang.String getType()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(TYPE$4);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "type" attribute
     */
    public org.apache.xmlbeans.XmlString xgetType()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_attribute_user(TYPE$4);
            return target;
        }
    }
    
    /**
     * True if has "type" attribute
     */
    public boolean isSetType()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().find_attribute_user(TYPE$4) != null;
        }
    }
    
    /**
     * Sets the "type" attribute
     */
    public void setType(java.lang.String type)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(TYPE$4);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_attribute_user(TYPE$4);
            }
            target.setStringValue(type);
        }
    }
    
    /**
     * Sets (as xml) the "type" attribute
     */
    public void xsetType(org.apache.xmlbeans.XmlString type)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_attribute_user(TYPE$4);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlString)get_store().add_attribute_user(TYPE$4);
            }
            target.set(type);
        }
    }
    
    /**
     * Unsets the "type" attribute
     */
    public void unsetType()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_attribute(TYPE$4);
        }
    }
}
