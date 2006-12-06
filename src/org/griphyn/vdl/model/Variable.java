/*
 * XML Type:  Variable
 * Namespace: http://www.griphyn.org/2006/08/vdl
 * Java type: org.griphyn.vdl.model.Variable
 *
 * Automatically generated - do not modify.
 */
package org.griphyn.vdl.model;


/**
 * An XML Variable(@http://www.griphyn.org/2006/08/vdl).
 *
 * This is a complex type.
 */
public interface Variable extends org.apache.xmlbeans.XmlObject
{
    public static final org.apache.xmlbeans.SchemaType type = (org.apache.xmlbeans.SchemaType)
        org.apache.xmlbeans.XmlBeans.typeSystemForClassLoader(Variable.class.getClassLoader(), "schemaorg_apache_xmlbeans.system.s846598305CE89DEF7AE9D98B8ED64120").resolveHandle("variablede66type");
    
    /**
     * Gets the "array" element
     */
    org.griphyn.vdl.model.Array getArray();
    
    /**
     * True if has "array" element
     */
    boolean isSetArray();
    
    /**
     * Sets the "array" element
     */
    void setArray(org.griphyn.vdl.model.Array array);
    
    /**
     * Appends and returns a new empty "array" element
     */
    org.griphyn.vdl.model.Array addNewArray();
    
    /**
     * Unsets the "array" element
     */
    void unsetArray();
    
    /**
     * Gets the "range" element
     */
    org.griphyn.vdl.model.Range getRange();
    
    /**
     * True if has "range" element
     */
    boolean isSetRange();
    
    /**
     * Sets the "range" element
     */
    void setRange(org.griphyn.vdl.model.Range range);
    
    /**
     * Appends and returns a new empty "range" element
     */
    org.griphyn.vdl.model.Range addNewRange();
    
    /**
     * Unsets the "range" element
     */
    void unsetRange();
    
    /**
     * Gets the "function" element
     */
    org.griphyn.vdl.model.Function getFunction();
    
    /**
     * True if has "function" element
     */
    boolean isSetFunction();
    
    /**
     * Sets the "function" element
     */
    void setFunction(org.griphyn.vdl.model.Function function);
    
    /**
     * Appends and returns a new empty "function" element
     */
    org.griphyn.vdl.model.Function addNewFunction();
    
    /**
     * Unsets the "function" element
     */
    void unsetFunction();
    
    /**
     * Gets the "name" attribute
     */
    java.lang.String getName();
    
    /**
     * Gets (as xml) the "name" attribute
     */
    org.apache.xmlbeans.XmlNCName xgetName();
    
    /**
     * Sets the "name" attribute
     */
    void setName(java.lang.String name);
    
    /**
     * Sets (as xml) the "name" attribute
     */
    void xsetName(org.apache.xmlbeans.XmlNCName name);
    
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
     * A factory class with static methods for creating instances
     * of this type.
     */
    
    public static final class Factory
    {
        public static org.griphyn.vdl.model.Variable newInstance() {
          return (org.griphyn.vdl.model.Variable) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, null ); }
        
        public static org.griphyn.vdl.model.Variable newInstance(org.apache.xmlbeans.XmlOptions options) {
          return (org.griphyn.vdl.model.Variable) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, options ); }
        
        /** @param xmlAsString the string value to parse */
        public static org.griphyn.vdl.model.Variable parse(java.lang.String xmlAsString) throws org.apache.xmlbeans.XmlException {
          return (org.griphyn.vdl.model.Variable) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xmlAsString, type, null ); }
        
        public static org.griphyn.vdl.model.Variable parse(java.lang.String xmlAsString, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (org.griphyn.vdl.model.Variable) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xmlAsString, type, options ); }
        
        /** @param file the file from which to load an xml document */
        public static org.griphyn.vdl.model.Variable parse(java.io.File file) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (org.griphyn.vdl.model.Variable) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( file, type, null ); }
        
        public static org.griphyn.vdl.model.Variable parse(java.io.File file, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (org.griphyn.vdl.model.Variable) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( file, type, options ); }
        
        public static org.griphyn.vdl.model.Variable parse(java.net.URL u) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (org.griphyn.vdl.model.Variable) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( u, type, null ); }
        
        public static org.griphyn.vdl.model.Variable parse(java.net.URL u, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (org.griphyn.vdl.model.Variable) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( u, type, options ); }
        
        public static org.griphyn.vdl.model.Variable parse(java.io.InputStream is) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (org.griphyn.vdl.model.Variable) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( is, type, null ); }
        
        public static org.griphyn.vdl.model.Variable parse(java.io.InputStream is, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (org.griphyn.vdl.model.Variable) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( is, type, options ); }
        
        public static org.griphyn.vdl.model.Variable parse(java.io.Reader r) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (org.griphyn.vdl.model.Variable) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( r, type, null ); }
        
        public static org.griphyn.vdl.model.Variable parse(java.io.Reader r, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (org.griphyn.vdl.model.Variable) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( r, type, options ); }
        
        public static org.griphyn.vdl.model.Variable parse(javax.xml.stream.XMLStreamReader sr) throws org.apache.xmlbeans.XmlException {
          return (org.griphyn.vdl.model.Variable) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( sr, type, null ); }
        
        public static org.griphyn.vdl.model.Variable parse(javax.xml.stream.XMLStreamReader sr, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (org.griphyn.vdl.model.Variable) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( sr, type, options ); }
        
        public static org.griphyn.vdl.model.Variable parse(org.w3c.dom.Node node) throws org.apache.xmlbeans.XmlException {
          return (org.griphyn.vdl.model.Variable) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( node, type, null ); }
        
        public static org.griphyn.vdl.model.Variable parse(org.w3c.dom.Node node, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (org.griphyn.vdl.model.Variable) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( node, type, options ); }
        
        /** @deprecated {@link XMLInputStream} */
        public static org.griphyn.vdl.model.Variable parse(org.apache.xmlbeans.xml.stream.XMLInputStream xis) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return (org.griphyn.vdl.model.Variable) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xis, type, null ); }
        
        /** @deprecated {@link XMLInputStream} */
        public static org.griphyn.vdl.model.Variable parse(org.apache.xmlbeans.xml.stream.XMLInputStream xis, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return (org.griphyn.vdl.model.Variable) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xis, type, options ); }
        
        /** @deprecated {@link XMLInputStream} */
        public static org.apache.xmlbeans.xml.stream.XMLInputStream newValidatingXMLInputStream(org.apache.xmlbeans.xml.stream.XMLInputStream xis) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newValidatingXMLInputStream( xis, type, null ); }
        
        /** @deprecated {@link XMLInputStream} */
        public static org.apache.xmlbeans.xml.stream.XMLInputStream newValidatingXMLInputStream(org.apache.xmlbeans.xml.stream.XMLInputStream xis, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newValidatingXMLInputStream( xis, type, options ); }
        
        private Factory() { } // No instance of this class allowed
    }
}
