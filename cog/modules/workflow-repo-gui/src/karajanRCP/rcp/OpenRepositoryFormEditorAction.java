/**
 * 
 */
package karajanRCP.rcp;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.*;

/**
 * @see IWorkbenchWindowActionDelegate
 */
public class OpenRepositoryFormEditorAction
		implements
			IWorkbenchWindowActionDelegate {
	private IWorkbenchWindow window;

	public OpenRepositoryFormEditorAction(){
		
	}
	/*
     * 
     */
	protected void openEditor(String inputName, String editorId) {
		openEditor(new RepositoryFormEditorInput(inputName), editorId);
	}
	
	protected void openEditor(IEditorInput input, String editorId) {
		IWorkbenchPage page = window.getActivePage();
		try {
			IViewPart vPart = page.findView("karajanRCP.views.GraphViewer");
			window.getActivePage().hideView(vPart);
			page.openEditor(input, editorId, true);
		} catch (PartInitException e) {
			System.out.println(e);
		}
	}
	
	protected IWorkbenchWindow getWindow() {
		return window;
	}
	
	public void run(IAction action) {
		openEditor(new RepositoryFormEditorInput("Repository Manager"), "karajanRCP.rcp.RepositoryFormEditor"); 
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

}
