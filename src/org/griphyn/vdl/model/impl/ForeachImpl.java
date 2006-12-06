/*
 * XML Type:  Foreach
 * Namespace: http://www.griphyn.org/2006/08/vdl
 * Java type: org.griphyn.vdl.model.Foreach
 *
 * Automatically generated - do not modify.
 */
package org.griphyn.vdl.model.impl;
/**
 * An XML Foreach(@http://www.griphyn.org/2006/08/vdl).
 *
 * This is a complex type.
 */
public class ForeachImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements org.griphyn.vdl.model.Foreach
{
    
    public ForeachImpl(org.apache.xmlbeans.SchemaType sType)
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
    private static final javax.xml.namespace.QName VAR$22 = 
        new javax.xml.namespace.QName("", "var");
    private static final javax.xml.namespace.QName IN$24 = 
        new javax.xml.namespace.QName("", "in");
    private static final javax.xml.namespace.QName BEGIN$26 = 
        new javax.xml.namespace.QName("", "begin");
    private static final javax.xml.namespace.QName END$28 = 
        new javax.xml.namespace.QName("", "end");
    private static final javax.xml.namespace.QName STEP$30 = 
        new javax.xml.namespace.QName("", "step");
    private static final javax.xml.namespace.QName INDEXVAR$32 = 
        new javax.xml.namespace.QName("", "indexVar");
    private static final javax.xml.namespace.QName MODE$34 = 
        new javax.xml.namespace.QName("", "mode");
    
    
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
    
