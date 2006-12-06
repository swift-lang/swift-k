/*
 * XML Type:  Dataset
 * Namespace: http://www.griphyn.org/2006/08/vdl
 * Java type: org.griphyn.vdl.model.Dataset
 *
 * Automatically generated - do not modify.
 */
package org.griphyn.vdl.model.impl;
/**
 * An XML Dataset(@http://www.griphyn.org/2006/08/vdl).
 *
 * This is a complex type.
 */
public class DatasetImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements org.griphyn.vdl.model.Dataset
{
    
    public DatasetImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName FILE$0 = 
        new javax.xml.namespace.QName("http://www.griphyn.org/2006/08/vdl", "file");
    private static final javax.xml.namespace.QName MAPPING$2 = 
        new javax.xml.namespace.QName("http://www.griphyn.org/2006/08/vdl", "mapping");
    private static final javax.xml.namespace.QName NAME$4 = 
        new javax.xml.namespace.QName("", "name");
    private static final javax.xml.namespace.QName TYPE$6 = 
        new javax.xml.namespace.QName("", "type");
    private static final javax.xml.namespace.QName ISARRAY1$8 = 
        new javax.xml.namespace.QName("", "isArray");
    
    
    /**
     * Gets the "file" element
     */
    public org.griphyn.vdl.model.Dataset.File getFile()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.griphyn.vdl.model.Dataset.File target = null;
            target = (org.griphyn.vdl.model.Dataset.File)get_store().find_element_user(FILE$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * True if has "file" element
     */
    public boolean isSetFile()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(FILE$0) != 0;
        }
    }
    
    /**
     * Sets the "file" element
     */
    public void setFile(org.griphyn.vdl.model.Dataset.File file)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.griphyn.vdl.model.Dataset.File target = null;
            target = (org.griphyn.vdl.model.Dataset.File)get_store().find_element_user(FILE$0, 0);
            if (target == null)
            {
                target = (org.griphyn.vdl.model.Dataset.File)get_store().add_element_user(FILE$0);
            }
            target.set(file);
        }
    }
    
    /**
     * Appends and returns a new empty "file" element
     */
    public org.griphyn.vdl.model.Dataset.File addNewFile()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.griphyn.vdl.model.Dataset.File target = null;
            target = (org.griphyn.vdl.model.Dataset.File)get_store().add_element_user(FILE$0);
            return target;
        }
    }
    
    /**
     * Unsets the "file" element
     */
    public void unsetFile()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(FILE$0, 0);
        }
    }
    
    /**
     * Gets the "mapping" element
     */
    public org.griphyn.vdl.model.Dataset.Mapping getMapping()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.griphyn.vdl.model.Dataset.Mapping target = null;
            target = (org.griphyn.vdl.model.Dataset.Mapping)get_store().find_element_user(MAPPING$2, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * True if has "mapping" element
     */
    public boolean isSetMapping()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(MAPPING$2) != 0;
        }
    }
    
    /**
     * Sets the "mapping" element
     */
    public void setMapping(org.griphyn.vdl.model.Dataset.Mapping mapping)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.griphyn.vdl.model.Dataset.Mapping target = null;
            target = (org.griphyn.vdl.model.Dataset.Mapping)get_store().find_element_user(MAPPING$2, 0);
            if (target == null)
            {
                target = (org.griphyn.vdl.model.Dataset.Mapping)get_store().add_element_user(MAPPING$2);
            }
            target.set(mapping);
        }
    }
    
    /**
     * Appends and returns a new empty "mapping" element
     */
    public org.griphyn.vdl.model.Dataset.Mapping addNewMapping()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.griphyn.vdl.model.Dataset.Mapping target = null;
            target = (org.griphyn.vdl.model.Dataset.Mapping)get_store().add_element_user(MAPPING$2);
            return target;
        }
    }
    
    /**
     * Unsets the "mapping" element
     */
    public void unsetMapping()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(MAPPING$2, 0);
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
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(NAME$4);
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
            target = (org.apache.xmlbeans.XmlNCName)get_store().find_attribute_user(NAME$4);
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
            return get_store().find_attribute_user(NAME$4) != null;
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
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(NAME$4);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_attribute_user(NAME$4);
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
            target = (org.apache.xmlbeans.XmlNCName)get_store().find_attribute_user(NAME$4);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlNCName)get_store().add_attribute_user(NAME$4);
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
            get_store().remove_attribute(NAME$4);
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
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(TYPE$6);
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
            target = (org.apache.xmlbeans.XmlQName)get_store().find_attribute_user(TYPE$6);
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
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(TYPE$6);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_attribute_user(TYPE$6);
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
            target = (org.apache.xmlbeans.XmlQName)get_store().find_attribute_user(TYPE$6);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlQName)get_store().add_attribute_user(TYPE$6);
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
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(ISARRAY1$8);
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
            target = (org.apache.xmlbeans.XmlBoolean)get_store().find_attribute_user(ISARRAY1$8);
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
            return get_store().find_attribute_user(ISARRAY1$8) != null;
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
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(ISARRAY1$8);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_attribute_user(ISARRAY1$8);
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
            target = (org.apache.xmlbeans.XmlBoolean)get_store().find_attribute_user(ISARRAY1$8);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlBoolean)get_store().add_attribute_user(ISARRAY1$8);
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
            get_store().remove_attribute(ISARRAY1$8);
        }
    }
    /**
     * An XML file(@http://www.griphyn.org/2006/08/vdl).
     *
     * This is a complex type.
     */
    public static class FileImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements org.griphyn.vdl.model.Dataset.File
    {
        
        public FileImpl(org.apache.xmlbeans.SchemaType sType)
        {
            super(sType);
        }
        
        private static final javax.xml.namespace.QName NAME$0 = 
            new javax.xml.namespace.QName("", "name");
        
        
        /**
         * Gets the "name" attribute
         */
        public java.lang.String getName()
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.apache.xmlbeans.SimpleValue target = null;
                target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(NAME$0);
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
                target = (org.apache.xmlbeans.XmlString)get_store().find_attribute_user(NAME$0);
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
                return get_store().find_attribute_user(NAME$0) != null;
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
                target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(NAME$0);
                if (target == null)
                {
                    target = (org.apache.xmlbeans.SimpleValue)get_store().add_attribute_user(NAME$0);
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
                target = (org.apache.xmlbeans.XmlString)get_store().find_attribute_user(NAME$0);
                if (target == null)
                {
                    target = (org.apache.xmlbeans.XmlString)get_store().add_attribute_user(NAME$0);
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
                get_store().remove_attribute(NAME$0);
            }
        }
    }
    /**
     * An XML mapping(@http://www.griphyn.org/2006/08/vdl).
     *
     * This is a complex type.
     */
    public static class MappingImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements org.griphyn.vdl.model.Dataset.Mapping
    {
        
        public MappingImpl(org.apache.xmlbeans.SchemaType sType)
        {
            super(sType);
        }
        
        private static final javax.xml.namespace.QName PARAM$0 = 
            new javax.xml.namespace.QName("http://www.griphyn.org/2006/08/vdl", "param");
        private static final javax.xml.namespace.QName DESCRIPTOR$2 = 
            new javax.xml.namespace.QName("", "descriptor");
        
        
        /**
         * Gets array of all "param" elements
         */
        public org.griphyn.vdl.model.Dataset.Mapping.Param[] getParamArray()
        {
            synchronized (monitor())
            {
                check_orphaned();
                java.util.List targetList = new java.util.ArrayList();
                get_store().find_all_element_users(PARAM$0, targetList);
                org.griphyn.vdl.model.Dataset.Mapping.Param[] result = new org.griphyn.vdl.model.Dataset.Mapping.Param[targetList.size()];
                targetList.toArray(result);
                return result;
            }
        }
        
        /**
         * Gets ith "param" element
         */
        public org.griphyn.vdl.model.Dataset.Mapping.Param getParamArray(int i)
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.griphyn.vdl.model.Dataset.Mapping.Param target = null;
                target = (org.griphyn.vdl.model.Dataset.Mapping.Param)get_store().find_element_user(PARAM$0, i);
                if (target == null)
                {
                    throw new IndexOutOfBoundsException();
                }
                return target;
            }
        }
        
        /**
         * Returns number of "param" element
         */
        public int sizeOfParamArray()
        {
            synchronized (monitor())
            {
                check_orphaned();
                return get_store().count_elements(PARAM$0);
            }
        }
        
        /**
         * Sets array of all "param" element
         */
        public void setParamArray(org.griphyn.vdl.model.Dataset.Mapping.Param[] paramArray)
        {
            synchronized (monitor())
            {
                check_orphaned();
                arraySetterHelper(paramArray, PARAM$0);
            }
        }
        
        /**
         * Sets ith "param" element
         */
        public void setParamArray(int i, org.griphyn.vdl.model.Dataset.Mapping.Param param)
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.griphyn.vdl.model.Dataset.Mapping.Param target = null;
                target = (org.griphyn.vdl.model.Dataset.Mapping.Param)get_store().find_element_user(PARAM$0, i);
                if (target == null)
                {
                    throw new IndexOutOfBoundsException();
                }
                target.set(param);
            }
        }
        
        /**
         * Inserts and returns a new empty value (as xml) as the ith "param" element
         */
        public org.griphyn.vdl.model.Dataset.Mapping.Param insertNewParam(int i)
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.griphyn.vdl.model.Dataset.Mapping.Param target = null;
                target = (org.griphyn.vdl.model.Dataset.Mapping.Param)get_store().insert_element_user(PARAM$0, i);
                return target;
            }
        }
        
        /**
         * Appends and returns a new empty value (as xml) as the last "param" element
         */
        public org.griphyn.vdl.model.Dataset.Mapping.Param addNewParam()
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.griphyn.vdl.model.Dataset.Mapping.Param target = null;
                target = (org.griphyn.vdl.model.Dataset.Mapping.Param)get_store().add_element_user(PARAM$0);
                return target;
            }
        }
        
        /**
         * Removes the ith "param" element
         */
        public void removeParam(int i)
        {
            synchronized (monitor())
            {
                check_orphaned();
                get_store().remove_element(PARAM$0, i);
            }
        }
        
        /**
         * Gets the "descriptor" attribute
         */
        public java.lang.String getDescriptor()
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.apache.xmlbeans.SimpleValue target = null;
                target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(DESCRIPTOR$2);
                if (target == null)
                {
                    return null;
                }
                return target.getStringValue();
            }
        }
        
        /**
         * Gets (as xml) the "descriptor" attribute
         */
        public org.apache.xmlbeans.XmlString xgetDescriptor()
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.apache.xmlbeans.XmlString target = null;
                target = (org.apache.xmlbeans.XmlString)get_store().find_attribute_user(DESCRIPTOR$2);
                return target;
            }
        }
        
        /**
         * Sets the "descriptor" attribute
         */
        public void setDescriptor(java.lang.String descriptor)
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.apache.xmlbeans.SimpleValue target = null;
                target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(DESCRIPTOR$2);
                if (target == null)
                {
                    target = (org.apache.xmlbeans.SimpleValue)get_store().add_attribute_user(DESCRIPTOR$2);
                }
                target.setStringValue(descriptor);
            }
        }
        
        /**
         * Sets (as xml) the "descriptor" attribute
         */
        public void xsetDescriptor(org.apache.xmlbeans.XmlString descriptor)
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.apache.xmlbeans.XmlString target = null;
                target = (org.apache.xmlbeans.XmlString)get_store().find_attribute_user(DESCRIPTOR$2);
                if (target == null)
                {
                    target = (org.apache.xmlbeans.XmlString)get_store().add_attribute_user(DESCRIPTOR$2);
                }
                target.set(descriptor);
            }
        }
        /**
         * An XML param(@http://www.griphyn.org/2006/08/vdl).
         *
         * This is a complex type.
         */
        public static class ParamImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements org.griphyn.vdl.model.Dataset.Mapping.Param
        {
            
            public ParamImpl(org.apache.xmlbeans.SchemaType sType)
            {
                super(sType);
            }
            
            private static final javax.xml.namespace.QName FUNCTION$0 = 
                new javax.xml.namespace.QName("http://www.griphyn.org/2006/08/vdl", "function");
            private static final javax.xml.namespace.QName NAME$2 = 
                new javax.xml.namespace.QName("", "name");
            private static final javax.xml.namespace.QName VALUE$4 = 
                new javax.xml.namespace.QName("", "value");
            
            
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
            public org.apache.xmlbeans.XmlNMTOKEN xgetName()
            {
                synchronized (monitor())
                {
                    check_orphaned();
                    org.apache.xmlbeans.XmlNMTOKEN target = null;
                    target = (org.apache.xmlbeans.XmlNMTOKEN)get_store().find_attribute_user(NAME$2);
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
            public void xsetName(org.apache.xmlbeans.XmlNMTOKEN name)
            {
                synchronized (monitor())
                {
                    check_orphaned();
                    org.apache.xmlbeans.XmlNMTOKEN target = null;
                    target = (org.apache.xmlbeans.XmlNMTOKEN)get_store().find_attribute_user(NAME$2);
                    if (target == null)
                    {
                      target = (org.apache.xmlbeans.XmlNMTOKEN)get_store().add_attribute_user(NAME$2);
                    }
                    target.set(name);
                }
            }
            
            /**
             * Gets the "value" attribute
             */
            public java.lang.String getValue()
            {
                synchronized (monitor())
                {
                    check_orphaned();
                    org.apache.xmlbeans.SimpleValue target = null;
                    target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(VALUE$4);
                    if (target == null)
                    {
                      return null;
                    }
                    return target.getStringValue();
                }
            }
            
            /**
             * Gets (as xml) the "value" attribute
             */
            public org.apache.xmlbeans.XmlString xgetValue()
            {
                synchronized (monitor())
                {
                    check_orphaned();
                    org.apache.xmlbeans.XmlString target = null;
                    target = (org.apache.xmlbeans.XmlString)get_store().find_attribute_user(VALUE$4);
                    return target;
                }
            }
            
            /**
             * True if has "value" attribute
             */
            public boolean isSetValue()
            {
                synchronized (monitor())
                {
                    check_orphaned();
                    return get_store().find_attribute_user(VALUE$4) != null;
                }
            }
            
            /**
             * Sets the "value" attribute
             */
            public void setValue(java.lang.String value)
            {
                synchronized (monitor())
                {
                    check_orphaned();
                    org.apache.xmlbeans.SimpleValue target = null;
                    target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(VALUE$4);
                    if (target == null)
                    {
                      target = (org.apache.xmlbeans.SimpleValue)get_store().add_attribute_user(VALUE$4);
                    }
                    target.setStringValue(value);
                }
            }
            
            /**
             * Sets (as xml) the "value" attribute
             */
            public void xsetValue(org.apache.xmlbeans.XmlString value)
            {
                synchronized (monitor())
                {
                    check_orphaned();
                    org.apache.xmlbeans.XmlString target = null;
                    target = (org.apache.xmlbeans.XmlString)get_store().find_attribute_user(VALUE$4);
                    if (target == null)
                    {
                      target = (org.apache.xmlbeans.XmlString)get_store().add_attribute_user(VALUE$4);
                    }
                    target.set(value);
                }
            }
            
            /**
             * Unsets the "value" attribute
             */
            public void unsetValue()
            {
                synchronized (monitor())
                {
                    check_orphaned();
                    get_store().remove_attribute(VALUE$4);
                }
            }
        }
    }
}
