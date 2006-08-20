/*
 * Created on Jun 3, 2005
 *
 */
package org.globus.cog.repository.XMLUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

//JDOM classes
import org.globus.cog.repository.RepositoryProperties;
import org.globus.cog.repository.impl.jdbc.DerbyRepository;
import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;


import org.apache.log4j.Logger;
import org.apache.xerces.parsers.DOMParser;

/**
 * @author dkodeboy
 */
public class ComponentParser {

    private DOMParser parser;
    private Document doc;
    public String xmlFilePath = "../examples/test.xml";
    private String xsdFile = "component.xsd";
    private FileInputStream xsdFileIS;
    private URL xmlURL;
    private URI xmlURI;
    private File xmlFile;
    private FileInputStream xmlFileIS;
    private static Logger logger = Logger.getLogger(ComponentParser.class);
    
	public ComponentParser(String xmlFilePath){
   logger.debug("Assigning xml file"); 
	 this.xmlFilePath = xmlFilePath;
   logger.debug("Obtaining the resource from fileName");
   try {
   //xmlFileIS = (FileInputStream) Thread.currentThread().getContextClassLoader().getResourceAsStream(xmlFilePath);
   //xmlFile  = RepositoryProperties.getDefaultPropertiesFile(xmlFilePath);
   xmlFileIS = new FileInputStream(xmlFilePath);
   //BufferedReader br = new BufferedReader(new InputStreamReader(xmlFileIS));
   //String out = br.readLine();
   //System.out.println("First Line: " + out );
   }catch(FileNotFoundException fnf) {
     logger.debug("Unable to find file" + xmlFilePath);
     logger.debug(fnf.getMessage());
   }
   catch(Exception e) {
     logger.debug("Unable to get Resource" + xmlFilePath);
     
     logger.debug(e.getMessage());
   }
   
   
	}
	
	public boolean validate(){
		boolean status = false; 
    logger.debug("Validating file " + this.xmlFilePath);
		// Turn on validation if set to true
		SAXBuilder builder = new SAXBuilder("org.apache.xerces.parsers.SAXParser", false);
    
    if ( (xmlFileIS == null)) {logger.debug("Builder/xmlIS object is null");}
    
		// To validate against a schema
		//builder.setFeature("http://apache.org/xml/features/validation/schema", true);
    try {
    xsdFileIS = (FileInputStream) Thread.currentThread().getContextClassLoader().getResourceAsStream(xsdFile);
    if ( (xsdFileIS == null)) {logger.debug("xsdFile IS object is null");}
    }
    catch(Exception e) {
      logger.debug("Unable to get Resource" + xsdFile);
      logger.debug(e.getMessage());
    }
		//
    //builder.setProperty("http://apache.org/xml/properties/schema/external-noNamespaceSchemaLocation", "component.xsd");
    
    logger.debug("builder.build");
		// Command line should offer URIs or file names
		try {
      doc = builder.build(xmlFileIS);
      if(doc == null ) System.out.println("@@ unvalidated Document is null");
			// If there are no well-formedness or validity errors, 
			// then no exception is thrown.
			status = true; 
			logger.debug("File " + xmlFilePath + " is valid.");
		}
		// indicates a well-formedness or validity error
		catch (JDOMException e) { 
			
      logger.debug(xmlFilePath + " is not valid.");
			logger.debug(e.getMessage());
      e.printStackTrace();
			status = false; 
		}  
		catch (IOException e) { 
			logger.debug("Could not check " + xmlFilePath);
			logger.debug(" because " + e.getMessage());
			status = false; 
		}  
    catch(NullPointerException e) {
      logger.debug("NPE: ");
      e.printStackTrace();
    }
		return status;
	}
	
	public String getComponentName(){
		String compName = new String();
		Element elem = doc.getRootElement();
		Element metadataElem = elem.getChild("metadata");
		// Get the name from the attributes of the metadata tag 
		List attList = metadataElem.getChildren();
		Iterator iter = attList.iterator();
		while(iter.hasNext()){
			Element elem1 = (Element) iter.next();
			String element = elem1.getName();
			String value = elem1.getValue();
			if(element == "name") compName = value;
		}
		return compName;
		
	}
	public Hashtable parseMetaData(){
		Hashtable metadataTable = new Hashtable();
		Element elem = doc.getRootElement();
		Element metadataElem = elem.getChild("metadata");
		//save all the attributes of the metadata tag as separate elements in the hashtable
		List attList = metadataElem.getAttributes();
		Iterator iter = attList.iterator();
		while(iter.hasNext()){
			Attribute att = (Attribute) iter.next();
			String attribute = att.getName();
			String value = att.getValue();
			logger.debug("Attribute, Value: "+ attribute + " , " + value);
			metadataTable.put(attribute, value);
		}
		//save all the elements within the metadata element in the hashtable
		List elemList = metadataElem.getChildren();
		iter = elemList.iterator();
		while(iter.hasNext()){
			Element childElement = (Element) iter.next();
			String attribute = childElement.getName();
			String value = childElement.getValue();
			logger.debug("Attribute, Value: "+ attribute + " , " + value);
			metadataTable.put(attribute, value);
		}
		return metadataTable;	
	}
	
	public String parseCode(){
		String compString = null;
		Element elem = doc.getRootElement();
		compString = elem.getChildText("source");
		return compString;
	}
	public static void main(String[] args){
		ComponentParser cParse = new ComponentParser("../examples/test.xml");
		boolean status = cParse.validate();
		System.out.println(status);
		System.out.println(cParse.parseCode());
		cParse.parseMetaData();
	}
}
