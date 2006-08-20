
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

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Label;
import java.awt.Panel;
import java.security.cert.X509Certificate;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

import org.globus.gsi.CertUtil;

/**
 * @author Jean-Claude Cote</a>
 */
public class CertInfoApplet extends GIPApplet {

	private static final String PROPERTY_FILE = "CertInfoApplet";
	
    public void init() {
        super.init();
        
        // Setup UI.
        Panel titlePanel = null;
        if (appletTitle.length() > 0) {
            titlePanel = new Panel(); 
            titlePanel.add(new Label(appletTitle));
            titlePanel.setFont(new Font("Arial", Font.BOLD, 24));
            titlePanel.setBackground(bgColor);
        }
        
        Panel statusPanel = new Panel();
        Font font = new Font("Courier", Font.PLAIN, 12);
        status.setFont(font);
        
        // Change the status area size
        status.setRows(25);
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
        gridbag.setConstraints(statusPanel, c);
        mainPanel.add(statusPanel);
        mainPanel.setLayout(gridbag);

        this.add(mainPanel);
        this.setBackground(bgColor);
    }

    public void start() {

        try {
            X509Certificate cert = CertUtil.loadCertificate(userCertFile);

            boolean globusStyle = false;
            String dn = null;
            if (globusStyle) {
            dn = CertUtil.toGlobusID(cert.getSubjectDN().getName());
            } else {
            dn = cert.getSubjectDN().getName();
            }
            appendToStatus("subject     : " + dn);
    
            dn = null;
            if (globusStyle) {
            dn = CertUtil.toGlobusID(cert.getIssuerDN().getName());
            } else {
            dn = cert.getIssuerDN().getName();
            }
            appendToStatus("issuer      : " + dn);
    
            TimeZone tz   = null;
            DateFormat df = null;
            if (globusStyle) {
                tz = TimeZone.getTimeZone("GMT");
                df = new SimpleDateFormat("MMM dd HH:mm:ss yyyy z");
                df.setTimeZone(tz);
            }

            String dt = null;
            if (globusStyle) {
            dt = df.format(cert.getNotBefore());
            } else {
            dt = cert.getNotBefore().toString();
            }
            appendToStatus("start date  : " + dt);
    
            dt = null;
            if (globusStyle) {
            dt = df.format(cert.getNotAfter());
            } else {
            dt = cert.getNotAfter().toString();
            }
            appendToStatus("end date    : " + dt);
    
            appendToStatus("certificate :");
            appendToStatus(cert.toString());
    
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

    /* (non-Javadoc)
     * @see ca.gc.nrc.gip.applets.GIPApplet#getPropertyFileLoc()
     */
    protected String getPropertyFileName() {
        // TODO Auto-generated method stub
        return PROPERTY_FILE;
    }


}
