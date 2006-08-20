//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

package org.globus.cog.abstraction.impl.file.webdav;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.net.PasswordAuthentication;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

public class CredentialsDialog {
    private static final String NOTHING = "";

    private JOptionPane optionPane = new JOptionPane();
    private JDialog dialog;

    private JTextField usernameField = new JTextField();
    private JPasswordField passwordField = new JPasswordField();

    private PasswordAuthentication result = null;

    public CredentialsDialog() {
        // init sizes
        usernameField.setPreferredSize(new Dimension(125, 20));
        passwordField.setPreferredSize(new Dimension(125, 20));

        // the main panel
        JPanel main = new JPanel(new BorderLayout());

        // Labels
        JPanel labels = new JPanel(new GridLayout(0, 1));
        labels.add(new JLabel("Username: "));
        labels.add(new JLabel("Password: "));

        // username and password labels/fields
        JPanel fields = new JPanel(new GridLayout(0, 1));
        fields.add(usernameField);
        fields.add(passwordField);

        main.add(labels, BorderLayout.WEST);
        main.add(fields, BorderLayout.CENTER);

        optionPane.setMessage(main);
        optionPane.setOptionType(JOptionPane.OK_CANCEL_OPTION);
        dialog = optionPane.createDialog(null, "Enter Your WebDAV Credentials");
    }

    protected synchronized void okButtonPushed() {
        String uname = usernameField.getText();
        char[] passwd = passwordField.getPassword();
        if (NOTHING.equals(uname) || NOTHING.equals(passwd)) {
            result = null;
        } else {
            result = new PasswordAuthentication(uname, passwd);
        }
    }

    public static PasswordAuthentication showCredentialsDialog() {
        return new CredentialsDialog().getResult();
    }

    public PasswordAuthentication getResult() {
        dialog.show();
        if (optionPane.getValue() != null
                && ((Integer) optionPane.getValue()).equals(new Integer(
                        JOptionPane.OK_OPTION))) {
            okButtonPushed();

        } else {
            result = null;
        }
        return result;
    }
}