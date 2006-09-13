package karajanRCP.rcp;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

public class Perspective implements IPerspectiveFactory {

	public void createInitialLayout(IPageLayout layout) {
		String editorArea = layout.getEditorArea();
		layout.setEditorAreaVisible(false);
		layout.setFixed(true);
	
		IFolderLayout folder = layout.createFolder("folder", IPageLayout.LEFT, 0.25f, editorArea);
        folder.addView("karajanRCP.views.KarajanView");
        //layout.addFastView("karajan.views.KarajanView");

		
        IFolderLayout folder1 = layout.createFolder("folder1", IPageLayout.BOTTOM, 0.80f, editorArea );
        folder1.addView("org.eclipse.ui.views.PropertySheet");
		
		
        IFolderLayout folder2 = layout.createFolder("folder2", IPageLayout.RIGHT, 0.10f, editorArea);
        folder2.addView("karajanRCP.views.GraphViewer");

		
		
        
		 //layout.addStandaloneView(IPageLayout.ID_PROP_SHEET, false, IPageLayout.BOTTOM, 0.25f, editorArea);
		 
        //layout.addFastView("karajan.views.ComponentTableView"); 
		//layout.addStandaloneView(View.ID,  false, IPageLayout.LEFT, 1.0f, editorArea);
	}

}

