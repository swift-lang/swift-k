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
package com.sshtools.j2ssh.transport;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.sshtools.j2ssh.SshException;
import com.sshtools.j2ssh.transport.cipher.SshCipher;
import com.sshtools.j2ssh.transport.cipher.SshCipherFactory;
import com.sshtools.j2ssh.transport.hmac.SshHmac;
import com.sshtools.j2ssh.transport.hmac.SshHmacFactory;
import com.sshtools.j2ssh.transport.kex.KeyExchangeException;
import com.sshtools.j2ssh.transport.kex.SshKeyExchange;
import com.sshtools.j2ssh.transport.publickey.SshKeyPair;
import com.sshtools.j2ssh.transport.publickey.SshKeyPairFactory;
import com.sshtools.j2ssh.transport.publickey.SshPublicKey;


/**
 * DOCUMENT ME!
 *
 * @author $author$
 * @version $Revision: 1.1 $
 */
public class TransportProtocolClient extends TransportProtocolCommon {
    /** DOCUMENT ME! */
    protected SshPublicKey pk;
    private HostKeyVerification hosts;
    private Map services = new HashMap();
    private SshMessageStore ms = new SshMessageStore();

    /**
     * Creates a new TransportProtocolClient object.
     *
     * @param hosts DOCUMENT ME!
     *
     * @throws TransportProtocolException DOCUMENT ME!
     */
    public TransportProtocolClient(HostKeyVerification hosts)
        throws TransportProtocolException {
        super();
        this.hosts = hosts;
    }

    /**
     * DOCUMENT ME!
     *
     * @param msg DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     */
    public void onMessageReceived(SshMessage msg) throws IOException {
        throw new IOException("No messages are registered");
    }

    /**
     * DOCUMENT ME!
     *
     * @throws MessageAlreadyRegisteredException DOCUMENT ME!
     */
    public void registerTransportMessages()
        throws MessageAlreadyRegisteredException {
        // Setup our private message store, we wont be registering any direct messages
        ms.registerMessage(SshMsgServiceAccept.SSH_MSG_SERVICE_ACCEPT,
            SshMsgServiceAccept.class);
        this.addMessageStore(ms);
    }

    /**
     * DOCUMENT ME!
     *
     * @param service DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     * @throws SshException DOCUMENT ME!
     */
    public void requestService(Service service) throws IOException {
        // Make sure the service is supported
        if (service.getState().getValue() != ServiceState.SERVICE_UNINITIALIZED) {
            throw new IOException("The service instance must be uninitialized");
        }

        if ((state.getValue() != TransportProtocolState.CONNECTED)
                && (state.getValue() != TransportProtocolState.PERFORMING_KEYEXCHANGE)) {
            throw new IOException("The transport protocol is not connected");
        }

        try {
            state.waitForState(TransportProtocolState.CONNECTED);
        } catch (InterruptedException ie) {
            throw new IOException("The operation was interrupted");
        }

        service.init(Service.REQUESTING_SERVICE, this); // , null);

        // Put the service on our list awaiting acceptance
        services.put(service.getServiceName(), service);

        // Create and send the message
        SshMessage msg = new SshMsgServiceRequest(service.getServiceName());
        sendMessage(msg, this);

        try {
            // Wait for the accept message, if the service is not accepted the
            // transport protocol disconencts which should cause an excpetion
            msg = ms.getMessage(SshMsgServiceAccept.SSH_MSG_SERVICE_ACCEPT);
        } catch (InterruptedException ex) {
            throw new SshException(
                "The thread was interrupted whilst waiting for a transport protocol message");
        }

        return;
    }

