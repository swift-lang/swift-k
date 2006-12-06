/*
 * XML Type:  Dataset
 * Namespace: http://www.griphyn.org/2006/08/vdl
 * Java type: org.griphyn.vdl.model.Dataset
 *
 * Automatically generated - do not modify.
 */
package org.griphyn.vdl.model;


/**
 * An XML Dataset(@http://www.griphyn.org/2006/08/vdl).
 *
 * This is a complex type.
 */
public interface Dataset extends org.apache.xmlbeans.XmlObject
{
    public static final org.apache.xmlbeans.SchemaType type = (org.apache.xmlbeans.SchemaType)
        org.apache.xmlbeans.XmlBeans.typeSystemForClassLoader(Dataset.class.getClassLoader(), "schemaorg_apache_xmlbeans.system.s846598305CE89DEF7AE9D98B8ED64120").resolveHandle("dataset185ctype");
    
    /**
     * Gets the "file" element
     */
    org.griphyn.vdl.model.Dataset.File getFile();
    
    /**
     * True if has "file" element
     */
    boolean isSetFile();
    
    /**
     * Sets the "file" element
     */
    void setFile(org.griphyn.vdl.model.Dataset.File file);
    
    /**
     * Appends and returns a new empty "file" element
     */
    org.griphyn.vdl.model.Dataset.File addNewFile();
    
    /**
     * Unsets the "file" element
     */
    void unsetFile();
    
    /**
     * Gets the "mapping" element
     */
    org.griphyn.vdl.model.Dataset.Mapping getMapping();
    
    /**
     * True if has "mapping" element
     */
    boolean isSetMapping();
    
    /**
     * Sets the "mapping" element
     */
    void setMapping(org.griphyn.vdl.model.Dataset.Mapping mapping);
    
    /**
     * Appends and returns a new empty "mapping" element
     */
    org.griphyn.vdl.model.Dataset.Mapping addNewMapping();
    
    /**
     * Unsets the "mapping" element
     */
    void unsetMapping();
    
    /**
     * Gets the "name" attribute
     */
    java.lang.String getName();
    
    /**
     * Gets (as xml) the "name" attribute
     */
    org.apache.xmlbeans.XmlNCName xgetName();
    
    /**
     * True if has "name" attribute
     */
    boolean isSetName();
    
    /**
     * Sets the "name" attribute
     */
    void setName(java.lang.String name);
    
    /**
     * Sets (as xml) the "name" attribute
     */
    void xsetName(org.apache.xmlbeans.XmlNCName name);
    
    /**
     * Unsets the "name" attribute
     */
    void unsetName();
    
    /**
     * Gets the "type" attribute
     */
    javax.xml.namespace.QName getType();
    
    /**
     * Gets (as xml) the "type" attribute
     */
    org.apache.xmlbeans.XmlQName xgetType();
    
    /**
     * Sets the "type" attribute
     */
    void setType(javax.xml.namespace.QName type);
    
    /**
     * Sets (as xml) the "type" attribute
     */
    void xsetType(org.apache.xmlbeans.XmlQName type);
    
    /**
     * Gets the "isArray" attribute
     */
    boolean getIsArray1();
    
    /**
     * Gets (as xml) the "isArray" attribute
     */
    org.apache.xmlbeans.XmlBoolean xgetIsArray1();
    
    /**
     * True if has "isArray" attribute
     */
    boolean isSetIsArray1();
    
    /**
     * Sets the "isArray" attribute
     */
    void setIsArray1(boolean isArray1);
    
    /**
     * Sets (as xml) the "isArray" attribute
     */
    void xsetIsArray1(org.apache.xmlbeans.XmlBoolean isArray1);
    
    /**
     * Unsets the "isArray" attribute
     */
    void unsetIsArray1();
    
