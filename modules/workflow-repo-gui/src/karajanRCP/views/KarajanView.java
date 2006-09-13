package karajanRCP.views;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.cogkit.repository.ComponentNodeInfo;
import org.cogkit.repository.LibraryElementLoader;
import org.cogkit.repository.NodeElementPropertySource;
import org.cogkit.repository.util.InfoParser;
import org.cogkit.repository.util.NodeInfo;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.model.AdaptableList;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.eclipse.ui.part.*;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.graphics.Image;
import org.eclipse.jface.action.*;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.*;
import org.eclipse.ui.views.properties.*;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.SWT;
import org.eclipse.core.runtime.IAdaptable;
import org.globus.cog.gui.grapheditor.generic.RootContainerHelper;


/**
 * This sample class demonstrates how to plug-in a new
 * workbench view. The view shows data obtained from the
 * model. The sample creates a dummy model on the fly,
 * but a real implementation would connect to the model
 * available either in this or another plug-in (e.g. the workspace).
 * The view is connected to the model using a content provider.
 * <p>
 * The view uses a label provider to define how model
 * objects should be presented in the view. Each
 * view can present the same model objects using
 * different labels and icons, if needed. Alternatively,
 * a single label provider can be shared between views
 * in order to ensure that objects of the same type are
 * presented in the same way everywhere.
 * <p>
 */

public class KarajanView extends ViewPart {
    public static KarajanView kView;
	private TreeViewer viewer;
    private TableViewer nodeView;
    private NodeInfo[] nodes;
	private DrillDownAdapter drillDownAdapter;
	private Action action1;
	private Action action2;
	private Action doubleClickAction;
    private ArrayList myListeners;
    private static NodeElementPropertySource nodeElPS = null ;
    private static Logger logger = Logger.getLogger(KarajanView.class);
    
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
	
	class ComponentTreeParent extends ComponentTreeObject {
		private ArrayList children;
		public ComponentTreeParent(String name) {
			super(name);
			children = new ArrayList();
		}
        
        public ComponentTreeParent(String name, String type) {
            super(name, type);
            children = new ArrayList();
        }
        
		public void addChild(ComponentTreeObject child) {
			children.add(child);
			child.setParent(this);
		}
		public void removeChild(ComponentTreeObject child) {
			children.remove(child);
			child.setParent(null);
		}
		public ComponentTreeObject [] getChildren() {
			return (ComponentTreeObject [])children.toArray(new ComponentTreeObject[children.size()]);
		}
		public boolean hasChildren() {
			return children.size()>0;
		}
	}

	class ViewContentProvider implements IStructuredContentProvider, 
										   ITreeContentProvider {
		private ComponentTreeParent invisibleRoot;

		public void inputChanged(Viewer v, Object oldInput, Object newInput) {
		}
		
		public ComponentTreeParent getRoot(){
			return invisibleRoot;
		}
		public void dispose() {
		}
		public Object[] getElements(Object parent) {
			if (parent.equals(getViewSite())) {
				if (invisibleRoot==null) initialize();
				return getChildren(invisibleRoot);
			}
			return getChildren(parent);
		}
		public Object getParent(Object child) {
			if (child instanceof ComponentTreeObject) {
				return ((ComponentTreeObject)child).getParent();
			}
			return null;
		}
        
		public Object [] getChildren(Object parent) {
			if (parent instanceof ComponentTreeParent) {
				return ((ComponentTreeParent)parent).getChildren();
			}
			return new Object[0];
		}
		public boolean hasChildren(Object parent) {
			if (parent instanceof ComponentTreeParent)
				return ((ComponentTreeParent)parent).hasChildren();
			return false;
		}

		private void initialize() {
            
            //Code to call Karajan Libraries
            LibraryElementLoader libLoader = new LibraryElementLoader();
            
            String output = new String();
            PrintStream orig = System.out;
            try {
                output = libLoader.libraryInfo("sys.k");
            } catch (IOException e) {
                e.printStackTrace();
            }
            
            System.setOut(orig);
            
            InfoParser iParse = new InfoParser();
            iParse.setInfo(output);
            nodes = iParse.getInfo();  
            
            Vector parents = new Vector();
          
            
            for(int i=0; i < nodes.length; i++ ){
                
                Iterator itr = parents.iterator();
                boolean parentExists = false; 
                
                while(itr.hasNext()){
                    ComponentTreeParent p = (ComponentTreeParent)itr.next();
                    if(nodes[i].getLibName().equals(p.getName())){
                        p.addChild(new ComponentTreeObject(nodes[i]));
                        parentExists = true;
                    }
                }
                
                if(!parentExists){
                    ComponentTreeParent p = new ComponentTreeParent(nodes[i].getLibName(), "library");
                    p.addChild(new ComponentTreeObject(nodes[i]));
                    parents.add(p);
                }
            }
            
			ComponentTreeParent root = new ComponentTreeParent("Components", "karajan-embedded");
            
            Iterator itr = parents.iterator();
            while(itr.hasNext()){
                root.addChild((ComponentTreeParent) itr.next());
            }
            
            
            //TreeParent root = new TreeParent("Root");
			invisibleRoot = new ComponentTreeParent("");
			invisibleRoot.addChild(root);
		}
	}
	class ViewLabelProvider extends LabelProvider {

