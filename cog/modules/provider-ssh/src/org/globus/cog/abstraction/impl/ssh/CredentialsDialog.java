//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

package org.globus.cog.abstraction.impl.ssh;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.PasswordAuthentication;
import java.util.Arrays;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import org.globus.cog.abstraction.impl.common.PublicKeyAuthentication;

public abstract class CredentialsDialog {
    private static final String NOTHING = "";
    private static final String SSH_HOME = System.getProperty("user.home")
            + File.separator + ".ssh";

    protected String userName, privateKey;

    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(String privatekey) {
        this.privateKey = privatekey;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String username) {
        this.userName = username;
    }

    public abstract Object getResult();

    public static Object showCredentialsDialog() {
        return showCredentialsDialog(null, null);
    }

    public static Object showCredentialsDialog(String userName,
            String privateKey) {
        return showCredentialsDialog(userName, privateKey, false);
    }

    public static Object showCredentialsDialog(String userName,
            String privateKey, boolean forceTextMode) {
        return showCredentialsDialog(null, userName, privateKey, forceTextMode);
    }

    public static Object showCredentialsDialog(String target, String userName,
            String privateKey, boolean forceTextMode) {
        CredentialsDialog cd;
        try {
            if (GraphicsEnvironment.isHeadless() || forceTextMode) {
                cd = new ConsoleCredentialsDialog(target);
            }
            else {
                cd = new SwingCredentialsDialog(target);
            }
        }
        catch (InternalError e) {
            cd = new ConsoleCredentialsDialog(target);
        }
        if (userName != null) {
            cd.setUserName(userName);
        }
        if (privateKey != null) {
            cd.setPrivateKey(privateKey);
        }
        return cd.getResult();
    }

    public static class SwingCredentialsDialog extends CredentialsDialog {
        private JOptionPane optionPane = new JOptionPane();
        private JDialog dialog;

        private JLabel passwordLabel;
        private JTextField usernameField = new JTextField();
        private JPasswordField passwordField = new JPasswordField();
        private JTextField privateKeyField = new JTextField();

        private JButton choosePathButton = new JButton("Browse");

        private String target;

        public SwingCredentialsDialog() {
            this(null);
        }