    /**
     * An XML file(@http://www.griphyn.org/2006/08/vdl).
     *
     * This is a complex type.
     */
    public interface File extends org.apache.xmlbeans.XmlObject
    {
        public static final org.apache.xmlbeans.SchemaType type = (org.apache.xmlbeans.SchemaType)
            org.apache.xmlbeans.XmlBeans.typeSystemForClassLoader(File.class.getClassLoader(), "schemaorg_apache_xmlbeans.system.s846598305CE89DEF7AE9D98B8ED64120").resolveHandle("filead44elemtype");
        
        /**
         * Gets the "name" attribute
         */
        java.lang.String getName();
        
        /**
         * Gets (as xml) the "name" attribute
         */
        org.apache.xmlbeans.XmlString xgetName();
        
        /**
         * True if has "name" attribute
         */
        boolean isSetName();
        
        /**
         * Sets the "name" attribute
         */
        void setName(java.lang.String name);
        
        /**
         * Sets (as xml) the "name" attribute
         */
        void xsetName(org.apache.xmlbeans.XmlString name);
        
        /**
         * Unsets the "name" attribute
         */
        void unsetName();
        
        /**
         * A factory class with static methods for creating instances
         * of this type.
         */
        
        public static final class Factory
        {
            public static org.griphyn.vdl.model.Dataset.File newInstance() {
              return (org.griphyn.vdl.model.Dataset.File) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, null ); }
            
            public static org.griphyn.vdl.model.Dataset.File newInstance(org.apache.xmlbeans.XmlOptions options) {
              return (org.griphyn.vdl.model.Dataset.File) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, options ); }
            
