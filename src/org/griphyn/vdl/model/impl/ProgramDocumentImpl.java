/*
 * An XML document type.
 * Localname: program
 * Namespace: http://www.griphyn.org/2006/08/vdl
 * Java type: org.griphyn.vdl.model.ProgramDocument
 *
 * Automatically generated - do not modify.
 */
package org.griphyn.vdl.model.impl;
/**
 * A document containing one program(@http://www.griphyn.org/2006/08/vdl) element.
 *
 * This is a complex type.
 */
public class ProgramDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements org.griphyn.vdl.model.ProgramDocument
{
    
    public ProgramDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName PROGRAM$0 = 
        new javax.xml.namespace.QName("http://www.griphyn.org/2006/08/vdl", "program");
    
    
    /**
     * Gets the "program" element
     */
    public org.griphyn.vdl.model.ProgramDocument.Program getProgram()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.griphyn.vdl.model.ProgramDocument.Program target = null;
            target = (org.griphyn.vdl.model.ProgramDocument.Program)get_store().find_element_user(PROGRAM$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "program" element
     */
    public void setProgram(org.griphyn.vdl.model.ProgramDocument.Program program)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.griphyn.vdl.model.ProgramDocument.Program target = null;
            target = (org.griphyn.vdl.model.ProgramDocument.Program)get_store().find_element_user(PROGRAM$0, 0);
            if (target == null)
            {
                target = (org.griphyn.vdl.model.ProgramDocument.Program)get_store().add_element_user(PROGRAM$0);
            }
            target.set(program);
        }
    }
    
    /**
     * Appends and returns a new empty "program" element
     */
    public org.griphyn.vdl.model.ProgramDocument.Program addNewProgram()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.griphyn.vdl.model.ProgramDocument.Program target = null;
            target = (org.griphyn.vdl.model.ProgramDocument.Program)get_store().add_element_user(PROGRAM$0);
            return target;
        }
    }
    /**
     * An XML program(@http://www.griphyn.org/2006/08/vdl).
     *
     * This is a complex type.
     */
    public static class ProgramImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements org.griphyn.vdl.model.ProgramDocument.Program
    {
        
        public ProgramImpl(org.apache.xmlbeans.SchemaType sType)
        {
            super(sType);
        }
        
        private static final javax.xml.namespace.QName TYPES$0 = 
            new javax.xml.namespace.QName("http://www.griphyn.org/2006/08/vdl", "types");
        private static final javax.xml.namespace.QName PROCEDURE$2 = 
            new javax.xml.namespace.QName("http://www.griphyn.org/2006/08/vdl", "procedure");
        private static final javax.xml.namespace.QName VARIABLE$4 = 
            new javax.xml.namespace.QName("http://www.griphyn.org/2006/08/vdl", "variable");
        private static final javax.xml.namespace.QName DATASET$6 = 
            new javax.xml.namespace.QName("http://www.griphyn.org/2006/08/vdl", "dataset");
        private static final javax.xml.namespace.QName ASSIGN$8 = 
            new javax.xml.namespace.QName("http://www.griphyn.org/2006/08/vdl", "assign");
        private static final javax.xml.namespace.QName CALL$10 = 
            new javax.xml.namespace.QName("http://www.griphyn.org/2006/08/vdl", "call");
        private static final javax.xml.namespace.QName FOREACH$12 = 
            new javax.xml.namespace.QName("http://www.griphyn.org/2006/08/vdl", "foreach");
        private static final javax.xml.namespace.QName IF$14 = 
            new javax.xml.namespace.QName("http://www.griphyn.org/2006/08/vdl", "if");
        private static final javax.xml.namespace.QName WHILE$16 = 
            new javax.xml.namespace.QName("http://www.griphyn.org/2006/08/vdl", "while");
        private static final javax.xml.namespace.QName REPEAT$18 = 
            new javax.xml.namespace.QName("http://www.griphyn.org/2006/08/vdl", "repeat");
        private static final javax.xml.namespace.QName SWITCH$20 = 
            new javax.xml.namespace.QName("http://www.griphyn.org/2006/08/vdl", "switch");
        private static final javax.xml.namespace.QName CONTINUE$22 = 
            new javax.xml.namespace.QName("http://www.griphyn.org/2006/08/vdl", "continue");
        private static final javax.xml.namespace.QName BREAK$24 = 
            new javax.xml.namespace.QName("http://www.griphyn.org/2006/08/vdl", "break");
        private static final javax.xml.namespace.QName TARGETNAMESPACE$26 = 
            new javax.xml.namespace.QName("", "targetNamespace");
        
        
        /**
         * Gets the "types" element
         */
        public org.griphyn.vdl.model.TypesDocument.Types getTypes()
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.griphyn.vdl.model.TypesDocument.Types target = null;
                target = (org.griphyn.vdl.model.TypesDocument.Types)get_store().find_element_user(TYPES$0, 0);
                if (target == null)
                {
                    return null;
                }
                return target;
            }
        }
        
        /**
         * True if has "types" element
         */
        public boolean isSetTypes()
        {
            synchronized (monitor())
            {
                check_orphaned();
                return get_store().count_elements(TYPES$0) != 0;
            }
        }
        
        /**
         * Sets the "types" element
         */
        public void setTypes(org.griphyn.vdl.model.TypesDocument.Types types)
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.griphyn.vdl.model.TypesDocument.Types target = null;
                target = (org.griphyn.vdl.model.TypesDocument.Types)get_store().find_element_user(TYPES$0, 0);
                if (target == null)
                {
                    target = (org.griphyn.vdl.model.TypesDocument.Types)get_store().add_element_user(TYPES$0);
                }
                target.set(types);
            }
        }
        
        /**
         * Appends and returns a new empty "types" element
         */
        public org.griphyn.vdl.model.TypesDocument.Types addNewTypes()
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.griphyn.vdl.model.TypesDocument.Types target = null;
                target = (org.griphyn.vdl.model.TypesDocument.Types)get_store().add_element_user(TYPES$0);
                return target;
            }
        }
        
        /**
         * Unsets the "types" element
         */
        public void unsetTypes()
        {
            synchronized (monitor())
            {
                check_orphaned();
                get_store().remove_element(TYPES$0, 0);
            }
        }
        
        /**
         * Gets array of all "procedure" elements
         */
        public org.griphyn.vdl.model.Procedure[] getProcedureArray()
        {
            synchronized (monitor())
            {
                check_orphaned();
                java.util.List targetList = new java.util.ArrayList();
                get_store().find_all_element_users(PROCEDURE$2, targetList);
                org.griphyn.vdl.model.Procedure[] result = new org.griphyn.vdl.model.Procedure[targetList.size()];
                targetList.toArray(result);
                return result;
            }
        }
        
        /**
         * Gets ith "procedure" element
         */
        public org.griphyn.vdl.model.Procedure getProcedureArray(int i)
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.griphyn.vdl.model.Procedure target = null;
                target = (org.griphyn.vdl.model.Procedure)get_store().find_element_user(PROCEDURE$2, i);
                if (target == null)
                {
                    throw new IndexOutOfBoundsException();
                }
                return target;
            }
        }
        
        /**
         * Returns number of "procedure" element
         */
        public int sizeOfProcedureArray()
        {
            synchronized (monitor())
            {
                check_orphaned();
                return get_store().count_elements(PROCEDURE$2);
            }
        }
        
        /**
         * Sets array of all "procedure" element
         */
        public void setProcedureArray(org.griphyn.vdl.model.Procedure[] procedureArray)
        {
            synchronized (monitor())
            {
                check_orphaned();
                arraySetterHelper(procedureArray, PROCEDURE$2);
            }
        }
        
        /**
         * Sets ith "procedure" element
         */
        public void setProcedureArray(int i, org.griphyn.vdl.model.Procedure procedure)
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.griphyn.vdl.model.Procedure target = null;
                target = (org.griphyn.vdl.model.Procedure)get_store().find_element_user(PROCEDURE$2, i);
                if (target == null)
                {
                    throw new IndexOutOfBoundsException();
                }
                target.set(procedure);
            }
        }
        
        /**
         * Inserts and returns a new empty value (as xml) as the ith "procedure" element
         */
        public org.griphyn.vdl.model.Procedure insertNewProcedure(int i)
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.griphyn.vdl.model.Procedure target = null;
                target = (org.griphyn.vdl.model.Procedure)get_store().insert_element_user(PROCEDURE$2, i);
                return target;
            }
        }
        
        /**
         * Appends and returns a new empty value (as xml) as the last "procedure" element
         */
        public org.griphyn.vdl.model.Procedure addNewProcedure()
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.griphyn.vdl.model.Procedure target = null;
                target = (org.griphyn.vdl.model.Procedure)get_store().add_element_user(PROCEDURE$2);
                return target;
            }
        }
        
        /**
         * Removes the ith "procedure" element
         */
        public void removeProcedure(int i)
        {
            synchronized (monitor())
            {
                check_orphaned();
                get_store().remove_element(PROCEDURE$2, i);
            }
        }
        
        /**
         * Gets array of all "variable" elements
         */
        public org.griphyn.vdl.model.Variable[] getVariableArray()
        {
            synchronized (monitor())
            {
                check_orphaned();
                java.util.List targetList = new java.util.ArrayList();
                get_store().find_all_element_users(VARIABLE$4, targetList);
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
                target = (org.griphyn.vdl.model.Variable)get_store().find_element_user(VARIABLE$4, i);
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
                return get_store().count_elements(VARIABLE$4);
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
                arraySetterHelper(variableArray, VARIABLE$4);
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
                target = (org.griphyn.vdl.model.Variable)get_store().find_element_user(VARIABLE$4, i);
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
                target = (org.griphyn.vdl.model.Variable)get_store().insert_element_user(VARIABLE$4, i);
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
                target = (org.griphyn.vdl.model.Variable)get_store().add_element_user(VARIABLE$4);
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
                get_store().remove_element(VARIABLE$4, i);
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
                get_store().find_all_element_users(DATASET$6, targetList);
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
                target = (org.griphyn.vdl.model.Dataset)get_store().find_element_user(DATASET$6, i);
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
                return get_store().count_elements(DATASET$6);
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
                arraySetterHelper(datasetArray, DATASET$6);
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
                target = (org.griphyn.vdl.model.Dataset)get_store().find_element_user(DATASET$6, i);
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
                target = (org.griphyn.vdl.model.Dataset)get_store().insert_element_user(DATASET$6, i);
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
                target = (org.griphyn.vdl.model.Dataset)get_store().add_element_user(DATASET$6);
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
                get_store().remove_element(DATASET$6, i);
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
                get_store().find_all_element_users(ASSIGN$8, targetList);
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
                target = (org.griphyn.vdl.model.Assign)get_store().find_element_user(ASSIGN$8, i);
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
                return get_store().count_elements(ASSIGN$8);
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
                arraySetterHelper(assignArray, ASSIGN$8);
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
                target = (org.griphyn.vdl.model.Assign)get_store().find_element_user(ASSIGN$8, i);
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
                target = (org.griphyn.vdl.model.Assign)get_store().insert_element_user(ASSIGN$8, i);
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
                target = (org.griphyn.vdl.model.Assign)get_store().add_element_user(ASSIGN$8);
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
                get_store().remove_element(ASSIGN$8, i);
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
                get_store().find_all_element_users(CALL$10, targetList);
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
                target = (org.griphyn.vdl.model.Call)get_store().find_element_user(CALL$10, i);
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
                return get_store().count_elements(CALL$10);
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
                arraySetterHelper(callArray, CALL$10);
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
                target = (org.griphyn.vdl.model.Call)get_store().find_element_user(CALL$10, i);
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
                target = (org.griphyn.vdl.model.Call)get_store().insert_element_user(CALL$10, i);
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
                target = (org.griphyn.vdl.model.Call)get_store().add_element_user(CALL$10);
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
                get_store().remove_element(CALL$10, i);
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
                get_store().find_all_element_users(FOREACH$12, targetList);
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
                target = (org.griphyn.vdl.model.Foreach)get_store().find_element_user(FOREACH$12, i);
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
                return get_store().count_elements(FOREACH$12);
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
                arraySetterHelper(foreachArray, FOREACH$12);
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
                target = (org.griphyn.vdl.model.Foreach)get_store().find_element_user(FOREACH$12, i);
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
                target = (org.griphyn.vdl.model.Foreach)get_store().insert_element_user(FOREACH$12, i);
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
                target = (org.griphyn.vdl.model.Foreach)get_store().add_element_user(FOREACH$12);
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
                get_store().remove_element(FOREACH$12, i);
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
                get_store().find_all_element_users(IF$14, targetList);
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
                target = (org.griphyn.vdl.model.If)get_store().find_element_user(IF$14, i);
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
                return get_store().count_elements(IF$14);
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
                arraySetterHelper(xifArray, IF$14);
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
                target = (org.griphyn.vdl.model.If)get_store().find_element_user(IF$14, i);
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
                target = (org.griphyn.vdl.model.If)get_store().insert_element_user(IF$14, i);
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
                target = (org.griphyn.vdl.model.If)get_store().add_element_user(IF$14);
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
                get_store().remove_element(IF$14, i);
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
                get_store().find_all_element_users(WHILE$16, targetList);
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
                target = (org.griphyn.vdl.model.While)get_store().find_element_user(WHILE$16, i);
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
                return get_store().count_elements(WHILE$16);
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
                arraySetterHelper(xwhileArray, WHILE$16);
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
                target = (org.griphyn.vdl.model.While)get_store().find_element_user(WHILE$16, i);
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
                target = (org.griphyn.vdl.model.While)get_store().insert_element_user(WHILE$16, i);
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
                target = (org.griphyn.vdl.model.While)get_store().add_element_user(WHILE$16);
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
                get_store().remove_element(WHILE$16, i);
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
                get_store().find_all_element_users(REPEAT$18, targetList);
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
                target = (org.griphyn.vdl.model.Repeat)get_store().find_element_user(REPEAT$18, i);
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
                return get_store().count_elements(REPEAT$18);
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
                arraySetterHelper(repeatArray, REPEAT$18);
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
                target = (org.griphyn.vdl.model.Repeat)get_store().find_element_user(REPEAT$18, i);
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
                target = (org.griphyn.vdl.model.Repeat)get_store().insert_element_user(REPEAT$18, i);
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
                target = (org.griphyn.vdl.model.Repeat)get_store().add_element_user(REPEAT$18);
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
                get_store().remove_element(REPEAT$18, i);
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
                get_store().find_all_element_users(SWITCH$20, targetList);
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
                target = (org.griphyn.vdl.model.Switch)get_store().find_element_user(SWITCH$20, i);
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
                return get_store().count_elements(SWITCH$20);
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
                arraySetterHelper(xswitchArray, SWITCH$20);
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
                target = (org.griphyn.vdl.model.Switch)get_store().find_element_user(SWITCH$20, i);
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
                target = (org.griphyn.vdl.model.Switch)get_store().insert_element_user(SWITCH$20, i);
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
                target = (org.griphyn.vdl.model.Switch)get_store().add_element_user(SWITCH$20);
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
                get_store().remove_element(SWITCH$20, i);
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
                get_store().find_all_element_users(CONTINUE$22, targetList);
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
                target = (org.apache.xmlbeans.XmlObject)get_store().find_element_user(CONTINUE$22, i);
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
                return get_store().count_elements(CONTINUE$22);
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
                arraySetterHelper(xcontinueArray, CONTINUE$22);
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
                target = (org.apache.xmlbeans.XmlObject)get_store().find_element_user(CONTINUE$22, i);
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
                target = (org.apache.xmlbeans.XmlObject)get_store().insert_element_user(CONTINUE$22, i);
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
                target = (org.apache.xmlbeans.XmlObject)get_store().add_element_user(CONTINUE$22);
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
                get_store().remove_element(CONTINUE$22, i);
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
                get_store().find_all_element_users(BREAK$24, targetList);
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
                target = (org.apache.xmlbeans.XmlObject)get_store().find_element_user(BREAK$24, i);
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
                return get_store().count_elements(BREAK$24);
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
                arraySetterHelper(xbreakArray, BREAK$24);
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
                target = (org.apache.xmlbeans.XmlObject)get_store().find_element_user(BREAK$24, i);
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
                target = (org.apache.xmlbeans.XmlObject)get_store().insert_element_user(BREAK$24, i);
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
                target = (org.apache.xmlbeans.XmlObject)get_store().add_element_user(BREAK$24);
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
                get_store().remove_element(BREAK$24, i);
            }
        }
        
        /**
         * Gets the "targetNamespace" attribute
         */
        public java.lang.String getTargetNamespace()
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.apache.xmlbeans.SimpleValue target = null;
                target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(TARGETNAMESPACE$26);
                if (target == null)
                {
                    return null;
                }
                return target.getStringValue();
            }
        }
        
        /**
         * Gets (as xml) the "targetNamespace" attribute
         */
        public org.apache.xmlbeans.XmlAnyURI xgetTargetNamespace()
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.apache.xmlbeans.XmlAnyURI target = null;
                target = (org.apache.xmlbeans.XmlAnyURI)get_store().find_attribute_user(TARGETNAMESPACE$26);
                return target;
            }
        }
        
        /**
         * True if has "targetNamespace" attribute
         */
        public boolean isSetTargetNamespace()
        {
            synchronized (monitor())
            {
                check_orphaned();
                return get_store().find_attribute_user(TARGETNAMESPACE$26) != null;
            }
        }
        
        /**
         * Sets the "targetNamespace" attribute
         */
        public void setTargetNamespace(java.lang.String targetNamespace)
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.apache.xmlbeans.SimpleValue target = null;
                target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(TARGETNAMESPACE$26);
                if (target == null)
                {
                    target = (org.apache.xmlbeans.SimpleValue)get_store().add_attribute_user(TARGETNAMESPACE$26);
                }
                target.setStringValue(targetNamespace);
            }
        }
        
        /**
         * Sets (as xml) the "targetNamespace" attribute
         */
        public void xsetTargetNamespace(org.apache.xmlbeans.XmlAnyURI targetNamespace)
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.apache.xmlbeans.XmlAnyURI target = null;
                target = (org.apache.xmlbeans.XmlAnyURI)get_store().find_attribute_user(TARGETNAMESPACE$26);
                if (target == null)
                {
                    target = (org.apache.xmlbeans.XmlAnyURI)get_store().add_attribute_user(TARGETNAMESPACE$26);
                }
                target.set(targetNamespace);
            }
        }
        
        /**
         * Unsets the "targetNamespace" attribute
         */
        public void unsetTargetNamespace()
        {
            synchronized (monitor())
            {
                check_orphaned();
                get_store().remove_attribute(TARGETNAMESPACE$26);
            }
        }
    }
}
