/*
 * XML Type:  While
 * Namespace: http://www.griphyn.org/2006/08/vdl
 * Java type: org.griphyn.vdl.model.While
 *
 * Automatically generated - do not modify.
 */
package org.griphyn.vdl.model;


/**
 * An XML While(@http://www.griphyn.org/2006/08/vdl).
 *
 * This is a complex type.
 */
public interface While extends org.apache.xmlbeans.XmlObject
{
    public static final org.apache.xmlbeans.SchemaType type = (org.apache.xmlbeans.SchemaType)
        org.apache.xmlbeans.XmlBeans.typeSystemForClassLoader(While.class.getClassLoader(), "schemaorg_apache_xmlbeans.system.s846598305CE89DEF7AE9D98B8ED64120").resolveHandle("while3d43type");
    
    /**
     * Gets array of all "variable" elements
     */
    org.griphyn.vdl.model.Variable[] getVariableArray();
    
    /**
     * Gets ith "variable" element
     */
    org.griphyn.vdl.model.Variable getVariableArray(int i);
    
    /**
     * Returns number of "variable" element
     */
    int sizeOfVariableArray();
    
    /**
     * Sets array of all "variable" element
     */
    void setVariableArray(org.griphyn.vdl.model.Variable[] variableArray);
    
    /**
     * Sets ith "variable" element
     */
    void setVariableArray(int i, org.griphyn.vdl.model.Variable variable);
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "variable" element
     */
    org.griphyn.vdl.model.Variable insertNewVariable(int i);
    
    /**
     * Appends and returns a new empty value (as xml) as the last "variable" element
     */
    org.griphyn.vdl.model.Variable addNewVariable();
    
    /**
     * Removes the ith "variable" element
     */
    void removeVariable(int i);
    
    /**
     * Gets array of all "dataset" elements
     */
    org.griphyn.vdl.model.Dataset[] getDatasetArray();
    
    /**
     * Gets ith "dataset" element
     */
    org.griphyn.vdl.model.Dataset getDatasetArray(int i);
    
    /**
     * Returns number of "dataset" element
     */
    int sizeOfDatasetArray();
    
    /**
     * Sets array of all "dataset" element
     */
    void setDatasetArray(org.griphyn.vdl.model.Dataset[] datasetArray);
    
    /**
     * Sets ith "dataset" element
     */
    void setDatasetArray(int i, org.griphyn.vdl.model.Dataset dataset);
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "dataset" element
     */
    org.griphyn.vdl.model.Dataset insertNewDataset(int i);
    
    /**
     * Appends and returns a new empty value (as xml) as the last "dataset" element
     */
    org.griphyn.vdl.model.Dataset addNewDataset();
    
    /**
     * Removes the ith "dataset" element
     */
    void removeDataset(int i);
    
    /**
     * Gets array of all "assign" elements
     */
    org.griphyn.vdl.model.Assign[] getAssignArray();
    
    /**
     * Gets ith "assign" element
     */
    org.griphyn.vdl.model.Assign getAssignArray(int i);
    
    /**
     * Returns number of "assign" element
     */
    int sizeOfAssignArray();
    
    /**
     * Sets array of all "assign" element
     */
    void setAssignArray(org.griphyn.vdl.model.Assign[] assignArray);
    
    /**
     * Sets ith "assign" element
     */
    void setAssignArray(int i, org.griphyn.vdl.model.Assign assign);
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "assign" element
     */
    org.griphyn.vdl.model.Assign insertNewAssign(int i);
    
    /**
     * Appends and returns a new empty value (as xml) as the last "assign" element
     */
    org.griphyn.vdl.model.Assign addNewAssign();
    
    /**
     * Removes the ith "assign" element
     */
    void removeAssign(int i);
    
    /**
     * Gets array of all "call" elements
     */
    org.griphyn.vdl.model.Call[] getCallArray();
    
    /**
     * Gets ith "call" element
     */
    org.griphyn.vdl.model.Call getCallArray(int i);
    
