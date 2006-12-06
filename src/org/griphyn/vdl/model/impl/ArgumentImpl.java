/*
 * XML Type:  Argument
 * Namespace: http://www.griphyn.org/2006/08/vdl
 * Java type: org.griphyn.vdl.model.Argument
 *
 * Automatically generated - do not modify.
 */
package org.griphyn.vdl.model.impl;
/**
 * An XML Argument(@http://www.griphyn.org/2006/08/vdl).
 *
 * This is a complex type.
 */
public class ArgumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements org.griphyn.vdl.model.Argument
{
    
    public ArgumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName FUNCTION$0 = 
        new javax.xml.namespace.QName("http://www.griphyn.org/2006/08/vdl", "function");
    
    
    /**
     * Gets array of all "function" elements
     */
    public org.griphyn.vdl.model.Function[] getFunctionArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            java.util.List targetList = new java.util.ArrayList();
            get_store().find_all_element_users(FUNCTION$0, targetList);
            org.griphyn.vdl.model.Function[] result = new org.griphyn.vdl.model.Function[targetList.size()];
            targetList.toArray(result);
            return result;
        }
    }
    
    /**
     * Gets ith "function" element
     */
    public org.griphyn.vdl.model.Function getFunctionArray(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.griphyn.vdl.model.Function target = null;
            target = (org.griphyn.vdl.model.Function)get_store().find_element_user(FUNCTION$0, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            return target;
        }
    }
    
    /**
     * Returns number of "function" element
     */
    public int sizeOfFunctionArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(FUNCTION$0);
        }
    }
    
    /**
     * Sets array of all "function" element
     */
    public void setFunctionArray(org.griphyn.vdl.model.Function[] functionArray)
    {
        synchronized (monitor())
        {
            check_orphaned();
            arraySetterHelper(functionArray, FUNCTION$0);
        }
    }
    
    /**
     * Sets ith "function" element
     */
    public void setFunctionArray(int i, org.griphyn.vdl.model.Function function)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.griphyn.vdl.model.Function target = null;
            target = (org.griphyn.vdl.model.Function)get_store().find_element_user(FUNCTION$0, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            target.set(function);
        }
    }
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "function" element
     */
    public org.griphyn.vdl.model.Function insertNewFunction(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.griphyn.vdl.model.Function target = null;
            target = (org.griphyn.vdl.model.Function)get_store().insert_element_user(FUNCTION$0, i);
            return target;
        }
    }
    
    /**
     * Appends and returns a new empty value (as xml) as the last "function" element
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
     * Removes the ith "function" element
     */
    public void removeFunction(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(FUNCTION$0, i);
        }
    }
}