    /**
     * DOCUMENT ME!
     */
    protected void onDisconnect() {
        Iterator it = services.entrySet().iterator();
        Map.Entry entry;

        while (it.hasNext()) {
            entry = (Map.Entry) it.next();
            ((Service) entry.getValue()).stop();
            services.remove(entry.getKey());
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws AlgorithmNotAgreedException DOCUMENT ME!
     */
    protected String getDecryptionAlgorithm()
        throws AlgorithmNotAgreedException {
        return determineAlgorithm(clientKexInit.getSupportedSCEncryption(),
            serverKexInit.getSupportedSCEncryption());
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws AlgorithmNotAgreedException DOCUMENT ME!
     */
    protected String getEncryptionAlgorithm()
        throws AlgorithmNotAgreedException {
        return determineAlgorithm(clientKexInit.getSupportedCSEncryption(),
            serverKexInit.getSupportedCSEncryption());
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws AlgorithmNotAgreedException DOCUMENT ME!
     */
    protected String getInputStreamCompAlgortihm()
        throws AlgorithmNotAgreedException {
        return determineAlgorithm(clientKexInit.getSupportedSCComp(),
            serverKexInit.getSupportedSCComp());
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws AlgorithmNotAgreedException DOCUMENT ME!
     */
    protected String getInputStreamMacAlgorithm()
        throws AlgorithmNotAgreedException {
        return determineAlgorithm(clientKexInit.getSupportedSCMac(),
            serverKexInit.getSupportedSCMac());
    }

    /**
     * DOCUMENT ME!
     */
    protected void setLocalIdent() {
        clientIdent = "SSH-" + PROTOCOL_VERSION + "-"
            + SOFTWARE_VERSION_COMMENTS + " [CLIENT]";
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String getLocalId() {
        return clientIdent;
    }

    /**
     * DOCUMENT ME!
     *
     * @param msg DOCUMENT ME!
     */
    protected void setLocalKexInit(SshMsgKexInit msg) {
        log.debug(msg.toString());
        clientKexInit = msg;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    protected SshMsgKexInit getLocalKexInit() {
        return clientKexInit;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws AlgorithmNotAgreedException DOCUMENT ME!
     */
    protected String getOutputStreamCompAlgorithm()
        throws AlgorithmNotAgreedException {
        return determineAlgorithm(clientKexInit.getSupportedCSComp(),
            serverKexInit.getSupportedCSComp());
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws AlgorithmNotAgreedException DOCUMENT ME!
     */
    protected String getOutputStreamMacAlgorithm()
        throws AlgorithmNotAgreedException {
        return determineAlgorithm(clientKexInit.getSupportedCSMac(),
            serverKexInit.getSupportedCSMac());
    }

    /**
     * DOCUMENT ME!
     *
     * @param ident DOCUMENT ME!
     */
    protected void setRemoteIdent(String ident) {
        serverIdent = ident;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String getRemoteId() {
        return serverIdent;
    }

    /**
     * DOCUMENT ME!
     *
     * @param msg DOCUMENT ME!
     */
    protected void setRemoteKexInit(SshMsgKexInit msg) {
        serverKexInit = msg;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    protected SshMsgKexInit getRemoteKexInit() {
        return serverKexInit;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public SshPublicKey getServerHostKey() {
        return pk;
    }

    /**
     * DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     * @throws TransportProtocolException DOCUMENT ME!
     */
    protected void onStartTransportProtocol() throws IOException {
        while ((state.getValue() != TransportProtocolState.CONNECTED)
                && (state.getValue() != TransportProtocolState.DISCONNECTED)) {
            try {
                state.waitForStateUpdate();
            } catch (InterruptedException ex) {
                throw new IOException("The operation was interrupted");
            }
        }

        if (state.getValue() == TransportProtocolState.DISCONNECTED) {
            if (state.hasError()) {
                throw state.getLastError();
            } else {
                throw new TransportProtocolException(
                    "The connection did not complete");
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param kex DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     */
    protected void performKeyExchange(SshKeyExchange kex)
        throws IOException {
        // Start the key exchange instance
        kex.performClientExchange(clientIdent, serverIdent,
            clientKexInit.toByteArray(), serverKexInit.toByteArray());

        // Verify the hoskey
        if (!verifyHostKey(kex.getHostKey(), kex.getSignature(),
                    kex.getExchangeHash())) {
             sendDisconnect(SshMsgDisconnect.HOST_KEY_NOT_VERIFIABLE,
                "The host key supplied was not valid",
                new KeyExchangeException(
                    "The host key is invalid or was not accepted!"));
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param encryptCSKey DOCUMENT ME!
     * @param encryptCSIV DOCUMENT ME!
     * @param encryptSCKey DOCUMENT ME!
     * @param encryptSCIV DOCUMENT ME!
     * @param macCSKey DOCUMENT ME!
     * @param macSCKey DOCUMENT ME!
     *
     * @throws AlgorithmNotAgreedException DOCUMENT ME!
     * @throws AlgorithmOperationException DOCUMENT ME!
     * @throws AlgorithmNotSupportedException DOCUMENT ME!
     * @throws AlgorithmInitializationException DOCUMENT ME!
     */
    protected void setupNewKeys(byte[] encryptCSKey, byte[] encryptCSIV,
        byte[] encryptSCKey, byte[] encryptSCIV, byte[] macCSKey,
        byte[] macSCKey)
        throws AlgorithmNotAgreedException, AlgorithmOperationException,
            AlgorithmNotSupportedException, AlgorithmInitializationException {
        // Setup the encryption cipher
        SshCipher sshCipher = SshCipherFactory.newInstance(getEncryptionAlgorithm());
        sshCipher.init(SshCipher.ENCRYPT_MODE, encryptCSIV, encryptCSKey);
        algorithmsOut.setCipher(sshCipher);

        // Setup the decryption cipher
        sshCipher = SshCipherFactory.newInstance(getDecryptionAlgorithm());
        sshCipher.init(SshCipher.DECRYPT_MODE, encryptSCIV, encryptSCKey);
        algorithmsIn.setCipher(sshCipher);

        // Create and put our macs into operation
        SshHmac hmac = SshHmacFactory.newInstance(getOutputStreamMacAlgorithm());
        hmac.init(macCSKey);
        algorithmsOut.setHmac(hmac);

        hmac = SshHmacFactory.newInstance(getInputStreamMacAlgorithm());
        hmac.init(macSCKey);
        algorithmsIn.setHmac(hmac);
    }

    /**
     * DOCUMENT ME!
     *
     * @param key DOCUMENT ME!
     * @param sig DOCUMENT ME!
     * @param sigdata DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws TransportProtocolException DOCUMENT ME!
     */
    protected boolean verifyHostKey(byte[] key, byte[] sig, byte[] sigdata)
        throws TransportProtocolException {
        // Determine the public key algorithm and obtain an instance
        SshKeyPair pair = SshKeyPairFactory.newInstance(determineAlgorithm(
                    clientKexInit.getSupportedPublicKeys(),
                    serverKexInit.getSupportedPublicKeys()));

        // Iniialize the public key instance
        pk = pair.setPublicKey(key);

        // We have a valid key so verify it against the allowed hosts
        String host;

        try {
            InetAddress addr = InetAddress.getByName(properties.getHost());

            if (!addr.getHostAddress().equals(properties.getHost())) {
                host = addr.getHostName() + "," + addr.getHostAddress();
            } else {
                host = addr.getHostAddress();
            }
        } catch (UnknownHostException ex) {
            log.info("The host " + properties.getHost()
                + " could not be resolved");
            host = properties.getHost();
        }

        if (!hosts.verifyHost(host, pk)) {
          log.info("The host key was not accepted");
          return false;
        }

        boolean result = pk.verifySignature(sig, sigdata);
        log.info("The host key signature is " + (result ? " valid" : "invalid"));
        return result;
    }
}
