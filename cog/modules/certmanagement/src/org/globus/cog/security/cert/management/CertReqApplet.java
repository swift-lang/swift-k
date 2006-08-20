
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
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.StringTokenizer;

import org.globus.cog.security.cert.request.GridCertRequest;

/**
 * @author Jean-Claude Cote
 */

public class CertReqApplet extends GIPApplet implements ActionListener {
    
	private static final String PROPERTY_FILE = "CertRequestApplet";
	// Values configured by property file.
	private String emailSubject = null;
	private String genAction = null;
	private String mailRequest = null;
    
	private String countryNameLabel = "Country Name";
	private String organizationLabel = "Organization";
	private String organizationalUnitLabel = "Organizational Unit";
	private String nameLabel = "Name";
	private String passPhraseLabel = "PassPhrase";
	private String confirmPassPhraseLabel = "Confirm PassPhrase";
	private String yourEmailAddressLabel = "Your e-mail address";
    private String serviceLabel = "Service";
    private String hostLabel = "Host";
	
	
	// UI elements.
    private Button mailButton = null;
    private Button genButton = null;
    private TextField passwordField = new TextField();
    private TextField passwordConfField = new TextField();
    private TextField countryField = new TextField("CA");
    private TextField organizationField = new TextField("Grid");
    private TextField organizationUnitField = new TextField();
    private TextField nameField = new TextField();
    private TextField hostField = new TextField();
    private TextField serviceField = new TextField();
    private String certReqFileContent = "";
    private TextField fromField = new TextField();
    
    String country = "";
    String organization = "";
    String organizationUnit = "";
    String name = "";
    String host = "";
    String service = "host"; // ldap
    private boolean bHostCertReq = false; 