		public String getText(Object obj) {
			return obj.toString();
		}
		public Image getImage(Object obj) {
			String imageKey = ISharedImages.IMG_OBJ_ELEMENT;
			if (obj instanceof ComponentTreeParent)
			   imageKey = ISharedImages.IMG_OBJ_FOLDER;
			return PlatformUI.getWorkbench().getSharedImages().getImage(imageKey);
		}
	}
	class NameSorter extends ViewerSorter {
	}

	/**
	 * The constructor.
	 */
	public KarajanView() {
        super();
        kView = this;
	}

	/**
	 * This is a callback that will allow us
	 * to create the viewer and initialize it.
	 */
	public void createPartControl(Composite parent) {
		viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		drillDownAdapter = new DrillDownAdapter(viewer);
		viewer.setContentProvider(new ViewContentProvider());
		viewer.setLabelProvider(new ViewLabelProvider());
        viewer.setAutoExpandLevel(2);
		
		// fill in the element		
		AdaptableList ctlList = new AdaptableList();
	    viewer.getTree().getSelection();
		//ctlList.add(((ViewContentProvider)viewer.getContentProvider()).invisibleRoot);
	
		makeActions();
		hookContextMenu();
		hookDoubleClickAction();
		contributeToActionBars();   
		viewer.setSorter(new NameSorter());
		viewer.setInput(getSite());
	    // ADD the JFace Viewer as a Selection Provider to the View site.
	    getSite().setSelectionProvider(viewer);
	}

	
	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				KarajanView.this.fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, viewer);
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalPullDown(IMenuManager manager) {
		manager.add(action1);
		manager.add(new Separator());
		manager.add(action2);
	}

	private void fillContextMenu(IMenuManager manager) {
		manager.add(action1);
		manager.add(action2);
		manager.add(new Separator());
		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}
	
	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(action1);
		manager.add(action2);
		manager.add(new Separator());
		drillDownAdapter.addNavigationActions(manager);
	}

	private void makeActions() {
		action1 = new Action() {
			public void run() {
                ISelection selection = viewer.getSelection();
                Object obj = ((IStructuredSelection)selection).getFirstElement();
                for(int i=0; i < nodes.length; i++ ){
                    if(nodes[i].getNodeName().equals(obj.toString())){
                        String optArgs = null;
                        String mandArgs = null;
                        if(((ComponentNodeInfo) nodes[i]).getMandatoryArgsStr().equals(" ")){ mandArgs = "N/A";}
                        else{mandArgs = ((ComponentNodeInfo) nodes[i]).getMandatoryArgsStr();}
                        if(((ComponentNodeInfo) nodes[i]).getOptionalArgsStr().equals("  ")){ optArgs = "N/A";}
                        else{optArgs = ((ComponentNodeInfo) nodes[i]).getOptionalArgsStr();}
                        showMessage("Mandatory arguments: " +  mandArgs
                                    + " \n " + 
                                    "Optional arguments: " +  optArgs
                                    );
                    }
                }
               
			}
		};
		action1.setText("View Args");
		action1.setToolTipText("Displays component's arguments");
		action1.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().
			getImageDescriptor(ISharedImages.IMG_DEF_VIEW));
		
		action2 = new Action() {
			public void run() {
                ISelection selection = viewer.getSelection();
                Object obj = ((IStructuredSelection)selection).getFirstElement();
              
                try{
                    

                    showMessage("Added to the graph1");
                  

                    //Post Selection Change, inform listeners
                    for (Iterator iter = myListeners.iterator(); iter.hasNext();) {
                        IPropertyChangeListener element = (IPropertyChangeListener) iter.next();
                        element.propertyChange(new PropertyChangeEvent(this, 
                        "TreeSelection" , null , obj.toString()));
                        showMessage("Added to the graph2");
                    }
                
                }
                catch(Exception e){
                    e.printStackTrace();
                }
				showMessage("done");
			}
		};
		action2.setText("Add to workflow");
		action2.setToolTipText("Adds to the graph");
		action2.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().
				getImageDescriptor(ISharedImages.IMG_OBJ_ELEMENT));
		doubleClickAction = new Action() {
			public void run() {
				ISelection selection = viewer.getSelection();
				Object obj = ((IStructuredSelection)selection).getFirstElement();
				showMessage("Double-click detected on "+obj.toString());
			}
		};
	}

	private void hookDoubleClickAction() {
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				doubleClickAction.run();
			}
		});
	}
	private void showMessage(String message) {
		MessageDialog.openInformation(
			viewer.getControl().getShell(),
			"CoG Workflow View",
			message);
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		//viewer.getControl().setFocus();
       
	}
    
    // A public method that allows listener registration
    public void addPropertyChangeListener(IPropertyChangeListener listener) {
        if(!myListeners.contains(listener))
            myListeners.add(listener);
      
        logger.debug("Added Listener");
    }

    // A public method that allows listener registration
    public void removePropertyChangeListener(IPropertyChangeListener listener) {
        myListeners.remove(listener);
    }
    
    public static KarajanView getInstance(){
        return kView;
    }
    
}
