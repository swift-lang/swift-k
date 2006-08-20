
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/**
 * Copyright (c) 2003, National Research Council of Canada
 * All rights reserved.
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this 
 * software and associated documentation files (the "Software"), to deal in the Software 
 * without restriction, including without limitation the rights to use, copy, modify, merge, 
 * publish, distribute, and/or sell copies of the Software, and to permit persons to whom the 
 * Software is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice(s) and this licence appear in all copies of the Software or 
 * substantial portions of the Software, and that both the above copyright notice(s) and this 
 * license appear in supporting documentation.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, 
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES 
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND 
 * NONINFRINGEMENT OF THIRD PARTY RIGHTS. IN NO EVENT SHALL THE 
 * COPYRIGHT HOLDER OR HOLDERS INCLUDED IN THIS NOTICE BE LIABLE 
 * FOR ANY CLAIM, OR ANY DIRECT, INDIRECT, SPECIAL OR CONSEQUENTIAL 
 * DAMAGES, OR ANY DAMAGES WHATSOEVER (INCLUDING, BUT NOT 
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS 
 * OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWSOEVER 
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN AN ACTION OF 
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR 
 * OTHERWISE) ARISING IN ANY WAY OUT OF OR IN CONNECTION WITH THE 
 * SOFTWARE OR THE USE OF THE SOFTWARE, EVEN IF ADVISED OF THE 
 * POSSIBILITY OF SUCH DAMAGE.
 * 
 * Except as contained in this notice, the name of a copyright holder shall NOT be used in 
 * advertising or otherwise to promote the sale, use or other dealings in this Software 
 * without specific prior written authorization.  Title to copyright in this software and any 
 * associated documentation will at all times remain with copyright holders.
 */

package org.globus.cog.security.cert.management;

import java.awt.Button;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.Panel;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;

import org.globus.gsi.CertUtil;
import org.globus.gsi.OpenSSLKey;
import org.globus.gsi.bc.BouncyCastleOpenSSLKey;

/**
 * @author Jean-Claude Cote</a>
 */
public class CertDestroyApplet extends GIPApplet implements ActionListener {

    private static final String PROPERTY_FILE = "CertDestroyApplet";
    // Values configured by property file.
    private String destroyCredential = null;
    private String mailNotification = null;
    
    private String passPhraseLabel = "PassPhrase";
    private String yourEmailAddressLabel = "Your e-mail address";   
    
    
    // UI elements.
    private Button mailNotificationButton = null;
    private Button destroyCredentialButton = null;
    private TextField passwordField = new TextField();
    private TextField fromField = new TextField();
    
    private X509Certificate cert = null;
    

    public void init() {
        super.init();
        
        destroyCredential = getLocString("DestroyCredential");
        mailNotification = getLocString("NotifyCA");

        // Setup UI.
        mailNotificationButton = new Button(mailNotification);
        destroyCredentialButton = new Button(destroyCredential);
        
        
        Panel titlePanel = null;
        if (appletTitle.length() > 0) {
            titlePanel = new Panel(); 
            titlePanel.add(new Label(appletTitle));
            titlePanel.setFont(new Font("Arial", Font.BOLD, 24));
            titlePanel.setBackground(bgColor);
        }
        
        Panel inputPanel = new Panel();
        inputPanel.add(new Label(passPhraseLabel));
        passwordField.setEchoChar('*');
        inputPanel.add(passwordField);
        inputPanel.add(new Label(yourEmailAddressLabel));
        inputPanel.add(fromField);
        inputPanel.setLayout(new GridLayout(0, 2));
        inputPanel.setBackground(bgColor);

        Panel buttonPanel = new Panel();
        destroyCredentialButton.addActionListener(this);
        buttonPanel.add(destroyCredentialButton);
        mailNotificationButton.addActionListener(this);
        buttonPanel.add(mailNotificationButton);
        buttonPanel.setLayout(new FlowLayout());

        Panel statusPanel = new Panel();
        Font font = new Font("Courier", Font.PLAIN, 12);
        status.setFont(font);
        statusPanel.add(status);

        Panel mainPanel = new Panel();
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        if (titlePanel != null) {
            c.weightx = 1.0;
            c.gridwidth = GridBagConstraints.REMAINDER; //end row
            gridbag.setConstraints(titlePanel, c);
            mainPanel.add(titlePanel);
        }
        c.weightx = 1.0;
        c.gridwidth = GridBagConstraints.REMAINDER; //end row
        gridbag.setConstraints(inputPanel, c);
        mainPanel.add(inputPanel);
        c.weightx = 1.0;
        c.gridwidth = GridBagConstraints.REMAINDER; //end row
        gridbag.setConstraints(buttonPanel, c);
        mainPanel.add(buttonPanel);
        c.weightx = 1.0;
        c.gridwidth = GridBagConstraints.REMAINDER; //end row
        gridbag.setConstraints(statusPanel, c);
        mainPanel.add(statusPanel);
        mainPanel.setLayout(gridbag);

        this.add(mainPanel);
        this.setBackground(bgColor);
    }

