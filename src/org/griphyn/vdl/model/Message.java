/*
 * XML Type:  Message
 * Namespace: http://www.griphyn.org/2006/08/vdl
 * Java type: org.griphyn.vdl.model.Message
 *
 * Automatically generated - do not modify.
 */
package org.griphyn.vdl.model;


/**
 * An XML Message(@http://www.griphyn.org/2006/08/vdl).
 *
 * This is a complex type.
 */
public interface Message extends org.apache.xmlbeans.XmlObject
{
    public static final org.apache.xmlbeans.SchemaType type = (org.apache.xmlbeans.SchemaType)
        org.apache.xmlbeans.XmlBeans.typeSystemForClassLoader(Message.class.getClassLoader(), "schemaorg_apache_xmlbeans.system.s846598305CE89DEF7AE9D98B8ED64120").resolveHandle("message6a6dtype");
    
    /**
     * Gets array of all "part" elements
     */
    org.griphyn.vdl.model.Part[] getPartArray();
    
    /**
     * Gets ith "part" element
     */
    org.griphyn.vdl.model.Part getPartArray(int i);
    
    /**
     * Returns number of "part" element
     */
    int sizeOfPartArray();
    
    /**
     * Sets array of all "part" element
     */
    void setPartArray(org.griphyn.vdl.model.Part[] partArray);
    
    /**
     * Sets ith "part" element
     */
    void setPartArray(int i, org.griphyn.vdl.model.Part part);
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "part" element
     */
    org.griphyn.vdl.model.Part insertNewPart(int i);
    
    /**
     * Appends and returns a new empty value (as xml) as the last "part" element
     */
    org.griphyn.vdl.model.Part addNewPart();
    
    /**
     * Removes the ith "part" element
     */
    void removePart(int i);
    
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
        public static org.griphyn.vdl.model.Message newInstance() {
          return (org.griphyn.vdl.model.Message) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, null ); }
        
        public static org.griphyn.vdl.model.Message newInstance(org.apache.xmlbeans.XmlOptions options) {
          return (org.griphyn.vdl.model.Message) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, options ); }
        
        /** @param xmlAsString the string value to parse */
        public static org.griphyn.vdl.model.Message parse(java.lang.String xmlAsString) throws org.apache.xmlbeans.XmlException {
          return (org.griphyn.vdl.model.Message) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xmlAsString, type, null ); }
        
        public static org.griphyn.vdl.model.Message parse(java.lang.String xmlAsString, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (org.griphyn.vdl.model.Message) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xmlAsString, type, options ); }
        
        /** @param file the file from which to load an xml document */
        public static org.griphyn.vdl.model.Message parse(java.io.File file) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (org.griphyn.vdl.model.Message) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( file, type, null ); }
        
        public static org.griphyn.vdl.model.Message parse(java.io.File file, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (org.griphyn.vdl.model.Message) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( file, type, options ); }
        
        public static org.griphyn.vdl.model.Message parse(java.net.URL u) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (org.griphyn.vdl.model.Message) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( u, type, null ); }
        
        public static org.griphyn.vdl.model.Message parse(java.net.URL u, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (org.griphyn.vdl.model.Message) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( u, type, options ); }
        
        public static org.griphyn.vdl.model.Message parse(java.io.InputStream is) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (org.griphyn.vdl.model.Message) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( is, type, null ); }
        
        public static org.griphyn.vdl.model.Message parse(java.io.InputStream is, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (org.griphyn.vdl.model.Message) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( is, type, options ); }
        
        public static org.griphyn.vdl.model.Message parse(java.io.Reader r) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (org.griphyn.vdl.model.Message) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( r, type, null ); }
        
        public static org.griphyn.vdl.model.Message parse(java.io.Reader r, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (org.griphyn.vdl.model.Message) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( r, type, options ); }
        
        public static org.griphyn.vdl.model.Message parse(javax.xml.stream.XMLStreamReader sr) throws org.apache.xmlbeans.XmlException {
          return (org.griphyn.vdl.model.Message) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( sr, type, null ); }
        
        public static org.griphyn.vdl.model.Message parse(javax.xml.stream.XMLStreamReader sr, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (org.griphyn.vdl.model.Message) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( sr, type, options ); }
        
        public static org.griphyn.vdl.model.Message parse(org.w3c.dom.Node node) throws org.apache.xmlbeans.XmlException {
          return (org.griphyn.vdl.model.Message) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( node, type, null ); }
        
        public static org.griphyn.vdl.model.Message parse(org.w3c.dom.Node node, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (org.griphyn.vdl.model.Message) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( node, type, options ); }
        
        /** @deprecated {@link XMLInputStream} */
        public static org.griphyn.vdl.model.Message parse(org.apache.xmlbeans.xml.stream.XMLInputStream xis) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return (org.griphyn.vdl.model.Message) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xis, type, null ); }
        
        /** @deprecated {@link XMLInputStream} */
        public static org.griphyn.vdl.model.Message parse(org.apache.xmlbeans.xml.stream.XMLInputStream xis, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return (org.griphyn.vdl.model.Message) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xis, type, options ); }
        
        /** @deprecated {@link XMLInputStream} */
        public static org.apache.xmlbeans.xml.stream.XMLInputStream newValidatingXMLInputStream(org.apache.xmlbeans.xml.stream.XMLInputStream xis) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newValidatingXMLInputStream( xis, type, null ); }
        
        /** @deprecated {@link XMLInputStream} */
        public static org.apache.xmlbeans.xml.stream.XMLInputStream newValidatingXMLInputStream(org.apache.xmlbeans.xml.stream.XMLInputStream xis, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newValidatingXMLInputStream( xis, type, options ); }
        
        private Factory() { } // No instance of this class allowed
    }
}
