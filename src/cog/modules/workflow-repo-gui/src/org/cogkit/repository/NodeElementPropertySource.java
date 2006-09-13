package org.cogkit.repository;

import java.util.HashMap;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Set;
import java.util.Vector;

import org.cogkit.repository.util.NodeInfo;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.PropertyDescriptor;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

import karajanRCP.views.KarajanView;
import karajanRCP.views.KarajanView.ComponentTreeObject;

public class NodeElementPropertySource implements IPropertySource{
    
    final protected ComponentTreeObject nodeElement;
    private ComponentNodeInfo node;
    private IPropertyDescriptor[] propertyDescriptors;
    protected static final HashMap PROPS = new HashMap();
    
    /**
     * Creates a new NodeElementPropertySource.
     *
     * @param element  the element whose properties this instance represents
     */
    public NodeElementPropertySource(ComponentTreeObject element) {
       
		// TODO: check if the node is not the type we need
	   this.nodeElement = element;
	   
	   try{
		
       this.node = (ComponentNodeInfo) element.getNode();

	   System.out.println("saved Node");
       //Creating a PropertiesMap
       initProperties(node);
	   System.out.println("inited properties" + PROPS.toString());
       }
	   catch(Exception e){
		   e.printStackTrace();
	   }
    }
    
    private void initProperties(NodeInfo node){
        
        PROPS.put("nodeName", node.getNodeName());
        PROPS.put("libName", node.getLibName());
        
		//System.out.println("adding properties");
        // Get the mandatory Arguments from the node
        Vector mandArgs = ((ComponentNodeInfo) node).getMandatoryArgs(); 
        ListIterator iter = mandArgs.listIterator(); 
        while(iter.hasNext()){
            // Need to Update Node Info and change this part
            PROPS.put(iter.next(), "value");
         }
         
         //Get the optional Arguments from the node
         Vector optArgs = ((ComponentNodeInfo) node).getOptionalArgs(); 
         iter = optArgs.listIterator(); 
         while(iter.hasNext()){
            // Need to Update Node Info and change this part
            PROPS.put(iter.next(), "value");
         }
    }
    
    /**
     * @see org.eclipse.ui.views.properties.IPropertySource#getPropertyDescriptors()
     */
    public IPropertyDescriptor[] getPropertyDescriptors() {
		
		
       if (propertyDescriptors == null) {
           
		   
           IPropertyDescriptor[] staticDescriptors = createStaticDescriptors();
           IPropertyDescriptor[] argDescriptors1 = createMandatoryArgumentDescriptors();
           IPropertyDescriptor[] argDescriptors2 = createOptionalArgumentDescriptors();
		   propertyDescriptors = new IPropertyDescriptor[staticDescriptors.length + argDescriptors1.length + argDescriptors2.length];
           
		   int j =0; 
           
           for(int i=0 ; i < staticDescriptors.length; i++){
               propertyDescriptors[j] = staticDescriptors[i];
			   j++;
           }
           
		   
		   
           for(int i=0 ; i < argDescriptors1.length; i++){
               propertyDescriptors[j] = argDescriptors1[i];
               j++;
           }
           
           for(int i=0 ; i < argDescriptors2.length; i++){
               propertyDescriptors[j] = argDescriptors2[i];
               j++;
           }
       }
	   
	   if(propertyDescriptors != null) System.out.println("returning descriptors");
         return propertyDescriptors;
                

}
       
   public IPropertyDescriptor[] createMandatoryArgumentDescriptors(){
       
       //More than 10 arguments are not there anyway but need to change to make more dynamic
       IPropertyDescriptor[] propertyDescriptors = new IPropertyDescriptor[10];
       int i = 0; 
       
       //Get the mandatory Arguments from the node
       Vector mandArgs = node.getMandatoryArgs();
       
       //Interator for the argument Vector
       ListIterator iter = mandArgs.listIterator(); 
       
       //Create a property descriptor for each of the arguments under 
       // the mandatory argument category 
       while(iter.hasNext()){
	   String textName = (String) iter.next();
       PropertyDescriptor textDescriptor = new TextPropertyDescriptor(textName, textName);
       textDescriptor.setCategory("Mandatory Arguments");
       
       propertyDescriptors[i] = textDescriptor; i++;
       }
       
       return propertyDescriptors;
   }
   
