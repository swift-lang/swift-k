
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
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.Panel;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

import org.globus.cog.security.cert.request.GridCertRenewalRequest;
import org.globus.gsi.gssapi.GlobusGSSCredentialImpl;

/**
 * @author Jean-Claude Cote
 */
public class CertRenewApplet extends GIPApplet implements ActionListener {

	private static final String PROPERTY_FILE = "CertRenewApplet";
	// Values configured by property file.
	private String renewAction = null;
	private String mailRenewRequest = null;
    
	private String oldPassPhraseLabel = "Old PassPhrase";
	private String passPhraseLabel = "PassPhrase";
    private String confPassPhraseLabel = "Confirm PassPhrase";
	private String yourEmailAddressLabel = "Your e-mail address";
    private String challengeLabel = "Challenge";	
	
	
	// UI elements.
    private Button mailButton = null;
    private Button renewButton = null;
    private TextField oldPasswordField = new TextField();
    private TextField passwordField = new TextField();
    private TextField confPasswordField = new TextField();
    private TextField challengeField = new TextField();
    private String certRenewEmailBody = "";
    private TextField fromField = new TextField();
    
    public void init() {
        super.init();
        
        renewAction = getLocString("RenewRequestAction");
        mailRenewRequest = getLocString("MailRenewAction");

        // Setup UI.
        mailButton = new Button(mailRenewRequest);
        renewButton = new Button(renewAction);
        
        
        Panel titlePanel = null;
        if (appletTitle.length() > 0) {
        	titlePanel = new Panel(); 
	        titlePanel.add(new Label(appletTitle));
	        titlePanel.setFont(new Font("Arial", Font.BOLD, 24));
	        titlePanel.setBackground(bgColor);
        }
        
        Panel inputPanel = new Panel();
        inputPanel.add(new Label(oldPassPhraseLabel));
        oldPasswordField.setEchoChar('*');
        inputPanel.add(oldPasswordField);
        inputPanel.add(new Label(passPhraseLabel));
        inputPanel.add(passwordField);
        passwordField.setEchoChar('*');
        inputPanel.add(new Label(confPassPhraseLabel));
        inputPanel.add(confPasswordField);
        confPasswordField.setEchoChar('*');
        inputPanel.add(new Label(yourEmailAddressLabel));
        inputPanel.add(fromField);
        inputPanel.add(new Label(challengeLabel));
        inputPanel.add(challengeField);
        inputPanel.setLayout(new GridLayout(0, 2));
        inputPanel.setBackground(bgColor);

        Panel buttonPanel = new Panel();
        renewButton.addActionListener(this);
        buttonPanel.add(renewButton);
        mailButton.addActionListener(this);
        buttonPanel.add(mailButton);
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

    public void actionPerformed(ActionEvent e) {

        boolean bOk = true;
        
        if (debug){
            this.doDebugTests();
        }
        
        // Get ui values.
        String from = fromField.getText();
        String oldPassword = oldPasswordField.getText();
        String password = passwordField.getText();
        String password2 = confPasswordField.getText();
        String chalenge = challengeField.getText();

        // Get cog values.        
        String userCertRenewFile = userCertFile.substring(0, userCertFile.length() - 4) + "_renew_request.pem";
        String newPrivKeyFile = userKeyFile.substring(0, userKeyFile.length() - 4) + "_new.pem";

        try {

            if (e.getActionCommand() == renewAction) {
                
                bOk = checkCertDir();

                // Verify new password.
                if (bOk) {
                    bOk = verifyPassword(password, password2);
                }                                
                
                if (bOk) {
                    boolean bFileExists = false;
                    File f = new File(newPrivKeyFile);
                    if (f.exists()) {
                        appendToStatus(newPrivKeyFile + getLocString("msg001"));
                        bFileExists = true;
                    }
                    f = new File(userCertRenewFile);
                    if (f.exists()) {
                        appendToStatus(userCertRenewFile + getLocString("msg001"));
                        bFileExists = true;
                    }
        
                    if (bFileExists) {
                        appendToStatus(getLocString("msg002"));
                        appendToStatus(getLocString("msg003"));
                        bOk = false;
                    }
                }

                if (bOk){
                    if (chalenge.length() == 0){
                        appendToStatus(getLocString("msg004"));
                        bOk = false;
                    }
                }

                // Generate renew request.            
                if (bOk) {
                    appendToStatus(getLocString("msg005") + chalenge);
                    
                    // Generate a proxy, and keypair from current cert and key
                    // $GRID_PROXY_INIT -hours 1 -bits 1024 -out $TMPPROXY -cert $CERTFILE -key $KEYFILE
                    int credLifetimeSeconds = 300; // life time of proxy 5 min.
                    GlobusGSSCredentialImpl cred = createNewProxy(oldPassword, credLifetimeSeconds, 1024);
                    if (cred != null){
                        GridCertRenewalRequest.genRenewRequest(
                                        cred,
                                        password,
                                        chalenge,
                                        newPrivKeyFile,
                                        userCertRenewFile );
                        appendToStatus(getLocString("msg006"));
                        appendToStatus(getLocString("msg007") + newPrivKeyFile);
                        appendToStatus(getLocString("msg008") + userCertRenewFile);
                        appendToStatus(getLocString("msg009") + this.emailAddressOfCA + getLocString("msg010")); 
                    }
                }
            }
            else if (e.getActionCommand() == mailRenewRequest) {

                // Check from email address.
                if (bOk) {
                    if (from.length() == 0) {
                        appendToStatus(getLocString("msg011"));
                        bOk = false;
                    }
                }

                // Load mail body from cert renew file.
                String mailBody = loadFile(userCertRenewFile);

                // Send the request to the CA.
                if (bOk && mailBody.length() != 0) {
                    if (sendMail(from, mailBody)) {
                        appendToStatus(getLocString("msg012") + emailAddressOfCA );
                    }
                }
                
            }
            else {
                appendToStatus(getLocString("msg013") + e.getActionCommand() );
            }
        }
        catch (Exception ex) {
            this.appendExceptionDetailsToStatus(ex);
        }
    }

    private String loadFile(String fileName) throws FileNotFoundException, IOException {
        File f = new File(fileName);
        String data = "";
        BufferedReader in = new BufferedReader( new InputStreamReader(new FileInputStream(f)));
        String sLine = in.readLine();
        while (sLine != null) {
            data += sLine + "\n";
            sLine = in.readLine();
        }
        in.close();
        return data;
    }

    /* (non-Javadoc)
     * @see ca.gc.nrc.gip.applets.GIPApplet#getPropertyFileLoc()
     */
    protected String getPropertyFileName() {
        // TODO Auto-generated method stub
        return PROPERTY_FILE;
    }


}
