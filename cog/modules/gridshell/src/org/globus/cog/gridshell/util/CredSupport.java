/*
 * 
 */
package org.globus.cog.gridshell.util;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.PasswordAuthentication;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

import org.globus.cog.abstraction.impl.common.PublicKeyAuthentication;

/**
 * 
 */
public class CredSupport {
    private static final String SSH_HOME = System.getProperty("user.home")
            + File.separator + ".ssh/";

    private JPanel panel;

    private JPasswordField passField;

    private JTextField unField, pkField;

    private char[] password;

    private String username, publicKey;

    private Component parent;

    public CredSupport(final Component parent) {
        this.parent = parent;
        SpringLayout layout = new SpringLayout();
        panel = new JPanel(layout);

        // init username
        unField = new JTextField(15);
        panel.add(new JLabel("Username: "));
        panel.add(unField);

        // init the password            
        passField = new JPasswordField(15);
        panel.add(new JLabel("Password: "));
        panel.add(passField);

        pkField = new JTextField(15);
        JButton browseButton = new JButton("Browse...");
        browseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent aEvent) {
                JFileChooser fileChooser = new JFileChooser(SSH_HOME);
                int returnVal = fileChooser.showOpenDialog(parent);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    pkField.setText(fileChooser.getSelectedFile().getPath());
                }
            }
        });
        panel.add(new JLabel("Public Key: "));
        JPanel pkJPanelCenter = new JPanel(new FlowLayout());
        pkJPanelCenter.add(pkField);
        pkJPanelCenter.add(browseButton);
        panel.add(pkJPanelCenter);

        SpringUtilities.makeCompactGrid(panel, 3, 2, 10, 10, 6, 10);

    }

    private final void setPassword(char[] value) {
        password = value;
    }

    private final void setUsername(String username) {
        this.username = username;
    }

    /**
     * Returns and removes reference to the password
     */
    private synchronized final char[] getPasswordValue() {
        char[] tmp = password;
        password = null;
        return tmp;
    }

    private synchronized final String getUsernameValue() {
        String result = username;
        username = null;
        return result;
    }

    public synchronized final char[] getPassword() {
        JPanel passJPanel = new JPanel(new SpringLayout());
        passJPanel.add(new JLabel("Password: "));
        passJPanel.add(passField);
        SpringUtilities.makeCompactGrid(passJPanel,1,2,5,5,5,5);

        MessageDialog passwordDialog = new MessageDialog(passJPanel, parent,
                "Enter your password");
        passwordDialog.setOkAction(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                setPassword(passField.getPassword());
                passField.setText(null);
            }
        });
        passwordDialog.show();
        return getPasswordValue();
    }

    public synchronized String getUserName() {
        JPanel usernameJPanel = new JPanel(new SpringLayout());
        usernameJPanel.add(new JLabel("Username: "));
        usernameJPanel.add(unField);

        MessageDialog unameDialog = new MessageDialog(usernameJPanel, parent,
                "Enter your username");
        unameDialog.setOkAction(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                setUsername(unField.getText());
                unField.setText(null);
            }
        });        
        return getUsernameValue();
    }

    public synchronized Object getCredentials() {
        MessageDialog credDialog = new MessageDialog(panel, parent,
                "Enter the credentials");
        credDialog.setOkAction(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                setPassword(passField.getPassword());
                passField.setText(null);
                setUsername(unField.getText());
                unField.setText(null);
                publicKey = pkField.getText();
                pkField.setText(null);
            }
        });
        credDialog.show();

        if (username == null || "".equals(username)) {
            return null;
        } else if (publicKey == null || "".equals(publicKey)) {
            return new PasswordAuthentication(getUsernameValue(),
                    getPasswordValue());
        } else {
            return new PublicKeyAuthentication(getUsernameValue(), publicKey,
                    getPasswordValue());
        }
    }

}