/*
 * XML Type:  Call
 * Namespace: http://www.griphyn.org/2006/08/vdl
 * Java type: org.griphyn.vdl.model.Call
 *
 * Automatically generated - do not modify.
 */
package org.griphyn.vdl.model;


/**
 * An XML Call(@http://www.griphyn.org/2006/08/vdl).
 *
 * This is a complex type.
 */
public interface Call extends org.apache.xmlbeans.XmlObject
{
    public static final org.apache.xmlbeans.SchemaType type = (org.apache.xmlbeans.SchemaType)
        org.apache.xmlbeans.XmlBeans.typeSystemForClassLoader(Call.class.getClassLoader(), "schemaorg_apache_xmlbeans.system.s846598305CE89DEF7AE9D98B8ED64120").resolveHandle("call1ec4type");
    
    /**
     * Gets array of all "output" elements
     */
    org.griphyn.vdl.model.ActualParameter[] getOutputArray();
    
    /**
     * Gets ith "output" element
     */
    org.griphyn.vdl.model.ActualParameter getOutputArray(int i);
    
    /**
     * Returns number of "output" element
     */
    int sizeOfOutputArray();
    
    /**
     * Sets array of all "output" element
     */
    void setOutputArray(org.griphyn.vdl.model.ActualParameter[] outputArray);
    
    /**
     * Sets ith "output" element
     */
    void setOutputArray(int i, org.griphyn.vdl.model.ActualParameter output);
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "output" element
     */
    org.griphyn.vdl.model.ActualParameter insertNewOutput(int i);
    
    /**
     * Appends and returns a new empty value (as xml) as the last "output" element
     */
    org.griphyn.vdl.model.ActualParameter addNewOutput();
    
    /**
     * Removes the ith "output" element
     */
    void removeOutput(int i);
    
    /**
     * Gets array of all "input" elements
     */
    org.griphyn.vdl.model.ActualParameter[] getInputArray();
    
    /**
     * Gets ith "input" element
     */
    org.griphyn.vdl.model.ActualParameter getInputArray(int i);
    
    /**
     * Returns number of "input" element
     */
    int sizeOfInputArray();
    
    /**
     * Sets array of all "input" element
     */
    void setInputArray(org.griphyn.vdl.model.ActualParameter[] inputArray);
    
    /**
     * Sets ith "input" element
     */
    void setInputArray(int i, org.griphyn.vdl.model.ActualParameter input);
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "input" element
     */
    org.griphyn.vdl.model.ActualParameter insertNewInput(int i);
    
    /**
     * Appends and returns a new empty value (as xml) as the last "input" element
     */
    org.griphyn.vdl.model.ActualParameter addNewInput();
    
    /**
     * Removes the ith "input" element
     */
    void removeInput(int i);
    
    /**
     * Gets the "id" attribute
     */
    java.lang.String getId();
    
    /**
     * Gets (as xml) the "id" attribute
     */
    org.apache.xmlbeans.XmlID xgetId();
    
    /**
     * True if has "id" attribute
     */
    boolean isSetId();
    
    /**
     * Sets the "id" attribute
     */
    void setId(java.lang.String id);
    
    /**
     * Sets (as xml) the "id" attribute
     */
    void xsetId(org.apache.xmlbeans.XmlID id);
    
    /**
     * Unsets the "id" attribute
     */
    void unsetId();
    
    /**
     * Gets the "proc" attribute
     */
    javax.xml.namespace.QName getProc();
    
    /**
     * Gets (as xml) the "proc" attribute
     */
    org.apache.xmlbeans.XmlQName xgetProc();
    
    /**
     * Sets the "proc" attribute
     */
    void setProc(javax.xml.namespace.QName proc);
    
    /**
     * Sets (as xml) the "proc" attribute
     */
    void xsetProc(org.apache.xmlbeans.XmlQName proc);
    
    /**
     * A factory class with static methods for creating instances
     * of this type.
     */
    
