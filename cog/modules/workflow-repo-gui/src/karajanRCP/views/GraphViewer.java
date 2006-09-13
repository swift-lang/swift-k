package karajanRCP.views;

import java.awt.Component;
import javax.swing.Icon;
import javax.swing.JLabel;

import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TableViewer;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.ViewPart;
import org.globus.cog.gui.grapheditor.Launcher;
import org.globus.cog.gui.grapheditor.RendererFactory;
import org.globus.cog.gui.grapheditor.nodes.NodeComponent;
import org.globus.cog.gui.grapheditor.targets.swing.SwingCanvasRenderer;
import org.globus.cog.gui.grapheditor.targets.swing.SwingRootCanvasRenderer;
import org.globus.cog.util.ImageLoader;
import org.globus.cog.util.graph.GraphInterface;
import org.globus.cog.util.graph.Node;
import org.globus.cog.gui.grapheditor.canvas.AbstractCanvas;
import org.globus.cog.gui.grapheditor.canvas.GraphCanvas;
import org.globus.cog.gui.grapheditor.generic.GenericEdge;
import org.globus.cog.gui.grapheditor.generic.RootCanvas;
import org.globus.cog.gui.grapheditor.generic.RootNode;
import org.globus.cog.gui.grapheditor.generic.GenericNode;
import org.globus.cog.karajan.viewer.KarajanRootContainer;
import org.globus.cog.karajan.viewer.KarajanRootNode;


public class GraphViewer extends ViewPart implements ISelectionListener{
	public static final String ID = "GraphViewer";
    private TableViewer graphViewer;
    public static JLabel label;
    private static KarajanRootContainer container;

    private static GraphInterface g;
    private static GraphCanvas c;
    

	/**
	 * This is a callback that will allow us to create the viewer and initialize
	 * it.
	 */
	public void createPartControl(Composite parent) {
        
        Icon ICON_CLOSE = ImageLoader.loadIcon("images/16x16/co/fileclose.png");
        
        //  add this view as a selection listener to the workbench page
        getSite().getWorkbenchWindow().
        getSelectionService().addSelectionListener((ISelectionListener) this);
        
        Composite swtAwtComponent = new Composite(parent, SWT.EMBEDDED);
        java.awt.Frame frame = SWT_AWT.new_Frame( swtAwtComponent ); 
        //Component panel = initCanvas();  
        KarajanRootNode root = new KarajanRootNode();
        container = new KarajanRootContainer(root);
        Launcher.parseProperties("target.properties", root);
        Launcher.parseProperties("grapheditor.properties", root);
        root.setPropertyValue("karajan.panel", container);
        container.run();
        frame.add(container.getPanel());
        //g = container.getTransformation().getGraph();
       
        //addNode("Start");
        //addNode(c.getGraph(), "test B");
	}
    
    public Component initCanvas(){
        
        NodeComponent p = new RootNode();
        c = p.getCanvas();
        if (c == null) {
            c = p.createCanvas();
        }
        
        RendererFactory.setCurrentTarget("swing");
        RendererFactory.addClassRenderer(AbstractCanvas.class, "swing", SwingCanvasRenderer.class);
        RendererFactory.addClassRenderer(RootCanvas.class, "swing2", SwingRootCanvasRenderer.class);
        SwingCanvasRenderer canvasRenderer =  (SwingCanvasRenderer) c.newRenderer("swing");
        canvasRenderer.initialize();
        
        return canvasRenderer.getComponent();
    }
    
   public void addNode( String nodeName){
        
       
        GenericNode gn1;
        NodeComponent nc = new GenericNode();
        nc.set_ID(nodeName);
        System.out.println("set node comp");
        //g = container.getCanvas().getGraph();
		
		// NOTE: If a graph is not loaded then this fails because there isnt a transformation ... so how do I check to see
		// if a transformation exists
		
        g = container.getTransformation().getGraph();
		c = container.getCanvas();
		  System.out.println("adding node");
        Node n1 = g.addNode(gn1 = new GenericNode());
		   System.out.println("added node");
        //Node n1 = c.getGraph().addNode(nc);
        gn1.setName(nodeName);
        System.out.println("set name");
        c.invalidate();
        
    }
    
    public void addEdge(Node n1, Node n2){
        
        GenericNode gn1;
        c.getGraph().addEdge(n1, n2, new GenericEdge());
        c.invalidate();
        
    }

    public static GraphCanvas getGraph(){
        return c;
    }
    
	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
        
	}

    public void selectionChanged(IWorkbenchPart part, ISelection selection) {
        
        if (selection != null) {
            if (selection instanceof IStructuredSelection) {
              IStructuredSelection ss = (IStructuredSelection) selection;
              
              if (ss.isEmpty())
                System.out.println("<empty selection>");
              else{
                String comp = ss.getFirstElement().toString();
                System.out.println("First selected element Class is " + ss.getFirstElement().getClass().getName());
                String className = ss.getFirstElement().getClass().getName();
                if(!className.endsWith("TreeParent")){
                 System.out.println("First selected element is - " + comp);
               //  addNode(comp);
                 
                }
              }
            
            } else {
            System.out.println("<empty selection>");
          }  
        }
        
    }

    public void propertyChange(PropertyChangeEvent event) {
 
        //This view is interested in the added node
        //The data is being sourced by another plugin in the background.

        if( event.getProperty().equals("TreeSelection")) {
            Object val = event.getNewValue();
            // add the object to the graph
            addNode(val.toString());
        }
    }
    

}