        public SwingCredentialsDialog(String target) {
            this.target = target;
            // init sizes
            usernameField.setPreferredSize(new Dimension(125, 20));
            passwordField.setPreferredSize(new Dimension(125, 20));
            privateKeyField.setPreferredSize(new Dimension(150, 20));

            // the main panel
            JPanel main = new JPanel(new BorderLayout());

            // Labels
            JPanel labels = new JPanel(new GridLayout(0, 1));
            labels.add(new JLabel("Username: "));
            labels.add(passwordLabel = new JLabel("Password: "));
            JLabel pkLabel = new JLabel("Private Key: ");
            pkLabel
                .setToolTipText("Your private key if needed, else leave blank");
            labels.add(pkLabel);

            // username and password labels/fields
            JPanel fields = new JPanel(new GridLayout(0, 1));
            fields.add(usernameField);
            fields.add(passwordField);

            // path to the private key field/button
            JPanel pKeyPanel = new JPanel(new BorderLayout());
            privateKeyField
                .setToolTipText("Your private key if needed, else leave blank");
            pKeyPanel.add(privateKeyField, BorderLayout.CENTER);
            pKeyPanel.add(choosePathButton, BorderLayout.EAST);

            // add an action listener
            choosePathButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent aEvent) {
                    choosePathToPrivateKey();
                }
            });

            fields.add(pKeyPanel);

            main.add(labels, BorderLayout.WEST);
            main.add(fields, BorderLayout.CENTER);

            optionPane.setMessage(main);
            optionPane.setOptionType(JOptionPane.OK_CANCEL_OPTION);
            dialog = optionPane.createDialog(null,
                target == null ? "Enter SSH Credentials"
                        : "Enter SSH Credentials for " + target);
        }

        protected void choosePathToPrivateKey() {
            JFileChooser fileChooser = new JFileChooser(SSH_HOME);
            fileChooser.setFileHidingEnabled(false);
            int returnVal = fileChooser.showOpenDialog(optionPane);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                setPrivateKeyFieldText(fileChooser.getSelectedFile()
                    .getAbsolutePath());
            }
        }

        protected synchronized Object okButtonPushed() {
            String uname = usernameField.getText();
            char[] passwd = passwordField.getPassword();
            String pKeyPath = privateKeyField.getText();

            if (NOTHING.equals(uname) && NOTHING.equals(passwd)
                    && NOTHING.equals(pKeyPath)) {
                return null;
            }
            else if (passwd == null) { // prevent null pointers
                return null;
            }
            else if (NOTHING.equals(pKeyPath)) {
                return new PasswordAuthentication(uname, passwd);
            }
            else {
                return new PublicKeyAuthentication(uname, pKeyPath, passwd);
            }
        }

        public Object getResult() {
            dialog.setVisible(true);
            if (optionPane.getValue() != null
                    && ((Integer) optionPane.getValue()).equals(new Integer(
                        JOptionPane.OK_OPTION))) {
                return okButtonPushed();
            }
            else {
                return null;
            }
        }

        public void setPrivateKey(String privatekey) {
            super.setPrivateKey(privatekey);
            setPrivateKeyFieldText(privatekey);
        }

        private void setPrivateKeyFieldText(String privateKey) {
            privateKeyField.setText(privateKey);
            if (privateKey == null || privateKey.equals("")) {
                passwordLabel.setText("Password: ");
            }
            else {
                passwordLabel.setText("Passphrase: ");
            }
        }

        public void setUserName(String username) {
            super.setUserName(username);
            usernameField.setText(username);
            if (username != null) {
                passwordField.requestFocus();
            }
        }

    }

    public static class ConsoleCredentialsDialog extends CredentialsDialog {
        public String TAB = "\t";
        public static final int MAX_MASKED_CHARS = 80;
        private String target;

        public ConsoleCredentialsDialog() {
            this(null);
        }

        public ConsoleCredentialsDialog(String target) {
            this.target = target;
        }

        public Object getResult() {
            synchronized (ConsoleCredentialsDialog.class) {
                String uprompt = target == null ? "Username: " : target
                        + " username: ";
                if (userName == null) {
                    System.out.print(uprompt);
                    userName = input();
                }
                else {
                    System.out.println(uprompt + userName);
                }
                String pprompt = target == null ? "Password: " : target
                        + " password: ";
                if (privateKey == null) {
                    System.out
                        .println("Empty password for public key authentication.");
                    System.out.print(pprompt);
                    char[] tmp = inputMasked();
                    if (tmp.length == 0) {
                        for (int i = 0; i < 80; i++) {
                            System.out.print('\b');
                        }
                        String defaultPK = getDefaultPrivateKey();
                        System.out.println("Private key file [" + defaultPK
                                + "]: ");
                        privateKey = input();
                        if (privateKey == null || privateKey.equals("")) {
                            privateKey = defaultPK;
                        }
                        System.out.print("Passphrase: ");
                        return new PublicKeyAuthentication(userName,
                            privateKey, inputMasked());
                    }
                    else {
                        return new PasswordAuthentication(userName, tmp);
                    }
                }
                else {
                    System.out.println("Private key: " + privateKey);
                    System.out.print("Passphrase: ");
                    return new PublicKeyAuthentication(userName, privateKey,
                        inputMasked());
                }
            }
        }

        protected String getDefaultPrivateKey() {
            File pk;
            pk = new File(SSH_HOME, "identity");
            if (pk.exists()) {
                return pk.getAbsolutePath();
            }
            pk = new File(SSH_HOME);
            if (pk.exists()) {
                return pk.getAbsolutePath();
            }
            return "";
        }

        protected String input() {
            BufferedReader br = new BufferedReader(new InputStreamReader(
                System.in));
            try {
                return br.readLine();
            }
            catch (IOException e) {
                return "";
            }
        }

        protected synchronized char[] inputMasked() {
            char[] buf = new char[MAX_MASKED_CHARS];
            int crt = 0;
            char c;
            ConsoleMasker.startMasking();
            while (crt < MAX_MASKED_CHARS) {
                try {
                    c = (char) System.in.read();
                    if (c == '\n') {
                        break;
                    }
                    else {
                        buf[crt++] = c;
                    }
                }
                catch (IOException e) {
                    break;
                }
            }
            ConsoleMasker.stopMasking();
            char[] in = new char[crt];
            System.arraycopy(buf, 0, in, 0, crt);
            Arrays.fill(buf, '\0');
            return in;
        }
    }

    public static class ConsoleMasker extends Thread {
        private static ConsoleMasker masker;
        private volatile boolean done;

        public synchronized static void startMasking() {
            if (masker != null) {
                throw new IllegalStateException("Another maskeing thread");
            }
            masker = new ConsoleMasker();
            masker.start();
        }

        public synchronized static void stopMasking() {
            if (masker == null) {
                throw new IllegalStateException("No masking thread is active");
            }
            masker.done();
            masker = null;
        }

        public ConsoleMasker() {
            this.setPriority(Thread.MAX_PRIORITY);
            this.setName("Console Masking");
        }

        public void run() {
            System.out.print(' ');
            char crt = ' ';
            while (!done) {
                System.out.print('\b');
                System.out.print(crt++);
                System.out.flush();
                if (crt == 127) {
                    crt = ' ';
                }
                try {
                    // 25 fps
                    Thread.sleep(40);
                }
                catch (InterruptedException e) {
                    return;
                }
            }
        }

        private void done() {
            done = true;
        }
    }
}