    /**
     * Returns number of "call" element
     */
    int sizeOfCallArray();
    
    /**
     * Sets array of all "call" element
     */
    void setCallArray(org.griphyn.vdl.model.Call[] callArray);
    
    /**
     * Sets ith "call" element
     */
    void setCallArray(int i, org.griphyn.vdl.model.Call call);
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "call" element
     */
    org.griphyn.vdl.model.Call insertNewCall(int i);
    
    /**
     * Appends and returns a new empty value (as xml) as the last "call" element
     */
    org.griphyn.vdl.model.Call addNewCall();
    
    /**
     * Removes the ith "call" element
     */
    void removeCall(int i);
    
    /**
     * Gets array of all "foreach" elements
     */
    org.griphyn.vdl.model.Foreach[] getForeachArray();
    
    /**
     * Gets ith "foreach" element
     */
    org.griphyn.vdl.model.Foreach getForeachArray(int i);
    
    /**
     * Returns number of "foreach" element
     */
    int sizeOfForeachArray();
    
    /**
     * Sets array of all "foreach" element
     */
    void setForeachArray(org.griphyn.vdl.model.Foreach[] foreachArray);
    
    /**
     * Sets ith "foreach" element
     */
    void setForeachArray(int i, org.griphyn.vdl.model.Foreach foreach);
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "foreach" element
     */
    org.griphyn.vdl.model.Foreach insertNewForeach(int i);
    
    /**
     * Appends and returns a new empty value (as xml) as the last "foreach" element
     */
    org.griphyn.vdl.model.Foreach addNewForeach();
    
    /**
     * Removes the ith "foreach" element
     */
    void removeForeach(int i);
    
    /**
     * Gets array of all "if" elements
     */
    org.griphyn.vdl.model.If[] getIfArray();
    
    /**
     * Gets ith "if" element
     */
    org.griphyn.vdl.model.If getIfArray(int i);
    
    /**
     * Returns number of "if" element
     */
    int sizeOfIfArray();
    
    /**
     * Sets array of all "if" element
     */
    void setIfArray(org.griphyn.vdl.model.If[] xifArray);
    
    /**
     * Sets ith "if" element
     */
    void setIfArray(int i, org.griphyn.vdl.model.If xif);
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "if" element
     */
    org.griphyn.vdl.model.If insertNewIf(int i);
    
    /**
     * Appends and returns a new empty value (as xml) as the last "if" element
     */
    org.griphyn.vdl.model.If addNewIf();
    
    /**
     * Removes the ith "if" element
     */
    void removeIf(int i);
    
    /**
     * Gets array of all "while" elements
     */
    org.griphyn.vdl.model.While[] getWhileArray();
    
    /**
     * Gets ith "while" element
     */
    org.griphyn.vdl.model.While getWhileArray(int i);
    
    /**
     * Returns number of "while" element
     */
    int sizeOfWhileArray();
    
    /**
     * Sets array of all "while" element
     */
    void setWhileArray(org.griphyn.vdl.model.While[] xwhileArray);
    
    /**
     * Sets ith "while" element
     */
    void setWhileArray(int i, org.griphyn.vdl.model.While xwhile);
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "while" element
     */
    org.griphyn.vdl.model.While insertNewWhile(int i);
    
    /**
     * Appends and returns a new empty value (as xml) as the last "while" element
     */
    org.griphyn.vdl.model.While addNewWhile();
    
    /**
     * Removes the ith "while" element
     */
    void removeWhile(int i);
    
    /**
     * Gets array of all "repeat" elements
     */
    org.griphyn.vdl.model.Repeat[] getRepeatArray();
    
    /**
     * Gets ith "repeat" element
     */
    org.griphyn.vdl.model.Repeat getRepeatArray(int i);
    
    /**
     * Returns number of "repeat" element
     */
    int sizeOfRepeatArray();
    
    /**
     * Sets array of all "repeat" element
     */
    void setRepeatArray(org.griphyn.vdl.model.Repeat[] repeatArray);
    
