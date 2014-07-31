/*
 * Swift Parallel Scripting Language (http://swift-lang.org)
 * Code from Java CoG Kit Project (see notice below) with modifications.
 *
 * Copyright 2005-2014 University of Chicago
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/**
 * Copyright (c) 2004, National Research Council of Canada
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

import java.applet.Applet;
import java.awt.Button;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.Panel;
import java.awt.TextArea;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.StringTokenizer;

import org.globus.common.CoGProperties;
import org.globus.gsi.CertUtil;
import org.globus.gsi.GSIConstants;
import org.globus.gsi.GlobusCredential;
import org.globus.gsi.OpenSSLKey;
import org.globus.gsi.bc.BouncyCastleCertProcessingFactory;
import org.globus.gsi.bc.BouncyCastleOpenSSLKey;
import org.globus.gsi.gssapi.GlobusGSSCredentialImpl;
import org.ietf.jgss.GSSCredential;

/**
 * @author Jean-Claude Cote
 */
public abstract class GIPApplet extends Applet {

    protected static final boolean debug = true;
    protected String emailAddressOfCA = "jean-claude.cote@cnrc-nrc.gc.ca";
    protected TextArea status = new TextArea(12, 50);

    protected Color bgColor = Color.white;

    private ResourceBundle resource = null;
    // Values configured by property file.
    private String emailSubject = null;
    protected String appletTitle = null;
    private String caCertFiles = null;

    // Cog values.
    protected String certDir = "";
    protected String userCertFile = "";
    protected String userKeyFile = "";
    protected String userCertReqFile = "";
    protected CoGProperties cogProps = CoGProperties.getDefault();

    
    public void init() {
        
        CertUtil.init();
        
        resetCertFileLoc();

        loadParams();
        
        loadProperties();
        
        handleCAInstallation();

    }

    protected void resetCertFileLoc() {
        // Get default location of cert.
        userCertFile = cogProps.getUserCertFile();
        userKeyFile = cogProps.getUserKeyFile();
        
        // Get root dir of default cert location.
        int pos = userKeyFile.lastIndexOf(File.separator);
        certDir = userKeyFile.substring(0, pos + 1);
        
        // Cert request file name.
        userCertReqFile = userCertFile.substring(0, userCertFile.length() - 4) + "_request.pem";
    }

    private void loadParams(){
        // Get param values.
        // set color to what ever user specified.
        String color = getParameter("backGroundColor");
        if (color != null && color.length() > 0) {
            bgColor = Color.decode(getParameter("backGroundColor"));
        }
    
        // set ca email to what ever user specified.
        String ca = getParameter("emailAddressOfCA");
        if (ca != null && ca.length() > 0) {
            emailAddressOfCA = getParameter("emailAddressOfCA");
        }
    }

    protected abstract String getPropertyFileName();
    

    public String getLocString( String key ){
        return resource.getString(key);
    }
            
    private void loadProperties(){
        
        Locale currentLocale = Locale.getDefault();

        System.out.println("Getting resource bundle for: " + currentLocale.toString());

        resource = ResourceBundle.getBundle("conf/" + getPropertyFileName(), currentLocale);
        
        System.out.println("Resource bundle data:");
        Enumeration e = resource.getKeys();
        String key = null;
        while( e.hasMoreElements() ){
            key = (String)e.nextElement();
            System.out.println(key + " = " + resource.getString(key));
        }
        
        appletTitle = getLocString("AppletTitle");
        emailSubject = getLocString("EmailSubject");
        caCertFiles = getLocString("CACertFiles");
        
    }
    
    protected void appendToStatus(String s) {
    	String statusText = s + "\n";
    	status.append(statusText);
    }