            private Factory() { } // No instance of this class allowed
        }
    }
    
    /**
     * An XML mapping(@http://www.griphyn.org/2006/08/vdl).
     *
     * This is a complex type.
     */
    public interface Mapping extends org.apache.xmlbeans.XmlObject
    {
        public static final org.apache.xmlbeans.SchemaType type = (org.apache.xmlbeans.SchemaType)
            org.apache.xmlbeans.XmlBeans.typeSystemForClassLoader(Mapping.class.getClassLoader(), "schemaorg_apache_xmlbeans.system.s846598305CE89DEF7AE9D98B8ED64120").resolveHandle("mapping9962elemtype");
        
        /**
         * Gets array of all "param" elements
         */
        org.griphyn.vdl.model.Dataset.Mapping.Param[] getParamArray();
        
        /**
         * Gets ith "param" element
         */
        org.griphyn.vdl.model.Dataset.Mapping.Param getParamArray(int i);
        
        /**
         * Returns number of "param" element
         */
        int sizeOfParamArray();
        
        /**
         * Sets array of all "param" element
         */
        void setParamArray(org.griphyn.vdl.model.Dataset.Mapping.Param[] paramArray);
        
        /**
         * Sets ith "param" element
         */
        void setParamArray(int i, org.griphyn.vdl.model.Dataset.Mapping.Param param);
        
        /**
         * Inserts and returns a new empty value (as xml) as the ith "param" element
         */
        org.griphyn.vdl.model.Dataset.Mapping.Param insertNewParam(int i);
        
        /**
         * Appends and returns a new empty value (as xml) as the last "param" element
         */
        org.griphyn.vdl.model.Dataset.Mapping.Param addNewParam();
        
        /**
         * Removes the ith "param" element
         */
        void removeParam(int i);
        
        /**
         * Gets the "descriptor" attribute
         */
        java.lang.String getDescriptor();
        
        /**
         * Gets (as xml) the "descriptor" attribute
         */
        org.apache.xmlbeans.XmlString xgetDescriptor();
        
        /**
         * Sets the "descriptor" attribute
         */
        void setDescriptor(java.lang.String descriptor);
        
        /**
         * Sets (as xml) the "descriptor" attribute
         */
        void xsetDescriptor(org.apache.xmlbeans.XmlString descriptor);
        
        /**
         * An XML param(@http://www.griphyn.org/2006/08/vdl).
         *
         * This is a complex type.
         */
        public interface Param extends org.apache.xmlbeans.XmlObject
        {
            public static final org.apache.xmlbeans.SchemaType type = (org.apache.xmlbeans.SchemaType)
                org.apache.xmlbeans.XmlBeans.typeSystemForClassLoader(Param.class.getClassLoader(), "schemaorg_apache_xmlbeans.system.s846598305CE89DEF7AE9D98B8ED64120").resolveHandle("paramc649elemtype");
            
            /**
             * Gets array of all "function" elements
             */
            org.griphyn.vdl.model.Function[] getFunctionArray();
            
            /**
             * Gets ith "function" element
             */
            org.griphyn.vdl.model.Function getFunctionArray(int i);
            
            /**
             * Returns number of "function" element
             */
            int sizeOfFunctionArray();
            
            /**
             * Sets array of all "function" element
             */
            void setFunctionArray(org.griphyn.vdl.model.Function[] functionArray);
            
            /**
             * Sets ith "function" element
             */
            void setFunctionArray(int i, org.griphyn.vdl.model.Function function);
            
            /**
             * Inserts and returns a new empty value (as xml) as the ith "function" element
             */
            org.griphyn.vdl.model.Function insertNewFunction(int i);
            
            /**
             * Appends and returns a new empty value (as xml) as the last "function" element
             */
            org.griphyn.vdl.model.Function addNewFunction();
            
            /**
             * Removes the ith "function" element
             */
            void removeFunction(int i);
            
            /**
             * Gets the "name" attribute
             */
            java.lang.String getName();
            
            /**
             * Gets (as xml) the "name" attribute
             */
            org.apache.xmlbeans.XmlNMTOKEN xgetName();
            
            /**
             * Sets the "name" attribute
             */
            void setName(java.lang.String name);
            
            /**
             * Sets (as xml) the "name" attribute
             */
            void xsetName(org.apache.xmlbeans.XmlNMTOKEN name);
            
            /**
             * Gets the "value" attribute
             */
            java.lang.String getValue();
            
            /**
             * Gets (as xml) the "value" attribute
             */
            org.apache.xmlbeans.XmlString xgetValue();
            
            /**
             * True if has "value" attribute
             */
            boolean isSetValue();
            
            /**
             * Sets the "value" attribute
             */
            void setValue(java.lang.String value);
            
            /**
             * Sets (as xml) the "value" attribute
             */
            void xsetValue(org.apache.xmlbeans.XmlString value);
            
            /**
             * Unsets the "value" attribute
             */
            void unsetValue();
            
            /**
             * A factory class with static methods for creating instances
             * of this type.
             */
            
            public static final class Factory
            {
                public static org.griphyn.vdl.model.Dataset.Mapping.Param newInstance() {
                  return (org.griphyn.vdl.model.Dataset.Mapping.Param) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, null ); }
                
                public static org.griphyn.vdl.model.Dataset.Mapping.Param newInstance(org.apache.xmlbeans.XmlOptions options) {
                  return (org.griphyn.vdl.model.Dataset.Mapping.Param) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, options ); }
                
                private Factory() { } // No instance of this class allowed
            }
        }
        
        /**
         * A factory class with static methods for creating instances
         * of this type.
         */
        
        public static final class Factory
        {
            public static org.griphyn.vdl.model.Dataset.Mapping newInstance() {
              return (org.griphyn.vdl.model.Dataset.Mapping) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, null ); }
            
            public static org.griphyn.vdl.model.Dataset.Mapping newInstance(org.apache.xmlbeans.XmlOptions options) {
              return (org.griphyn.vdl.model.Dataset.Mapping) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, options ); }
            
            private Factory() { } // No instance of this class allowed
        }
    }
    
    /**
     * A factory class with static methods for creating instances
     * of this type.
     */
    
    public static final class Factory
    {
        public static org.griphyn.vdl.model.Dataset newInstance() {
          return (org.griphyn.vdl.model.Dataset) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, null ); }
        
        public static org.griphyn.vdl.model.Dataset newInstance(org.apache.xmlbeans.XmlOptions options) {
          return (org.griphyn.vdl.model.Dataset) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, options ); }
        
        /** @param xmlAsString the string value to parse */
        public static org.griphyn.vdl.model.Dataset parse(java.lang.String xmlAsString) throws org.apache.xmlbeans.XmlException {
          return (org.griphyn.vdl.model.Dataset) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xmlAsString, type, null ); }
        
        public static org.griphyn.vdl.model.Dataset parse(java.lang.String xmlAsString, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (org.griphyn.vdl.model.Dataset) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xmlAsString, type, options ); }
        
        /** @param file the file from which to load an xml document */
        public static org.griphyn.vdl.model.Dataset parse(java.io.File file) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (org.griphyn.vdl.model.Dataset) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( file, type, null ); }
        
        public static org.griphyn.vdl.model.Dataset parse(java.io.File file, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (org.griphyn.vdl.model.Dataset) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( file, type, options ); }
        
        public static org.griphyn.vdl.model.Dataset parse(java.net.URL u) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (org.griphyn.vdl.model.Dataset) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( u, type, null ); }
        
        public static org.griphyn.vdl.model.Dataset parse(java.net.URL u, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (org.griphyn.vdl.model.Dataset) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( u, type, options ); }
        
        public static org.griphyn.vdl.model.Dataset parse(java.io.InputStream is) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (org.griphyn.vdl.model.Dataset) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( is, type, null ); }
        
        public static org.griphyn.vdl.model.Dataset parse(java.io.InputStream is, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (org.griphyn.vdl.model.Dataset) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( is, type, options ); }
        
        public static org.griphyn.vdl.model.Dataset parse(java.io.Reader r) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (org.griphyn.vdl.model.Dataset) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( r, type, null ); }
        
        public static org.griphyn.vdl.model.Dataset parse(java.io.Reader r, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (org.griphyn.vdl.model.Dataset) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( r, type, options ); }
        
        public static org.griphyn.vdl.model.Dataset parse(javax.xml.stream.XMLStreamReader sr) throws org.apache.xmlbeans.XmlException {
          return (org.griphyn.vdl.model.Dataset) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( sr, type, null ); }
        
        public static org.griphyn.vdl.model.Dataset parse(javax.xml.stream.XMLStreamReader sr, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (org.griphyn.vdl.model.Dataset) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( sr, type, options ); }
        
        public static org.griphyn.vdl.model.Dataset parse(org.w3c.dom.Node node) throws org.apache.xmlbeans.XmlException {
          return (org.griphyn.vdl.model.Dataset) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( node, type, null ); }
        
        public static org.griphyn.vdl.model.Dataset parse(org.w3c.dom.Node node, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (org.griphyn.vdl.model.Dataset) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( node, type, options ); }
        
        /** @deprecated {@link XMLInputStream} */
        public static org.griphyn.vdl.model.Dataset parse(org.apache.xmlbeans.xml.stream.XMLInputStream xis) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return (org.griphyn.vdl.model.Dataset) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xis, type, null ); }
        
        /** @deprecated {@link XMLInputStream} */
        public static org.griphyn.vdl.model.Dataset parse(org.apache.xmlbeans.xml.stream.XMLInputStream xis, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return (org.griphyn.vdl.model.Dataset) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xis, type, options ); }
        
        /** @deprecated {@link XMLInputStream} */
        public static org.apache.xmlbeans.xml.stream.XMLInputStream newValidatingXMLInputStream(org.apache.xmlbeans.xml.stream.XMLInputStream xis) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newValidatingXMLInputStream( xis, type, null ); }
        
        /** @deprecated {@link XMLInputStream} */
        public static org.apache.xmlbeans.xml.stream.XMLInputStream newValidatingXMLInputStream(org.apache.xmlbeans.xml.stream.XMLInputStream xis, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newValidatingXMLInputStream( xis, type, options ); }
        
        private Factory() { } // No instance of this class allowed
    }
}
