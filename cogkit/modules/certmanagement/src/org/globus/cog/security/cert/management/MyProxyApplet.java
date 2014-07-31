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
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import org.globus.gsi.CertUtil;
import org.globus.myproxy.CredentialInfo;
import org.globus.myproxy.MyProxy;
import org.globus.myproxy.MyProxyException;
import org.globus.util.Util;
import org.gridforum.jgss.ExtendedGSSCredential;
import org.ietf.jgss.GSSCredential;

/**
 * @author Jean-Claude Cote
 */

public class MyProxyApplet extends GIPApplet implements ActionListener {

    private static final int KEY_LENGTH = 512;
    private static final int MYPROXY_SERVER_PORT = 7512;
    public static final int PORTAL_LIFETIME_HOURS = 2;
	private static final String PROPERTY_FILE = "MyProxyApplet";
    private int credLifetimeNumDays = 7;

	// Values configured by property file.
	private String putAction = null;
    private String getAction = null;
	private String infoAction = null;
	private String destroyAction = null;
    private String myProxyHostLabel = null;
    private String myProxyAccountNameLabel = null;
    private String gridPassPhraseLabel = null;
    private String myProxyPassPhraseLabel = null;
    private String myProxyPassPhraseConfirmationLabel = null;
    
	
	private Button putButton = null;
	private Button infoButton = null;
	private Button destroyButton = null;
    private Button getButton = null;
	private TextField myProxyHost = new TextField();
	private TextField myProxyUserNameField = new TextField();
	private TextField keyPasswordField = new TextField();
	private TextField myproxyPasswordField = new TextField();
	private TextField myproxyPasswordFieldConfirmation = new TextField();
    private TextField lifetimeField = new TextField("7");
	private String hostname = null;
    private String username = null;
    private String keyPassword = null;
    private String myproxyPassword = null;
    private GSSCredential credential = null;


    public void init(){
        super.init();
		// set host name.
		String host = getParameter("myProxyHost");
		if (host != null && host.length() > 0) {
			myProxyHost.setText(getParameter("myProxyHost"));
		}
		
        getAction = getLocString("getAction");
		putAction = getLocString("putAction");
		infoAction = getLocString("infoAction");
		destroyAction = getLocString("destroyAction");

        // Setup UI.
        putButton = new Button(putAction);
        infoButton = new Button(infoAction);
        getButton = new Button(getAction);
        destroyButton = new Button(destroyAction);

		Panel titlePanel = null;
		if (appletTitle.length() > 0) {
			titlePanel = new Panel();
			titlePanel.add(new Label(appletTitle));
			titlePanel.setFont(new Font("Arial", Font.BOLD, 24));
			titlePanel.setBackground(bgColor);
		}
		
		Panel inputPanel = new Panel();
		inputPanel.add(new Label(getLocString("MyProxy_host")));
		inputPanel.add(myProxyHost);
		inputPanel.add(new Label(getLocString("MyProxy_Account_Name")));
		inputPanel.add(myProxyUserNameField);
		inputPanel.add(new Label(getLocString("Grid_PassPhrase")));
		keyPasswordField.setEchoChar('*');
		inputPanel.add(keyPasswordField);
		inputPanel.add(new Label(getLocString("MyProxy_PassPhrase")));
		myproxyPasswordField.setEchoChar('*');
		inputPanel.add(myproxyPasswordField);
		inputPanel.add(new Label(getLocString("MyProxy_PassPhrase_Confirmation")));
		myproxyPasswordFieldConfirmation.setEchoChar('*');
		inputPanel.add(myproxyPasswordFieldConfirmation);
        inputPanel.add(new Label(getLocString("Lifetime")));
        inputPanel.add(lifetimeField);
		inputPanel.setLayout(new GridLayout(0, 2));
		inputPanel.setBackground(bgColor);

		Panel buttonPanel = new Panel();
		putButton.addActionListener(this);
		buttonPanel.add(putButton);
		infoButton.addActionListener(this);
		buttonPanel.add(infoButton);
        getButton.addActionListener(this);
        buttonPanel.add(getButton);
		destroyButton.addActionListener(this);
		buttonPanel.add(destroyButton);
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
		if(debug){
			doDebugTests();
		}
    	
		keyPassword = keyPasswordField.getText();
		hostname = myProxyHost.getText();
		username = myProxyUserNameField.getText();
		myproxyPassword = myproxyPasswordField.getText();
		String myproxyPassword2 = myproxyPasswordFieldConfirmation.getText();
        String tempLifetimeString = lifetimeField.getText();
 
		boolean bOk = true;
        
        if (tempLifetimeString.length() == 0 ) {
            tempLifetimeString = "7";
        }
        
        credLifetimeNumDays = Integer.parseInt(tempLifetimeString);
		
		if (username.length() == 0 ) {
			appendToStatus(getLocString("msg001"));
			bOk = false;
		}
		
		if( bOk ) {
			if (myproxyPassword.compareTo(myproxyPassword2) != 0) {
				appendToStatus(getLocString("msg003"));
				bOk = false;
			}
			else {
				if (myproxyPassword.length() < 5) {
					appendToStatus(getLocString("msg004"));
					bOk = false;
				}
			}
		}
		

		if( bOk ) {
	    	if( e.getActionCommand() == infoAction ) {
	    		doInfo();
	    	}
	    	else if( e.getActionCommand() == destroyAction ) {
                if( keyPassword.length() == 0) {
                    appendToStatus(getLocString("msg002"));
                    bOk = false;
                }
                // Check that certs exists.
                if (bOk) {
                    bOk = checkCertsExists();
                }
	    		doDestroy();
	    	}
	    	else if( e.getActionCommand() == putAction ){
                if( keyPassword.length() == 0) {
                    appendToStatus(getLocString("msg002"));
                    bOk = false;
                }
                // Check that certs exists.
                if (bOk) {
                    bOk = checkCertsExists();
                }
	    		doPut();
            }
            else if( e.getActionCommand() == getAction ){
                doGet();
            }
            else {
               appendToStatus(getLocString("msg005") + e.getActionCommand() );
            }
       }
    }

