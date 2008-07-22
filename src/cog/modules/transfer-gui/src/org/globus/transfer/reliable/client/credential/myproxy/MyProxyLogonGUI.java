/*
 * Copyright 2007 The Board of Trustees of the University of Illinois.
 * All rights reserved.
 * 
 * Developed by:
 * 
 *   MyProxy Team
 *   National Center for Supercomputing Applications
 *   University of Illinois
 *   http://myproxy.ncsa.uiuc.edu/
 * 
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal with the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 * 
 *   Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimers.
 * 
 *   Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimers in the
 *   documentation and/or other materials provided with the distribution.
 * 
 *   Neither the names of the National Center for Supercomputing
 *   Applications, the University of Illinois, nor the names of its
 *   contributors may be used to endorse or promote products derived from
 *   this Software without specific prior written permission.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR
 * ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS WITH THE SOFTWARE.
 */
package org.globus.transfer.reliable.client.credential.myproxy;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.globus.transfer.reliable.client.utils.LogFileUtils;

/**
 * The MyProxyLogonGUI class provides a Swing user interface to
 * {@link MyProxyLogon}.
 * @version 1.0
 */
public class MyProxyLogonGUI extends JPanel implements ActionListener {
    private static final long serialVersionUID = 1L;
    static Log logger = LogFactory.getLog(MyProxyLogonGUI.class.getName());
    public final static String version = "1.0";

    protected MyProxyLogon myproxy;

    protected Properties properties;
    protected static final String PROPERTIES_PATH = "/.MyProxyLogon";

    protected JTextField usernameField;
    protected JLabel usernameFieldLabel;
    protected static final String usernameFieldString = "Username";
    protected static final String usernameFieldProperty = "Username";

    protected JPasswordField passwordField;
    protected JLabel passwordFieldLabel;
    protected static final String passwordFieldString = "Passphrase";
    protected static final String passwordFieldProperty = "Passphrase";
    protected static final String passwordInfoString = "Enter passphrase to logon.\n";

    protected JTextField crednameField;
    protected JLabel crednameFieldLabel;
    protected static final String crednameFieldString = "Credential Name";
    protected static final String crednameFieldProperty = "CredentialName";

    protected JTextField lifetimeField;
    protected JLabel lifetimeFieldLabel;
    protected static final String lifetimeFieldString = "Lifetime (hours)";
    protected static final String lifetimeFieldProperty = "Lifetime";

    protected JTextField hostnameField;
    protected JLabel hostnameFieldLabel;
    protected static final String hostnameFieldString = "Hostname";
    protected static final String hostnameFieldProperty = "Hostname";

    protected JTextField portField;
    protected JLabel portFieldLabel;
    protected static final String portFieldString = "Port";
    protected static final String portFieldProperty = "Port";

    protected JTextField outputField;
    protected JLabel outputFieldLabel;
    protected static final String outputFieldString = "Output";
    protected static final String outputFieldProperty = "Output";

    protected JCheckBox trustRootsCheckBox;
    protected static final String trustRootsProperty = "TrustRoots";
    protected static final String trustRootsPropertyYes = "yes";
    protected static final String trustRootsPropertyNo = "no";

    protected JButton button;
    protected static final String buttonFieldString = "Logon";

    protected JTextArea statusTextArea;
    protected JScrollPane statusScrollPane;

