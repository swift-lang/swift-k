//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jun 23, 2010
 */
package org.globus.cog.abstraction.impl.ssh;

import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.common.task.InvalidSecurityContextException;
import org.globus.cog.abstraction.impl.ssh.ProxyForwarder.Info;
import org.globus.cog.abstraction.impl.ssh.ProxyForwarder.Key;
import org.globus.cog.abstraction.interfaces.Delegation;
import org.globus.gsi.CertUtil;
import org.globus.gsi.GSIConstants;
import org.globus.gsi.GlobusCredential;
import org.globus.gsi.GlobusCredentialException;
import org.globus.gsi.X509ExtensionSet;
import org.globus.gsi.bc.BouncyCastleCertProcessingFactory;

/**
 * 
 * The non-use of the term "delegation" is intentional since there is one aspect
 * of delegation that does not apply here. When doing delegation the client
 * never sees the private key of the delegated credential.
 * 
 */
public class ProxyForwardingManager {
    public static final Logger logger = Logger.getLogger(ProxyForwardingManager.class);

    public static final long TIME_MARGIN = 10000;
        
    private static ProxyForwardingManager dm;

    public static synchronized ProxyForwardingManager getDefault() {
        if (dm == null) {
            dm = new ProxyForwardingManager();
        }
        return dm;
    }
    
    private Map<Key, Info> state;

    private ProxyForwardingManager() {
        this.state = new HashMap<Key, Info>();
    }

    public synchronized Info forwardProxy(int type, ProxyForwarder f)
            throws InvalidSecurityContextException {
        if (type == Delegation.NO_DELEGATION) {
            return null;
        }
        Key key = f.getKey(type);
        Info info = state.get(key);
        if (info == null || info.expirationTime - TIME_MARGIN < System.currentTimeMillis()) {
            info = actualForward(key, f);
            state.put(key, info);
        }
        return info;
    }

    private Info actualForward(Key key, ProxyForwarder f)
            throws InvalidSecurityContextException {
        try {
            GlobusCredential cred = generateNewCredential(key);
            return f.writeCredential(cred);
        }
        catch (InvalidSecurityContextException e) {
            throw e;
        }
        catch (Exception e) {
            throw new InvalidSecurityContextException(e);
        }
    }
    
    private GlobusCredential generateNewCredential(Key key) throws GlobusCredentialException,
            InvalidSecurityContextException, GeneralSecurityException {
        GlobusCredential src = GlobusCredential.getDefaultCredential();
        if (src == null) {
            throw new InvalidSecurityContextException("No default credential found");
        }

        // If only the security stuff in DelegationUtil@GT4 would be
        // separable from the WS crap
        BouncyCastleCertProcessingFactory factory =
                    BouncyCastleCertProcessingFactory.getDefault();
        int delegType = (key.delegationType == Delegation.FULL_DELEGATION ?
                    GSIConstants.DELEGATION_FULL : GSIConstants.DELEGATION_LIMITED);

        KeyPair newKeyPair = CertUtil.generateKeyPair("RSA", src.getStrength());

        X509Certificate[] srcChain = src.getCertificateChain();
        X509Certificate newCert = null;

        try {
            newCert = factory.createProxyCertificate(srcChain[0],
                                        src.getPrivateKey(),
                                        newKeyPair.getPublic(), -1,
                                        key.delegationType == Delegation.FULL_DELEGATION ?
                                                GSIConstants.DELEGATION_FULL
                                                : GSIConstants.DELEGATION_LIMITED,
                                        (X509ExtensionSet) null, null);
        }
        catch (GeneralSecurityException e) {
            throw new InvalidSecurityContextException("Delegation failed", e);
        }

        X509Certificate[] newChain = new X509Certificate[srcChain.length + 1];
        newChain[0] = newCert;
        System.arraycopy(srcChain, 0, newChain, 1, srcChain.length);

        return new GlobusCredential(newKeyPair.getPrivate(), newChain);
    }
}
