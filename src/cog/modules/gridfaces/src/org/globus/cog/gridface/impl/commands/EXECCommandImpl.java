
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.gridface.impl.commands;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.globus.cog.abstraction.interfaces.ExecutableObject;
import org.globus.cog.abstraction.interfaces.Status;
import org.globus.cog.gridface.interfaces.GridCommand;

public class EXECCommandImpl extends GridCommandImpl implements GridCommand {

    public EXECCommandImpl() {
        super();
        setCommand("exec");
    }

    public ExecutableObject prepareTask() throws Exception {
        if (validate() == true) {
            this.task = prepareJobSubmissionTask();
            return task;
        } else {
            return null;
        }
    }

    public boolean validate() {
		if (super.validate() == false)
		return false;

        //Validate job spec interface
        if ((getAttribute("executable") != null)
            && (getAttribute("servicecontact") != null))
            return true;
        else
            return false;
    }

    public Object getOutput() {
        if (getStatus().getStatusCode() == Status.COMPLETED) {
            return task.getStdOutput();
        } else
            return null;
    }
    
    /**
     * gets field names and the values from a specific class and returns
     * as hash where field names are values and the values are keys
     * 
     * @param className
     * @return
     */
    public static HashMap reflectFieldsValuesAsKeys(String className) throws Exception {
      HashMap keyValues = new HashMap();
      Class errorCodesClass = Class.forName(className);
        
      Field[] errorFields = errorCodesClass.getFields();
      for(int i=0;i<errorFields.length;i++) {
        Field thisField = errorFields[i];
        keyValues.put(thisField.get(errorCodesClass),thisField.getName());          
      }        
      
      return keyValues;
    }

    public static String getMessageFromErrorCode(String errorCodeMessage) throws Exception {
      Pattern pattern = Pattern.compile(".*?(\\d+)");
      Matcher matcher = pattern.matcher(errorCodeMessage);
      String errorCode = matcher.replaceAll("$1");
      return reflectFieldsValuesAsKeys("org.globus.gram.internal.GRAMProtocolErrorConstants").get(new Integer(errorCode))+"";
    }
    /**
     * add labels to the jglobus error codes
     */
    public Exception getException() {
    	if(super.getException() ==  null) { return null; }
    	String errorCodeMessage = super.getException().toString();
    	try {
    	  String message = getMessageFromErrorCode(errorCodeMessage);
    	  return new Exception("Error Message ["+errorCodeMessage+"]: "+message+"\n", super.getException());
    	}catch (Exception exception) {
    	  return new Exception(errorCodeMessage,super.getException());	
    	}    	
    }
}