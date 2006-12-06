/*
 * XML Type:  ApplicationBinding
 * Namespace: http://www.griphyn.org/2006/08/vdl
 * Java type: org.griphyn.vdl.model.ApplicationBinding
 *
 * Automatically generated - do not modify.
 */
package org.griphyn.vdl.model;


/**
 * An XML ApplicationBinding(@http://www.griphyn.org/2006/08/vdl).
 *
 * This is a complex type.
 */
public interface ApplicationBinding extends org.apache.xmlbeans.XmlObject
{
    public static final org.apache.xmlbeans.SchemaType type = (org.apache.xmlbeans.SchemaType)
        org.apache.xmlbeans.XmlBeans.typeSystemForClassLoader(ApplicationBinding.class.getClassLoader(), "schemaorg_apache_xmlbeans.system.s846598305CE89DEF7AE9D98B8ED64120").resolveHandle("applicationbinding1a6dtype");
    
    /**
     * Gets the "appenv" element
     */
    org.apache.xmlbeans.XmlObject getAppenv();
    
    /**
     * True if has "appenv" element
     */
    boolean isSetAppenv();
    
    /**
     * Sets the "appenv" element
     */
    void setAppenv(org.apache.xmlbeans.XmlObject appenv);
    
    /**
     * Appends and returns a new empty "appenv" element
     */
    org.apache.xmlbeans.XmlObject addNewAppenv();
    
    /**
     * Unsets the "appenv" element
     */
    void unsetAppenv();
    
    /**
     * Gets the "executable" element
     */
    org.apache.xmlbeans.XmlObject getExecutable();
    
    /**
     * Sets the "executable" element
     */
    void setExecutable(org.apache.xmlbeans.XmlObject executable);
    
    /**
     * Appends and returns a new empty "executable" element
     */
    org.apache.xmlbeans.XmlObject addNewExecutable();
    
    /**
     * Gets array of all "argument" elements
     */
    org.griphyn.vdl.model.Argument[] getArgumentArray();
    
    /**
     * Gets ith "argument" element
     */
    org.griphyn.vdl.model.Argument getArgumentArray(int i);
    
    /**
     * Returns number of "argument" element
     */
    int sizeOfArgumentArray();
    
    /**
     * Sets array of all "argument" element
     */
    void setArgumentArray(org.griphyn.vdl.model.Argument[] argumentArray);
    
    /**
     * Sets ith "argument" element
     */
    void setArgumentArray(int i, org.griphyn.vdl.model.Argument argument);
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "argument" element
     */
    org.griphyn.vdl.model.Argument insertNewArgument(int i);
    
    /**
     * Appends and returns a new empty value (as xml) as the last "argument" element
     */
    org.griphyn.vdl.model.Argument addNewArgument();
    
    /**
     * Removes the ith "argument" element
     */
    void removeArgument(int i);
    
    /**
     * Gets the "stdin" element
     */
    org.griphyn.vdl.model.Argument getStdin();
    
    /**
     * True if has "stdin" element
     */
    boolean isSetStdin();
    
    /**
     * Sets the "stdin" element
     */
    void setStdin(org.griphyn.vdl.model.Argument stdin);
    
    /**
     * Appends and returns a new empty "stdin" element
     */
    org.griphyn.vdl.model.Argument addNewStdin();
    
    /**
     * Unsets the "stdin" element
     */
    void unsetStdin();
    
    /**
     * Gets the "stdout" element
     */
    org.griphyn.vdl.model.Argument getStdout();
    
    /**
     * True if has "stdout" element
     */
    boolean isSetStdout();
    
    /**
     * Sets the "stdout" element
     */
    void setStdout(org.griphyn.vdl.model.Argument stdout);
    
    /**
     * Appends and returns a new empty "stdout" element
     */
    org.griphyn.vdl.model.Argument addNewStdout();
    
    /**
     * Unsets the "stdout" element
     */
    void unsetStdout();
    
    /**
     * Gets the "stderr" element
     */
    org.griphyn.vdl.model.Argument getStderr();
    
    /**
     * True if has "stderr" element
     */
    boolean isSetStderr();
    
    /**
     * Sets the "stderr" element
     */
    void setStderr(org.griphyn.vdl.model.Argument stderr);
    
    /**
     * Appends and returns a new empty "stderr" element
     */
    org.griphyn.vdl.model.Argument addNewStderr();
    
    /**
     * Unsets the "stderr" element
     */
    void unsetStderr();
    
    /**
     * Gets array of all "profile" elements
     */
    org.griphyn.vdl.model.Profile[] getProfileArray();
    
    /**
     * Gets ith "profile" element
     */
    org.griphyn.vdl.model.Profile getProfileArray(int i);
    
    /**
     * Returns number of "profile" element
     */
    int sizeOfProfileArray();
    
    /**
     * Sets array of all "profile" element
     */
    void setProfileArray(org.griphyn.vdl.model.Profile[] profileArray);
    
