/*
 *  
 */
package org.globus.cog.gui.util;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.globus.cog.util.HTTPPost;

public class RegistrationPanel extends JPanel implements DocumentListener {
	private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(RegistrationPanel.class);

	private static final String DEFAULT_POST_URL = "http://www.cogkit.org/register/register.php";

	private JTextField[] fields;

	private JTextPane description;
	private JCheckBox reregister, publishProject, projectAlreadyThere, contactMe;

	public RegistrationPanel() {
		setLayout(new SimpleGridLayout(4, 1));
		JPanel panel = new JPanel();
		panel.setLayout(new SimpleGridLayout(5, 5));
		fields = new JTextField[8];
		for (int i = 0; i < 8; i++) {
			fields[i] = new JTextField();
		}
		panel.add(new JLabel("First Name:"), new GridPosition(0, 0));
		panel.add(fields[0], new GridPosition(0, 1));
		panel.add(new JLabel("Last Name:"), new GridPosition(0, 3));
		panel.add(fields[1], new GridPosition(0, 4));
		panel.add(new JLabel("Title:"), new GridPosition(1, 0));
		panel.add(fields[2], new GridPosition(1, 1));
		panel.add(new JLabel("Organization:"), new GridPosition(1, 3));
		panel.add(fields[3], new GridPosition(1, 4));
		panel.add(new JLabel("Country:"), new GridPosition(2, 0));
		panel.add(fields[4], new GridPosition(2, 1));
		panel.add(new JLabel("Email:"), new GridPosition(3, 0));
		panel.add(fields[5], new GridPosition(3, 1));
		panel.add(new JLabel("Project Name:"), new GridPosition(4, 0));
		panel.add(fields[6], new GridPosition(4, 1));
		panel.add(new JLabel("Web-Site:"), new GridPosition(4, 3));
		panel.add(fields[7], new GridPosition(4, 4));
		int hgt = fields[0].getPreferredSize().height;
		Dimension pref = new Dimension(SimpleGridLayout.Expand, hgt);
		for (int i = 0; i < 8; i++) {
			fields[i].setPreferredSize(pref);
			fields[i].getDocument().addDocumentListener(this);
		}

		add(panel, new GridPosition(0, 0));
		panel.setPreferredSize(new Dimension(SimpleGridLayout.Expand, SimpleGridLayout.Expand));

		panel = new JPanel();
		panel.setLayout(new SimpleGridLayout(3, 1));
		panel.setPreferredSize(new Dimension(SimpleGridLayout.Expand, 60));
		panel.add(
				publishProject = new JCheckBox("Include or update my project on out project page"),
				new GridPosition(0, 0));
		panel.add(
				projectAlreadyThere = new JCheckBox("My project is already on the project page"),
				new GridPosition(1, 0));
		panel.add(
				contactMe = new JCheckBox("Contact me to find more about my project"),
				new GridPosition(2, 0));
		add(panel, new GridPosition(1, 0));

		panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.add(new JLabel("Describe your Project and the use of the Java CoG Kit:"),
				BorderLayout.NORTH);
		panel.add(new JScrollPane(description = new JTextPane(),
				ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED), BorderLayout.CENTER);
		panel.setPreferredSize(new Dimension(SimpleGridLayout.Expand, SimpleGridLayout.Expand));
		description.getDocument().addDocumentListener(this);
		add(panel, new GridPosition(2, 0));

		reregister = new JCheckBox("Re-send registration");
		add(reregister, new GridPosition(3, 0));
		loadInfo();
	}

	private void loadInfo() {
		try {
			BufferedReader r = new BufferedReader(new FileReader(System.getProperty("user.home")
					+ File.separator + ".globus" + File.separator + "cogreg.txt"));
			for (int i = 0; i < 8; i++) {
				fields[i].setText(r.readLine());
			}
			String l = r.readLine();
			if (l != null && l.startsWith("#@") && l.length() == 5) {
				publishProject.setSelected(l.charAt(2) == '1');
				projectAlreadyThere.setSelected(l.charAt(3) == '1');
				contactMe.setSelected(l.charAt(4) == '1');
				l = r.readLine();
			}
			StringBuffer sb = new StringBuffer();
			while (l != null) {
				sb.append(l);
				l = r.readLine();
			}
			description.setText(sb.toString());
			reregister.setSelected(false);
			r.close();
		}
		catch (Exception e) {
			reregister.setSelected(true);
		}
	}

	private static final String[] argnames = new String[] { "firstname", "lastname", "title",
			"organization", "country", "email", "project", "wwwlink" };

	public void submit(boolean resubmit) throws IOException {
		if (reregister.isSelected() || resubmit) {
			// init our arguments
			HashMap args = new HashMap();
			for (int i = 0; i < 8; i++) {
				args.put(argnames[i], fields[i].getText());
			}
			args.put("publish", yesno(publishProject));
			args.put("alreadypublished", yesno(projectAlreadyThere));
			args.put("contact", yesno(contactMe));
			args.put("description", description.getText());
			args.put("version", "Java CoG Kit 4");

			try {
				String html = new HTTPPost().post(new URL(DEFAULT_POST_URL), args);

				if (logger.isDebugEnabled()) {
					logger.debug("html=" + html);
				}
				if (html.indexOf("There was a problem") != -1) {
					throw new IOException("An error was encountered in the registration script");
				}
				reregister.setSelected(false);
			}
			catch (MalformedURLException e) {
				// This should not be happening with a hardcoded url
				logger.debug("Failed to submit results", e);
				JOptionPane.showMessageDialog(this, e.getMessage(), "Invalid URL",
						JOptionPane.ERROR_MESSAGE);
			}

			try {
				FileWriter wr = new FileWriter(System.getProperty("user.home") + File.separator
						+ ".globus" + File.separator + "cogreg.txt");
				for (int i = 0; i < 8; i++) {
					wr.write(fields[i].getText() + "\n");
				}
				wr.write("#@"+bnum(publishProject)+bnum(projectAlreadyThere)+bnum(contactMe)+"\n");
				wr.write(description.getText());
				wr.close();
			}
			catch (Exception e) {
				logger.warn(e);
				/*
				 * Exceptions while saving the registration info are not
				 * critical.
				 */
			}
		}
	}
	
	private String yesno(JCheckBox cb) {
		if (cb.isSelected()) {
			return "yes";
		}
		else {
			return "no";
		}
	}
	
	private char bnum(JCheckBox cb) {
		if (cb.isSelected()) {
			return '1';
		}
		else {
			return '0';
		}
	}


	public void insertUpdate(DocumentEvent e) {
		if (!reregister.isSelected()) {
			reregister.doClick();
		}
	}

	public void removeUpdate(DocumentEvent e) {
		if (!reregister.isSelected()) {
			reregister.doClick();
		}
	}

	public void changedUpdate(DocumentEvent e) {
		if (!reregister.isSelected()) {
			reregister.doClick();
		}
	}

	public JCheckBox getReregister() {
		return reregister;
	}
}