    /**
     * Constructs a MyProxyLogonGUI object.
     */
    public MyProxyLogonGUI() {
	myproxy = new MyProxyLogon();

	loadProperties();

	GridBagLayout gridbag = new GridBagLayout();
	GridBagConstraints c = new GridBagConstraints();
	setLayout(gridbag);
	setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

	usernameField = createField(usernameFieldString, properties
		.getProperty(usernameFieldProperty, myproxy.getUsername()));
	usernameFieldLabel = createLabel(usernameFieldString, usernameField);
	usernameField.setToolTipText("Enter your MyProxy username.");

	passwordField = new JPasswordField(10);
	passwordField.setActionCommand(passwordFieldString);
	passwordField.addActionListener(this);
	passwordFieldLabel = createLabel(passwordFieldString, passwordField);
	passwordField.setToolTipText("Enter your MyProxy passphrase.");

	crednameField = createField(crednameFieldString,
		properties.getProperty(crednameFieldProperty, myproxy
			.getCredentialName()));
	crednameFieldLabel = createLabel(crednameFieldString, hostnameField);
	crednameField
	.setToolTipText("Optionally enter your MyProxy credential name.  Leave blank to use your default credential.");

	lifetimeField = createField(lifetimeFieldString, properties
		.getProperty(lifetimeFieldProperty, Integer.toString(myproxy
			.getLifetime() / 3600)));
	lifetimeFieldLabel = createLabel(lifetimeFieldString, lifetimeField);
	lifetimeField
	.setToolTipText("Enter the number of hours for your requested credentials to be valid.");

	hostnameField = createField(hostnameFieldString, properties
		.getProperty(hostnameFieldProperty, myproxy.getHost()));
	hostnameFieldLabel = createLabel(hostnameFieldString, hostnameField);
	hostnameField
	.setToolTipText("Enter the hostname of your MyProxy server (for example: myproxy.ncsa.uiuc.edu).");

	portField = createField(portFieldString, properties.getProperty(
		portFieldProperty, Integer.toString(myproxy.getPort())));
	portFieldLabel = createLabel(portFieldString, portField);
	portField
	.setToolTipText("Enter the TCP port of your MyProxy server (usually 7512).");

	String trustRootPath = MyProxyLogon.getTrustRootPath();
	String existingTrustRootPath = MyProxyLogon.getExistingTrustRootPath();
	trustRootsCheckBox = new JCheckBox("Write trust roots to "
		+ trustRootPath + ".");
	String trustRootsPropVal = properties.getProperty(trustRootsProperty);
	if (trustRootsPropVal != null
		&& trustRootsPropVal.equals(trustRootsPropertyYes)) {
	    trustRootsCheckBox.setSelected(true);
	} else if (trustRootsPropVal != null
		&& trustRootsPropVal.equals(trustRootsPropertyNo)) {
	    trustRootsCheckBox.setSelected(false);
	} else if (existingTrustRootPath == null
		|| trustRootPath.equals(existingTrustRootPath)) {
	    trustRootsCheckBox.setSelected(true);
	} else {
	    trustRootsCheckBox.setSelected(false);
	}
	trustRootsCheckBox
	.setToolTipText("Check this box to download the latest CA certificates, certificate revocation lists, and CA signing policy files from MyProxy.");

	String proxyLoc;
	try {
	    proxyLoc = MyProxyLogon.getProxyLocation();
	} catch (Exception e) {
	    proxyLoc = "";
	}
	outputField = createField(outputFieldString, properties.getProperty(
		outputFieldProperty, proxyLoc));
	outputFieldLabel = createLabel(outputFieldString, outputField);
	outputField
	.setToolTipText("Enter the path to store your credential from MyProxy.  Leave blank if you don't want to retrieve a credential.");

	JLabel[] labels = { usernameFieldLabel, passwordFieldLabel,
		crednameFieldLabel, lifetimeFieldLabel, hostnameFieldLabel,
		portFieldLabel, outputFieldLabel };
	JTextField[] textFields = { usernameField, passwordField,
		crednameField, lifetimeField, hostnameField, portField,
		outputField };
	int numLabels = labels.length;

	c.anchor = GridBagConstraints.LINE_END;
	for (int i = 0; i < numLabels; i++) {
	    c.gridwidth = GridBagConstraints.RELATIVE;
	    c.fill = GridBagConstraints.NONE;
	    c.weightx = 0.0;
	    add(labels[i], c);

	    c.gridwidth = GridBagConstraints.REMAINDER;
	    c.fill = GridBagConstraints.HORIZONTAL;
	    c.weightx = 1.0;
	    add(textFields[i], c);
	}

	button = new JButton(buttonFieldString);
	button.setActionCommand(buttonFieldString);
	button.addActionListener(this);
	button.setVerticalTextPosition(AbstractButton.CENTER);
	button.setHorizontalTextPosition(AbstractButton.CENTER);
	button.setToolTipText("Press this button to logon to MyProxy.");

	statusTextArea = new JTextArea(4, 10);
	statusTextArea.setEditable(false);
	statusTextArea.setLineWrap(true);
	statusTextArea.setWrapStyleWord(true);
	statusScrollPane = new JScrollPane(statusTextArea);
	statusTextArea.setText(passwordInfoString);
	statusTextArea.setToolTipText("This area contains status messages.");

	c.gridwidth = GridBagConstraints.REMAINDER; // last
	c.fill = GridBagConstraints.HORIZONTAL;
	c.weightx = 1.0;
	add(trustRootsCheckBox, c);
	add(button, c);
	add(statusScrollPane, c);
    }