   public IPropertyDescriptor[] createOptionalArgumentDescriptors(){

       //More than 10 arguments are not there anyway but need to change to make more dynamic
       IPropertyDescriptor[] propertyDescriptors = new IPropertyDescriptor[10];
       int i = 0; 
       
       //Get the optional Arguments from the node
       Vector optArgs = node.getOptionalArgs();
       
       //Interator for the argument Vector
       ListIterator iter = optArgs.listIterator(); 
       
       //Create a property descriptor for each of the arguments under 
       // the optional argument category 
       while(iter.hasNext()){
       String textName = (String) iter.next();
       PropertyDescriptor textDescriptor = new TextPropertyDescriptor(textName, textName);
       textDescriptor.setCategory("Optional Arguments");
       
       propertyDescriptors[i] = textDescriptor; i++;
       }
       
       return propertyDescriptors;
   }
   
   public IPropertyDescriptor[] createStaticDescriptors(){
       
       IPropertyDescriptor[] propertyDescriptors = new IPropertyDescriptor[2];
       int i = 0; 
       
       //Get the optional Arguments from the node
       String nodeName = node.getNodeName();
       String libName = node.getLibName();
       
       //Create a property descriptor for the name 
       PropertyDescriptor textDescriptor = new TextPropertyDescriptor("nodeName", "Node Name");
       propertyDescriptors[i] = textDescriptor; i++;
      
       //Create a property descriptor for the libName 
       textDescriptor = new TextPropertyDescriptor("libName", "Library Name");      
       propertyDescriptors[i] = textDescriptor;
       
       
       return propertyDescriptors;
   }
   
   
   //Chnaging the value of the NodeElement
    protected void firePropertyChanged(String propName, Object value) {
        NodeInfo node = nodeElement.getNode();
     
        // set node.setArgs(); for this the map has to be added to NodeInfo
        // Update the TreeObject and call the View to refresh or a window refresh
        
       /* if (propName.equals()) {   
            ctl.setFont(new Font (ctl.getDisplay(),new FontData((String)value)) );
            return;
        }
        if (propName.equals(PROPERTY_TEXT)) {
            ctl.setText((String)value);
            return;
        }
        */
    }
    

    public void setPropertyValue(Object name, Object value) {
        
		System.out.println("setProperty value");
        if(!name.equals("nodeName") && !name.equals("libName")){
            
         firePropertyChanged((String)name,value);
         Set set = PROPS.keySet();
         Iterator iter = set.iterator();
        
         while(iter.hasNext()){
          if (name.equals(iter.next())) {
            PROPS.put(name , value);    
            return;
           }
          }
         
        }
    }
   
    public Object getEditableValue() {
        return this;
    }

 
    public Object getPropertyValue(Object name){
       
		System.out.println("getProperty value for - " + name.toString() );
		try{
        Set set = PROPS.keySet();
        Iterator iter = set.iterator();
       
        while(iter.hasNext()){
         if (name.equals(iter.next())) {
			 System.out.println("found Property Value - " + PROPS.get(name));
           return PROPS.get(name);    
		  
          }
         }
		}
		catch(Exception e){
			e.printStackTrace();
		}
        return " ";
        
    }

    public boolean isPropertySet(Object arg0) {
        return false;
    }

    public void resetPropertyValue(Object arg0) {   
    }
    
	public static void main(String[] args){
		NodeInfo node = new ComponentNodeInfo();
		node.setNodeName("testnode");
		node.setLibName("testLib");
	    ((ComponentNodeInfo) node).setMandatoryArgs("mand");
		
		NodeElementPropertySource n = new NodeElementPropertySource(new KarajanView().new ComponentTreeObject(node));
		IPropertyDescriptor[] propertyDesc = n.getPropertyDescriptors();
	}
    
}