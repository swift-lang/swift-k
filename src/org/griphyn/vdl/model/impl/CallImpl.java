/*
 * XML Type:  Call
 * Namespace: http://www.griphyn.org/2006/08/vdl
 * Java type: org.griphyn.vdl.model.Call
 *
 * Automatically generated - do not modify.
 */
package org.griphyn.vdl.model.impl;
/**
 * An XML Call(@http://www.griphyn.org/2006/08/vdl).
 *
 * This is a complex type.
 */
public class CallImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements org.griphyn.vdl.model.Call
{
    
    public CallImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName OUTPUT$0 = 
        new javax.xml.namespace.QName("http://www.griphyn.org/2006/08/vdl", "output");
    private static final javax.xml.namespace.QName INPUT$2 = 
        new javax.xml.namespace.QName("http://www.griphyn.org/2006/08/vdl", "input");
    private static final javax.xml.namespace.QName ID$4 = 
        new javax.xml.namespace.QName("", "id");
    private static final javax.xml.namespace.QName PROC$6 = 
        new javax.xml.namespace.QName("", "proc");
    
    
    /**
     * Gets array of all "output" elements
     */
    public org.griphyn.vdl.model.ActualParameter[] getOutputArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            java.util.List targetList = new java.util.ArrayList();
            get_store().find_all_element_users(OUTPUT$0, targetList);
            org.griphyn.vdl.model.ActualParameter[] result = new org.griphyn.vdl.model.ActualParameter[targetList.size()];
            targetList.toArray(result);
            return result;
        }
    }
    
    /**
     * Gets ith "output" element
     */
    public org.griphyn.vdl.model.ActualParameter getOutputArray(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.griphyn.vdl.model.ActualParameter target = null;
            target = (org.griphyn.vdl.model.ActualParameter)get_store().find_element_user(OUTPUT$0, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            return target;
        }
    }
    
    /**
     * Returns number of "output" element
     */
    public int sizeOfOutputArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(OUTPUT$0);
        }
    }
    
    /**
     * Sets array of all "output" element
     */
    public void setOutputArray(org.griphyn.vdl.model.ActualParameter[] outputArray)
    {
        synchronized (monitor())
        {
            check_orphaned();
            arraySetterHelper(outputArray, OUTPUT$0);
        }
    }
    
    /**
     * Sets ith "output" element
     */
    public void setOutputArray(int i, org.griphyn.vdl.model.ActualParameter output)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.griphyn.vdl.model.ActualParameter target = null;
            target = (org.griphyn.vdl.model.ActualParameter)get_store().find_element_user(OUTPUT$0, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            target.set(output);
        }
    }
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "output" element
     */
    public org.griphyn.vdl.model.ActualParameter insertNewOutput(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.griphyn.vdl.model.ActualParameter target = null;
            target = (org.griphyn.vdl.model.ActualParameter)get_store().insert_element_user(OUTPUT$0, i);
            return target;
        }
    }
    
    /**
     * Appends and returns a new empty value (as xml) as the last "output" element
     */
    public org.griphyn.vdl.model.ActualParameter addNewOutput()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.griphyn.vdl.model.ActualParameter target = null;
            target = (org.griphyn.vdl.model.ActualParameter)get_store().add_element_user(OUTPUT$0);
            return target;
        }
    }
    
    /**
     * Removes the ith "output" element
     */
    public void removeOutput(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(OUTPUT$0, i);
        }
    }
    
    /**
     * Gets array of all "input" elements
     */
    public org.griphyn.vdl.model.ActualParameter[] getInputArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            java.util.List targetList = new java.util.ArrayList();
            get_store().find_all_element_users(INPUT$2, targetList);
            org.griphyn.vdl.model.ActualParameter[] result = new org.griphyn.vdl.model.ActualParameter[targetList.size()];
            targetList.toArray(result);
            return result;
        }
    }
    
    /**
     * Gets ith "input" element
     */
    public org.griphyn.vdl.model.ActualParameter getInputArray(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.griphyn.vdl.model.ActualParameter target = null;
            target = (org.griphyn.vdl.model.ActualParameter)get_store().find_element_user(INPUT$2, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            return target;
        }
    }
    
    /**
     * Returns number of "input" element
     */
    public int sizeOfInputArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(INPUT$2);
        }
    }
    
    /**
     * Sets array of all "input" element
     */
    public void setInputArray(org.griphyn.vdl.model.ActualParameter[] inputArray)
    {
        synchronized (monitor())
        {
            check_orphaned();
            arraySetterHelper(inputArray, INPUT$2);
        }
    }
    
    /**
     * Sets ith "input" element
     */
    public void setInputArray(int i, org.griphyn.vdl.model.ActualParameter input)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.griphyn.vdl.model.ActualParameter target = null;
            target = (org.griphyn.vdl.model.ActualParameter)get_store().find_element_user(INPUT$2, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            target.set(input);
        }
    }
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "input" element
     */
    public org.griphyn.vdl.model.ActualParameter insertNewInput(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.griphyn.vdl.model.ActualParameter target = null;
            target = (org.griphyn.vdl.model.ActualParameter)get_store().insert_element_user(INPUT$2, i);
            return target;
        }
    }
    
    /**
     * Appends and returns a new empty value (as xml) as the last "input" element
     */
    public org.griphyn.vdl.model.ActualParameter addNewInput()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.griphyn.vdl.model.ActualParameter target = null;
            target = (org.griphyn.vdl.model.ActualParameter)get_store().add_element_user(INPUT$2);
            return target;
        }
    }
    
    /**
     * Removes the ith "input" element
     */
    public void removeInput(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(INPUT$2, i);
        }
    }
    
    /**
     * Gets the "id" attribute
     */
    public java.lang.String getId()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(ID$4);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "id" attribute
     */
    public org.apache.xmlbeans.XmlID xgetId()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlID target = null;
            target = (org.apache.xmlbeans.XmlID)get_store().find_attribute_user(ID$4);
            return target;
        }
    }
    
    /**
     * True if has "id" attribute
     */
    public boolean isSetId()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().find_attribute_user(ID$4) != null;
        }
    }
    
    /**
     * Sets the "id" attribute
     */
    public void setId(java.lang.String id)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(ID$4);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_attribute_user(ID$4);
            }
            target.setStringValue(id);
        }
    }
    
    /**
     * Sets (as xml) the "id" attribute
     */
    public void xsetId(org.apache.xmlbeans.XmlID id)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlID target = null;
            target = (org.apache.xmlbeans.XmlID)get_store().find_attribute_user(ID$4);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlID)get_store().add_attribute_user(ID$4);
            }
            target.set(id);
        }
    }
    
    /**
     * Unsets the "id" attribute
     */
    public void unsetId()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_attribute(ID$4);
        }
    }
    
    /**
     * Gets the "proc" attribute
     */
    public javax.xml.namespace.QName getProc()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(PROC$6);
            if (target == null)
            {
                return null;
            }
            return target.getQNameValue();
        }
    }
    
    /**
     * Gets (as xml) the "proc" attribute
     */
    public org.apache.xmlbeans.XmlQName xgetProc()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlQName target = null;
            target = (org.apache.xmlbeans.XmlQName)get_store().find_attribute_user(PROC$6);
            return target;
        }
    }
    
    /**
     * Sets the "proc" attribute
     */
    public void setProc(javax.xml.namespace.QName proc)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(PROC$6);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_attribute_user(PROC$6);
            }
            target.setQNameValue(proc);
        }
    }
    
    /**
     * Sets (as xml) the "proc" attribute
     */
    public void xsetProc(org.apache.xmlbeans.XmlQName proc)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlQName target = null;
            target = (org.apache.xmlbeans.XmlQName)get_store().find_attribute_user(PROC$6);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlQName)get_store().add_attribute_user(PROC$6);
            }
            target.set(proc);
        }
    }
}
