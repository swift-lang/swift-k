// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/*
 *  Sshtools - Java SSH2 API
 *
 *  Copyright (C) 2002 Lee David Painter.
 *
 *  Written by: 2002 Lee David Painter <lee@sshtools.com>
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Library General Public License
 *  as published by the Free Software Foundation; either version 2 of
 *  the License, or (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Library General Public License for more details.
 *
 *  You should have received a copy of the GNU Library General Public
 *  License along with this program; if not, write to the Free Software
 *  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package org.globus.cog.abstraction.impl.ssh;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.common.CredentialsDialog.Prompt;
import org.globus.cog.abstraction.impl.common.PasswordAuthentication;
import org.globus.cog.abstraction.impl.common.PublicKeyAuthentication;
import org.globus.cog.abstraction.impl.common.task.InvalidSecurityContextException;
import org.globus.cog.abstraction.impl.common.task.InvalidServiceContactException;
import org.globus.cog.abstraction.impl.common.task.TaskSubmissionException;

import com.sshtools.common.hosts.AbstractHostKeyVerification;
import com.sshtools.j2ssh.SshClient;
import com.sshtools.j2ssh.authentication.AuthenticationProtocolState;
import com.sshtools.j2ssh.authentication.KBIAuthenticationClient;
import com.sshtools.j2ssh.authentication.KBIPrompt;
import com.sshtools.j2ssh.authentication.KBIRequestHandler;
import com.sshtools.j2ssh.authentication.PasswordAuthenticationClient;
import com.sshtools.j2ssh.authentication.PublicKeyAuthenticationClient;
import com.sshtools.j2ssh.configuration.ConfigurationLoader;
import com.sshtools.j2ssh.configuration.SshConnectionProperties;
import com.sshtools.j2ssh.session.SessionChannelClient;
import com.sshtools.j2ssh.transport.InvalidHostFileException;
import com.sshtools.j2ssh.transport.cipher.SshCipherFactory;
import com.sshtools.j2ssh.transport.hmac.SshHmacFactory;
import com.sshtools.j2ssh.transport.publickey.InvalidSshKeyException;
import com.sshtools.j2ssh.transport.publickey.SshPrivateKey;
import com.sshtools.j2ssh.transport.publickey.SshPrivateKeyFile;

public class Ssh {
    public static final Logger logger = Logger.getLogger(Ssh.class);

    public static final int DEFAULT_SOCKET_TIMEOUT = 120 * 1000;
    protected String host;
    protected int port = 22;
    protected String username;
    protected Object credentials;
    protected String cipher;
    protected String mac;
    protected String fingerprint;
    protected String logfile = "ssh.log";
    protected boolean verifyhost = true;
    protected boolean always = false;
    protected SshClient client;
    protected String sshtoolsHome;
    protected SessionChannelClient session = null;
    private boolean connected, connecting, promptedForUsername;

    private InteractiveAuthentication interactive;

    static {
        if (System.getProperty("sshtools.home") == null) {
            System.setProperty("sshtools.home", System.getProperty("user.home")
                    + File.separator + ".globus");
        }
        File dir = new File(System.getProperty("sshtools.home"), "conf");
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                throw new RuntimeException("Cannot create "
                        + dir.getAbsolutePath()
                        + ". Please check if you have a .globus directory"
                        + "in your home directory, and that it is writable");
            }
        }
        else if (!dir.isDirectory()) {
            throw new RuntimeException("Cannot create directory: "
                    + dir.getAbsolutePath()
                    + ". A file with that name already exists");
        }
        File hosts = new File(dir, "hosts.xml");
        if (!hosts.exists()) {
            try {
                FileWriter w = new FileWriter(hosts);
                w.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
                w.write("<HostAuthorizations>\n");
                w
                    .write("<!-- please consult the j2ssh documentation for details on this file -->\n");
                w.write("</HostAuthorizations>\n");
                w.close();
            }
            catch (IOException e) {
                throw new RuntimeException(
                    "Cannot create hosts.xml file. Please check if you have a .globus directory"
                            + "in your home directory, and that it is writable",
                    e);
            }
        }
    }

    public Ssh() {
        setVerifyhost(false);
    }

    public SessionChannelClient openSessionChannel() throws IOException {
        return client.openSessionChannel();
    }

    public void connect() throws InvalidSecurityContextException,
            InvalidServiceContactException, TaskSubmissionException {
        try {
            if (host == null) {
                throw new InvalidServiceContactException(
                    "You must provide a host to connect to.");
            }

            if (verifyhost && (fingerprint == null)) {
                throw new InvalidSecurityContextException(
                    "Public key fingerprint required to verify the host");
            }

            if (sshtoolsHome != null) {
                System.setProperty("sshtools.home", sshtoolsHome);
            }

            if (logger.isDebugEnabled()) {
                logger.debug("Initializing J2SSH");
            }

            if (logfile != null) {
                System.setProperty("sshtools.logfile", logfile);
            }

            ConfigurationLoader.initialize(false);

            if (logger.isDebugEnabled()) {
                logger.debug("Creating connection to " + host + ":"
                        + String.valueOf(port));
            }

            if (client == null) {
                client = new SshClient();

                SshConnectionProperties properties = new SshConnectionProperties();
                properties.setHost(host);
                properties.setPort(port);

                if (cipher != null) {
                    if (SshCipherFactory.getSupportedCiphers().contains(cipher)) {
                        properties.setPrefSCEncryption(cipher);
                        properties.setPrefCSEncryption(cipher);
                    }
                    else {
                        if (logger.isDebugEnabled()) {
                            logger
                                .debug(cipher
                                        + " is not a supported cipher, using default "
                                        + SshCipherFactory.getDefaultCipher());
                        }
                    }
                }

                if (mac != null) {
                    if (SshHmacFactory.getSupportedMacs().contains(mac)) {
                        properties.setPrefCSMac(mac);
                        properties.setPrefSCMac(mac);
                    }
                    else {
                        if (logger.isDebugEnabled()) {
                            logger.debug(mac
                                    + " is not a supported mac, using default "
                                    + SshHmacFactory.getDefaultHmac());
                        }
                    }
                }

                if (logger.isDebugEnabled()) {
                    logger.debug("Connecting....");
                }

                client.setSocketTimeout(DEFAULT_SOCKET_TIMEOUT);
                client.connect(properties, new AbstractHostKeyVerification() {
                    public void onUnknownHost(String hostname,
                            String fingerprint) throws InvalidHostFileException {
                        if (Ssh.this.verifyhost) {
                            if (fingerprint
                                .equalsIgnoreCase(Ssh.this.fingerprint)) {
                                allowHost(hostname, fingerprint, always);
                            }
                        }
                        else {
                            allowHost(hostname, fingerprint, always);
                        }
                    }

                    public void onHostKeyMismatch(String hostname,
                            String allowed, String supplied)
                            throws InvalidHostFileException {
                        if (Ssh.this.verifyhost) {
                            if (supplied.equalsIgnoreCase(Ssh.this.fingerprint)) {
                                allowHost(hostname, supplied, always);
                            }
                        }
                        else {
                            allowHost(hostname, supplied, always);
                        }
                    }

                    public void onDeniedHost(String host) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("The server host key is denied!");
                        }
                    }

                });

                boolean authenticated = false;
                if (credentials instanceof PublicKeyAuthentication) {
                    authenticated = authenticateWithPublicKey(true);
                }
                else if (credentials instanceof PasswordAuthentication) {
                    authenticated = authenticateWithPassword(true);
                }
                else if (credentials instanceof InteractiveAuthentication) {
                    authenticated = authenticateWithKBI(true);
                }
                
                if (authenticated) {
                    return;
                }

                if (username == null) {
                    promptForUsername();
                }

                if (username == null) {
                    throw new InvalidSecurityContextException(
                        "Authentication canceled by the user");
                }

                if (logger.isDebugEnabled()) {
                    logger.debug("Authenticating " + username);
                }

                List l = client.getAvailableAuthMethods(username);
                if (logger.isDebugEnabled()) {
                    logger.debug("Valid methods: " + l);
                }
                
                Iterator i = l.iterator();
                while (i.hasNext()) {
                    String method = (String) i.next();
                    if ("publickey".equals(method)) {
                        authenticated = authenticateWithPublicKey(false);
                    }
                    else if ("password".equals(method)) {
                        authenticated = authenticateWithPassword(false);
                    }
                    else if ("keyboard-interactive".equals(method)) {
                        authenticated = authenticateWithKBI(false);
                    }
                    else {
                        if (logger.isDebugEnabled()) {
                            logger
                                .debug("Skipping unknown authentication method: "
                                        + method);
                        }
                    }
                    if (authenticated) {
                        break;
                    }
                }
                if (!authenticated) {
                    throw new InvalidSecurityContextException(
                    "Authentication failed");
                }
            }

        }
        catch (IOException sshe) {
            logger.debug(sshe);
            throw new TaskSubmissionException("SSH Connection failed: "
                    + sshe.getMessage());
        }
    }

    private void promptForUsername() {
        username = new String(CredentialsDialog.showCredentialsDialog(host,
            new Prompt[] { new Prompt("Username: ", Prompt.TYPE_TEXT, System
                .getProperty("user.name")) })[0]);
        promptedForUsername = username != null;
    }

    private boolean authenticateWithPublicKey(boolean force)
            throws InvalidSecurityContextException, InvalidSshKeyException,
            IOException {
        int result;
        String keyfile = null;
        char[] passphrase = null;
        if (credentials instanceof PublicKeyAuthentication) {
            PublicKeyAuthentication auth = (PublicKeyAuthentication) credentials;
            keyfile = auth.getPrivateKeyFile().getAbsolutePath();
            passphrase = auth.getPassPhrase();
            username = auth.getUsername();
        }
        else if (credentials instanceof InteractiveAuthentication) {
            InteractiveAuthentication auth = (InteractiveAuthentication) credentials;
            keyfile = auth.getKeyFile();
            passphrase = auth.getPassPhrase();
            username = auth.getUsername();
        }

        if (!force || keyfile == null) {
            keyfile = System.getProperty("user.home")
                    + File.separator + ".ssh" + File.separator + "identity";
            if (passphrase == null) {
                passphrase = new char[0];
            }
        }
        if (keyfile == null || passphrase == null) {
            if (username == null) {
                username = System.getProperty("user.name");
            }
            char[][] results = CredentialsDialog.showCredentialsDialog(host,
                !promptedForUsername ? 
                        new Prompt[] {
                            new Prompt("Username: ", Prompt.TYPE_TEXT, username),
                            new Prompt("Private Key: ", Prompt.TYPE_FILE, keyfile),
                            new Prompt("Passphrase: ", Prompt.TYPE_HIDDEN_TEXT) }
                        : 
                            new Prompt[] {
                                new Prompt("Private Key: ", Prompt.TYPE_FILE, keyfile),
                                new Prompt("Passphrase: ", Prompt.TYPE_HIDDEN_TEXT) });
            if (results == null) {
                throw new InvalidSecurityContextException(
                    "Authentication canceled by user");
            }
            if (results.length == 3) {
                username = new String(results[0]);
                keyfile = new String(results[1]);
                passphrase = results[2];
            }
            else {
                keyfile = new String(results[0]);
                passphrase = results[1];
            }
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Performing public key authentication");
        }

        PublicKeyAuthenticationClient pk = new PublicKeyAuthenticationClient();

        // Open up the private key file
        SshPrivateKeyFile file = SshPrivateKeyFile.parse(new File(keyfile));

        // If the private key is passphrase protected then ask for
        // the passphrase
        if (file.isPassphraseProtected() && (passphrase == null)) {
            throw new InvalidSecurityContextException(
                "Private key file is passphrase protected, please supply a valid passphrase");
        }

        // Get the key
        SshPrivateKey key;
        try {
            key = file.toPrivateKey(new String(passphrase));
        }
        catch (IOException e) {
            if (force) {
                throw new InvalidSecurityContextException("Invalid private key or passphrase", e);
            }
            else {
                return false;
            }
        }
        pk.setUsername(username);
        pk.setKey(key);

        // Try the authentication
        result = client.authenticate(pk);

        if (result == AuthenticationProtocolState.COMPLETE) {
            return true;
        }
        else if (result == AuthenticationProtocolState.PARTIAL) {
            if (logger.isDebugEnabled()) {
                logger.error("Public Key Authentication succeeded "
                        + "but further authentication required!");
            }
            if (force) {
                throw new InvalidSecurityContextException(
                    "The server requested additional authentication, but none is possible.");
            }
            else {
                return false;
            }
        }
        else {
            if (force) {
                throw new InvalidSecurityContextException(
                    "Public Key Authentication failed");
            }
            else {
                return false;
            }
        }
    }

    private boolean authenticateWithPassword(boolean force)
            throws InvalidSecurityContextException, IOException {
        char[] password = null;
        if (credentials instanceof PasswordAuthentication) {
            PasswordAuthentication auth = (PasswordAuthentication) credentials;
            password = auth.getPassword();
            username = auth.getUsername();
        }
        else if (credentials instanceof InteractiveAuthentication) {
            InteractiveAuthentication auth = (InteractiveAuthentication) credentials;
            password = auth.getPassword();
            username = auth.getUsername();
        }

        if (username == null || password == null) {
            if (username == null) {
                username = System.getProperty("user.name");
            }
            char[][] results = CredentialsDialog.showCredentialsDialog(host,
                !promptedForUsername ?
                        new Prompt[] {
                            new Prompt("Username: ", Prompt.TYPE_TEXT, username),
                            new Prompt("Password: ", Prompt.TYPE_HIDDEN_TEXT) }
                            :
                        new Prompt[] {
                            new Prompt("Password: ", Prompt.TYPE_HIDDEN_TEXT) });
            if (results == null) {
                throw new InvalidSecurityContextException(
                    "Authentication canceled by user");
            }
            if (results.length == 2) {
                username = new String(results[0]);
                password = results[1];
            }
            else {
                password = results[0];
            }
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Performing password authentication");
        }

        PasswordAuthenticationClient pwd = new PasswordAuthenticationClient();
        pwd.setUsername(username);
        pwd.setPassword(new String(password));

        int result = client.authenticate(pwd);

        if (result == AuthenticationProtocolState.COMPLETE) {
            if (logger.isDebugEnabled()) {
                logger.debug("Authentication complete");
            }
            return true;
        }
        else if (result == AuthenticationProtocolState.PARTIAL) {
            if (logger.isDebugEnabled()) {
                logger.error("Password Authentication succeeded "
                        + "but further authentication required!");
            }
            if (force) {
                throw new InvalidSecurityContextException(
                    "The server requested additional authentication, but none is possible.");
            }
            else {
                return false;
            }
        }
        else {
            if (force) {
                throw new InvalidSecurityContextException(
                    "Password Authentication failed");
            }
            else {
                return false;
            }
        }

    }

    private boolean authenticateWithKBI(boolean force)
            throws InvalidSecurityContextException, IOException {
        final KBIAuthenticationClient kbi = new KBIAuthenticationClient();
        kbi.setKBIRequestHandler(new KBIRequestHandler() {
            public void showPrompts(String name, String instruction,
                    KBIPrompt[] prompts) {
                if (prompts == null) {
                    return;
                }
                Prompt[] p = new Prompt[prompts.length];
                for (int i = 0; i < prompts.length; i++) {
                    p[i] = new Prompt(prompts[i].getPrompt(),
                        Prompt.TYPE_HIDDEN_TEXT);
                }
                if (logger.isDebugEnabled()) {
                    logger.debug("Displaying credential dialog");
                }
                char[][] results = CredentialsDialog.showCredentialsDialog(host
                        + (empty(name) ? "" : " - " + name) 
                        + (empty(instruction) ? "" : " - " + instruction), p);
                if (results != null) {
                    for (int i = 0; i < prompts.length; i++) {
                        prompts[i].setResponse(new String(results[i]));
                    }
                }
            }
        });

        if (logger.isDebugEnabled()) {
            logger.debug("Performing keyboard-interactive authentication");
        }
        if (username == null) {
            promptForUsername();
        }
        kbi.setUsername(username);
        int result = client.authenticate(kbi);
        if (result == AuthenticationProtocolState.COMPLETE) {
            if (logger.isDebugEnabled()) {
                logger.debug("Authentication complete");
            }
            return true;
        }
        else if (result == AuthenticationProtocolState.PARTIAL) {
            if (logger.isDebugEnabled()) {
                logger.error("Keyboard Interactive Authentication succeeded "
                        + "but further authentication required!");
            }
            if (force) {
                throw new InvalidSecurityContextException(
                    "The server requested additional authentication, but none is possible.");
            }
            else {
                return false;
            }
        }
        else {
            if (force) {
                throw new InvalidSecurityContextException(
                    "Keyboard Interactive Authentication failed");
            }
            else {
                return false;
            }
        }
    }
    
    private boolean empty(String s) {
        return s == null || s.equals("");
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setCredentials(Object credentials) {
        this.credentials = credentials;
    }

    public void setCipher(String cipher) {
        this.cipher = cipher;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public void setLogfile(String logfile) {
        this.logfile = logfile;
    }

    public void setVerifyhost(boolean verifyhost) {
        this.verifyhost = verifyhost;
    }

    public void setAlways(boolean always) {
        this.always = always;
    }

    public void setSshtoolshome(String sshtoolsHome) {
        this.sshtoolsHome = sshtoolsHome;
    }

    public void disconnect() {
        client.disconnect();
    }

}