    /**
     * Sets ith "profile" element
     */
    void setProfileArray(int i, org.griphyn.vdl.model.Profile profile);
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "profile" element
     */
    org.griphyn.vdl.model.Profile insertNewProfile(int i);
    
    /**
     * Appends and returns a new empty value (as xml) as the last "profile" element
     */
    org.griphyn.vdl.model.Profile addNewProfile();
    
    /**
     * Removes the ith "profile" element
     */
    void removeProfile(int i);
    
    /**
     * A factory class with static methods for creating instances
     * of this type.
     */
    
    public static final class Factory
    {
        public static org.griphyn.vdl.model.ApplicationBinding newInstance() {
          return (org.griphyn.vdl.model.ApplicationBinding) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, null ); }
        
        public static org.griphyn.vdl.model.ApplicationBinding newInstance(org.apache.xmlbeans.XmlOptions options) {
          return (org.griphyn.vdl.model.ApplicationBinding) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, options ); }
        
        /** @param xmlAsString the string value to parse */
        public static org.griphyn.vdl.model.ApplicationBinding parse(java.lang.String xmlAsString) throws org.apache.xmlbeans.XmlException {
          return (org.griphyn.vdl.model.ApplicationBinding) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xmlAsString, type, null ); }
        
        public static org.griphyn.vdl.model.ApplicationBinding parse(java.lang.String xmlAsString, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (org.griphyn.vdl.model.ApplicationBinding) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xmlAsString, type, options ); }
        
        /** @param file the file from which to load an xml document */
        public static org.griphyn.vdl.model.ApplicationBinding parse(java.io.File file) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (org.griphyn.vdl.model.ApplicationBinding) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( file, type, null ); }
        
        public static org.griphyn.vdl.model.ApplicationBinding parse(java.io.File file, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (org.griphyn.vdl.model.ApplicationBinding) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( file, type, options ); }
        
        public static org.griphyn.vdl.model.ApplicationBinding parse(java.net.URL u) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (org.griphyn.vdl.model.ApplicationBinding) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( u, type, null ); }
        
        public static org.griphyn.vdl.model.ApplicationBinding parse(java.net.URL u, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (org.griphyn.vdl.model.ApplicationBinding) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( u, type, options ); }
        
        public static org.griphyn.vdl.model.ApplicationBinding parse(java.io.InputStream is) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (org.griphyn.vdl.model.ApplicationBinding) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( is, type, null ); }
        
        public static org.griphyn.vdl.model.ApplicationBinding parse(java.io.InputStream is, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (org.griphyn.vdl.model.ApplicationBinding) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( is, type, options ); }
        
        public static org.griphyn.vdl.model.ApplicationBinding parse(java.io.Reader r) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (org.griphyn.vdl.model.ApplicationBinding) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( r, type, null ); }
        
        public static org.griphyn.vdl.model.ApplicationBinding parse(java.io.Reader r, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (org.griphyn.vdl.model.ApplicationBinding) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( r, type, options ); }
        
        public static org.griphyn.vdl.model.ApplicationBinding parse(javax.xml.stream.XMLStreamReader sr) throws org.apache.xmlbeans.XmlException {
          return (org.griphyn.vdl.model.ApplicationBinding) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( sr, type, null ); }
        
        public static org.griphyn.vdl.model.ApplicationBinding parse(javax.xml.stream.XMLStreamReader sr, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (org.griphyn.vdl.model.ApplicationBinding) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( sr, type, options ); }
        
        public static org.griphyn.vdl.model.ApplicationBinding parse(org.w3c.dom.Node node) throws org.apache.xmlbeans.XmlException {
          return (org.griphyn.vdl.model.ApplicationBinding) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( node, type, null ); }
        
        public static org.griphyn.vdl.model.ApplicationBinding parse(org.w3c.dom.Node node, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (org.griphyn.vdl.model.ApplicationBinding) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( node, type, options ); }
        
        /** @deprecated {@link XMLInputStream} */
        public static org.griphyn.vdl.model.ApplicationBinding parse(org.apache.xmlbeans.xml.stream.XMLInputStream xis) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return (org.griphyn.vdl.model.ApplicationBinding) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xis, type, null ); }
        
        /** @deprecated {@link XMLInputStream} */
        public static org.griphyn.vdl.model.ApplicationBinding parse(org.apache.xmlbeans.xml.stream.XMLInputStream xis, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return (org.griphyn.vdl.model.ApplicationBinding) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xis, type, options ); }
        
        /** @deprecated {@link XMLInputStream} */
        public static org.apache.xmlbeans.xml.stream.XMLInputStream newValidatingXMLInputStream(org.apache.xmlbeans.xml.stream.XMLInputStream xis) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newValidatingXMLInputStream( xis, type, null ); }
        
        /** @deprecated {@link XMLInputStream} */
        public static org.apache.xmlbeans.xml.stream.XMLInputStream newValidatingXMLInputStream(org.apache.xmlbeans.xml.stream.XMLInputStream xis, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newValidatingXMLInputStream( xis, type, options ); }
        
        private Factory() { } // No instance of this class allowed
    }
}
