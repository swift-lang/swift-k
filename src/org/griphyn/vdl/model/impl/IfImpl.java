/*
 * XML Type:  If
 * Namespace: http://www.griphyn.org/2006/08/vdl
 * Java type: org.griphyn.vdl.model.If
 *
 * Automatically generated - do not modify.
 */
package org.griphyn.vdl.model.impl;
/**
 * An XML If(@http://www.griphyn.org/2006/08/vdl).
 *
 * This is a complex type.
 */
public class IfImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements org.griphyn.vdl.model.If
{
    
    public IfImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName THEN$0 = 
        new javax.xml.namespace.QName("http://www.griphyn.org/2006/08/vdl", "then");
    private static final javax.xml.namespace.QName ELSE$2 = 
        new javax.xml.namespace.QName("http://www.griphyn.org/2006/08/vdl", "else");
    private static final javax.xml.namespace.QName TEST$4 = 
        new javax.xml.namespace.QName("", "test");
    
    
    /**
     * Gets the "then" element
     */
    public org.griphyn.vdl.model.If.Then getThen()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.griphyn.vdl.model.If.Then target = null;
            target = (org.griphyn.vdl.model.If.Then)get_store().find_element_user(THEN$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * True if has "then" element
     */
    public boolean isSetThen()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(THEN$0) != 0;
        }
    }
    
    /**
     * Sets the "then" element
     */
    public void setThen(org.griphyn.vdl.model.If.Then then)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.griphyn.vdl.model.If.Then target = null;
            target = (org.griphyn.vdl.model.If.Then)get_store().find_element_user(THEN$0, 0);
            if (target == null)
            {
                target = (org.griphyn.vdl.model.If.Then)get_store().add_element_user(THEN$0);
            }
            target.set(then);
        }
    }
    
    /**
     * Appends and returns a new empty "then" element
     */
    public org.griphyn.vdl.model.If.Then addNewThen()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.griphyn.vdl.model.If.Then target = null;
            target = (org.griphyn.vdl.model.If.Then)get_store().add_element_user(THEN$0);
            return target;
        }
    }
    
    /**
     * Unsets the "then" element
     */
    public void unsetThen()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(THEN$0, 0);
        }
    }
    
    /**
     * Gets the "else" element
     */
    public org.griphyn.vdl.model.If.Else getElse()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.griphyn.vdl.model.If.Else target = null;
            target = (org.griphyn.vdl.model.If.Else)get_store().find_element_user(ELSE$2, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * True if has "else" element
     */
    public boolean isSetElse()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(ELSE$2) != 0;
        }
    }
    
    /**
     * Sets the "else" element
     */
    public void setElse(org.griphyn.vdl.model.If.Else xelse)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.griphyn.vdl.model.If.Else target = null;
            target = (org.griphyn.vdl.model.If.Else)get_store().find_element_user(ELSE$2, 0);
            if (target == null)
            {
                target = (org.griphyn.vdl.model.If.Else)get_store().add_element_user(ELSE$2);
            }
            target.set(xelse);
        }
    }
    
    /**
     * Appends and returns a new empty "else" element
     */
    public org.griphyn.vdl.model.If.Else addNewElse()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.griphyn.vdl.model.If.Else target = null;
            target = (org.griphyn.vdl.model.If.Else)get_store().add_element_user(ELSE$2);
            return target;
        }
    }
    
    /**
     * Unsets the "else" element
     */
    public void unsetElse()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(ELSE$2, 0);
        }
    }
    
    /**
     * Gets the "test" attribute
     */
    public java.lang.String getTest()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(TEST$4);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "test" attribute
     */
    public org.apache.xmlbeans.XmlString xgetTest()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_attribute_user(TEST$4);
            return target;
        }
    }
    
    /**
     * True if has "test" attribute
     */
    public boolean isSetTest()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().find_attribute_user(TEST$4) != null;
        }
    }
    
    /**
     * Sets the "test" attribute
     */
    public void setTest(java.lang.String test)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(TEST$4);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_attribute_user(TEST$4);
            }
            target.setStringValue(test);
        }
    }
    
    /**
     * Sets (as xml) the "test" attribute
     */
    public void xsetTest(org.apache.xmlbeans.XmlString test)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_attribute_user(TEST$4);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlString)get_store().add_attribute_user(TEST$4);
            }
            target.set(test);
        }
    }
    
    /**
     * Unsets the "test" attribute
     */
    public void unsetTest()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_attribute(TEST$4);
        }
    }
    /**
     * An XML then(@http://www.griphyn.org/2006/08/vdl).
     *
     * This is a complex type.
     */
    public static class ThenImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements org.griphyn.vdl.model.If.Then
    {
        
        public ThenImpl(org.apache.xmlbeans.SchemaType sType)
        {
            super(sType);
        }
        
        private static final javax.xml.namespace.QName VARIABLE$0 = 
            new javax.xml.namespace.QName("http://www.griphyn.org/2006/08/vdl", "variable");
        private static final javax.xml.namespace.QName DATASET$2 = 
            new javax.xml.namespace.QName("http://www.griphyn.org/2006/08/vdl", "dataset");
        private static final javax.xml.namespace.QName ASSIGN$4 = 
            new javax.xml.namespace.QName("http://www.griphyn.org/2006/08/vdl", "assign");
        private static final javax.xml.namespace.QName CALL$6 = 
            new javax.xml.namespace.QName("http://www.griphyn.org/2006/08/vdl", "call");
        private static final javax.xml.namespace.QName FOREACH$8 = 
            new javax.xml.namespace.QName("http://www.griphyn.org/2006/08/vdl", "foreach");
        private static final javax.xml.namespace.QName IF$10 = 
            new javax.xml.namespace.QName("http://www.griphyn.org/2006/08/vdl", "if");
        private static final javax.xml.namespace.QName WHILE$12 = 
            new javax.xml.namespace.QName("http://www.griphyn.org/2006/08/vdl", "while");
        private static final javax.xml.namespace.QName REPEAT$14 = 
            new javax.xml.namespace.QName("http://www.griphyn.org/2006/08/vdl", "repeat");
        private static final javax.xml.namespace.QName SWITCH$16 = 
            new javax.xml.namespace.QName("http://www.griphyn.org/2006/08/vdl", "switch");
        private static final javax.xml.namespace.QName CONTINUE$18 = 
            new javax.xml.namespace.QName("http://www.griphyn.org/2006/08/vdl", "continue");
        private static final javax.xml.namespace.QName BREAK$20 = 
            new javax.xml.namespace.QName("http://www.griphyn.org/2006/08/vdl", "break");
        
        
        /**
         * Gets array of all "variable" elements
         */
        public org.griphyn.vdl.model.Variable[] getVariableArray()
        {
            synchronized (monitor())
            {
                check_orphaned();
                java.util.List targetList = new java.util.ArrayList();
                get_store().find_all_element_users(VARIABLE$0, targetList);
                org.griphyn.vdl.model.Variable[] result = new org.griphyn.vdl.model.Variable[targetList.size()];
                targetList.toArray(result);
                return result;
            }
        }
        
        /**
         * Gets ith "variable" element
         */
        public org.griphyn.vdl.model.Variable getVariableArray(int i)
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.griphyn.vdl.model.Variable target = null;
                target = (org.griphyn.vdl.model.Variable)get_store().find_element_user(VARIABLE$0, i);
                if (target == null)
                {
                    throw new IndexOutOfBoundsException();
                }
                return target;
            }
        }
        
        /**
         * Returns number of "variable" element
         */
        public int sizeOfVariableArray()
        {
            synchronized (monitor())
            {
                check_orphaned();
                return get_store().count_elements(VARIABLE$0);
            }
        }
        
        /**
         * Sets array of all "variable" element
         */
        public void setVariableArray(org.griphyn.vdl.model.Variable[] variableArray)
        {
            synchronized (monitor())
            {
                check_orphaned();
                arraySetterHelper(variableArray, VARIABLE$0);
            }
        }
        
        /**
         * Sets ith "variable" element
         */
        public void setVariableArray(int i, org.griphyn.vdl.model.Variable variable)
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.griphyn.vdl.model.Variable target = null;
                target = (org.griphyn.vdl.model.Variable)get_store().find_element_user(VARIABLE$0, i);
                if (target == null)
                {
                    throw new IndexOutOfBoundsException();
                }
                target.set(variable);
            }
        }
        
        /**
         * Inserts and returns a new empty value (as xml) as the ith "variable" element
         */
        public org.griphyn.vdl.model.Variable insertNewVariable(int i)
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.griphyn.vdl.model.Variable target = null;
                target = (org.griphyn.vdl.model.Variable)get_store().insert_element_user(VARIABLE$0, i);
                return target;
            }
        }
        
        /**
         * Appends and returns a new empty value (as xml) as the last "variable" element
         */
        public org.griphyn.vdl.model.Variable addNewVariable()
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.griphyn.vdl.model.Variable target = null;
                target = (org.griphyn.vdl.model.Variable)get_store().add_element_user(VARIABLE$0);
                return target;
            }
        }
        
        /**
         * Removes the ith "variable" element
         */
        public void removeVariable(int i)
        {
            synchronized (monitor())
            {
                check_orphaned();
                get_store().remove_element(VARIABLE$0, i);
            }
        }
        
        /**
         * Gets array of all "dataset" elements
         */
        public org.griphyn.vdl.model.Dataset[] getDatasetArray()
        {
            synchronized (monitor())
            {
                check_orphaned();
                java.util.List targetList = new java.util.ArrayList();
                get_store().find_all_element_users(DATASET$2, targetList);
                org.griphyn.vdl.model.Dataset[] result = new org.griphyn.vdl.model.Dataset[targetList.size()];
                targetList.toArray(result);
                return result;
            }
        }
        
        /**
         * Gets ith "dataset" element
         */
        public org.griphyn.vdl.model.Dataset getDatasetArray(int i)
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.griphyn.vdl.model.Dataset target = null;
                target = (org.griphyn.vdl.model.Dataset)get_store().find_element_user(DATASET$2, i);
                if (target == null)
                {
                    throw new IndexOutOfBoundsException();
                }
                return target;
            }
        }
        
        /**
         * Returns number of "dataset" element
         */
        public int sizeOfDatasetArray()
        {
            synchronized (monitor())
            {
                check_orphaned();
                return get_store().count_elements(DATASET$2);
            }
        }
        
        /**
         * Sets array of all "dataset" element
         */
        public void setDatasetArray(org.griphyn.vdl.model.Dataset[] datasetArray)
        {
            synchronized (monitor())
            {
                check_orphaned();
                arraySetterHelper(datasetArray, DATASET$2);
            }
        }
        
        /**
         * Sets ith "dataset" element
         */
        public void setDatasetArray(int i, org.griphyn.vdl.model.Dataset dataset)
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.griphyn.vdl.model.Dataset target = null;
                target = (org.griphyn.vdl.model.Dataset)get_store().find_element_user(DATASET$2, i);
                if (target == null)
                {
                    throw new IndexOutOfBoundsException();
                }
                target.set(dataset);
            }
        }
        
        /**
         * Inserts and returns a new empty value (as xml) as the ith "dataset" element
         */
        public org.griphyn.vdl.model.Dataset insertNewDataset(int i)
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.griphyn.vdl.model.Dataset target = null;
                target = (org.griphyn.vdl.model.Dataset)get_store().insert_element_user(DATASET$2, i);
                return target;
            }
        }
        
        /**
         * Appends and returns a new empty value (as xml) as the last "dataset" element
         */
        public org.griphyn.vdl.model.Dataset addNewDataset()
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.griphyn.vdl.model.Dataset target = null;
                target = (org.griphyn.vdl.model.Dataset)get_store().add_element_user(DATASET$2);
                return target;
            }
        }
        
        /**
         * Removes the ith "dataset" element
         */
        public void removeDataset(int i)
        {
            synchronized (monitor())
            {
                check_orphaned();
                get_store().remove_element(DATASET$2, i);
            }
        }
        
        /**
         * Gets array of all "assign" elements
         */
        public org.griphyn.vdl.model.Assign[] getAssignArray()
        {
            synchronized (monitor())
            {
                check_orphaned();
                java.util.List targetList = new java.util.ArrayList();
                get_store().find_all_element_users(ASSIGN$4, targetList);
                org.griphyn.vdl.model.Assign[] result = new org.griphyn.vdl.model.Assign[targetList.size()];
                targetList.toArray(result);
                return result;
            }
        }
        
        /**
         * Gets ith "assign" element
         */
        public org.griphyn.vdl.model.Assign getAssignArray(int i)
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.griphyn.vdl.model.Assign target = null;
                target = (org.griphyn.vdl.model.Assign)get_store().find_element_user(ASSIGN$4, i);
                if (target == null)
                {
                    throw new IndexOutOfBoundsException();
                }
                return target;
            }
        }
        
        /**
         * Returns number of "assign" element
         */
        public int sizeOfAssignArray()
        {
            synchronized (monitor())
            {
                check_orphaned();
                return get_store().count_elements(ASSIGN$4);
            }
        }
        
        /**
         * Sets array of all "assign" element
         */
        public void setAssignArray(org.griphyn.vdl.model.Assign[] assignArray)
        {
            synchronized (monitor())
            {
                check_orphaned();
                arraySetterHelper(assignArray, ASSIGN$4);
            }
        }
        
        /**
         * Sets ith "assign" element
         */
        public void setAssignArray(int i, org.griphyn.vdl.model.Assign assign)
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.griphyn.vdl.model.Assign target = null;
                target = (org.griphyn.vdl.model.Assign)get_store().find_element_user(ASSIGN$4, i);
                if (target == null)
                {
                    throw new IndexOutOfBoundsException();
                }
                target.set(assign);
            }
        }
        
        /**
         * Inserts and returns a new empty value (as xml) as the ith "assign" element
         */
        public org.griphyn.vdl.model.Assign insertNewAssign(int i)
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.griphyn.vdl.model.Assign target = null;
                target = (org.griphyn.vdl.model.Assign)get_store().insert_element_user(ASSIGN$4, i);
                return target;
            }
        }
        
        /**
         * Appends and returns a new empty value (as xml) as the last "assign" element
         */
        public org.griphyn.vdl.model.Assign addNewAssign()
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.griphyn.vdl.model.Assign target = null;
                target = (org.griphyn.vdl.model.Assign)get_store().add_element_user(ASSIGN$4);
                return target;
            }
        }
        
        /**
         * Removes the ith "assign" element
         */
        public void removeAssign(int i)
        {
            synchronized (monitor())
            {
                check_orphaned();
                get_store().remove_element(ASSIGN$4, i);
            }
        }
        
        /**
         * Gets array of all "call" elements
         */
        public org.griphyn.vdl.model.Call[] getCallArray()
        {
            synchronized (monitor())
            {
                check_orphaned();
                java.util.List targetList = new java.util.ArrayList();
                get_store().find_all_element_users(CALL$6, targetList);
                org.griphyn.vdl.model.Call[] result = new org.griphyn.vdl.model.Call[targetList.size()];
                targetList.toArray(result);
                return result;
            }
        }
        
        /**
         * Gets ith "call" element
         */
        public org.griphyn.vdl.model.Call getCallArray(int i)
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.griphyn.vdl.model.Call target = null;
                target = (org.griphyn.vdl.model.Call)get_store().find_element_user(CALL$6, i);
                if (target == null)
                {
                    throw new IndexOutOfBoundsException();
                }
                return target;
            }
        }
        
        /**
         * Returns number of "call" element
         */
        public int sizeOfCallArray()
        {
            synchronized (monitor())
            {
                check_orphaned();
                return get_store().count_elements(CALL$6);
            }
        }
        
        /**
         * Sets array of all "call" element
         */
        public void setCallArray(org.griphyn.vdl.model.Call[] callArray)
        {
            synchronized (monitor())
            {
                check_orphaned();
                arraySetterHelper(callArray, CALL$6);
            }
        }
        
        /**
         * Sets ith "call" element
         */
        public void setCallArray(int i, org.griphyn.vdl.model.Call call)
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.griphyn.vdl.model.Call target = null;
                target = (org.griphyn.vdl.model.Call)get_store().find_element_user(CALL$6, i);
                if (target == null)
                {
                    throw new IndexOutOfBoundsException();
                }
                target.set(call);
            }
        }
        
        /**
         * Inserts and returns a new empty value (as xml) as the ith "call" element
         */
        public org.griphyn.vdl.model.Call insertNewCall(int i)
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.griphyn.vdl.model.Call target = null;
                target = (org.griphyn.vdl.model.Call)get_store().insert_element_user(CALL$6, i);
                return target;
            }
        }
        
        /**
         * Appends and returns a new empty value (as xml) as the last "call" element
         */
        public org.griphyn.vdl.model.Call addNewCall()
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.griphyn.vdl.model.Call target = null;
                target = (org.griphyn.vdl.model.Call)get_store().add_element_user(CALL$6);
                return target;
            }
        }
        
        /**
         * Removes the ith "call" element
         */
        public void removeCall(int i)
        {
            synchronized (monitor())
            {
                check_orphaned();
                get_store().remove_element(CALL$6, i);
            }
        }
        
        /**
         * Gets array of all "foreach" elements
         */
        public org.griphyn.vdl.model.Foreach[] getForeachArray()
        {
            synchronized (monitor())
            {
                check_orphaned();
                java.util.List targetList = new java.util.ArrayList();
                get_store().find_all_element_users(FOREACH$8, targetList);
                org.griphyn.vdl.model.Foreach[] result = new org.griphyn.vdl.model.Foreach[targetList.size()];
                targetList.toArray(result);
                return result;
            }
        }
        
        /**
         * Gets ith "foreach" element
         */
        public org.griphyn.vdl.model.Foreach getForeachArray(int i)
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.griphyn.vdl.model.Foreach target = null;
                target = (org.griphyn.vdl.model.Foreach)get_store().find_element_user(FOREACH$8, i);
                if (target == null)
                {
                    throw new IndexOutOfBoundsException();
                }
                return target;
            }
        }
        
        /**
         * Returns number of "foreach" element
         */
        public int sizeOfForeachArray()
        {
            synchronized (monitor())
            {
                check_orphaned();
                return get_store().count_elements(FOREACH$8);
            }
        }
        
        /**
         * Sets array of all "foreach" element
         */
        public void setForeachArray(org.griphyn.vdl.model.Foreach[] foreachArray)
        {
            synchronized (monitor())
            {
                check_orphaned();
                arraySetterHelper(foreachArray, FOREACH$8);
            }
        }
        
        /**
         * Sets ith "foreach" element
         */
        public void setForeachArray(int i, org.griphyn.vdl.model.Foreach foreach)
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.griphyn.vdl.model.Foreach target = null;
                target = (org.griphyn.vdl.model.Foreach)get_store().find_element_user(FOREACH$8, i);
                if (target == null)
                {
                    throw new IndexOutOfBoundsException();
                }
                target.set(foreach);
            }
        }
        
        /**
         * Inserts and returns a new empty value (as xml) as the ith "foreach" element
         */
        public org.griphyn.vdl.model.Foreach insertNewForeach(int i)
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.griphyn.vdl.model.Foreach target = null;
                target = (org.griphyn.vdl.model.Foreach)get_store().insert_element_user(FOREACH$8, i);
                return target;
            }
        }
        
        /**
         * Appends and returns a new empty value (as xml) as the last "foreach" element
         */
        public org.griphyn.vdl.model.Foreach addNewForeach()
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.griphyn.vdl.model.Foreach target = null;
                target = (org.griphyn.vdl.model.Foreach)get_store().add_element_user(FOREACH$8);
                return target;
            }
        }
        
        /**
         * Removes the ith "foreach" element
         */
        public void removeForeach(int i)
        {
            synchronized (monitor())
            {
                check_orphaned();
                get_store().remove_element(FOREACH$8, i);
            }
        }
        
        /**
         * Gets array of all "if" elements
         */
        public org.griphyn.vdl.model.If[] getIfArray()
        {
            synchronized (monitor())
            {
                check_orphaned();
                java.util.List targetList = new java.util.ArrayList();
                get_store().find_all_element_users(IF$10, targetList);
                org.griphyn.vdl.model.If[] result = new org.griphyn.vdl.model.If[targetList.size()];
                targetList.toArray(result);
                return result;
            }
        }
        
        /**
         * Gets ith "if" element
         */
        public org.griphyn.vdl.model.If getIfArray(int i)
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.griphyn.vdl.model.If target = null;
                target = (org.griphyn.vdl.model.If)get_store().find_element_user(IF$10, i);
                if (target == null)
                {
                    throw new IndexOutOfBoundsException();
                }
                return target;
            }
        }
        
        /**
         * Returns number of "if" element
         */
        public int sizeOfIfArray()
        {
            synchronized (monitor())
            {
                check_orphaned();
                return get_store().count_elements(IF$10);
            }
        }
        
        /**
         * Sets array of all "if" element
         */
        public void setIfArray(org.griphyn.vdl.model.If[] xifArray)
        {
            synchronized (monitor())
            {
                check_orphaned();
                arraySetterHelper(xifArray, IF$10);
            }
        }
        
        /**
         * Sets ith "if" element
         */
        public void setIfArray(int i, org.griphyn.vdl.model.If xif)
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.griphyn.vdl.model.If target = null;
                target = (org.griphyn.vdl.model.If)get_store().find_element_user(IF$10, i);
                if (target == null)
                {
                    throw new IndexOutOfBoundsException();
                }
                target.set(xif);
            }
        }
        
        /**
         * Inserts and returns a new empty value (as xml) as the ith "if" element
         */
        public org.griphyn.vdl.model.If insertNewIf(int i)
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.griphyn.vdl.model.If target = null;
                target = (org.griphyn.vdl.model.If)get_store().insert_element_user(IF$10, i);
                return target;
            }
        }
        
        /**
         * Appends and returns a new empty value (as xml) as the last "if" element
         */
        public org.griphyn.vdl.model.If addNewIf()
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.griphyn.vdl.model.If target = null;
                target = (org.griphyn.vdl.model.If)get_store().add_element_user(IF$10);
                return target;
            }
        }
        
        /**
         * Removes the ith "if" element
         */
        public void removeIf(int i)
        {
            synchronized (monitor())
            {
                check_orphaned();
                get_store().remove_element(IF$10, i);
            }
        }
        
        /**
         * Gets array of all "while" elements
         */
        public org.griphyn.vdl.model.While[] getWhileArray()
        {
            synchronized (monitor())
            {
                check_orphaned();
                java.util.List targetList = new java.util.ArrayList();
                get_store().find_all_element_users(WHILE$12, targetList);
                org.griphyn.vdl.model.While[] result = new org.griphyn.vdl.model.While[targetList.size()];
                targetList.toArray(result);
                return result;
            }
        }
        
        /**
         * Gets ith "while" element
         */
        public org.griphyn.vdl.model.While getWhileArray(int i)
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.griphyn.vdl.model.While target = null;
                target = (org.griphyn.vdl.model.While)get_store().find_element_user(WHILE$12, i);
                if (target == null)
                {
                    throw new IndexOutOfBoundsException();
                }
                return target;
            }
        }
        
        /**
         * Returns number of "while" element
         */
        public int sizeOfWhileArray()
        {
            synchronized (monitor())
            {
                check_orphaned();
                return get_store().count_elements(WHILE$12);
            }
        }
        
        /**
         * Sets array of all "while" element
         */
        public void setWhileArray(org.griphyn.vdl.model.While[] xwhileArray)
        {
            synchronized (monitor())
            {
                check_orphaned();
                arraySetterHelper(xwhileArray, WHILE$12);
            }
        }
        
        /**
         * Sets ith "while" element
         */
        public void setWhileArray(int i, org.griphyn.vdl.model.While xwhile)
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.griphyn.vdl.model.While target = null;
                target = (org.griphyn.vdl.model.While)get_store().find_element_user(WHILE$12, i);
                if (target == null)
                {
                    throw new IndexOutOfBoundsException();
                }
                target.set(xwhile);
            }
        }
        
        /**
         * Inserts and returns a new empty value (as xml) as the ith "while" element
         */
        public org.griphyn.vdl.model.While insertNewWhile(int i)
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.griphyn.vdl.model.While target = null;
                target = (org.griphyn.vdl.model.While)get_store().insert_element_user(WHILE$12, i);
                return target;
            }
        }
        
        /**
         * Appends and returns a new empty value (as xml) as the last "while" element
         */
        public org.griphyn.vdl.model.While addNewWhile()
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.griphyn.vdl.model.While target = null;
                target = (org.griphyn.vdl.model.While)get_store().add_element_user(WHILE$12);
                return target;
            }
        }
        
        /**
         * Removes the ith "while" element
         */
        public void removeWhile(int i)
        {
            synchronized (monitor())
            {
                check_orphaned();
                get_store().remove_element(WHILE$12, i);
            }
        }
        
        /**
         * Gets array of all "repeat" elements
         */
        public org.griphyn.vdl.model.Repeat[] getRepeatArray()
        {
            synchronized (monitor())
            {
                check_orphaned();
                java.util.List targetList = new java.util.ArrayList();
                get_store().find_all_element_users(REPEAT$14, targetList);
                org.griphyn.vdl.model.Repeat[] result = new org.griphyn.vdl.model.Repeat[targetList.size()];
                targetList.toArray(result);
                return result;
            }
        }
        
        /**
         * Gets ith "repeat" element
         */
        public org.griphyn.vdl.model.Repeat getRepeatArray(int i)
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.griphyn.vdl.model.Repeat target = null;
                target = (org.griphyn.vdl.model.Repeat)get_store().find_element_user(REPEAT$14, i);
                if (target == null)
                {
                    throw new IndexOutOfBoundsException();
                }
                return target;
            }
        }
        
        /**
         * Returns number of "repeat" element
         */
        public int sizeOfRepeatArray()
        {
            synchronized (monitor())
            {
                check_orphaned();
                return get_store().count_elements(REPEAT$14);
            }
        }
        
        /**
         * Sets array of all "repeat" element
         */
        public void setRepeatArray(org.griphyn.vdl.model.Repeat[] repeatArray)
        {
            synchronized (monitor())
            {
                check_orphaned();
                arraySetterHelper(repeatArray, REPEAT$14);
            }
        }
        
        /**
         * Sets ith "repeat" element
         */
        public void setRepeatArray(int i, org.griphyn.vdl.model.Repeat repeat)
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.griphyn.vdl.model.Repeat target = null;
                target = (org.griphyn.vdl.model.Repeat)get_store().find_element_user(REPEAT$14, i);
                if (target == null)
                {
                    throw new IndexOutOfBoundsException();
                }
                target.set(repeat);
            }
        }
        
        /**
         * Inserts and returns a new empty value (as xml) as the ith "repeat" element
         */
        public org.griphyn.vdl.model.Repeat insertNewRepeat(int i)
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.griphyn.vdl.model.Repeat target = null;
                target = (org.griphyn.vdl.model.Repeat)get_store().insert_element_user(REPEAT$14, i);
                return target;
            }
        }
        
        /**
         * Appends and returns a new empty value (as xml) as the last "repeat" element
         */
        public org.griphyn.vdl.model.Repeat addNewRepeat()
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.griphyn.vdl.model.Repeat target = null;
                target = (org.griphyn.vdl.model.Repeat)get_store().add_element_user(REPEAT$14);
                return target;
            }
        }
        
        /**
         * Removes the ith "repeat" element
         */
        public void removeRepeat(int i)
        {
            synchronized (monitor())
            {
                check_orphaned();
                get_store().remove_element(REPEAT$14, i);
            }
        }
        
        /**
         * Gets array of all "switch" elements
         */
        public org.griphyn.vdl.model.Switch[] getSwitchArray()
        {
            synchronized (monitor())
            {
                check_orphaned();
                java.util.List targetList = new java.util.ArrayList();
                get_store().find_all_element_users(SWITCH$16, targetList);
                org.griphyn.vdl.model.Switch[] result = new org.griphyn.vdl.model.Switch[targetList.size()];
                targetList.toArray(result);
                return result;
            }
        }
        
        /**
         * Gets ith "switch" element
         */
        public org.griphyn.vdl.model.Switch getSwitchArray(int i)
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.griphyn.vdl.model.Switch target = null;
                target = (org.griphyn.vdl.model.Switch)get_store().find_element_user(SWITCH$16, i);
                if (target == null)
                {
                    throw new IndexOutOfBoundsException();
                }
                return target;
            }
        }
        
        /**
         * Returns number of "switch" element
         */
        public int sizeOfSwitchArray()
        {
            synchronized (monitor())
            {
                check_orphaned();
                return get_store().count_elements(SWITCH$16);
            }
        }
        
        /**
         * Sets array of all "switch" element
         */
        public void setSwitchArray(org.griphyn.vdl.model.Switch[] xswitchArray)
        {
            synchronized (monitor())
            {
                check_orphaned();
                arraySetterHelper(xswitchArray, SWITCH$16);
            }
        }
        
        /**
         * Sets ith "switch" element
         */
        public void setSwitchArray(int i, org.griphyn.vdl.model.Switch xswitch)
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.griphyn.vdl.model.Switch target = null;
                target = (org.griphyn.vdl.model.Switch)get_store().find_element_user(SWITCH$16, i);
                if (target == null)
                {
                    throw new IndexOutOfBoundsException();
                }
                target.set(xswitch);
            }
        }
        
        /**
         * Inserts and returns a new empty value (as xml) as the ith "switch" element
         */
        public org.griphyn.vdl.model.Switch insertNewSwitch(int i)
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.griphyn.vdl.model.Switch target = null;
                target = (org.griphyn.vdl.model.Switch)get_store().insert_element_user(SWITCH$16, i);
                return target;
            }
        }
        
        /**
         * Appends and returns a new empty value (as xml) as the last "switch" element
         */
        public org.griphyn.vdl.model.Switch addNewSwitch()
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.griphyn.vdl.model.Switch target = null;
                target = (org.griphyn.vdl.model.Switch)get_store().add_element_user(SWITCH$16);
                return target;
            }
        }
        
        /**
         * Removes the ith "switch" element
         */
        public void removeSwitch(int i)
        {
            synchronized (monitor())
            {
                check_orphaned();
                get_store().remove_element(SWITCH$16, i);
            }
        }
        
        /**
         * Gets array of all "continue" elements
         */
        public org.apache.xmlbeans.XmlObject[] getContinueArray()
        {
            synchronized (monitor())
            {
                check_orphaned();
                java.util.List targetList = new java.util.ArrayList();
                get_store().find_all_element_users(CONTINUE$18, targetList);
                org.apache.xmlbeans.XmlObject[] result = new org.apache.xmlbeans.XmlObject[targetList.size()];
                targetList.toArray(result);
                return result;
            }
        }
        
        /**
         * Gets ith "continue" element
         */
        public org.apache.xmlbeans.XmlObject getContinueArray(int i)
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.apache.xmlbeans.XmlObject target = null;
                target = (org.apache.xmlbeans.XmlObject)get_store().find_element_user(CONTINUE$18, i);
                if (target == null)
                {
                    throw new IndexOutOfBoundsException();
                }
                return target;
            }
        }
        
        /**
         * Returns number of "continue" element
         */
        public int sizeOfContinueArray()
        {
            synchronized (monitor())
            {
                check_orphaned();
                return get_store().count_elements(CONTINUE$18);
            }
        }
        
        /**
         * Sets array of all "continue" element
         */
        public void setContinueArray(org.apache.xmlbeans.XmlObject[] xcontinueArray)
        {
            synchronized (monitor())
            {
                check_orphaned();
                arraySetterHelper(xcontinueArray, CONTINUE$18);
            }
        }
        
        /**
         * Sets ith "continue" element
         */
        public void setContinueArray(int i, org.apache.xmlbeans.XmlObject xcontinue)
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.apache.xmlbeans.XmlObject target = null;
                target = (org.apache.xmlbeans.XmlObject)get_store().find_element_user(CONTINUE$18, i);
                if (target == null)
                {
                    throw new IndexOutOfBoundsException();
                }
                target.set(xcontinue);
            }
        }
        
        /**
         * Inserts and returns a new empty value (as xml) as the ith "continue" element
         */
        public org.apache.xmlbeans.XmlObject insertNewContinue(int i)
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.apache.xmlbeans.XmlObject target = null;
                target = (org.apache.xmlbeans.XmlObject)get_store().insert_element_user(CONTINUE$18, i);
                return target;
            }
        }
        
        /**
         * Appends and returns a new empty value (as xml) as the last "continue" element
         */
        public org.apache.xmlbeans.XmlObject addNewContinue()
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.apache.xmlbeans.XmlObject target = null;
                target = (org.apache.xmlbeans.XmlObject)get_store().add_element_user(CONTINUE$18);
                return target;
            }
        }
        
        /**
         * Removes the ith "continue" element
         */
        public void removeContinue(int i)
        {
            synchronized (monitor())
            {
                check_orphaned();
                get_store().remove_element(CONTINUE$18, i);
            }
        }
        
        /**
         * Gets array of all "break" elements
         */
        public org.apache.xmlbeans.XmlObject[] getBreakArray()
        {
            synchronized (monitor())
            {
                check_orphaned();
                java.util.List targetList = new java.util.ArrayList();
                get_store().find_all_element_users(BREAK$20, targetList);
                org.apache.xmlbeans.XmlObject[] result = new org.apache.xmlbeans.XmlObject[targetList.size()];
                targetList.toArray(result);
                return result;
            }
        }
        
        /**
         * Gets ith "break" element
         */
        public org.apache.xmlbeans.XmlObject getBreakArray(int i)
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.apache.xmlbeans.XmlObject target = null;
                target = (org.apache.xmlbeans.XmlObject)get_store().find_element_user(BREAK$20, i);
                if (target == null)
                {
                    throw new IndexOutOfBoundsException();
                }
                return target;
            }
        }
        
        /**
         * Returns number of "break" element
         */
        public int sizeOfBreakArray()
        {
            synchronized (monitor())
            {
                check_orphaned();
                return get_store().count_elements(BREAK$20);
            }
        }
        
        /**
         * Sets array of all "break" element
         */
        public void setBreakArray(org.apache.xmlbeans.XmlObject[] xbreakArray)
        {
            synchronized (monitor())
            {
                check_orphaned();
                arraySetterHelper(xbreakArray, BREAK$20);
            }
        }
        
        /**
         * Sets ith "break" element
         */
        public void setBreakArray(int i, org.apache.xmlbeans.XmlObject xbreak)
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.apache.xmlbeans.XmlObject target = null;
                target = (org.apache.xmlbeans.XmlObject)get_store().find_element_user(BREAK$20, i);
                if (target == null)
                {
                    throw new IndexOutOfBoundsException();
                }
                target.set(xbreak);
            }
        }
        
        /**
         * Inserts and returns a new empty value (as xml) as the ith "break" element
         */
        public org.apache.xmlbeans.XmlObject insertNewBreak(int i)
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.apache.xmlbeans.XmlObject target = null;
                target = (org.apache.xmlbeans.XmlObject)get_store().insert_element_user(BREAK$20, i);
                return target;
            }
        }
        
        /**
         * Appends and returns a new empty value (as xml) as the last "break" element
         */
        public org.apache.xmlbeans.XmlObject addNewBreak()
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.apache.xmlbeans.XmlObject target = null;
                target = (org.apache.xmlbeans.XmlObject)get_store().add_element_user(BREAK$20);
                return target;
            }
        }
        
        /**
         * Removes the ith "break" element
         */
        public void removeBreak(int i)
        {
            synchronized (monitor())
            {
                check_orphaned();
                get_store().remove_element(BREAK$20, i);
            }
        }
    }
    /**
     * An XML else(@http://www.griphyn.org/2006/08/vdl).
     *
     * This is a complex type.
     */
    public static class ElseImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements org.griphyn.vdl.model.If.Else
    {
        
        public ElseImpl(org.apache.xmlbeans.SchemaType sType)
        {
            super(sType);
        }
        
        private static final javax.xml.namespace.QName VARIABLE$0 = 
            new javax.xml.namespace.QName("http://www.griphyn.org/2006/08/vdl", "variable");
        private static final javax.xml.namespace.QName DATASET$2 = 
            new javax.xml.namespace.QName("http://www.griphyn.org/2006/08/vdl", "dataset");
        private static final javax.xml.namespace.QName ASSIGN$4 = 
            new javax.xml.namespace.QName("http://www.griphyn.org/2006/08/vdl", "assign");
        private static final javax.xml.namespace.QName CALL$6 = 
            new javax.xml.namespace.QName("http://www.griphyn.org/2006/08/vdl", "call");
        private static final javax.xml.namespace.QName FOREACH$8 = 
            new javax.xml.namespace.QName("http://www.griphyn.org/2006/08/vdl", "foreach");
        private static final javax.xml.namespace.QName IF$10 = 
            new javax.xml.namespace.QName("http://www.griphyn.org/2006/08/vdl", "if");
        private static final javax.xml.namespace.QName WHILE$12 = 
            new javax.xml.namespace.QName("http://www.griphyn.org/2006/08/vdl", "while");
        private static final javax.xml.namespace.QName REPEAT$14 = 
            new javax.xml.namespace.QName("http://www.griphyn.org/2006/08/vdl", "repeat");
        private static final javax.xml.namespace.QName SWITCH$16 = 
            new javax.xml.namespace.QName("http://www.griphyn.org/2006/08/vdl", "switch");
        private static final javax.xml.namespace.QName CONTINUE$18 = 
            new javax.xml.namespace.QName("http://www.griphyn.org/2006/08/vdl", "continue");
        private static final javax.xml.namespace.QName BREAK$20 = 
            new javax.xml.namespace.QName("http://www.griphyn.org/2006/08/vdl", "break");
        
        
        /**
         * Gets array of all "variable" elements
         */
        public org.griphyn.vdl.model.Variable[] getVariableArray()
        {
            synchronized (monitor())
            {
                check_orphaned();
                java.util.List targetList = new java.util.ArrayList();
                get_store().find_all_element_users(VARIABLE$0, targetList);
                org.griphyn.vdl.model.Variable[] result = new org.griphyn.vdl.model.Variable[targetList.size()];
                targetList.toArray(result);
                return result;
            }
        }
        
        /**
         * Gets ith "variable" element
         */
        public org.griphyn.vdl.model.Variable getVariableArray(int i)
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.griphyn.vdl.model.Variable target = null;
                target = (org.griphyn.vdl.model.Variable)get_store().find_element_user(VARIABLE$0, i);
                if (target == null)
                {
                    throw new IndexOutOfBoundsException();
                }
                return target;
            }
        }
        
        /**
         * Returns number of "variable" element
         */
        public int sizeOfVariableArray()
        {
            synchronized (monitor())
            {
                check_orphaned();
                return get_store().count_elements(VARIABLE$0);
            }
        }
        
        /**
         * Sets array of all "variable" element
         */
        public void setVariableArray(org.griphyn.vdl.model.Variable[] variableArray)
        {
            synchronized (monitor())
            {
                check_orphaned();
                arraySetterHelper(variableArray, VARIABLE$0);
            }
        }
        
        /**
         * Sets ith "variable" element
         */
        public void setVariableArray(int i, org.griphyn.vdl.model.Variable variable)
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.griphyn.vdl.model.Variable target = null;
                target = (org.griphyn.vdl.model.Variable)get_store().find_element_user(VARIABLE$0, i);
                if (target == null)
                {
                    throw new IndexOutOfBoundsException();
                }
                target.set(variable);
            }
        }
        
        /**
         * Inserts and returns a new empty value (as xml) as the ith "variable" element
         */
        public org.griphyn.vdl.model.Variable insertNewVariable(int i)
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.griphyn.vdl.model.Variable target = null;
                target = (org.griphyn.vdl.model.Variable)get_store().insert_element_user(VARIABLE$0, i);
                return target;
            }
        }
        
        /**
         * Appends and returns a new empty value (as xml) as the last "variable" element
         */
        public org.griphyn.vdl.model.Variable addNewVariable()
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.griphyn.vdl.model.Variable target = null;
                target = (org.griphyn.vdl.model.Variable)get_store().add_element_user(VARIABLE$0);
                return target;
            }
        }
        
        /**
         * Removes the ith "variable" element
         */
        public void removeVariable(int i)
        {
            synchronized (monitor())
            {
                check_orphaned();
                get_store().remove_element(VARIABLE$0, i);
            }
        }
        
        /**
         * Gets array of all "dataset" elements
         */
        public org.griphyn.vdl.model.Dataset[] getDatasetArray()
        {
            synchronized (monitor())
            {
                check_orphaned();
                java.util.List targetList = new java.util.ArrayList();
                get_store().find_all_element_users(DATASET$2, targetList);
                org.griphyn.vdl.model.Dataset[] result = new org.griphyn.vdl.model.Dataset[targetList.size()];
                targetList.toArray(result);
                return result;
            }
        }
        
        /**
         * Gets ith "dataset" element
         */
        public org.griphyn.vdl.model.Dataset getDatasetArray(int i)
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.griphyn.vdl.model.Dataset target = null;
                target = (org.griphyn.vdl.model.Dataset)get_store().find_element_user(DATASET$2, i);
                if (target == null)
                {
                    throw new IndexOutOfBoundsException();
                }
                return target;
            }
        }
        
        /**
         * Returns number of "dataset" element
         */
        public int sizeOfDatasetArray()
        {
            synchronized (monitor())
            {
                check_orphaned();
                return get_store().count_elements(DATASET$2);
            }
        }
        
        /**
         * Sets array of all "dataset" element
         */
        public void setDatasetArray(org.griphyn.vdl.model.Dataset[] datasetArray)
        {
            synchronized (monitor())
            {
                check_orphaned();
                arraySetterHelper(datasetArray, DATASET$2);
            }
        }
        
        /**
         * Sets ith "dataset" element
         */
        public void setDatasetArray(int i, org.griphyn.vdl.model.Dataset dataset)
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.griphyn.vdl.model.Dataset target = null;
                target = (org.griphyn.vdl.model.Dataset)get_store().find_element_user(DATASET$2, i);
                if (target == null)
                {
                    throw new IndexOutOfBoundsException();
                }
                target.set(dataset);
            }
        }
        
        /**
         * Inserts and returns a new empty value (as xml) as the ith "dataset" element
         */
        public org.griphyn.vdl.model.Dataset insertNewDataset(int i)
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.griphyn.vdl.model.Dataset target = null;
                target = (org.griphyn.vdl.model.Dataset)get_store().insert_element_user(DATASET$2, i);
                return target;
            }
        }
        
        /**
         * Appends and returns a new empty value (as xml) as the last "dataset" element
         */
        public org.griphyn.vdl.model.Dataset addNewDataset()
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.griphyn.vdl.model.Dataset target = null;
                target = (org.griphyn.vdl.model.Dataset)get_store().add_element_user(DATASET$2);
                return target;
            }
        }
        
        /**
         * Removes the ith "dataset" element
         */
        public void removeDataset(int i)
        {
            synchronized (monitor())
            {
                check_orphaned();
                get_store().remove_element(DATASET$2, i);
            }
        }
        
        /**
         * Gets array of all "assign" elements
         */
        public org.griphyn.vdl.model.Assign[] getAssignArray()
        {
            synchronized (monitor())
            {
                check_orphaned();
                java.util.List targetList = new java.util.ArrayList();
                get_store().find_all_element_users(ASSIGN$4, targetList);
                org.griphyn.vdl.model.Assign[] result = new org.griphyn.vdl.model.Assign[targetList.size()];
                targetList.toArray(result);
                return result;
            }
        }
        
        /**
         * Gets ith "assign" element
         */
        public org.griphyn.vdl.model.Assign getAssignArray(int i)
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.griphyn.vdl.model.Assign target = null;
                target = (org.griphyn.vdl.model.Assign)get_store().find_element_user(ASSIGN$4, i);
                if (target == null)
                {
                    throw new IndexOutOfBoundsException();
                }
                return target;
            }
        }
        
        /**
         * Returns number of "assign" element
         */
        public int sizeOfAssignArray()
        {
            synchronized (monitor())
            {
                check_orphaned();
                return get_store().count_elements(ASSIGN$4);
            }
        }
        
        /**
         * Sets array of all "assign" element
         */
        public void setAssignArray(org.griphyn.vdl.model.Assign[] assignArray)
        {
            synchronized (monitor())
            {
                check_orphaned();
                arraySetterHelper(assignArray, ASSIGN$4);
            }
        }
        
        /**
         * Sets ith "assign" element
         */
        public void setAssignArray(int i, org.griphyn.vdl.model.Assign assign)
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.griphyn.vdl.model.Assign target = null;
                target = (org.griphyn.vdl.model.Assign)get_store().find_element_user(ASSIGN$4, i);
                if (target == null)
                {
                    throw new IndexOutOfBoundsException();
                }
                target.set(assign);
            }
        }
        
        /**
         * Inserts and returns a new empty value (as xml) as the ith "assign" element
         */
        public org.griphyn.vdl.model.Assign insertNewAssign(int i)
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.griphyn.vdl.model.Assign target = null;
                target = (org.griphyn.vdl.model.Assign)get_store().insert_element_user(ASSIGN$4, i);
                return target;
            }
        }
        
        /**
         * Appends and returns a new empty value (as xml) as the last "assign" element
         */
        public org.griphyn.vdl.model.Assign addNewAssign()
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.griphyn.vdl.model.Assign target = null;
                target = (org.griphyn.vdl.model.Assign)get_store().add_element_user(ASSIGN$4);
                return target;
            }
        }
        
        /**
         * Removes the ith "assign" element
         */
        public void removeAssign(int i)
        {
            synchronized (monitor())
            {
                check_orphaned();
                get_store().remove_element(ASSIGN$4, i);
            }
        }
        
        /**
         * Gets array of all "call" elements
         */
        public org.griphyn.vdl.model.Call[] getCallArray()
        {
            synchronized (monitor())
            {
                check_orphaned();
                java.util.List targetList = new java.util.ArrayList();
                get_store().find_all_element_users(CALL$6, targetList);
                org.griphyn.vdl.model.Call[] result = new org.griphyn.vdl.model.Call[targetList.size()];
                targetList.toArray(result);
                return result;
            }
        }
        
        /**
         * Gets ith "call" element
         */
        public org.griphyn.vdl.model.Call getCallArray(int i)
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.griphyn.vdl.model.Call target = null;
                target = (org.griphyn.vdl.model.Call)get_store().find_element_user(CALL$6, i);
                if (target == null)
                {
                    throw new IndexOutOfBoundsException();
                }
                return target;
            }
        }
        
        /**
         * Returns number of "call" element
         */
        public int sizeOfCallArray()
        {
            synchronized (monitor())
            {
                check_orphaned();
                return get_store().count_elements(CALL$6);
            }
        }
        
        /**
         * Sets array of all "call" element
         */
        public void setCallArray(org.griphyn.vdl.model.Call[] callArray)
        {
            synchronized (monitor())
            {
                check_orphaned();
                arraySetterHelper(callArray, CALL$6);
            }
        }
        
        /**
         * Sets ith "call" element
         */
        public void setCallArray(int i, org.griphyn.vdl.model.Call call)
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.griphyn.vdl.model.Call target = null;
                target = (org.griphyn.vdl.model.Call)get_store().find_element_user(CALL$6, i);
                if (target == null)
                {
                    throw new IndexOutOfBoundsException();
                }
                target.set(call);
            }
        }
        
        /**
         * Inserts and returns a new empty value (as xml) as the ith "call" element
         */
        public org.griphyn.vdl.model.Call insertNewCall(int i)
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.griphyn.vdl.model.Call target = null;
                target = (org.griphyn.vdl.model.Call)get_store().insert_element_user(CALL$6, i);
                return target;
            }
        }
        
        /**
         * Appends and returns a new empty value (as xml) as the last "call" element
         */
        public org.griphyn.vdl.model.Call addNewCall()
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.griphyn.vdl.model.Call target = null;
                target = (org.griphyn.vdl.model.Call)get_store().add_element_user(CALL$6);
                return target;
            }
        }
        
        /**
         * Removes the ith "call" element
         */
        public void removeCall(int i)
        {
            synchronized (monitor())
            {
                check_orphaned();
                get_store().remove_element(CALL$6, i);
            }
        }
        
        /**
         * Gets array of all "foreach" elements
         */
        public org.griphyn.vdl.model.Foreach[] getForeachArray()
        {
            synchronized (monitor())
            {
                check_orphaned();
                java.util.List targetList = new java.util.ArrayList();
                get_store().find_all_element_users(FOREACH$8, targetList);
                org.griphyn.vdl.model.Foreach[] result = new org.griphyn.vdl.model.Foreach[targetList.size()];
                targetList.toArray(result);
                return result;
            }
        }
        
        /**
         * Gets ith "foreach" element
         */
        public org.griphyn.vdl.model.Foreach getForeachArray(int i)
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.griphyn.vdl.model.Foreach target = null;
                target = (org.griphyn.vdl.model.Foreach)get_store().find_element_user(FOREACH$8, i);
                if (target == null)
                {
                    throw new IndexOutOfBoundsException();
                }
                return target;
            }
        }
        
        /**
         * Returns number of "foreach" element
         */
        public int sizeOfForeachArray()
        {
            synchronized (monitor())
            {
                check_orphaned();
                return get_store().count_elements(FOREACH$8);
            }
        }
        
        /**
         * Sets array of all "foreach" element
         */
        public void setForeachArray(org.griphyn.vdl.model.Foreach[] foreachArray)
        {
            synchronized (monitor())
            {
                check_orphaned();
                arraySetterHelper(foreachArray, FOREACH$8);
            }
        }
        
        /**
         * Sets ith "foreach" element
         */
        public void setForeachArray(int i, org.griphyn.vdl.model.Foreach foreach)
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.griphyn.vdl.model.Foreach target = null;
                target = (org.griphyn.vdl.model.Foreach)get_store().find_element_user(FOREACH$8, i);
                if (target == null)
                {
                    throw new IndexOutOfBoundsException();
                }
                target.set(foreach);
            }
        }
        
        /**
         * Inserts and returns a new empty value (as xml) as the ith "foreach" element
         */
        public org.griphyn.vdl.model.Foreach insertNewForeach(int i)
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.griphyn.vdl.model.Foreach target = null;
                target = (org.griphyn.vdl.model.Foreach)get_store().insert_element_user(FOREACH$8, i);
                return target;
            }
        }
        
        /**
         * Appends and returns a new empty value (as xml) as the last "foreach" element
         */
        public org.griphyn.vdl.model.Foreach addNewForeach()
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.griphyn.vdl.model.Foreach target = null;
                target = (org.griphyn.vdl.model.Foreach)get_store().add_element_user(FOREACH$8);
                return target;
            }
        }
        
        /**
         * Removes the ith "foreach" element
         */
        public void removeForeach(int i)
        {
            synchronized (monitor())
            {
                check_orphaned();
                get_store().remove_element(FOREACH$8, i);
            }
        }
        
        /**
         * Gets array of all "if" elements
         */
        public org.griphyn.vdl.model.If[] getIfArray()
        {
            synchronized (monitor())
            {
                check_orphaned();
                java.util.List targetList = new java.util.ArrayList();
                get_store().find_all_element_users(IF$10, targetList);
                org.griphyn.vdl.model.If[] result = new org.griphyn.vdl.model.If[targetList.size()];
                targetList.toArray(result);
                return result;
            }
        }
        
        /**
         * Gets ith "if" element
         */
        public org.griphyn.vdl.model.If getIfArray(int i)
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.griphyn.vdl.model.If target = null;
                target = (org.griphyn.vdl.model.If)get_store().find_element_user(IF$10, i);
                if (target == null)
                {
                    throw new IndexOutOfBoundsException();
                }
                return target;
            }
        }
        
        /**
         * Returns number of "if" element
         */
        public int sizeOfIfArray()
        {
            synchronized (monitor())
            {
                check_orphaned();
                return get_store().count_elements(IF$10);
            }
        }
        
        /**
         * Sets array of all "if" element
         */
        public void setIfArray(org.griphyn.vdl.model.If[] xifArray)
        {
            synchronized (monitor())
            {
                check_orphaned();
                arraySetterHelper(xifArray, IF$10);
            }
        }
        
        /**
         * Sets ith "if" element
         */
        public void setIfArray(int i, org.griphyn.vdl.model.If xif)
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.griphyn.vdl.model.If target = null;
                target = (org.griphyn.vdl.model.If)get_store().find_element_user(IF$10, i);
                if (target == null)
                {
                    throw new IndexOutOfBoundsException();
                }
                target.set(xif);
            }
        }
        
        /**
         * Inserts and returns a new empty value (as xml) as the ith "if" element
         */
        public org.griphyn.vdl.model.If insertNewIf(int i)
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.griphyn.vdl.model.If target = null;
                target = (org.griphyn.vdl.model.If)get_store().insert_element_user(IF$10, i);
                return target;
            }
        }
        
        /**
         * Appends and returns a new empty value (as xml) as the last "if" element
         */
        public org.griphyn.vdl.model.If addNewIf()
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.griphyn.vdl.model.If target = null;
                target = (org.griphyn.vdl.model.If)get_store().add_element_user(IF$10);
                return target;
            }
        }
        
        /**
         * Removes the ith "if" element
         */
        public void removeIf(int i)
        {
            synchronized (monitor())
            {
                check_orphaned();
                get_store().remove_element(IF$10, i);
            }
        }
        
        /**
         * Gets array of all "while" elements
         */
        public org.griphyn.vdl.model.While[] getWhileArray()
        {
            synchronized (monitor())
            {
                check_orphaned();
                java.util.List targetList = new java.util.ArrayList();
                get_store().find_all_element_users(WHILE$12, targetList);
                org.griphyn.vdl.model.While[] result = new org.griphyn.vdl.model.While[targetList.size()];
                targetList.toArray(result);
                return result;
            }
        }
        
        /**
         * Gets ith "while" element
         */
        public org.griphyn.vdl.model.While getWhileArray(int i)
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.griphyn.vdl.model.While target = null;
                target = (org.griphyn.vdl.model.While)get_store().find_element_user(WHILE$12, i);
                if (target == null)
                {
                    throw new IndexOutOfBoundsException();
                }
                return target;
            }
        }
        
        /**
         * Returns number of "while" element
         */
        public int sizeOfWhileArray()
        {
            synchronized (monitor())
            {
                check_orphaned();
                return get_store().count_elements(WHILE$12);
            }
        }
        
        /**
         * Sets array of all "while" element
         */
        public void setWhileArray(org.griphyn.vdl.model.While[] xwhileArray)
        {
            synchronized (monitor())
            {
                check_orphaned();
                arraySetterHelper(xwhileArray, WHILE$12);
            }
        }
        
        /**
         * Sets ith "while" element
         */
        public void setWhileArray(int i, org.griphyn.vdl.model.While xwhile)
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.griphyn.vdl.model.While target = null;
                target = (org.griphyn.vdl.model.While)get_store().find_element_user(WHILE$12, i);
                if (target == null)
                {
                    throw new IndexOutOfBoundsException();
                }
                target.set(xwhile);
            }
        }
        
        /**
         * Inserts and returns a new empty value (as xml) as the ith "while" element
         */
        public org.griphyn.vdl.model.While insertNewWhile(int i)
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.griphyn.vdl.model.While target = null;
                target = (org.griphyn.vdl.model.While)get_store().insert_element_user(WHILE$12, i);
                return target;
            }
        }
        
        /**
         * Appends and returns a new empty value (as xml) as the last "while" element
         */
        public org.griphyn.vdl.model.While addNewWhile()
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.griphyn.vdl.model.While target = null;
                target = (org.griphyn.vdl.model.While)get_store().add_element_user(WHILE$12);
                return target;
            }
        }
        
        /**
         * Removes the ith "while" element
         */
        public void removeWhile(int i)
        {
            synchronized (monitor())
            {
                check_orphaned();
                get_store().remove_element(WHILE$12, i);
            }
        }
        
        /**
         * Gets array of all "repeat" elements
         */
        public org.griphyn.vdl.model.Repeat[] getRepeatArray()
        {
            synchronized (monitor())
            {
                check_orphaned();
                java.util.List targetList = new java.util.ArrayList();
                get_store().find_all_element_users(REPEAT$14, targetList);
                org.griphyn.vdl.model.Repeat[] result = new org.griphyn.vdl.model.Repeat[targetList.size()];
                targetList.toArray(result);
                return result;
            }
        }
        
        /**
         * Gets ith "repeat" element
         */
        public org.griphyn.vdl.model.Repeat getRepeatArray(int i)
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.griphyn.vdl.model.Repeat target = null;
                target = (org.griphyn.vdl.model.Repeat)get_store().find_element_user(REPEAT$14, i);
                if (target == null)
                {
                    throw new IndexOutOfBoundsException();
                }
                return target;
            }
        }
        
        /**
         * Returns number of "repeat" element
         */
        public int sizeOfRepeatArray()
        {
            synchronized (monitor())
            {
                check_orphaned();
                return get_store().count_elements(REPEAT$14);
            }
        }
        
        /**
         * Sets array of all "repeat" element
         */
        public void setRepeatArray(org.griphyn.vdl.model.Repeat[] repeatArray)
        {
            synchronized (monitor())
            {
                check_orphaned();
                arraySetterHelper(repeatArray, REPEAT$14);
            }
        }
        
        /**
         * Sets ith "repeat" element
         */
        public void setRepeatArray(int i, org.griphyn.vdl.model.Repeat repeat)
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.griphyn.vdl.model.Repeat target = null;
                target = (org.griphyn.vdl.model.Repeat)get_store().find_element_user(REPEAT$14, i);
                if (target == null)
                {
                    throw new IndexOutOfBoundsException();
                }
                target.set(repeat);
            }
        }
        
        /**
         * Inserts and returns a new empty value (as xml) as the ith "repeat" element
         */
        public org.griphyn.vdl.model.Repeat insertNewRepeat(int i)
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.griphyn.vdl.model.Repeat target = null;
                target = (org.griphyn.vdl.model.Repeat)get_store().insert_element_user(REPEAT$14, i);
                return target;
            }
        }
        
        /**
         * Appends and returns a new empty value (as xml) as the last "repeat" element
         */
        public org.griphyn.vdl.model.Repeat addNewRepeat()
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.griphyn.vdl.model.Repeat target = null;
                target = (org.griphyn.vdl.model.Repeat)get_store().add_element_user(REPEAT$14);
                return target;
            }
        }
        
        /**
         * Removes the ith "repeat" element
         */
        public void removeRepeat(int i)
        {
            synchronized (monitor())
            {
                check_orphaned();
                get_store().remove_element(REPEAT$14, i);
            }
        }
        
        /**
         * Gets array of all "switch" elements
         */
        public org.griphyn.vdl.model.Switch[] getSwitchArray()
        {
            synchronized (monitor())
            {
                check_orphaned();
                java.util.List targetList = new java.util.ArrayList();
                get_store().find_all_element_users(SWITCH$16, targetList);
                org.griphyn.vdl.model.Switch[] result = new org.griphyn.vdl.model.Switch[targetList.size()];
                targetList.toArray(result);
                return result;
            }
        }
        
        /**
         * Gets ith "switch" element
         */
        public org.griphyn.vdl.model.Switch getSwitchArray(int i)
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.griphyn.vdl.model.Switch target = null;
                target = (org.griphyn.vdl.model.Switch)get_store().find_element_user(SWITCH$16, i);
                if (target == null)
                {
                    throw new IndexOutOfBoundsException();
                }
                return target;
            }
        }
        
        /**
         * Returns number of "switch" element
         */
        public int sizeOfSwitchArray()
        {
            synchronized (monitor())
            {
                check_orphaned();
                return get_store().count_elements(SWITCH$16);
            }
        }
        
        /**
         * Sets array of all "switch" element
         */
        public void setSwitchArray(org.griphyn.vdl.model.Switch[] xswitchArray)
        {
            synchronized (monitor())
            {
                check_orphaned();
                arraySetterHelper(xswitchArray, SWITCH$16);
            }
        }
        
        /**
         * Sets ith "switch" element
         */
        public void setSwitchArray(int i, org.griphyn.vdl.model.Switch xswitch)
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.griphyn.vdl.model.Switch target = null;
                target = (org.griphyn.vdl.model.Switch)get_store().find_element_user(SWITCH$16, i);
                if (target == null)
                {
                    throw new IndexOutOfBoundsException();
                }
                target.set(xswitch);
            }
        }
        
        /**
         * Inserts and returns a new empty value (as xml) as the ith "switch" element
         */
        public org.griphyn.vdl.model.Switch insertNewSwitch(int i)
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.griphyn.vdl.model.Switch target = null;
                target = (org.griphyn.vdl.model.Switch)get_store().insert_element_user(SWITCH$16, i);
                return target;
            }
        }
        
        /**
         * Appends and returns a new empty value (as xml) as the last "switch" element
         */
        public org.griphyn.vdl.model.Switch addNewSwitch()
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.griphyn.vdl.model.Switch target = null;
                target = (org.griphyn.vdl.model.Switch)get_store().add_element_user(SWITCH$16);
                return target;
            }
        }
        
        /**
         * Removes the ith "switch" element
         */
        public void removeSwitch(int i)
        {
            synchronized (monitor())
            {
                check_orphaned();
                get_store().remove_element(SWITCH$16, i);
            }
        }
        
        /**
         * Gets array of all "continue" elements
         */
        public org.apache.xmlbeans.XmlObject[] getContinueArray()
        {
            synchronized (monitor())
            {
                check_orphaned();
                java.util.List targetList = new java.util.ArrayList();
                get_store().find_all_element_users(CONTINUE$18, targetList);
                org.apache.xmlbeans.XmlObject[] result = new org.apache.xmlbeans.XmlObject[targetList.size()];
                targetList.toArray(result);
                return result;
            }
        }
        
        /**
         * Gets ith "continue" element
         */
        public org.apache.xmlbeans.XmlObject getContinueArray(int i)
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.apache.xmlbeans.XmlObject target = null;
                target = (org.apache.xmlbeans.XmlObject)get_store().find_element_user(CONTINUE$18, i);
                if (target == null)
                {
                    throw new IndexOutOfBoundsException();
                }
                return target;
            }
        }
        
        /**
         * Returns number of "continue" element
         */
        public int sizeOfContinueArray()
        {
            synchronized (monitor())
            {
                check_orphaned();
                return get_store().count_elements(CONTINUE$18);
            }
        }
        
        /**
         * Sets array of all "continue" element
         */
        public void setContinueArray(org.apache.xmlbeans.XmlObject[] xcontinueArray)
        {
            synchronized (monitor())
            {
                check_orphaned();
                arraySetterHelper(xcontinueArray, CONTINUE$18);
            }
        }
        
        /**
         * Sets ith "continue" element
         */
        public void setContinueArray(int i, org.apache.xmlbeans.XmlObject xcontinue)
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.apache.xmlbeans.XmlObject target = null;
                target = (org.apache.xmlbeans.XmlObject)get_store().find_element_user(CONTINUE$18, i);
                if (target == null)
                {
                    throw new IndexOutOfBoundsException();
                }
                target.set(xcontinue);
            }
        }
        
        /**
         * Inserts and returns a new empty value (as xml) as the ith "continue" element
         */
        public org.apache.xmlbeans.XmlObject insertNewContinue(int i)
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.apache.xmlbeans.XmlObject target = null;
                target = (org.apache.xmlbeans.XmlObject)get_store().insert_element_user(CONTINUE$18, i);
                return target;
            }
        }
        
        /**
         * Appends and returns a new empty value (as xml) as the last "continue" element
         */
        public org.apache.xmlbeans.XmlObject addNewContinue()
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.apache.xmlbeans.XmlObject target = null;
                target = (org.apache.xmlbeans.XmlObject)get_store().add_element_user(CONTINUE$18);
                return target;
            }
        }
        
        /**
         * Removes the ith "continue" element
         */
        public void removeContinue(int i)
        {
            synchronized (monitor())
            {
                check_orphaned();
                get_store().remove_element(CONTINUE$18, i);
            }
        }
        
        /**
         * Gets array of all "break" elements
         */
        public org.apache.xmlbeans.XmlObject[] getBreakArray()
        {
            synchronized (monitor())
            {
                check_orphaned();
                java.util.List targetList = new java.util.ArrayList();
                get_store().find_all_element_users(BREAK$20, targetList);
                org.apache.xmlbeans.XmlObject[] result = new org.apache.xmlbeans.XmlObject[targetList.size()];
                targetList.toArray(result);
                return result;
            }
        }
        
        /**
         * Gets ith "break" element
         */
        public org.apache.xmlbeans.XmlObject getBreakArray(int i)
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.apache.xmlbeans.XmlObject target = null;
                target = (org.apache.xmlbeans.XmlObject)get_store().find_element_user(BREAK$20, i);
                if (target == null)
                {
                    throw new IndexOutOfBoundsException();
                }
                return target;
            }
        }
        
        /**
         * Returns number of "break" element
         */
        public int sizeOfBreakArray()
        {
            synchronized (monitor())
            {
                check_orphaned();
                return get_store().count_elements(BREAK$20);
            }
        }
        
        /**
         * Sets array of all "break" element
         */
        public void setBreakArray(org.apache.xmlbeans.XmlObject[] xbreakArray)
        {
            synchronized (monitor())
            {
                check_orphaned();
                arraySetterHelper(xbreakArray, BREAK$20);
            }
        }
        
        /**
         * Sets ith "break" element
         */
        public void setBreakArray(int i, org.apache.xmlbeans.XmlObject xbreak)
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.apache.xmlbeans.XmlObject target = null;
                target = (org.apache.xmlbeans.XmlObject)get_store().find_element_user(BREAK$20, i);
                if (target == null)
                {
                    throw new IndexOutOfBoundsException();
                }
                target.set(xbreak);
            }
        }
        
        /**
         * Inserts and returns a new empty value (as xml) as the ith "break" element
         */
        public org.apache.xmlbeans.XmlObject insertNewBreak(int i)
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.apache.xmlbeans.XmlObject target = null;
                target = (org.apache.xmlbeans.XmlObject)get_store().insert_element_user(BREAK$20, i);
                return target;
            }
        }
        
        /**
         * Appends and returns a new empty value (as xml) as the last "break" element
         */
        public org.apache.xmlbeans.XmlObject addNewBreak()
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.apache.xmlbeans.XmlObject target = null;
                target = (org.apache.xmlbeans.XmlObject)get_store().add_element_user(BREAK$20);
                return target;
            }
        }
        
        /**
         * Removes the ith "break" element
         */
        public void removeBreak(int i)
        {
            synchronized (monitor())
            {
                check_orphaned();
                get_store().remove_element(BREAK$20, i);
            }
        }
    }
}