    /**
     * Sets ith "repeat" element
     */
    void setRepeatArray(int i, org.griphyn.vdl.model.Repeat repeat);
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "repeat" element
     */
    org.griphyn.vdl.model.Repeat insertNewRepeat(int i);
    
    /**
     * Appends and returns a new empty value (as xml) as the last "repeat" element
     */
    org.griphyn.vdl.model.Repeat addNewRepeat();
    
    /**
     * Removes the ith "repeat" element
     */
    void removeRepeat(int i);
    
    /**
     * Gets array of all "switch" elements
     */
    org.griphyn.vdl.model.Switch[] getSwitchArray();
    
    /**
     * Gets ith "switch" element
     */
    org.griphyn.vdl.model.Switch getSwitchArray(int i);
    
    /**
     * Returns number of "switch" element
     */
    int sizeOfSwitchArray();
    
    /**
     * Sets array of all "switch" element
     */
    void setSwitchArray(org.griphyn.vdl.model.Switch[] xswitchArray);
    
    /**
     * Sets ith "switch" element
     */
    void setSwitchArray(int i, org.griphyn.vdl.model.Switch xswitch);
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "switch" element
     */
    org.griphyn.vdl.model.Switch insertNewSwitch(int i);
    
    /**
     * Appends and returns a new empty value (as xml) as the last "switch" element
     */
    org.griphyn.vdl.model.Switch addNewSwitch();
    
    /**
     * Removes the ith "switch" element
     */
    void removeSwitch(int i);
    
    /**
     * Gets array of all "continue" elements
     */
    org.apache.xmlbeans.XmlObject[] getContinueArray();
    
    /**
     * Gets ith "continue" element
     */
    org.apache.xmlbeans.XmlObject getContinueArray(int i);
    
    /**
     * Returns number of "continue" element
     */
    int sizeOfContinueArray();
    
    /**
     * Sets array of all "continue" element
     */
    void setContinueArray(org.apache.xmlbeans.XmlObject[] xcontinueArray);
    
    /**
     * Sets ith "continue" element
     */
    void setContinueArray(int i, org.apache.xmlbeans.XmlObject xcontinue);
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "continue" element
     */
    org.apache.xmlbeans.XmlObject insertNewContinue(int i);
    
    /**
     * Appends and returns a new empty value (as xml) as the last "continue" element
     */
    org.apache.xmlbeans.XmlObject addNewContinue();
    
    /**
     * Removes the ith "continue" element
     */
    void removeContinue(int i);
    
    /**
     * Gets array of all "break" elements
     */
    org.apache.xmlbeans.XmlObject[] getBreakArray();
    
    /**
     * Gets ith "break" element
     */
    org.apache.xmlbeans.XmlObject getBreakArray(int i);
    
    /**
     * Returns number of "break" element
     */
    int sizeOfBreakArray();
    
    /**
     * Sets array of all "break" element
     */
    void setBreakArray(org.apache.xmlbeans.XmlObject[] xbreakArray);
    
    /**
     * Sets ith "break" element
     */
    void setBreakArray(int i, org.apache.xmlbeans.XmlObject xbreak);
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "break" element
     */
    org.apache.xmlbeans.XmlObject insertNewBreak(int i);
    
    /**
     * Appends and returns a new empty value (as xml) as the last "break" element
     */
    org.apache.xmlbeans.XmlObject addNewBreak();
    
    /**
     * Removes the ith "break" element
     */
    void removeBreak(int i);
    
    /**
     * Gets the "test" attribute
     */
    java.lang.String getTest();
    
    /**
     * Gets (as xml) the "test" attribute
     */
    org.apache.xmlbeans.XmlString xgetTest();
    
    /**
     * True if has "test" attribute
     */
    boolean isSetTest();
    
    /**
     * Sets the "test" attribute
     */
    void setTest(java.lang.String test);
    
    /**
     * Sets (as xml) the "test" attribute
     */
    void xsetTest(org.apache.xmlbeans.XmlString test);
    
    /**
     * Unsets the "test" attribute
     */
    void unsetTest();
    