    /**
     * Handles GUI events.
     */
    public void actionPerformed(ActionEvent e) {
	if (verifyInput()) {
	    if (passwordFieldString.equals(e.getActionCommand())
		    || buttonFieldString.equals(e.getActionCommand())) {
		logon();
	    }
	}
    }

    /**
     * Calls createAndShowGUI().
     */
    public static void main(String[] args) {
	SwingUtilities.invokeLater(new Runnable() {
	    public void run() {
		createAndShowGUI();
	    }
	});
    }

    protected void logon() {
	try {
	    myproxy.setUsername(usernameField.getText());
	    myproxy.setPassphrase(new String(passwordField.getPassword()));
	    if (crednameField.getText().length() > 0) {
		myproxy.setCredentialName(crednameField.getText());
	    }
	    myproxy
	    .setLifetime(Integer.parseInt(lifetimeField.getText()) * 3600);
	    myproxy.setHost(hostnameField.getText());
	    myproxy.setPort(Integer.parseInt(portField.getText()));
	    myproxy.requestTrustRoots(trustRootsCheckBox.isSelected());
	    statusTextArea.setText("Connecting to " + myproxy.getHost()
		    + "...\n");
	    myproxy.connect();
	    statusTextArea.setText("Logging on...\n");
	    myproxy.logon();
	    if (outputField.getText().length() == 0) {
		statusTextArea.setText("Logon successful.\n");
	    } else {
		statusTextArea.setText("Getting credentials...\n");
		myproxy.getCredentials();
		statusTextArea.setText("Writing credentials...\n");
		myproxy.saveCredentialsToFile(outputField.getText());
		statusTextArea.setText("Credentials written to "
			+ outputField.getText() + ".\n");
	    }
	    if (trustRootsCheckBox.isSelected() && myproxy.writeTrustRoots()) {
		statusTextArea.append("Trust roots written to "
			+ MyProxyLogon.getTrustRootPath() + ".\n");
	    }
	    saveProperties();
	} catch (Exception exception) {		
		logger.debug(exception.getMessage(), exception);		
	    statusTextArea.append("Error: " + exception.getMessage());
	} finally {
	    try {
		myproxy.disconnect();
	    } catch (Exception e2) {
	    }
	}
    }

    protected void saveProperties() {
	try {
	    FileOutputStream out = new FileOutputStream(System
		    .getProperty("user.home")
		    + PROPERTIES_PATH);
	    properties.setProperty(usernameFieldProperty, usernameField
		    .getText());
	    properties.setProperty(crednameFieldProperty, crednameField
		    .getText());
	    properties.setProperty(lifetimeFieldProperty, lifetimeField
		    .getText());
	    properties.setProperty(hostnameFieldProperty, hostnameField
		    .getText());
	    properties.setProperty(portFieldProperty, portField.getText());
	    properties.setProperty(outputFieldProperty, outputField.getText());
	    properties.setProperty(trustRootsProperty, trustRootsCheckBox
		    .isSelected() ? trustRootsPropertyYes
			    : trustRootsPropertyNo);
	    properties.store(out, MyProxyLogonGUI.class.getName());
	} catch (FileNotFoundException e) {
		logger.debug(e.getMessage(), e);
	    e.printStackTrace();
	} catch (IOException e) {
		logger.debug(e.getMessage(), e);
	    e.printStackTrace();
	}
    }