    public static final class Factory
    {
        public static org.griphyn.vdl.model.Call newInstance() {
          return (org.griphyn.vdl.model.Call) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, null ); }
        
        public static org.griphyn.vdl.model.Call newInstance(org.apache.xmlbeans.XmlOptions options) {
          return (org.griphyn.vdl.model.Call) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, options ); }
        
        /** @param xmlAsString the string value to parse */
        public static org.griphyn.vdl.model.Call parse(java.lang.String xmlAsString) throws org.apache.xmlbeans.XmlException {
          return (org.griphyn.vdl.model.Call) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xmlAsString, type, null ); }
        
        public static org.griphyn.vdl.model.Call parse(java.lang.String xmlAsString, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (org.griphyn.vdl.model.Call) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xmlAsString, type, options ); }
        
        /** @param file the file from which to load an xml document */
        public static org.griphyn.vdl.model.Call parse(java.io.File file) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (org.griphyn.vdl.model.Call) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( file, type, null ); }
        
        public static org.griphyn.vdl.model.Call parse(java.io.File file, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (org.griphyn.vdl.model.Call) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( file, type, options ); }
        
        public static org.griphyn.vdl.model.Call parse(java.net.URL u) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (org.griphyn.vdl.model.Call) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( u, type, null ); }
        
        public static org.griphyn.vdl.model.Call parse(java.net.URL u, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (org.griphyn.vdl.model.Call) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( u, type, options ); }
        
        public static org.griphyn.vdl.model.Call parse(java.io.InputStream is) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (org.griphyn.vdl.model.Call) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( is, type, null ); }
        
        public static org.griphyn.vdl.model.Call parse(java.io.InputStream is, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (org.griphyn.vdl.model.Call) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( is, type, options ); }
        
        public static org.griphyn.vdl.model.Call parse(java.io.Reader r) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (org.griphyn.vdl.model.Call) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( r, type, null ); }
        
        public static org.griphyn.vdl.model.Call parse(java.io.Reader r, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (org.griphyn.vdl.model.Call) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( r, type, options ); }
        
        public static org.griphyn.vdl.model.Call parse(javax.xml.stream.XMLStreamReader sr) throws org.apache.xmlbeans.XmlException {
          return (org.griphyn.vdl.model.Call) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( sr, type, null ); }
        
        public static org.griphyn.vdl.model.Call parse(javax.xml.stream.XMLStreamReader sr, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (org.griphyn.vdl.model.Call) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( sr, type, options ); }
        
        public static org.griphyn.vdl.model.Call parse(org.w3c.dom.Node node) throws org.apache.xmlbeans.XmlException {
          return (org.griphyn.vdl.model.Call) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( node, type, null ); }
        
        public static org.griphyn.vdl.model.Call parse(org.w3c.dom.Node node, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (org.griphyn.vdl.model.Call) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( node, type, options ); }
        
        /** @deprecated {@link XMLInputStream} */
        public static org.griphyn.vdl.model.Call parse(org.apache.xmlbeans.xml.stream.XMLInputStream xis) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return (org.griphyn.vdl.model.Call) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xis, type, null ); }
        
        /** @deprecated {@link XMLInputStream} */
        public static org.griphyn.vdl.model.Call parse(org.apache.xmlbeans.xml.stream.XMLInputStream xis, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return (org.griphyn.vdl.model.Call) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xis, type, options ); }
        
        /** @deprecated {@link XMLInputStream} */
        public static org.apache.xmlbeans.xml.stream.XMLInputStream newValidatingXMLInputStream(org.apache.xmlbeans.xml.stream.XMLInputStream xis) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newValidatingXMLInputStream( xis, type, null ); }
        
        /** @deprecated {@link XMLInputStream} */
        public static org.apache.xmlbeans.xml.stream.XMLInputStream newValidatingXMLInputStream(org.apache.xmlbeans.xml.stream.XMLInputStream xis, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newValidatingXMLInputStream( xis, type, options ); }
        
        private Factory() { } // No instance of this class allowed
    }
}