    /**
     * A factory class with static methods for creating instances
     * of this type.
     */
    
    public static final class Factory
    {
        public static org.griphyn.vdl.model.While newInstance() {
          return (org.griphyn.vdl.model.While) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, null ); }
        
        public static org.griphyn.vdl.model.While newInstance(org.apache.xmlbeans.XmlOptions options) {
          return (org.griphyn.vdl.model.While) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, options ); }
        
        /** @param xmlAsString the string value to parse */
        public static org.griphyn.vdl.model.While parse(java.lang.String xmlAsString) throws org.apache.xmlbeans.XmlException {
          return (org.griphyn.vdl.model.While) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xmlAsString, type, null ); }
        
        public static org.griphyn.vdl.model.While parse(java.lang.String xmlAsString, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (org.griphyn.vdl.model.While) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xmlAsString, type, options ); }
        
        /** @param file the file from which to load an xml document */
        public static org.griphyn.vdl.model.While parse(java.io.File file) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (org.griphyn.vdl.model.While) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( file, type, null ); }
        
        public static org.griphyn.vdl.model.While parse(java.io.File file, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (org.griphyn.vdl.model.While) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( file, type, options ); }
        
        public static org.griphyn.vdl.model.While parse(java.net.URL u) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (org.griphyn.vdl.model.While) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( u, type, null ); }
        
        public static org.griphyn.vdl.model.While parse(java.net.URL u, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (org.griphyn.vdl.model.While) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( u, type, options ); }
        
        public static org.griphyn.vdl.model.While parse(java.io.InputStream is) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (org.griphyn.vdl.model.While) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( is, type, null ); }
        
        public static org.griphyn.vdl.model.While parse(java.io.InputStream is, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (org.griphyn.vdl.model.While) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( is, type, options ); }
        
        public static org.griphyn.vdl.model.While parse(java.io.Reader r) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (org.griphyn.vdl.model.While) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( r, type, null ); }
        
        public static org.griphyn.vdl.model.While parse(java.io.Reader r, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (org.griphyn.vdl.model.While) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( r, type, options ); }
        
        public static org.griphyn.vdl.model.While parse(javax.xml.stream.XMLStreamReader sr) throws org.apache.xmlbeans.XmlException {
          return (org.griphyn.vdl.model.While) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( sr, type, null ); }
        
        public static org.griphyn.vdl.model.While parse(javax.xml.stream.XMLStreamReader sr, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (org.griphyn.vdl.model.While) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( sr, type, options ); }
        
        public static org.griphyn.vdl.model.While parse(org.w3c.dom.Node node) throws org.apache.xmlbeans.XmlException {
          return (org.griphyn.vdl.model.While) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( node, type, null ); }
        
        public static org.griphyn.vdl.model.While parse(org.w3c.dom.Node node, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (org.griphyn.vdl.model.While) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( node, type, options ); }
        
        /** @deprecated {@link XMLInputStream} */
        public static org.griphyn.vdl.model.While parse(org.apache.xmlbeans.xml.stream.XMLInputStream xis) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return (org.griphyn.vdl.model.While) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xis, type, null ); }
        
        /** @deprecated {@link XMLInputStream} */
        public static org.griphyn.vdl.model.While parse(org.apache.xmlbeans.xml.stream.XMLInputStream xis, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return (org.griphyn.vdl.model.While) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xis, type, options ); }
        
        /** @deprecated {@link XMLInputStream} */
        public static org.apache.xmlbeans.xml.stream.XMLInputStream newValidatingXMLInputStream(org.apache.xmlbeans.xml.stream.XMLInputStream xis) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newValidatingXMLInputStream( xis, type, null ); }
        
        /** @deprecated {@link XMLInputStream} */
        public static org.apache.xmlbeans.xml.stream.XMLInputStream newValidatingXMLInputStream(org.apache.xmlbeans.xml.stream.XMLInputStream xis, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newValidatingXMLInputStream( xis, type, options ); }
        
        private Factory() { } // No instance of this class allowed
    }
}
