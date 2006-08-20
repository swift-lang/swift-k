/*
 * Created on Jun 3, 2005
 */
package org.globus.cog.repository.XMLUtil;


import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.SAXParseException;

public class ErrorChecker extends DefaultHandler
{

   public ErrorChecker() {
   }
   
   public void error (SAXParseException e) {
      System.out.println("Parsing error:  "+e.getMessage());
   }

   public void warning (SAXParseException e) {
      System.out.println("Parsing problem:  "+e.getMessage());
   }

   public void fatalError (SAXParseException e) {
      System.out.println("Parsing error:  "+e.getMessage());
      System.out.println("Cannot continue.");
      System.exit(1);
   }
}
