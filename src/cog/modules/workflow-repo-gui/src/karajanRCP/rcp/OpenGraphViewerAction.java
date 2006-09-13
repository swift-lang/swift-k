
package karajanRCP.rcp;

import org.eclipse.jface.action.Action;

import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.*;

/**
 * @see IWorkbenchWindowActionDelegate
 */
public class OpenGraphViewerAction
              implements IWorkbenchWindowActionDelegate {
	
	private IWorkbenchWindow window;
	private IStructuredSelection selection;
	private IPageLayout layout;
	public final static String ID = "karajanRCP.rcp.action.OpenViewer";
	
	public OpenGraphViewerAction(){
		
		/*setId(ID);
		setText("&Graph Viewer");
		setToolTipText("Open the Graph Viewer");*/
	}
		
	protected void openViewer(String viewId) {
		IWorkbenchPage page = window.getActivePage();
		
		try {
		
			IEditorPart ePart = page.getActiveEditor();
			page.closeEditor(ePart, true);
			
			page.setEditorAreaVisible(false);
			
			page.showView("karajanRCP.views.GraphViewer");
			
		} catch (Exception e) {
			System.out.println(e);
		}
		
	}
	
	protected IWorkbenchWindow getWindow() {
		return window;
	}
	
	public void run(IAction action) {

		System.out.println("opening the viewer");
		openViewer("karajanRCP.views.GraphViewer"); 
	}
	
	/**
	 * @see IWorkbenchWindowActionDelegate#selectionChanged
	 */
	public void selectionChanged(IAction action, ISelection selection) {
	}
	
	/**
	 * @see IWorkbenchWindowActionDelegate#dispose
	 */
	public void dispose() {
	}
	
	/**
	 * @see IWorkbenchWindowActionDelegate#init
	 */
	public void init(IWorkbenchWindow window) {
		this.window = window;
	}
	
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		/*if(selection instanceof IStructuredSelection){
			selection = (IStructuredSelection) selection;
			
		}*/
	}

}
