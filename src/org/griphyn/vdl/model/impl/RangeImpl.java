/*
 * XML Type:  Range
 * Namespace: http://www.griphyn.org/2006/08/vdl
 * Java type: org.griphyn.vdl.model.Range
 *
 * Automatically generated - do not modify.
 */
package org.griphyn.vdl.model.impl;
/**
 * An XML Range(@http://www.griphyn.org/2006/08/vdl).
 *
 * This is a complex type.
 */
public class RangeImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements org.griphyn.vdl.model.Range
{
    
    public RangeImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName FROM$0 = 
        new javax.xml.namespace.QName("", "from");
    private static final javax.xml.namespace.QName TO$2 = 
        new javax.xml.namespace.QName("", "to");
    private static final javax.xml.namespace.QName STEP$4 = 
        new javax.xml.namespace.QName("", "step");
    
    
    /**
     * Gets the "from" attribute
     */
    public org.apache.xmlbeans.XmlAnySimpleType getFrom()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlAnySimpleType target = null;
            target = (org.apache.xmlbeans.XmlAnySimpleType)get_store().find_attribute_user(FROM$0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "from" attribute
     */
    public void setFrom(org.apache.xmlbeans.XmlAnySimpleType from)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlAnySimpleType target = null;
            target = (org.apache.xmlbeans.XmlAnySimpleType)get_store().find_attribute_user(FROM$0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlAnySimpleType)get_store().add_attribute_user(FROM$0);
            }
            target.set(from);
        }
    }
    
    /**
     * Appends and returns a new empty "from" attribute
     */
    public org.apache.xmlbeans.XmlAnySimpleType addNewFrom()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlAnySimpleType target = null;
            target = (org.apache.xmlbeans.XmlAnySimpleType)get_store().add_attribute_user(FROM$0);
            return target;
        }
    }
    
    /**
     * Gets the "to" attribute
     */
    public org.apache.xmlbeans.XmlAnySimpleType getTo()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlAnySimpleType target = null;
            target = (org.apache.xmlbeans.XmlAnySimpleType)get_store().find_attribute_user(TO$2);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "to" attribute
     */
    public void setTo(org.apache.xmlbeans.XmlAnySimpleType to)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlAnySimpleType target = null;
            target = (org.apache.xmlbeans.XmlAnySimpleType)get_store().find_attribute_user(TO$2);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlAnySimpleType)get_store().add_attribute_user(TO$2);
            }
            target.set(to);
        }
    }
    
    /**
     * Appends and returns a new empty "to" attribute
     */
    public org.apache.xmlbeans.XmlAnySimpleType addNewTo()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlAnySimpleType target = null;
            target = (org.apache.xmlbeans.XmlAnySimpleType)get_store().add_attribute_user(TO$2);
            return target;
        }
    }
    
    /**
     * Gets the "step" attribute
     */
    public org.apache.xmlbeans.XmlAnySimpleType getStep()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlAnySimpleType target = null;
            target = (org.apache.xmlbeans.XmlAnySimpleType)get_store().find_attribute_user(STEP$4);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * True if has "step" attribute
     */
    public boolean isSetStep()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().find_attribute_user(STEP$4) != null;
        }
    }
    
    /**
     * Sets the "step" attribute
     */
    public void setStep(org.apache.xmlbeans.XmlAnySimpleType step)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlAnySimpleType target = null;
            target = (org.apache.xmlbeans.XmlAnySimpleType)get_store().find_attribute_user(STEP$4);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlAnySimpleType)get_store().add_attribute_user(STEP$4);
            }
            target.set(step);
        }
    }
    
    /**
     * Appends and returns a new empty "step" attribute
     */
    public org.apache.xmlbeans.XmlAnySimpleType addNewStep()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlAnySimpleType target = null;
            target = (org.apache.xmlbeans.XmlAnySimpleType)get_store().add_attribute_user(STEP$4);
            return target;
        }
    }
    
    /**
     * Unsets the "step" attribute
     */
    public void unsetStep()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_attribute(STEP$4);
        }
    }
}
