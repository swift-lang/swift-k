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
import karajanRCP.views.KarajanView.ComponentTreeParent;

public class NodeElementPropertySource implements IPropertySource{
    
    final protected ComponentTreeObject nodeElement;
    private ComponentNodeInfo node;
    private Vector propertyDescriptors;
    protected static final HashMap PROPS = new HashMap();

	protected static final String PROPERTY_TEXT = "text";	
	private final Object PropertiesTable[][] = 
	{ { PROPERTY_TEXT, new TextPropertyDescriptor(PROPERTY_TEXT,"Text")},		  
	};	

    
    /**
     * Creates a new NodeElementPropertySource.
     *
     * @param element  the element whose properties this instance represents
     */
    public NodeElementPropertySource(ComponentTreeObject element) {
       
		// TODO: check if the node is not the type we need
	   this.nodeElement = element;
	   
	   try{
		
	   if(!(element instanceof ComponentTreeParent)){	   
		   this.node = (ComponentNodeInfo) element.getNode();
		   System.out.println("saved Node");
		   //Creating a PropertiesMap
		   initProperties(node);
		   System.out.println("inited properties" + PROPS.toString());
	   }
	   else if(element instanceof ComponentTreeParent){
		   PROPS.put("library", element.getName());
	   }
	   
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
	 //**
     /* @see org.eclipse.ui.views.properties.IPropertySource#getPropertyDescriptors()
     **/
     
	public IPropertyDescriptor[] getPropertyDescriptors() {

		// Create the property vector.
		try{			
			// Add each property supported.
			PropertyDescriptor descriptor;
			propertyDescriptors = null; 
			IPropertyDescriptor[] IProps = null;
			if(nodeElement instanceof ComponentTreeParent){

				IProps = new IPropertyDescriptor[1];
				IProps[0] = (IPropertyDescriptor)new TextPropertyDescriptor("library","Library Name");
				return IProps;
			}
			if(!(nodeElement instanceof ComponentTreeParent)){
			
				propertyDescriptors = createStaticDescriptors();
				propertyDescriptors.addAll(createMandatoryArgumentDescriptors());
				propertyDescriptors.addAll(createOptionalArgumentDescriptors());
			
				IProps = new IPropertyDescriptor[propertyDescriptors.size()];
				System.out.println("size ----" + propertyDescriptors.size());
				ListIterator iter = propertyDescriptors.listIterator();
			
				int j =0; 
				while(iter.hasNext()){
	               IProps[j] = (IPropertyDescriptor) iter.next();
				   j++;
				}	   
				return IProps;
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}
return null;
	}
    
  
    
   public Vector createMandatoryArgumentDescriptors(){
       
	   Vector propertyDescriptors = new Vector();
	   
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
		   propertyDescriptors.add((IPropertyDescriptor)textDescriptor);
		   
       }
       
       return propertyDescriptors;
   }
   
   public Vector createOptionalArgumentDescriptors(){

       Vector propertyDescriptors = new Vector();

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
		   propertyDescriptors.add((IPropertyDescriptor)textDescriptor);
      
	   }
       
       return propertyDescriptors;
   }
   
   public Vector createStaticDescriptors(){
       
       Vector propertyDescriptors = new Vector();
	   int i = 0; 
       
       //Get the optional Arguments from the node
       String nodeName = node.getNodeName();
       String libName = node.getLibName();
       
       //Create a property descriptor for the Library Name 
	   PropertyDescriptor textDescriptor = new TextPropertyDescriptor("libName", "Library Name");
	   textDescriptor.setCategory("Details");
       propertyDescriptors.add((IPropertyDescriptor)textDescriptor); 
      
       //Create a property descriptor for the Name       
	   textDescriptor = new TextPropertyDescriptor("nodeName", "Node Name");
	   textDescriptor.setCategory("Details");
	   propertyDescriptors.add((IPropertyDescriptor)textDescriptor);
       
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