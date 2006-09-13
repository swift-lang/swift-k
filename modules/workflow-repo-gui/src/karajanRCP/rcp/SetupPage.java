package karajanRCP.rcp;

import karajanRCP.KarajanRCPPlugin;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.FormColors;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormText;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;

public class SetupPage extends FormPage {

	public SetupPage(FormEditor editor) {
		// TODO Auto-generated constructor stu
		super(editor, "setup", "Setup");
	}
	
	protected void createFormContent(IManagedForm managedForm) {
		ScrolledForm form = managedForm.getForm();
		FormToolkit toolkit = managedForm.getToolkit();
		form.setText(Messages.getString("SetupPage.title"));
		form.setBackgroundImage(KarajanRCPPlugin.getDefault().getImage(KarajanRCPPlugin.IMG_FORM_BG));
		TableWrapLayout layout = new TableWrapLayout();
		layout.leftMargin = 10;
		layout.rightMargin = 10;
		form.getBody().setLayout(layout);
		TableWrapData td;
		layout.verticalSpacing = 10;
		
		
		createInstallSection(form, toolkit, "SetupPage.sectionInstall");
		createLocateSection(form, toolkit, "SetupPage.sectionLocate");
	
	}
	

	private void createExpandable(final ScrolledForm form, final FormToolkit toolkit) {
		final ExpandableComposite exp = toolkit.createExpandableComposite(form
				.getBody(), ExpandableComposite.TREE_NODE
		//	ExpandableComposite.NONE
				);
		exp.setActiveToggleColor(toolkit.getHyperlinkGroup()
				.getActiveForeground());
		exp.setToggleColor(toolkit.getColors().getColor(FormColors.SEPARATOR));
		Composite client = toolkit.createComposite(exp);
		exp.setClient(client);
		TableWrapLayout elayout = new TableWrapLayout();
		client.setLayout(elayout);
		elayout.leftMargin = elayout.rightMargin = 0;
		final Button button = toolkit.createButton(client, Messages.getString("RepositoryIntroPage.button"), SWT.PUSH); 
		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				//openFormWizard(button.getShell(), toolkit.getColors());
			}
		});
		exp.addExpansionListener(new ExpansionAdapter() {
			public void expansionStateChanged(ExpansionEvent e) {
				form.reflow(true);
			}
		});
		exp.setText(Messages.getString("RepositoryIntroPage.sectionTitle1")); //$NON-NLS-1$
		TableWrapData td = new TableWrapData();
		//td.colspan = 2;
		td.align = TableWrapData.LEFT;
		//td.align = TableWrapData.FILL;
		exp.setLayoutData(td);
	}
	
	private void createInstallSection(final ScrolledForm form, FormToolkit toolkit, String sectionTitle) {
		
		//-------------------------------------------------------
		// SECTION CREATION and DESCRIPTION
		
		
		Section section = toolkit.createSection(form.getBody(), 
    	  Section.DESCRIPTION|Section.TITLE_BAR|
    	  Section.TWISTIE|Section.EXPANDED);
		TableWrapData td = new TableWrapData(TableWrapData.FILL);
    	td.colspan = 2;
		section.setLayoutData(td);
		section.addExpansionListener(new ExpansionAdapter() {
		public void expansionStateChanged(ExpansionEvent e) {
			form.reflow(true);
		  }
		 });
		section.setText("Section title");
		section.setDescription("This is the description that goes "+
						      "below the title");
		Composite sectionClient = toolkit.createComposite(section);
		sectionClient.setLayout(new GridLayout());
		Button button = toolkit.createButton(sectionClient, "Radio 1", SWT.RADIO);
		button = toolkit.createButton(sectionClient, "Radio 2", SWT.RADIO);
		section.setClient(sectionClient);
		
		
		Label label = toolkit.createLabel(sectionClient, "Location:");
		Text text = toolkit.createText(sectionClient, " ");
	    td = new TableWrapData(TableWrapData.FILL_GRAB);
		
		text.setLayoutData(td);
		label = toolkit.createLabel(sectionClient, "Repository Name:");
		text = toolkit.createText(sectionClient, "");
	    td = new TableWrapData(TableWrapData.FILL_GRAB);
		text.setLayoutData(td);
		text.setVisible(true);
		
		//section.setClient();
		//td.align = TableWrapData.FILL;
		//td.grabHorizontal = true;
		
		
	}
	
	
	private void createLocateSection(ScrolledForm form, FormToolkit toolkit, String sectionTitle) {
		
	}
	
}



