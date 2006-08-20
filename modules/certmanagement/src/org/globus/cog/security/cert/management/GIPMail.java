
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/*
 * Created on Nov 6, 2003
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package org.globus.cog.security.cert.management;


import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 * @author Jean-Claude Cote
 */
public class GIPMail {
    private static final String SMTP_HOST = "mail.nrc.ca";
    private static final boolean debug = true;

    public static void postMail(String recipients[], String subject, String message, String from) {
        //Set the host smtp address
        Properties props = new Properties();
        props.put("mail.smtp.host", SMTP_HOST);
    
        // create some properties and get the default Session
        Session session = Session.getDefaultInstance(props, null);
        session.setDebug(debug);
    
        // create a message
        Message msg = new MimeMessage(session);
    
        // set the from and to address
        InternetAddress addressFrom = null;
        try {
            addressFrom = new InternetAddress(from);
            msg.setFrom(addressFrom);
        
            InternetAddress[] addressTo = new InternetAddress[recipients.length];
            for (int i = 0; i < recipients.length; i++) {
                addressTo[i] = new InternetAddress(recipients[i]);
            }
            msg.setRecipients(Message.RecipientType.TO, addressTo);
            // Setting the Subject and Content Type
            msg.setSubject(subject);
            msg.setContent(message, "text/plain");
            Transport.send(msg);
        }
        catch (AddressException e) {
            e.printStackTrace();
        }
        catch (MessagingException e1) {
            e1.printStackTrace();
        }
    }

}
