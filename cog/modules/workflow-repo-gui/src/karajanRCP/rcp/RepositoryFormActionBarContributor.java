/**
 * 
 */
package karajanRCP.rcp;

import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorActionBarContributor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;

/**
 * @author Deepti
 *
 */
public class RepositoryFormActionBarContributor  implements IEditorActionBarContributor {
/*
	private EditorAction action1;
	private EditorAction action2;
	private EditorAction action3;*/

	public RepositoryFormActionBarContributor() {
	/*
		action1 = new EditorAction(MessageUtil.getString("Editor_Action1")); 
		action1.setToolTipText(MessageUtil.getString("Readme_Editor_Action1")); 
		action1.setDisabledImageDescriptor(ReadmeImages.EDITOR_ACTION1_IMAGE_DISABLE);
		action1.setImageDescriptor(ReadmeImages.EDITOR_ACTION1_IMAGE_ENABLE);
	
		action2 = new RetargetAction(IReadmeConstants.RETARGET2, MessageUtil.getString("Editor_Action2")); 
		action2.setToolTipText(MessageUtil.getString("Readme_Editor_Action2")); 
		action2.setDisabledImageDescriptor(ReadmeImages.EDITOR_ACTION2_IMAGE_DISABLE);
		action2.setImageDescriptor(ReadmeImages.EDITOR_ACTION2_IMAGE_ENABLE);
	
		action3 = new LabelRetargetAction(IReadmeConstants.LABELRETARGET3, MessageUtil.getString("Editor_Action3"));
		action3.setDisabledImageDescriptor(ReadmeImages.EDITOR_ACTION3_IMAGE_DISABLE);
		action3.setImageDescriptor(ReadmeImages.EDITOR_ACTION3_IMAGE_ENABLE);*/
	
	}

	public void init(IActionBars bars, IWorkbenchPage page) {
		// TODO Auto-generated method stub
		
	}

	public void setActiveEditor(IEditorPart targetEditor) {
		// TODO Auto-generated method stub
		
	}

	public void dispose() {
		// TODO Auto-generated method stub
		
	}


}