    public void actionPerformed(ActionEvent actionEvent) {

        boolean bOk = true;

        try {
            // Load cert.
            if (cert == null){
                try {
                    cert = CertUtil.loadCertificate(userCertFile);
                }
                catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    bOk = false;
                }
                catch (GeneralSecurityException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    bOk = false;
                }
            }            
            
            if (actionEvent.getActionCommand() == destroyCredential) {

                String keyPassword = passwordField.getText();
                
                try {
                    OpenSSLKey key = new BouncyCastleOpenSSLKey(userKeyFile);
    
                    if (key.isEncrypted()) {
                        key.decrypt(keyPassword);
                    }
                } catch(IOException e1) {
                    appendToStatus("Error: Failed to load key: " + userKeyFile);
                    appendToStatus("Make sure you have a valide private key installed.");
                    e1.printStackTrace();
                    bOk = false;
                } catch(GeneralSecurityException e2) {
                    appendToStatus("Error: Wrong grid pass phrase!");
                    e2.printStackTrace();
                    bOk = false;
                }

                if (bOk){
                    boolean confirmed = confirmAction();
                
                    if (confirmed) {                
                        File f = new File(userKeyFile);
                        if (f.exists()) {
                            if( f.delete() ){
                                appendToStatus(userKeyFile + " deleted.");
                            }
                            else{
                                appendToStatus("Error: Could not delete " + userKeyFile);
                            }
                        }
                        else{
                            appendToStatus("Error: " + userKeyFile + " does not exists.");
                        }
                        f = new File(userCertFile);
                        if (f.exists()) {
                            if( f.delete() ){
                                appendToStatus(userCertFile + " deleted.");
                            }
                            else{
                                appendToStatus("Error: Could not delete " + userCertFile);
                            }
                        }
                        else{
                            appendToStatus("Error: " + userCertFile + " does not exists.");
                        }
                        f = new File(userCertReqFile);
                        if (f.exists()) {
                            if( f.delete() ){
                                appendToStatus(userCertReqFile + " deleted.");
                            }
                            else{
                                appendToStatus("Error: Could not delete " + userCertReqFile);
                            }
                        }
                        else{
                            appendToStatus("Error: " + userCertReqFile + " does not exists.");
                        }
                    }
                }
            } else if (actionEvent.getActionCommand() == mailNotification) {

                // Get recipient's email address.
                if (bOk) {
                    if (fromField.getText().length() == 0) {
                        appendToStatus("Please specify your e-mail address.");
                        bOk = false;
                    }
                }

                if (bOk){
                    String notificationEmail = buildNotificationEmail();

                    // Send the request to the CA.
                    if (notificationEmail.length() > 0) {
                        if (sendMail(fromField.getText(), notificationEmail)) {
                            appendToStatus("Your notification has been mailed to " + emailAddressOfCA );
                        }
                    }
                }
                
            } else {
                appendToStatus("Error: Unknown action " + actionEvent.getActionCommand() );
            }
        } catch (Exception ex) {
            // Write exection to Java console.            
            ex.printStackTrace();

            // Write exception to status area.
            String message = ex.getMessage() + "\n";
            StackTraceElement[] stackElements = ex.getStackTrace();
            for (int i = 0; i < stackElements.length; i++) {
                message += stackElements[i].toString() + "\n";
            }
            appendToStatus(message);
        }
    }

    private String buildNotificationEmail(){
        return "\n\n"
            + "Plase revoke my certificate\n"
            + "\n"
            + "==================================================================\n"
            + "\n"
            + "subject     : " + cert.getSubjectDN().getName()
            + "\n"
            + "issuer      : " + cert.getIssuerDN().getName()
            + "\n"
            + "start date  : " + cert.getNotBefore().toString()
            + "\n"
            + "end date    : " + cert.getNotAfter().toString()
            + "\n"
            + "==================================================================\n";
    }

    public boolean confirmAction(){
        boolean confirmation = false;
        
        String[] messages = new String[5];
        messages[0] = "Are you sure you want to destroy this certificate?";
        String dn = null;
        dn = cert.getSubjectDN().getName();
        messages[1] = "subject     : " + dn;
        dn = cert.getIssuerDN().getName();
        messages[2] = "issuer      : " + dn;
        String dt = null;
        dt = cert.getNotBefore().toString();
        messages[3] = "start date  : " + dt;
        dt = cert.getNotAfter().toString();
        messages[4] = "end date    : " + dt;
        
        Container container = this.getParent();
        while (! (container instanceof Frame)) container = container.getParent();
        Frame parent = (Frame) container;
                
        ConfirmationDialog d = new ConfirmationDialog(parent, messages);
        d.setModal(true);                
        d.show();
        appendToStatus("Confirmation " + d.getConfirmation() );
        confirmation = d.getConfirmation();
        return confirmation;
    }

    /* (non-Javadoc)
     * @see ca.gc.nrc.gip.applets.GIPApplet#getPropertyFileLoc()
     */
    protected String getPropertyFileName() {
        // TODO Auto-generated method stub
        return PROPERTY_FILE;
    }
    

}


class ConfirmationDialog extends Dialog implements ActionListener {
    
    private boolean confirmed = false;
    
    public boolean getConfirmation(){
        return confirmed;
    }

    public ConfirmationDialog(Frame parent, String[] messages) {

        super(parent, true);
        setTitle("Confirmation Dialog");

        Font font = new Font("Courier", Font.PLAIN, 12);
        setFont(font);

        int rows = messages.length;
        Panel textPanel = new Panel();
        textPanel.setLayout(new GridLayout(rows,1));
        for(int i = 0; i < rows; i++){
            textPanel.add(new Label(messages[i]));
        }
        add("Center", textPanel);
        
        Panel p = new Panel();
        p.setLayout(new FlowLayout());
        Button yes = new Button("Yes");
        yes.addActionListener(this);
        p.add(yes);
        Button no = new Button("No");
        no.addActionListener(this);
        p.add(no);
        add("South", p);
        
        setSize(300, 100);
        setLocation(100, 200);
        pack();

    }
    
    public void actionPerformed(ActionEvent e) {
        this.hide();
        this.dispose();

        if (e.getActionCommand() == "Yes") {
            confirmed = true;
        }
    }

}
