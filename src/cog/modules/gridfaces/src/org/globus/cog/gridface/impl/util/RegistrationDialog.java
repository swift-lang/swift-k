/*
 * 
 */
package org.globus.cog.gridface.impl.util;


import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.globus.cog.gridface.impl.texteditor.TextEditor;
import org.globus.cog.util.ArgumentParser;
import org.globus.cog.util.ArgumentParserException;


public class RegistrationDialog {	
	private static final org.apache.log4j.Logger logger =
		org.apache.log4j.Logger.getLogger(RegistrationDialog.class);
	
	public static final Action EXIT_JVM = new AbstractAction() {
		public void actionPerformed(ActionEvent aEvent) {
			System.exit(0);
		}
	};
	public static final Action DO_NOTHING = new AbstractAction() {
		public void actionPerformed(ActionEvent aEvent) {
		}
	};
	
	private static final String DEFAULT_POST_URL = "http://www.cogkit.org/php/download.php";
	private static final int DEFAULT_WIDTH = 300;
	private static final int DEFAULT_HEIGHT = 400;
	
	public Action completedAction = DO_NOTHING;
	
	private static final String[] softwareChoices = {
			"<choose-one>",
			"cogkit-4.0",
			"jglobus-0.9.13",
			"jglobus-1.1",
			"jglobus-1.2",
			"do not know",
			"other"
	};
	
	private JOptionPane optionPane = new JOptionPane();
	private JDialog dialog;
	private JPanel main,form,bottomForm,topForm; 
	
	private JTextField fname = new JTextField(),
	  lname = new JTextField(), 
	  title = new JTextField(),
	  organization = new JTextField(),
	  country = new JTextField(),
	  email = new JTextField(),
	  project = new JTextField(),
	  wwwlink = new JTextField();
	
	private JTextArea description = new JTextArea();
	private JComboBox software = new JComboBox(softwareChoices);
	
	private String url;
		
	public RegistrationDialog(String url, int WIDTH, int HEIGHT) {
		this.url = url;
		
		// Init panels		
		main = new JPanel(new BorderLayout());
		main.setPreferredSize(new Dimension(WIDTH,HEIGHT));
		form = new JPanel(new GridLayout(2,0));
		
		// put in a question to ask
		JLabel question = new JLabel("Register the CoGKit?");
		question.setHorizontalAlignment(JLabel.CENTER);
		main.add(question,BorderLayout.SOUTH);
		
		bottomForm = new JPanel(new GridLayout(0,1));
		topForm = new JPanel(new GridLayout(0,2));
		topForm.add(new JLabel("First Name:"));
		topForm.add(fname);
		topForm.add(new JLabel("Last Name:"));
		topForm.add(lname);
		topForm.add(new JLabel("Title:"));
		topForm.add(title);
		topForm.add(new JLabel("Organization:"));
		topForm.add(organization);
		topForm.add(new JLabel("Country:"));
		topForm.add(country);
		topForm.add(new JLabel("E-mail:"));
		topForm.add(email);
		topForm.add(new JLabel("Your Project Name:"));
		topForm.add(project);
		topForm.add(new JLabel("WWW Link:"));
		topForm.add(wwwlink);
		
		bottomForm.add(new JLabel("Describe your Project and the use of the CoG Kit:"),BorderLayout.NORTH);
		bottomForm.add(new JScrollPane(description,				
				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS),BorderLayout.CENTER);
		
		JPanel softwarePanel = new JPanel(new GridLayout(0,1));		
		softwarePanel.add(new JLabel("I use (intend to use) the following Software:"));
		softwarePanel.add(software);
		
		bottomForm.add(softwarePanel,BorderLayout.SOUTH);
		
		form.add(topForm);
		form.add(bottomForm);
		
		main.add(form,BorderLayout.CENTER);
		
		optionPane.setMessage(main);
		optionPane.setOptionType(JOptionPane.OK_CANCEL_OPTION);
		
		dialog = optionPane.createDialog(null,"CoGKit Registration Dialog");		
	}
	
	public void setCompletedAction(Action action) {
		this.completedAction = action;
	}

	
	public void okButtonPushed() {
		
		// init our arguments
		HashMap args = new HashMap();
		args.put("firstname",fname.getText());
		args.put("lastname",lname.getText());
		args.put("title",title.getText());
		args.put("organization",organization.getText());
		args.put("country",country.getText());
		args.put("email",email.getText());
		args.put("project",project.getText());
		args.put("wwwlink",wwwlink.getText());
		args.put("description",description.getText());
		args.put("version",software.getItemAt(software.getSelectedIndex()));
		
		try {
			// try and submit
			String html = new HTTPPost().post(url,args);
			
			if(logger.isDebugEnabled()) {
				logger.debug("html="+html);
			}
			
			createHTMLDialog("Your submission results",html);				
		} catch (MalformedURLException exception) {
			logger.error("Failed to submit results",exception);
			createHTMLDialog("Failed to submit results",
					"<h1>Could not submit results</h1><h2>"+ exception +
					"</h2><p>Please ensure you have specified a valid PostURL in log4j.properties (ie log4j.appender.ERROR-DIALOG.PostURL=http://www.somedomain.com/script.cgi)</p>");
					
		} catch (IOException exception) {
			logger.error("Failed to submit results",exception);
			createHTMLDialog("Failed to submit results",
					"<h1>Could not submit results</h1><h2>"+ exception +
					"</h2><p>Please ensure you have an internet connection and you have specified a valid PostURL in log4j.properties (ie log4j.appender.ERROR-DIALOG.PostURL=http://www.somedomain.com/script.cgi)</p>");
		}
	}
	/**
	 * Creates a dialog with the a title and the and html content
	 * @param title
	 * @param htmlContent
	 */
	public void createHTMLDialog(String title,String htmlContent) {
		TextEditor mainPanel = new TextEditor();
		mainPanel.setPreferredSize(main.getSize());
		mainPanel.setContentType("text/html");
		mainPanel.setText(htmlContent);
		
		optionPane.setMessage(mainPanel);
		
		dialog = optionPane.createDialog(null,title);
		dialog.show();
		
		if(completedAction != null) {
			completedAction.actionPerformed(null);
		}
	}
	
	public void show() {
		dialog.show();			
		if(optionPane.getValue() != null 
				&& ((Integer)optionPane.getValue()).equals(
						new Integer(JOptionPane.OK_OPTION)) ) {			
			okButtonPushed();				
		}else {
		}
	}
	public static void createAndShowGUI(String url,int width,int height) {
		RegistrationDialog registrationDialog = new RegistrationDialog(url,width,height);
		registrationDialog.setCompletedAction(RegistrationDialog.EXIT_JVM);
		registrationDialog.show();
	}
	
	public static void main(String[] args) {		
		final ArgumentParser argParser = new ArgumentParser();
		try {
			argParser.addOption("url","The url that the registration dialog will post to",ArgumentParser.OPTIONAL);
			argParser.addOption("width","The width of the dialog",ArgumentParser.OPTIONAL);
			argParser.addOption("height","The height of the dialog",ArgumentParser.OPTIONAL);
			
			argParser.parse(args);
		} catch (ArgumentParserException apException) {
			logger.error("Error Parsing arguments",apException);
		}
		
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				createAndShowGUI(argParser.getStringValue("url",DEFAULT_POST_URL),argParser.getIntValue("width",DEFAULT_WIDTH),argParser.getIntValue("height",DEFAULT_HEIGHT));
				System.exit(0);
			}
		});
		
	}
}
