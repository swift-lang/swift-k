/**
 * 
 */
package org.cogkit.repository;

import java.util.Iterator;
import java.util.Vector;

import org.cogkit.repository.util.NodeInfo;

/**
 * @author Deepti
 *
 */
public class ComponentNodeInfo extends NodeInfo{
    
	public Vector mandArgs = new Vector();
	public Vector optArgs = new Vector();
	   
    public void setMandatoryArgs(Vector mandArgs){
        this.mandArgs.addAll(mandArgs);
    }
    
    public void setOptionalArgs(Vector optArgs){
        this.optArgs.addAll(optArgs);
    } 
    
    public void setMandatoryArgs(String arg){
        this.mandArgs.add(arg);
    }
    
    public void setOptionalArgs(String arg){
        this.optArgs.add(arg);
    } 
	
	   public Vector getMandatoryArgs(){
	       return mandArgs;
	   }
	   
	   public Vector getOptionalArgs(){
	       return optArgs;
	   }
	   
	   //Space separated arguments
	   public String getMandatoryArgsStr(){
	       StringBuffer args = new StringBuffer();
	       Iterator itr = mandArgs.iterator();
	       while(itr.hasNext()){
	           args.append(itr.next().toString() + " ");
	       }
	       return args.toString();
	   }
	   
	   public String getOptionalArgsStr(){
	       StringBuffer args = new StringBuffer();
	       Iterator itr = optArgs.iterator();
	       while(itr.hasNext()){
	           args.append(itr.next().toString() + " ");
	       }
	       return args.toString();
	   }
}
