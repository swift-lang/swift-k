package karajanRCP.rcp;

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.FormColors;
import org.eclipse.ui.forms.IManagedForm;

import karajanRCP.KarajanRCPPlugin;
import karajanRCP.rcp.Messages;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormText;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;

public class RepositoryIntroPage extends FormPage {

	
	public RepositoryIntroPage(RepositoryFormEditor editor) {
		super(editor, "intro", "Repository Intro");
	}

	protected void createFormContent(IManagedForm managedForm) {
		ScrolledForm form = managedForm.getForm();
		FormToolkit toolkit = managedForm.getToolkit();
		form.setText(Messages.getString("RepositoryIntroPage.title"));
		form.setBackgroundImage(KarajanRCPPlugin.getDefault().getImage(KarajanRCPPlugin.IMG_FORM_BG));
		createSpacer(toolkit, form, 10);
		TableWrapLayout layout = new TableWrapLayout();
		layout.leftMargin = 10;
		layout.rightMargin = 10;
		form.getBody().setLayout(layout);
		TableWrapData td;
		layout.verticalSpacing = 10;
		
		/*Hyperlink link = toolkit.createHyperlink(form.getBody(),
				Messages.getString("RepositoryIntroPage.link"), SWT.WRAP); //$NON-NLS-1$
		link.addHyperlinkListener(new HyperlinkAdapter() {
			public void linkActivated(HyperlinkEvent e) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException ex) {
				}
			}
		});
		td = new TableWrapData();
		td.align = TableWrapData.LEFT;
		link.setLayoutData(td);*/
		//createExpandable(form, toolkit);
		
		createFormTextSection(form, toolkit, "RepositoryIntroPage.sectionTitle1", "RepositoryIntroPage.sectionDescription1");
		createFormTextSection(form, toolkit, "RepositoryIntroPage.sectionTitle2", "RepositoryIntroPage.sectionDescription2");
		createFormTextSection(form, toolkit, "RepositoryIntroPage.sectionTitle3", "RepositoryIntroPage.sectionDescription3");
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
	
	private void createFormTextSection(final ScrolledForm form, FormToolkit toolkit, String sectionTitle, String sectionDesc) {
		
		//-------------------------------------------------------
		// SECTION CREATION and DESCRIPTION
		Section section =
			toolkit.createSection(
				form.getBody(),
				Section.TWISTIE | Section.DESCRIPTION);
		section.setActiveToggleColor(
			toolkit.getHyperlinkGroup().getActiveForeground());
		section.setToggleColor(
			toolkit.getColors().getColor(FormColors.SEPARATOR));
		toolkit.createCompositeSeparator(section);
		FormText rtext = toolkit.createFormText(section, false);
		section.setClient(rtext);
		//loadFormText(rtext, toolkit);
        section.setExpanded(true);
		section.addExpansionListener(new ExpansionAdapter() {
			public void expansionStateChanged(ExpansionEvent e) {
				form.reflow(false);
			}
		});

		section.setText(Messages.getString(sectionTitle)); 
		section.setDescription(
		Messages.getString(sectionDesc)); 
		TableWrapData td = new TableWrapData();
		td.align = TableWrapData.FILL;
		td.grabHorizontal = true;
		section.setLayoutData(td);
				
		
	}

	private void loadFormText(final FormText rtext, FormToolkit toolkit) {
		rtext.addHyperlinkListener(new HyperlinkAdapter() {
			public void linkActivated(HyperlinkEvent e) {
				MessageDialog.openInformation(rtext.getShell(), Messages.getString("RepositoryIntroPage.mtitle"),  
				Messages.getString("RepositoryIntroPage.mtext") + e.getHref()); 
			}
		});
		rtext.setHyperlinkSettings(toolkit.getHyperlinkGroup());
		rtext.setImage("image1", KarajanRCPPlugin.getDefault().getImage(KarajanRCPPlugin.IMG_LARGE)); 
		InputStream is = RepositoryIntroPage.class.getResourceAsStream("index.xml");
		if (is!=null) {
			rtext.setContents(is, true);
			try {
				is.close();
			}
			catch (IOException e) {
			}
		}
	}
	
	
	private void createSpacer(FormToolkit toolkit, Composite parent, int span) {
		Label spacer = toolkit.createLabel(parent, ""); //$NON-NLS-1$
		GridData gd = new GridData();
		gd.horizontalSpan = span;
		spacer.setLayoutData(gd);
	}
	
}