    /**
     * Gets the "var" attribute
     */
    public java.lang.String getVar()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(VAR$22);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "var" attribute
     */
    public org.apache.xmlbeans.XmlNMTOKEN xgetVar()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlNMTOKEN target = null;
            target = (org.apache.xmlbeans.XmlNMTOKEN)get_store().find_attribute_user(VAR$22);
            return target;
        }
    }
    
    /**
     * True if has "var" attribute
     */
    public boolean isSetVar()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().find_attribute_user(VAR$22) != null;
        }
    }
    
    /**
     * Sets the "var" attribute
     */
    public void setVar(java.lang.String var)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(VAR$22);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_attribute_user(VAR$22);
            }
            target.setStringValue(var);
        }
    }
    
    /**
     * Sets (as xml) the "var" attribute
     */
    public void xsetVar(org.apache.xmlbeans.XmlNMTOKEN var)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlNMTOKEN target = null;
            target = (org.apache.xmlbeans.XmlNMTOKEN)get_store().find_attribute_user(VAR$22);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlNMTOKEN)get_store().add_attribute_user(VAR$22);
            }
            target.set(var);
        }
    }
    
    /**
     * Unsets the "var" attribute
     */
    public void unsetVar()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_attribute(VAR$22);
        }
    }
    
    /**
     * Gets the "in" attribute
     */
    public java.lang.String getIn()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(IN$24);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "in" attribute
     */
    public org.apache.xmlbeans.XmlString xgetIn()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_attribute_user(IN$24);
            return target;
        }
    }
    
    /**
     * True if has "in" attribute
     */
    public boolean isSetIn()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().find_attribute_user(IN$24) != null;
        }
    }
    
    /**
     * Sets the "in" attribute
     */
    public void setIn(java.lang.String in)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(IN$24);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_attribute_user(IN$24);
            }
            target.setStringValue(in);
        }
    }
    
    /**
     * Sets (as xml) the "in" attribute
     */
    public void xsetIn(org.apache.xmlbeans.XmlString in)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_attribute_user(IN$24);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlString)get_store().add_attribute_user(IN$24);
            }
            target.set(in);
        }
    }
    
    /**
     * Unsets the "in" attribute
     */
    public void unsetIn()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_attribute(IN$24);
        }
    }
    
    /**
     * Gets the "begin" attribute
     */
    public int getBegin()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(BEGIN$26);
            if (target == null)
            {
                return 0;
            }
            return target.getIntValue();
        }
    }
    
    /**
     * Gets (as xml) the "begin" attribute
     */
    public org.apache.xmlbeans.XmlInt xgetBegin()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlInt target = null;
            target = (org.apache.xmlbeans.XmlInt)get_store().find_attribute_user(BEGIN$26);
            return target;
        }
    }
    
    /**
     * True if has "begin" attribute
     */
    public boolean isSetBegin()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().find_attribute_user(BEGIN$26) != null;
        }
    }
    
    /**
     * Sets the "begin" attribute
     */
    public void setBegin(int begin)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(BEGIN$26);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_attribute_user(BEGIN$26);
            }
            target.setIntValue(begin);
        }
    }
    
    /**
     * Sets (as xml) the "begin" attribute
     */
    public void xsetBegin(org.apache.xmlbeans.XmlInt begin)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlInt target = null;
            target = (org.apache.xmlbeans.XmlInt)get_store().find_attribute_user(BEGIN$26);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlInt)get_store().add_attribute_user(BEGIN$26);
            }
            target.set(begin);
        }
    }
    
    /**
     * Unsets the "begin" attribute
     */
    public void unsetBegin()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_attribute(BEGIN$26);
        }
    }
    
    /**
     * Gets the "end" attribute
     */
    public int getEnd()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(END$28);
            if (target == null)
            {
                return 0;
            }
            return target.getIntValue();
        }
    }
    
    /**
     * Gets (as xml) the "end" attribute
     */
    public org.apache.xmlbeans.XmlInt xgetEnd()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlInt target = null;
            target = (org.apache.xmlbeans.XmlInt)get_store().find_attribute_user(END$28);
            return target;
        }
    }
    
    /**
     * True if has "end" attribute
     */
    public boolean isSetEnd()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().find_attribute_user(END$28) != null;
        }
    }
    
    /**
     * Sets the "end" attribute
     */
    public void setEnd(int end)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(END$28);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_attribute_user(END$28);
            }
            target.setIntValue(end);
        }
    }
    
    /**
     * Sets (as xml) the "end" attribute
     */
    public void xsetEnd(org.apache.xmlbeans.XmlInt end)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlInt target = null;
            target = (org.apache.xmlbeans.XmlInt)get_store().find_attribute_user(END$28);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlInt)get_store().add_attribute_user(END$28);
            }
            target.set(end);
        }
    }
    
    /**
     * Unsets the "end" attribute
     */
    public void unsetEnd()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_attribute(END$28);
        }
    }
    
    /**
     * Gets the "step" attribute
     */
    public int getStep()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(STEP$30);
            if (target == null)
            {
                return 0;
            }
            return target.getIntValue();
        }
    }
    
    /**
     * Gets (as xml) the "step" attribute
     */
    public org.apache.xmlbeans.XmlInt xgetStep()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlInt target = null;
            target = (org.apache.xmlbeans.XmlInt)get_store().find_attribute_user(STEP$30);
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
            return get_store().find_attribute_user(STEP$30) != null;
        }
    }
    
    /**
     * Sets the "step" attribute
     */
    public void setStep(int step)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(STEP$30);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_attribute_user(STEP$30);
            }
            target.setIntValue(step);
        }
    }
    
    /**
     * Sets (as xml) the "step" attribute
     */
    public void xsetStep(org.apache.xmlbeans.XmlInt step)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlInt target = null;
            target = (org.apache.xmlbeans.XmlInt)get_store().find_attribute_user(STEP$30);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlInt)get_store().add_attribute_user(STEP$30);
            }
            target.set(step);
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
            get_store().remove_attribute(STEP$30);
        }
    }
    
    /**
     * Gets the "indexVar" attribute
     */
    public java.lang.String getIndexVar()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(INDEXVAR$32);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "indexVar" attribute
     */
    public org.apache.xmlbeans.XmlNMTOKEN xgetIndexVar()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlNMTOKEN target = null;
            target = (org.apache.xmlbeans.XmlNMTOKEN)get_store().find_attribute_user(INDEXVAR$32);
            return target;
        }
    }
    
    /**
     * True if has "indexVar" attribute
     */
    public boolean isSetIndexVar()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().find_attribute_user(INDEXVAR$32) != null;
        }
    }
    
    /**
     * Sets the "indexVar" attribute
     */
    public void setIndexVar(java.lang.String indexVar)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(INDEXVAR$32);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_attribute_user(INDEXVAR$32);
            }
            target.setStringValue(indexVar);
        }
    }
    
    /**
     * Sets (as xml) the "indexVar" attribute
     */
    public void xsetIndexVar(org.apache.xmlbeans.XmlNMTOKEN indexVar)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlNMTOKEN target = null;
            target = (org.apache.xmlbeans.XmlNMTOKEN)get_store().find_attribute_user(INDEXVAR$32);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlNMTOKEN)get_store().add_attribute_user(INDEXVAR$32);
            }
            target.set(indexVar);
        }
    }
    
    /**
     * Unsets the "indexVar" attribute
     */
    public void unsetIndexVar()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_attribute(INDEXVAR$32);
        }
    }
    
    /**
     * Gets the "mode" attribute
     */
    public org.griphyn.vdl.model.Mode.Enum getMode()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(MODE$34);
            if (target == null)
            {
                return null;
            }
            return (org.griphyn.vdl.model.Mode.Enum)target.getEnumValue();
        }
    }
    
    /**
     * Gets (as xml) the "mode" attribute
     */
    public org.griphyn.vdl.model.Mode xgetMode()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.griphyn.vdl.model.Mode target = null;
            target = (org.griphyn.vdl.model.Mode)get_store().find_attribute_user(MODE$34);
            return target;
        }
    }
    
    /**
     * True if has "mode" attribute
     */
    public boolean isSetMode()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().find_attribute_user(MODE$34) != null;
        }
    }
    
    /**
     * Sets the "mode" attribute
     */
    public void setMode(org.griphyn.vdl.model.Mode.Enum mode)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(MODE$34);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_attribute_user(MODE$34);
            }
            target.setEnumValue(mode);
        }
    }
    
    /**
     * Sets (as xml) the "mode" attribute
     */
    public void xsetMode(org.griphyn.vdl.model.Mode mode)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.griphyn.vdl.model.Mode target = null;
            target = (org.griphyn.vdl.model.Mode)get_store().find_attribute_user(MODE$34);
            if (target == null)
            {
                target = (org.griphyn.vdl.model.Mode)get_store().add_attribute_user(MODE$34);
            }
            target.set(mode);
        }
    }
    
    /**
     * Unsets the "mode" attribute
     */
    public void unsetMode()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_attribute(MODE$34);
        }
    }
}