    public void init() {
        super.init();
        
        // Get param values.
            
        // Set certtype
        String type = getParameter("certificateRequestType");
        if (type != null && type.length() > 0) {
            if (type.equalsIgnoreCase("host")){
                bHostCertReq = true;
            }
        }

        // Try to find the FQDN.
        InetAddress inetAdd = null;
        try {
            inetAdd = InetAddress.getLocalHost();
        }
        catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        String hostName = inetAdd.getCanonicalHostName();
        System.out.println("GetCanonicalHostName returned: " + hostName);
        if (hostName != null && hostName.length() > 0){
            StringTokenizer tokens = new StringTokenizer(hostName, ".");
            if(tokens.countTokens() > 3){
                if (bHostCertReq){
                    host = hostName;
                }
                else{
                    String hostDomain = hostName.substring(hostName.indexOf(".") + 1,hostName.length());
                    organizationUnit = hostDomain;
                }
            }
        }
        

        genAction = getLocString("GenerateRequestAction");
        mailRequest = getLocString("MailRequestAction");
        
        // Setup UI.
        mailButton = new Button(mailRequest);
        genButton = new Button(genAction);
        
        Panel titlePanel = null;
        if (appletTitle.length() > 0) {
        	titlePanel = new Panel(); 
	        titlePanel.add(new Label(appletTitle));
	        titlePanel.setFont(new Font("Arial", Font.BOLD, 24));
	        titlePanel.setBackground(bgColor);
        }
        
        Panel inputPanel = new Panel();
        inputPanel.add(new Label(countryNameLabel));
        inputPanel.add(countryField);
        inputPanel.add(new Label(organizationLabel));
        inputPanel.add(organizationField);
        if (bHostCertReq){
            inputPanel.add(new Label(hostLabel));
            hostField.setText(host);
            inputPanel.add(hostField);
            inputPanel.add(new Label(serviceLabel));
            serviceField.setText(service);
            inputPanel.add(serviceField);
        }
        else{
            inputPanel.add(new Label(organizationalUnitLabel));
            organizationUnitField.setText(organizationUnit);
            inputPanel.add(organizationUnitField);
            inputPanel.add(new Label(nameLabel));
            nameField.setText(name);
            inputPanel.add(nameField);
            inputPanel.add(new Label(passPhraseLabel));
            passwordField.setEchoChar('*');
            inputPanel.add(passwordField);
            inputPanel.add(new Label(confirmPassPhraseLabel));
            inputPanel.add(passwordConfField);
            passwordConfField.setEchoChar('*');
        }
        inputPanel.add(new Label(yourEmailAddressLabel));
        inputPanel.add(fromField);
        inputPanel.setLayout(new GridLayout(0, 2));
        inputPanel.setBackground(bgColor);

        Panel buttonPanel = new Panel();
        genButton.addActionListener(this);
        buttonPanel.add(genButton);
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
        
        country = countryField.getText();
        organization = organizationField.getText();
        organizationUnit = organizationUnitField.getText();
        name = nameField.getText();
        host = hostField.getText();
        service = serviceField.getText();
        String from = fromField.getText();

        try {
            if (bOk) {
                
                if (bHostCertReq){
                    // Reset the cert file loc to user loc
                    // need to do this since we may have changed the loc base on the type of service.
                    resetCertFileLoc();        
                    
                    if(service.length() == 0){
                        service = "host";
                    }
                    name = service + "/" + host;
                    organizationUnit = name.substring(name.indexOf(".") + 1,name.length());
                    int i = userCertFile.lastIndexOf("user");
                    userCertFile = userCertFile.substring(0, i) + service + userCertFile.substring(i + 4, userCertFile.length());
                    userKeyFile = userKeyFile.substring(0, i) + service + userKeyFile.substring(i + 4, userKeyFile.length());
                    userCertReqFile = userCertReqFile.substring(0, i) + service + userCertReqFile.substring(i + 4, userCertReqFile.length());
                }
            }

            if (e.getActionCommand() == genAction) {

                bOk = checkCertDir();


                // Check not to overwrite any of these files.
                if (bOk) {
                    bOk = checkCertsDoNotExists();
                }

                String password = "";
                if (bOk) {
                    if (!bHostCertReq) {
                        password = passwordField.getText();
                        String password2 = passwordConfField.getText();
                        bOk = verifyPassword(password, password2);
                    }
                }
                                

                // Generate cert request.            
                if (bOk) {
                    String cn = 
                        "C="
                      + country
                      + ",O="
                      + organization
                      + ",OU="
                      + organizationUnit
                      + ",CN="
                      + name;                        
                    
                    appendToStatus("Generating cert request for: " + cn);                        
                    appendToStatus("Writing new private key to " + userKeyFile);
                    GridCertRequest.genCertificateRequest(
                        cn,
                        emailAddressOfCA,
                        password,
                        userKeyFile,
                        userCertFile,
                        userCertReqFile);
                    certReqFileContent = readCertReqFile(userCertReqFile);
                    appendToStatus(certReqFileContent);
                }
            } else if (e.getActionCommand() == mailRequest) {

                // Get recipient's email address.
                if (bOk) {
                    if (from.length() == 0) {
                        appendToStatus("Please specify your e-mail address.");
                        bOk = false;
                    }
                }

                // Get request from file if we generated it at an earlier date.
                if (bOk && certReqFileContent.length() == 0) {
                    certReqFileContent = readCertReqFile(userCertReqFile);
                }

                // Send the request to the CA.
                if (bOk && certReqFileContent.length() != 0) {
                    if (sendMail(from, certReqFileContent)) {
                        appendToStatus("Your request has been mailed to " + emailAddressOfCA );
                    }
                }
            } else {
                appendToStatus("Error: Unknown action " + e.getActionCommand() );
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

    private boolean checkCertsDoNotExists() {
        boolean bFileExists = false;
        File f = new File(userKeyFile);
        if (f.exists()) {
            appendToStatus(userKeyFile + " exists");
            bFileExists = true;
        }
        f = new File(userCertFile);
        if (f.exists()) {
            appendToStatus(userCertFile + " exists");
            bFileExists = true;
        }
        f = new File(userCertReqFile);
        if (f.exists()) {
            appendToStatus(userCertReqFile + " exists");
            bFileExists = true;
        }
        
        if (bFileExists) {
            appendToStatus("Looks like you already have credential.");
            appendToStatus("If you wish to create new ones you will need to move them to another location.");
        }
        return !bFileExists;
    }

    private String readCertReqFile(
        String userCertReqFile)
        throws FileNotFoundException, IOException {

        File fUserCertReqFile = new File(userCertReqFile);
        if (!fUserCertReqFile.exists() || !fUserCertReqFile.canRead()) {
            appendToStatus(
                "Can't read certificate request file: " + userCertReqFile);
            return "";
        }

        String certReqData = "";
        BufferedReader in =
            new BufferedReader(
                new InputStreamReader(new FileInputStream(userCertReqFile)));
        String sLine = in.readLine();
        while (sLine != null) {
            certReqData += sLine + "\n";
            sLine = in.readLine();
        }
        in.close();

        return certReqData;
    }

    /* (non-Javadoc)
     * @see ca.gc.nrc.gip.applets.GIPApplet#getPropertyFileLoc()
     */
    protected String getPropertyFileName() {
        // TODO Auto-generated method stub
        return PROPERTY_FILE;
    }
}
