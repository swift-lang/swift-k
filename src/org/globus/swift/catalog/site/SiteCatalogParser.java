/*
 * Swift Parallel Scripting Language (http://swift-lang.org)
 * Code from Java CoG Kit Project (see notice below) with modifications.
 *
 * Copyright 2005-2014 University of Chicago
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class SiteCatalogParser {
    public static final Logger logger = Logger.getLogger(SiteCatalogParser.class);
    
    public static final String SCHEMA_RESOURCE = "swift-sites-2.0.xsd";
    static final String JAXP_SCHEMA_LANGUAGE = "http://java.sun.com/xml/jaxp/properties/schemaLanguage";
    
    private File src;
    private boolean hadErrors;
    
    public SiteCatalogParser(String fileName) {
        this.src = new File(fileName);
    }
    
    public Document parse() throws ParserConfigurationException, SAXException, IOException {
        URL schemaURL = SiteCatalogParser.class.getClassLoader().getResource(SCHEMA_RESOURCE);
        
        if (schemaURL == null) {
            throw new IllegalStateException("Sites schema not found in resources: " + SCHEMA_RESOURCE);
        }
        
        SchemaFactory sfactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema schema = sfactory.newSchema(schemaURL);
        
        DocumentBuilderFactory dfactory = DocumentBuilderFactory.newInstance();
        dfactory.setNamespaceAware(true);
        dfactory.setSchema(schema);
        
        DocumentBuilder dbuilder = dfactory.newDocumentBuilder();
        dbuilder.setErrorHandler(new CErrorHandler());
        Document doc = dbuilder.parse(src);
        if (hadErrors) {
            System.err.println("Could not validate " + src.getPath() + 
                " against schema. Attempting to load document as-is.");
        }
                  
        return doc;
    }
    
    private class CErrorHandler implements ErrorHandler {

        @Override
        public void warning(SAXParseException e) throws SAXException {
            print(e, "Warning", false);
        }

        @Override
        public void error(SAXParseException e) throws SAXException {
            hadErrors = true;
            print(e, "Warning", true);
        }

        @Override
        public void fatalError(SAXParseException e) throws SAXException {
            print(e, "Error", true);
        }
        
        private void print(SAXParseException e, String header, boolean err) {
            String msg = "[" + header + "] " + src.getName() + ", line " + 
                e.getLineNumber() + ", col " + e.getColumnNumber() + ": " + e.getMessage();
            if (err) {
                System.err.println(msg);
            }
            else {
                System.out.println(msg);
            }
            if (logger.isInfoEnabled()) {
                logger.info(msg);
            }
        }
    }
}
