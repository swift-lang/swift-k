package org.cogkit.repository.util;

import org.cogkit.repository.ComponentNodeInfo;


public class InfoParser {

   public String parseString = null; 
   
   public void setInfo(String str){
       parseString = str;
   }
   public NodeInfo[] getInfo(){
       
       String[] lines = parseString.split("\n"); 
       NodeInfo[] nodes = new NodeInfo[lines.length];
       
       //System.out.println("No . lines " +  lines.length);
       
       for(int i=0; i < lines.length; i++)
       {
        nodes[i] = parseLine(lines[i]);
       }
    return nodes;
   }
   public NodeInfo parseLine(String line){
      
       
       NodeInfo nInfo = new ComponentNodeInfo();
       
       String[] lib = line.split(":",2);
       nInfo.setLibName(lib[0]);
       
       String[] node = lib[1].split("\\(");
       nInfo.setNodeName(node[0]);
       //System.out.println("Node " + node[0]);
       
       String[] args = node[1].split(",");      
       for(int i=0; i < args.length; i++){
          
          //System.out.println("Args " + i + ": "+ args[i]);
          
          if(args[i].equals(")")) break;
          
          if((args[i].endsWith("*")) || (args[i].endsWith("*)"))){
             ((ComponentNodeInfo) nInfo).setOptionalArgs(args[i].substring(0,args[i].lastIndexOf("*")));
          }
          
          else{
              if(!args[i].equals("...")){
                  if(args[i].endsWith(")"))
                   ((ComponentNodeInfo) nInfo).setMandatoryArgs(args[i].substring(0,args[i].lastIndexOf(")")));
                  else
                   ((ComponentNodeInfo) nInfo).setMandatoryArgs(args[i]);
              }
          }
          
       }
       
    return nInfo;
   }
}