    protected void loadProperties() {
	try {
	    properties = new Properties();
	    FileInputStream in = new FileInputStream(System
		    .getProperty("user.home")
		    + PROPERTIES_PATH);
	    properties.load(in);
	} catch (FileNotFoundException e) {
	    // ok, nothing to load
	} catch (IOException e) {
		logger.debug(e.getMessage(), e);
	    e.printStackTrace();
	}
    }

    /**
     * Create the GUI and show it. For thread safety, this method should be
     * invoked from the event dispatch thread.
     */
    public static void createAndShowGUI() {
	final JFrame frame = new JFrame("MyProxyLogon " + version);
	MyProxyLogonGUI gui = new MyProxyLogonGUI();
	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	frame.add(gui);
	frame.setLocation(50, 50);
	frame.pack();
	frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
	gui.passwordField.requestFocusInWindow();
	frame.addWindowListener(new WindowAdapter()
	{
		public void windowClosing(WindowEvent e) {			
			frame.dispose();			
		}
	});
		
	frame.setVisible(true);
    }

    private JTextField createField(String fieldString, String text) {
	JTextField field = new JTextField(10);
	field.setActionCommand(fieldString);
	field.addActionListener(this);
	if (text != null) {
	    field.setText(text);
	    field.setColumns(text.length());
	}
	return field;
    }

    private JLabel createLabel(String fieldString, Component c) {
	JLabel label = new JLabel(fieldString + ": ");
	label.setLabelFor(c);
	return label;
    }

    private boolean verifyInput() {
	boolean valid = true;
	StringBuffer infoString = new StringBuffer();
	if (usernameField.getText().length() == 0) {
	    valid = false;
	    infoString.append("Please specify a username.\n");
	}
	if (passwordField.getPassword().length == 0) {
	    valid = false;
	    infoString.append(passwordInfoString);
	} else if (passwordField.getPassword().length < myproxy.MIN_PASS_PHRASE_LEN) {
	    valid = false;
	    infoString.append("Passphrase must be at least ");
	    infoString.append(Integer.toString(myproxy.MIN_PASS_PHRASE_LEN));
	    infoString.append(" characters in length.\n");
	}
	if (lifetimeField.getText().length() == 0) {
	    lifetimeField.setText(Integer
		    .toString(myproxy.getLifetime() / 3600));
	}
	try {
	    Integer.parseInt(lifetimeField.getText());
	} catch (NumberFormatException e) {
		logger.debug(e.getMessage(), e);
	    valid = false;
	    infoString.append("Lifetime is not a valid integer.\n");
	}
	if (hostnameField.getText().length() == 0) {
	    valid = false;
	    infoString.append("Please specify a MyProxy server hostname.\n");
	} else {
	    try {
		InetAddress.getByName(hostnameField.getText());
	    } catch (UnknownHostException e) {
	    logger.debug(e.getMessage(), e);
		valid = false;
		infoString.append("Hostname \"");
		infoString.append(hostnameField.getText());
		infoString
		.append("\" is not valid. Please specify a valid MyProxy server hostname.\n");
	    }
	}
	if (portField.getText().length() == 0) {
	    portField.setText(Integer.toString(myproxy.getPort()));
	}
	try {
	    Integer.parseInt(portField.getText());
	} catch (NumberFormatException e) {
		logger.debug(e.getMessage(), e);
	    valid = false;
	    infoString
	    .append("Port is not a valid integer. Please specify a valid MyProxy server port (default: 7514).\n");
	}
	if (outputField.getText().length() > 0) {
	    File f = new File(outputField.getText());
	    if (f.exists() && !f.canWrite()) {
		valid = false;
		infoString.append(f.getPath());
		infoString
		.append(" exists and is not writable. Please specify a valid output file or specify no output path to only perform authentication.\n");
	    }
	}
	statusTextArea.setText(new String(infoString));
	return valid;
    }
}