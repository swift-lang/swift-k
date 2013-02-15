//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

package org.globus.cog.abstraction.impl.common;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.text.JTextComponent;

public abstract class CredentialsDialog {
    private static final String NOTHING = "";

    protected String userName;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String username) {
        this.userName = username;
    }

    public abstract char[][] getResults();

    public static Object showCredentialsDialog(String host) {
        return showCredentialsDialog(host, (String) null);
    }

    public static Object showCredentialsDialog(String host, String userName) {
        return showCredentialsDialog(host, userName, false);
    }

    public static char[][] showCredentialsDialog(String host, String userName, boolean forceTextMode) {
        return showCredentialsDialog(host, new Prompt[] {
                new Prompt("Username: ", Prompt.TYPE_TEXT, userName) },
            forceTextMode);
    }

    public static char[][] showCredentialsDialog(String title, Prompt[] prompts) {
        return showCredentialsDialog(title, prompts, false);
    }

    public static char[][] showCredentialsDialog(String title,
            Prompt[] prompts, boolean forceTextMode) {
        CredentialsDialog cd;
        try {
            if (GraphicsEnvironment.isHeadless() || forceTextMode) {
                cd = new ConsoleCredentialsDialog(title, prompts);
            }
            else {
                cd = new SwingCredentialsDialog(title, prompts);
            }
        }
        catch (InternalError e) {
            cd = new ConsoleCredentialsDialog(title, prompts);
        }
        return cd.getResults();
    }

    public static class SwingCredentialsDialog extends CredentialsDialog
            implements ActionListener, FocusListener, KeyListener {
        private JFrame dialog;

        private JLabel[] labels;
        private JLabel titleLabel;
        private JTextComponent[] fields;

        private JButton[] browse;

        private String title;
        private Prompt[] prompts;
        private boolean enterPressed, cancelPressed;
        private int focusIndex = -1;

        public SwingCredentialsDialog() {
            this(null, null);
        }

        public SwingCredentialsDialog(String title, Prompt[] prompts) {
            this.title = title;
            this.prompts = prompts;

            // the main panel
            JPanel main = new JPanel(new BorderLayout());
            titleLabel = new JLabel(title);
            titleLabel.setAlignmentX(0.5f);
            titleLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
            main.add(titleLabel, BorderLayout.NORTH);
            // Labels
            GridBagLayout gbl = new GridBagLayout();
            JPanel lpane = new JPanel(gbl);
            GridBagConstraints gbc = new GridBagConstraints();

            lpane.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));

            labels = new JLabel[prompts.length];
            fields = new JTextComponent[prompts.length];
            browse = new JButton[prompts.length];

            for (int i = 0; i < prompts.length; i++) {
                labels[i] = new JLabel(prompts[i].label);
                labels[i].setFont(new Font("SansSerif", Font.BOLD, 14));
                lpane.add(labels[i]);
                gbc.fill = GridBagConstraints.HORIZONTAL;
                gbc.gridy = i;
                gbc.gridx = 0;
                gbc.weightx = 0.0;
                gbl.setConstraints(labels[i], gbc);

                switch (prompts[i].type) {
                    case Prompt.TYPE_TEXT:
                        fields[i] = new JTextField();
                        break;
                    case Prompt.TYPE_HIDDEN_TEXT:
                        fields[i] = new JPasswordField();
                        break;
                    case Prompt.TYPE_FILE:
                        fields[i] = new JTextField();
                }
                fields[i].addFocusListener(this);
                fields[i].setSize(fields[i].getPreferredSize().height + 8, 120);
                fields[i].addKeyListener(this);
                if (prompts[i].def != null) {
                    fields[i].setText(prompts[i].def);
                }
                lpane.add(fields[i]);
                gbc.fill = GridBagConstraints.HORIZONTAL;
                gbc.gridx = 1;
                gbc.weightx = 1.0;
                gbl.setConstraints(fields[i], gbc);
                if (prompts[i].type == Prompt.TYPE_FILE) {
                    browse[i] = new JButton("...");
                    browse[i].addActionListener(this);
                    lpane.add(browse[i]);
                    gbc.gridx = 2;
                    gbc.weightx = 0.0;
                    gbl.setConstraints(browse[i], gbc);
                }
                if (prompts[i].def == null && focusIndex == -1) {
                    focusIndex = i;
                }
            }

            main.add(lpane, BorderLayout.CENTER);
            main.setBorder(BorderFactory.createCompoundBorder(BorderFactory
                .createLineBorder(Color.BLACK, 1), BorderFactory
                .createEmptyBorder(8, 8, 8, 8)));

            dialog = new JFrame();
            dialog.setTitle("SSH " + title);
            dialog.getContentPane().add(main);
            dialog.setSize(Math.max(320, titleLabel.getPreferredSize().width),
                titleLabel.getPreferredSize().height + prompts.length
                        * (labels[0].getPreferredSize().height + 8) + 24);
            dialog.setLocationRelativeTo(null);
            dialog.setUndecorated(true);
        }

        protected void choosePath(int index) {
            String existing = fields[index].getText();
            if (existing == null || existing.equals("")) {
                existing = getDefaultFilePath();
            }
            else {
                existing = new File(existing).getParentFile().getAbsolutePath();
            }
            JFileChooser fileChooser = new JFileChooser(existing);
            fileChooser.setFileHidingEnabled(false);
            int returnVal = fileChooser.showOpenDialog(null);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                fields[index].setText(fileChooser.getSelectedFile()
                    .getAbsolutePath());
            }
        }
        
        protected String getDefaultFilePath() {
            return System.getProperty("user.home");
        }

        public char[][] getResults() {
            dialog.setVisible(true);
            if (focusIndex != -1) {
                fields[focusIndex].requestFocus();
            }
            waitForKeyPress();
            char[][] results = new char[prompts.length][];

            for (int i = 0; i < prompts.length; i++) {
                if (fields[i] instanceof JPasswordField) {
                    if (cancelPressed) {
                        Arrays.fill(((JPasswordField) fields[i]).getPassword(), (char) 0);
                    }
                    results[i] = ((JPasswordField) fields[i]).getPassword();
                }
                else {
                    results[i] = fields[i].getText().toCharArray();
                }
            }

            dialog.setVisible(false);
            dialog.dispose();
            if (cancelPressed) {
                return null;
            }
            else {
                return results;
            }
        }

        private synchronized void waitForKeyPress() {
            while (!enterPressed && !cancelPressed) {
                try {
                    wait(250);
                }
                catch (InterruptedException e) {
                    return;
                }
            }
        }

        public void actionPerformed(ActionEvent e) {
            for (int i = 0; i < browse.length; i++) {
                if (e.getSource() == browse[i]) {
                    choosePath(i);
                    break;
                }
            }
        }

        public void focusGained(FocusEvent e) {
            if (e.getSource() instanceof JPasswordField) {
                JPasswordField src = (JPasswordField) e.getSource();
                int l;
                if ((l = src.getPassword().length) != 0) {
                    src.setSelectionStart(0);
                    src.setSelectionEnd(l);
                }
            }
            else {
                JTextComponent src = (JTextComponent) e.getSource();
                src.setSelectionStart(0);
                src.setSelectionEnd(src.getText().length());
            }
        }

        public void focusLost(FocusEvent e) {
        }

        public void keyPressed(KeyEvent e) {
        }

        public void keyReleased(KeyEvent e) {
        }

        public synchronized void keyTyped(KeyEvent e) {
            if (e.getKeyChar() == '\n') {
                for (int i = 0; i < fields.length; i++) {
                    if (e.getSource() == fields[i]) {
                        if (i < fields.length - 1) {
                            fields[i + 1].requestFocus();
                        }
                        else {
                            enterPressed = true;
                            notify();
                        }
                    }
                }
            }
            else if (e.getKeyChar() == 27) {
                cancelPressed = true;
                notify();
            }
        }
    }

    public static class ConsoleCredentialsDialog extends CredentialsDialog {
        public String TAB = "\t";
        public static final int MAX_MASKED_CHARS = 80;
        private String title;

        private Prompt[] prompts;

        public ConsoleCredentialsDialog() {
            this(null, null);
        }

        public ConsoleCredentialsDialog(String title, Prompt[] prompts) {
            this.title = title;
            this.prompts = prompts;
        }

        public char[][] getResults() {
            char[][] results = new char[prompts.length][];
            synchronized (ConsoleCredentialsDialog.class) {
                for (int i = 0; i < prompts.length; i++) {
                    String uprompt = title == null ? prompts[i].label
                            : title + " " + prompts[i].label + prompts[i].def == null ? ""
                                    : " [" + prompts[i].def + "] ";
                    System.out.print(uprompt);
                    switch (prompts[i].type) {
                        case Prompt.TYPE_TEXT:
                        case Prompt.TYPE_FILE:
                            results[i] = input().toCharArray();
                            break;
                        case Prompt.TYPE_HIDDEN_TEXT:
                            results[i] = inputMasked();
                    }
                }
                return results;
            }
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

    public static class Prompt {
        public static final int TYPE_TEXT = 0;
        public static final int TYPE_HIDDEN_TEXT = 1;
        public static final int TYPE_FILE = 2;
        public final String label;
        public final int type;
        public final String def;

        public Prompt(String label, int type) {
            this(label, type, null);
        }

        public Prompt(String label, int type, String def) {
            this.label = label;
            this.type = type;
            if (type == TYPE_HIDDEN_TEXT) {
                this.def = null;
            }
            else {
                this.def = def;
            }
        }
    }
}