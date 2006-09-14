package karajanRCP.views.viewNodes;

import karajanRCP.views.KarajanView.ComponentTreeParent;

import org.cogkit.repository.NodeElementPropertySource;
import org.cogkit.repository.util.NodeInfo;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.eclipse.ui.views.properties.IPropertySource;

/*
 * The content provider class is responsible for
 * providing objects to the view. It can wrap
 * existing objects in adapters or simply return
 * objects as-is. These objects may be sensitive
 * to the current input of the view, or ignore
 * it and always show the same content 
 * (like Task List, for example).
 */
 
public class ComponentTreeObject implements IAdaptable {
	private String nodeName;
    private String objectType;
    private NodeInfo node;
	private ComponentTreeParent parent;
    private static NodeElementPropertySource nodeElPS = null ;
	
	public ComponentTreeObject(NodeInfo node) {
		this.node = node;
        this.nodeName = node.getNodeName();
        this.objectType = "component";
        
	}
    
    public ComponentTreeObject(String str, String type) {
        
        this.nodeName = str;
        this.objectType = type;
    
    }
    
    public ComponentTreeObject(String str) {
        
        this.nodeName = str;
        this.objectType = "parent" ;
    
    }
    
	public String getName() {
		return nodeName;
	}
    
    public NodeInfo getNode(){
        return this.node;
    }
    
	public void setParent(ComponentTreeParent parent) {
		this.parent = parent;
	}
	public ComponentTreeParent getParent() {
		return parent;
	}
	public String toString() {
		return getName();
	}
	public Object getAdapter(Class adapter) {
       if (adapter == IPropertySource.class) {
           
           // cache the nodeelementpropertysource
		   System.out.println("adding Adapter");
		   try{
		   nodeElPS =  new NodeElementPropertySource(this);
		   }catch(Exception e){
			   e.printStackTrace();
		   }
           if(nodeElPS != null)System.out.println("returned non-null  Adapter");
		   return nodeElPS;
         }
	   
	   if (adapter == IWorkbenchAdapter.class){
		   System.out.println("returning workbench Adapter");
		   return this;
			
	   }
	
	   System.out.println("returning nullAdapter + " + adapter.getName());
	   return null ;
	}
}