    protected void appendExceptionDetailsToStatus(Exception ex) {
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

    protected GlobusGSSCredentialImpl createNewProxy(String keyPassword, int lifetime, int bits) {
    
        CertUtil.init();

    	X509Certificate userCert = null;
    	PrivateKey userKey       = null;  
        
    	CoGProperties props = CoGProperties.getDefault();
    
    	String userCertFile = props.getUserCertFile();
    	String userKeyFile  = props.getUserKeyFile();
    
    	try {
    		OpenSSLKey key = new BouncyCastleOpenSSLKey(userKeyFile);
    
    		if (key.isEncrypted()) {
    			key.decrypt(keyPassword);
    		}
    
    		userKey = key.getPrivateKey();
    	} catch(IOException e) {
    		appendToStatus("Error: Failed to load key: " + userKeyFile);
    		appendToStatus("Make sure you have a valide private key installed.");
    		e.printStackTrace();
    		return null;
    	} catch(GeneralSecurityException e) {
    		appendToStatus("Error: Wrong grid pass phrase!");
    		e.printStackTrace();
    		return null;
    	}
        
    	try {
    		userCert = CertUtil.loadCertificate(userCertFile);
    	} catch(IOException e) {
    		appendToStatus("Error: Failed to load cert: " + userCertFile);
    		appendToStatus("Make sure you have a valide certificate installed.");
    		e.printStackTrace();
    		return null;
    	} catch(GeneralSecurityException e) {
    		appendToStatus("Error: Unable to load user certificate: " +
    				   e.getMessage());
    		appendExceptionDetailsToStatus(e);
    		return null;
    	}
    	
    	BouncyCastleCertProcessingFactory factory =
    		BouncyCastleCertProcessingFactory.getDefault();
    
    	boolean limited = false;
    
    	int proxyType = (limited) ? 
    		GSIConstants.DELEGATION_LIMITED :
    		GSIConstants.DELEGATION_FULL;
    	
    	try {
    		GlobusCredential proxy = 
    		factory.createCredential(new X509Certificate[] {userCert},
    					 userKey,
    					 bits,
    					 lifetime,
    					 proxyType);
    
    		return new GlobusGSSCredentialImpl(proxy,
    						   GSSCredential.INITIATE_ONLY);
    
    	} catch (Exception e) {
    		appendToStatus("Failed to create a proxy: " + e.getMessage());
    		appendExceptionDetailsToStatus(e);
    		return null;
    	}
    }

    protected void doDebugTests() {
    	try {    	
    		System.err.println("doing some preleminary checks");
            
    		Properties p = System.getProperties();
    		p.list(System.out);
            
    		InetAddress inetAdd = InetAddress.getLocalHost();
    		System.out.println(inetAdd.getHostName());
    		System.out.println(inetAdd.toString());
    		System.out.println(inetAdd.getCanonicalHostName());
            
    		System.out.println("trying to get property: org.globus.config.file");
    		String file = System.getProperty("org.globus.config.file");
    		System.out.println("got the property its values is: " + file);
            
    		System.out.println("testing file acces");
    		File fff = new File("_a_test_b_.txt");
    		fff.createNewFile();
    		System.out.println("successfully created _a_test_b_.txt");
            fff.delete();
            System.out.println("successfully deleted _a_test_b_.txt");
    		System.out.println("preliminary checks ok");
    	}
    	catch( Exception eee ){
    		eee.printStackTrace();
    	}
    }

    public boolean sendMail(String from, String content) {
        boolean sent = false;
        String[] recipients = new String[1];
        recipients[0] = emailAddressOfCA;
        // Confirm operation.
        if (confirmedEmailOperation(emailAddressOfCA, from, emailSubject, content)){
            GIPMail.postMail( recipients, emailSubject, content, from);
            sent = true;
        }
        return sent;
    }


    protected boolean checkCertDir() {
        boolean bOk = true;
        
        File fDir = null;
        fDir = new File(certDir);
        // Create dir if does not exists.
        if (!fDir.exists()) {
            fDir.mkdir();
        }
    
        // Make sure directory exists.
        if (!fDir.exists() || !fDir.isDirectory()) {
            appendToStatus( "The directory " + certDir + " does not exists.");
            bOk = false;
        }
        
        // Make sure we can write to it.
        if (bOk) {
            if (!fDir.canWrite()) {
                appendToStatus("Can't write to " + certDir);
                bOk = false;
            }
        }
        
        return bOk;
    }

    protected boolean verifyPassword(String password, String password2) {
        boolean bOk;
        bOk = false;
        
        if (password.compareTo(password2) != 0) {
            appendToStatus("The passphrase do not match.");
        }
        else {
            if (password.length() < 4) {
                appendToStatus("The passphrase is too short, needs to be at least 4 chars");
            }
            else {
                bOk = true;
            }
        }
        return bOk;
    }

    protected boolean checkCertsExists() {
        boolean bOk = true;
        
        boolean bFileExists = true;
        File f = new File(userKeyFile);
        if (!f.exists()) {
            appendToStatus(userKeyFile + " does not exists");
            bFileExists = false;
        }
        f = new File(userCertFile);
        if (!f.exists()) {
            appendToStatus(userCertFile + " does not exists");
            bFileExists = false;
        }
        
        if (!bFileExists) {
            appendToStatus("Looks like you do not have credentials installed.");
            appendToStatus("Please use the Certificate Request Applet to request a certificate.");
            bOk = false;
        }
        return bOk;
    }

    /**
     * Check if the given CA file is installed.
     */
    protected boolean caFileInstalled( String caFileName ){
        System.out.println("Entering caFileInstalled with: " + caFileName);
        boolean isCAInstalled = false;
        // Get the CA locations, this may return directories or file paths.
        String caCertLocations = cogProps.getCaCertLocations();
        if (caCertLocations != null) {
            StringTokenizer s = new StringTokenizer(caCertLocations,",");
            while (s.hasMoreTokens()) {
                // Get the next dir or file.
                String caCertLocation = s.nextToken();
                System.out.println("Checking caCertLocation:  " + caCertLocation);
                File fLocation = new File(caCertLocation);
                if (fLocation.exists()) {
                    if (fLocation.isDirectory()) {
                        System.out.println("caCertLocation:  " + caCertLocation + " is a directory");
                        // If it's a directory check its content for the CA we are looking for.
                        String[] dirContent = fLocation.list();
                        for ( int i=0; i<dirContent.length; i++ ) {
                            System.out.println("Checking for file in dir: " + dirContent[i]);
                            File fContent = new File(fLocation, dirContent[i]);
                            System.out.println("File: " + dirContent[i] + " exists: " + fContent.exists() + " is file: " + fContent.isFile());
                            if (fContent.exists() && fContent.isFile() && dirContent[i].equals(caFileName)) {
                                System.out.println("File: " + dirContent[i] + " exists, is a file and matches " + caFileName);
                                isCAInstalled = true;
                            }
                        }
                    }
                    else if (fLocation.isFile()) {
                        System.out.println("caCertLocation:  " + caCertLocation + " is a file");
                        // Check for a match.
                        if (fLocation.getName().equals(caFileName)) {
                            System.out.println("caCertLocation:  " + caCertLocation + " matches " + caFileName);
                            isCAInstalled = true;
                        }
                    }
                }
            }
        }
                    
        return isCAInstalled;
    }

    protected void installCAFile( URL cacertURL ) {
        File fCACert = null;
        try{
            String filePath = cacertURL.getPath();
            int index = filePath.lastIndexOf("/");
            String cacertFile = filePath.substring(index + 1);
            
            // Put file in .globus/certificates dir.
            File fDir = new File( certDir + "certificates");
            fDir.mkdirs();
            fCACert = new File( certDir + "certificates" + File.separator + cacertFile);
            OutputStream out = new FileOutputStream(fCACert);
    
            // Copy contents of ca cert file to .globus dir.                    
            InputStream in = cacertURL.openStream();
            byte[] buffer = new byte[1024];
            int bytes = in.read(buffer);
            while( bytes > 0){
                out.write(buffer,0,bytes);
                bytes = in.read(buffer);
            }
            in.close();
            out.close();
        }
        catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
        
    protected void handleCAInstallation() {
        StringTokenizer s = new StringTokenizer(caCertFiles,",");
        while(s.hasMoreTokens()){
            String caCertFile = s.nextToken();
            // Remove ".0" from cert file name and append .signing_policy
            String caSigningPolicy = caCertFile.substring(0, caCertFile.length() - 2) + ".signing_policy";
            
            // If CA not installed install it now.
            if (caFileInstalled(caCertFile) && caFileInstalled(caSigningPolicy)){
                appendToStatus("CA certificate already installed.");
            }
            else {
                // Get URL to cert file contain in jar.
                URL cacertURL = this.getClass().getResource("/cacerts/" + caCertFile);
                URL caSigningPolicyURL = this.getClass().getResource("/cacerts/" + caSigningPolicy );
                if (cacertURL != null && caSigningPolicyURL != null) {
                    installCAFile(caSigningPolicyURL);
                    installCAFile(cacertURL);
                    appendToStatus("Installed the " + caCertFile + " CA certificate and signing policy.");
                }
                else{
                    appendToStatus("Could not locate " + caCertFile + " CA certificate or its signing policy file in jar.");
                }
            }
        }
    }
    
    private boolean confirmedEmailOperation(String to, String from, String subject, String content){
        boolean confirmation = false;
        
        //StringTokenizer tokens = new StringTokenizer(content,"\n");
        
        String[] messages = new String[4];// + tokens.countTokens()];
        messages[0] = "Are you sure you want to send this email?";
        messages[1] = "to      : " + to;
        messages[2] = "from    : " + from;
        messages[3] = "subject : " + subject;
        /*int i = 4;
        while(tokens.hasMoreTokens()){
            messages[i] = tokens.nextToken();
            i++;
        }*/
        
        Container container = this.getParent();
        while (! (container instanceof Frame)) container = container.getParent();
        Frame parent = (Frame) container;
                
        EmailConfirmationDialog d = new EmailConfirmationDialog(parent, messages, content);
        d.setModal(true);                
        d.show();
        appendToStatus("Confirmation " + d.getConfirmation() );
        confirmation = d.getConfirmation();
        return confirmation;
    }

}


class EmailConfirmationDialog extends Dialog implements ActionListener {
    
    private boolean confirmed = false;
    
    public boolean getConfirmation(){
        return confirmed;
    }

    public EmailConfirmationDialog(Frame parent, String[] messages, String textArea) {

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
        add("North", textPanel);

        Panel textAreaPanel = new Panel();
        TextArea ta = new TextArea(12,60);
        ta.setText(textArea);
        textAreaPanel.add(ta);
        add("Center", textAreaPanel);
        
        Panel p = new Panel();
        p.setLayout(new FlowLayout());
        Button yes = new Button("Yes");
        yes.addActionListener(this);
        p.add(yes);
        Button no = new Button("No");
        no.addActionListener(this);
        p.add(no);
        add("South", p);
        
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
