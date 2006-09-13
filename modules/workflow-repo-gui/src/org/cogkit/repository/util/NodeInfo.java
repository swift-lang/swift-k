package org.cogkit.repository.util;

import java.util.Iterator;
import java.util.Vector;

public class NodeInfo {

    public String name = new String();
    public String libName = new String();
    
    public void setNodeName(String name){
        
        this.name = name;
    }
    
    public void setLibName(String libName){
        this.libName = libName;
    }
    

    
   public String getNodeName(){
       return name;
   }
   
   public String getLibName(){
       return libName;
   }

   
}
