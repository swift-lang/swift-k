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

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.common.task.InvalidSecurityContextException;
import org.globus.cog.abstraction.impl.common.task.InvalidServiceContactException;
import org.globus.cog.abstraction.impl.common.task.TaskSubmissionException;

import com.sshtools.common.hosts.AbstractHostKeyVerification;
import com.sshtools.j2ssh.SshClient;
import com.sshtools.j2ssh.authentication.AuthenticationProtocolState;
import com.sshtools.j2ssh.authentication.PasswordAuthenticationClient;
import com.sshtools.j2ssh.authentication.PublicKeyAuthenticationClient;
import com.sshtools.j2ssh.configuration.ConfigurationLoader;
import com.sshtools.j2ssh.configuration.SshConnectionProperties;
import com.sshtools.j2ssh.session.SessionChannelClient;
import com.sshtools.j2ssh.transport.InvalidHostFileException;
import com.sshtools.j2ssh.transport.cipher.SshCipherFactory;
import com.sshtools.j2ssh.transport.hmac.SshHmacFactory;
import com.sshtools.j2ssh.transport.publickey.SshPrivateKey;
import com.sshtools.j2ssh.transport.publickey.SshPrivateKeyFile;

public class Ssh {
    public static final Logger logger = Logger.getLogger(Ssh.class);
    
    public static final int DEFAULT_SOCKET_TIMEOUT = 120 * 1000;
    protected String host;
    protected int port = 22;
    protected String username;
    protected String password;
    protected String keyfile;
    protected String passphrase;
    protected String cipher;
    protected String mac;
    protected String fingerprint;
    protected String logfile = "ssh.log";
    protected boolean verifyhost = true;
    protected boolean always = false;
    protected SshClient client;
    protected String sshtoolsHome;
    protected SessionChannelClient session = null;
    private boolean connected, connecting;

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

            if (username == null) {
                throw new InvalidSecurityContextException(
                        "You must supply a username for authentication.");
            }

            if ((password == null) && (keyfile == null)) {
                throw new InvalidSecurityContextException(
                        "You must supply either a password or keyfile/passphrase to authenticate!");
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
                properties.setUsername(username);

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
                                            + SshCipherFactory
                                                    .getDefaultCipher());
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

                int result;
                boolean authenticated = false;

                if (logger.isDebugEnabled()) {
                    logger.debug("Authenticating " + username);
                }

                if (keyfile != null) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Performing public key authentication");
                    }

                    PublicKeyAuthenticationClient pk = new PublicKeyAuthenticationClient();

                    // Open up the private key file
                    SshPrivateKeyFile file = SshPrivateKeyFile.parse(new File(
                            keyfile));

                    // If the private key is passphrase protected then ask for
                    // the passphrase
                    if (file.isPassphraseProtected() && (passphrase == null)) {
                        throw new InvalidSecurityContextException(
                                "Private key file is passphrase protected, please supply a valid passphrase");
                    }

                    // Get the key
                    SshPrivateKey key = file.toPrivateKey(passphrase);
                    pk.setUsername(username);
                    pk.setKey(key);

                    // Try the authentication
                    result = client.authenticate(pk);

                    if (result == AuthenticationProtocolState.COMPLETE) {
                        authenticated = true;
                    }
                    else if (result == AuthenticationProtocolState.PARTIAL) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("Public key authentication "
                                + "completed, attempting password authentication");
                        }
                    }
                    else {
                        throw new InvalidSecurityContextException(
                            "Public Key Authentication failed");
                    }
                }

                if ((password != null) && (authenticated == false)) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Performing password authentication");
                    }

                    PasswordAuthenticationClient pwd = new PasswordAuthenticationClient();
                    pwd.setUsername(username);
                    pwd.setPassword(password);

                    result = client.authenticate(pwd);

                    if (result == AuthenticationProtocolState.COMPLETE) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("Authentication complete");
                        }
                    }
                    else if (result == AuthenticationProtocolState.PARTIAL) {
                        if (logger.isDebugEnabled()) {
                            logger.error("Password Authentication succeeded "
                                + "but further authentication required!");
                        }
                    }
                    else {
                        throw new InvalidSecurityContextException(
                            "Password Authentication failed");
                    }
                }
            }

        }
        catch (IOException sshe) {
            logger.debug(sshe);
            throw new TaskSubmissionException("SSH Connection failed: "
                    + sshe.getMessage());
        }
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setKeyfile(String keyfile) {
        this.keyfile = keyfile;
    }

    public void setPassphrase(String passphrase) {
        this.passphrase = passphrase;
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
