//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jan 7, 2013
 */
package org.globus.swift.catalog.site;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class Parser {
    public static final String SCHEMA_RESOURCE = "swift-sites-1.0.xsd";
    
    private File src;
    
    public Parser(String fileName) {
        this.src = new File(fileName);
    }
    
    public Document parse() throws ParserConfigurationException, SAXException, IOException {
        URL schemaURL = Parser.class.getClassLoader().getResource(SCHEMA_RESOURCE);
        
        if (schemaURL == null) {
            throw new IllegalStateException("Sites schema not found in resources: " + SCHEMA_RESOURCE);
        }
        
        SchemaFactory sfactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema schema = sfactory.newSchema(schemaURL);
        
        DocumentBuilderFactory dfactory = DocumentBuilderFactory.newInstance();
        dfactory.setSchema(schema);
        DocumentBuilder dbuilder = dfactory.newDocumentBuilder();
        Document doc = dbuilder.parse(src); 
        
        return doc;
    }
    
}