    private void doGet() {
        int credLifetime = credLifetimeNumDays * 24 * 3600;
        CertUtil.init();
        MyProxy myProxy = new MyProxy(hostname, MYPROXY_SERVER_PORT);
        if( credential == null && keyPassword.length() > 0){
            credential = createNewProxy(keyPassword, credLifetime, KEY_LENGTH);
        }
        GSSCredential cred = null;
        try {
            cred = myProxy.get(credential, username, myproxyPassword, credLifetime);
            
            // create a file
            String outputFile = cogProps.getProxyFile();
            File f = new File(outputFile);
            String path = f.getPath();

            OutputStream out = null;
            try {
                out = new FileOutputStream(path);
                // set read only permissions
                Util.setFilePermissions(path, 600);
                // write the contents
                byte [] data = ((ExtendedGSSCredential)cred).export(ExtendedGSSCredential.IMPEXP_OPAQUE);
                out.write(data);
            }
            finally {
                if (out != null) {
                    try { out.close(); } catch(Exception e) {}
                }
            }
        
            appendToStatus("A proxy has been received for user " + username + " in " + path);
        
        }
        catch(Exception e) {
            appendExceptionDetailsToStatus(e);
        }
    }
    
    private void doInfo() {
        int credLifetime = credLifetimeNumDays * 24 * 3600;
		CertUtil.init();
		boolean bInfOk = false;
		MyProxy myProxy = new MyProxy(hostname, MYPROXY_SERVER_PORT);
		if( credential == null && keyPassword.length() > 0){
			credential = createNewProxy(keyPassword, credLifetime, KEY_LENGTH);
		}
			CredentialInfo inf = null;
			try {
				inf = myProxy.info(credential, username, myproxyPassword);
				bInfOk = true;
			}
			catch(MyProxyException e){
				appendExceptionDetailsToStatus(e);
			}
			if( bInfOk ){
				appendToStatus(getLocString("msg006") + hostname + " " + inf.getOwner() );
			}
		}
	
	private void doDestroy() {
        int credLifetime = credLifetimeNumDays * 24 * 3600;
		CertUtil.init();
		boolean bDestroyOk = false;
		MyProxy myProxy = new MyProxy(hostname, MYPROXY_SERVER_PORT);
		if( credential == null ){
			credential = createNewProxy(keyPassword, credLifetime, KEY_LENGTH);
		}
		if( credential != null ){
			try {
				myProxy.destroy(credential, username, myproxyPassword);
				bDestroyOk= true;
			}
			catch(MyProxyException e){
				appendExceptionDetailsToStatus(e);
			}
			if( bDestroyOk){
				appendToStatus(getLocString("msg007") + username );
			}
		}
	}


    private void doPut() {
        int lifetime     = PORTAL_LIFETIME_HOURS * 3600;
        int credLifetime = credLifetimeNumDays * 24 * 3600;

        CertUtil.init();
        boolean bPutOk = false;
        MyProxy myProxy = new MyProxy(hostname, MYPROXY_SERVER_PORT);
		if( credential == null ) {
			credential = createNewProxy(keyPassword, credLifetime, KEY_LENGTH);
		}
		if( credential != null ){
			try {
				myProxy.put(credential, username, myproxyPassword, lifetime);
				bPutOk = true;
			}
			catch(MyProxyException e){
				appendExceptionDetailsToStatus(e);
			}
			if( bPutOk ){
				appendToStatus(getLocString("msg008") + credLifetime / 3600 + getLocString("msg009") + (credLifetime / (3600 * 24)) + getLocString("msg010") + username + getLocString("msg011") + hostname + ".");
			}
		}
    }

    /* (non-Javadoc)
     * @see ca.gc.nrc.gip.applets.GIPApplet#getPropertyFileLoc()
     */
    protected String getPropertyFileName() {
        // TODO Auto-generated method stub
        return PROPERTY_FILE;
    }
}

