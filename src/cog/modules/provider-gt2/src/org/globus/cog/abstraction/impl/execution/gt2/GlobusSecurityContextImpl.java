// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.abstraction.impl.execution.gt2;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.common.task.SecurityContextImpl;
import org.globus.cog.abstraction.interfaces.Delegation;
import org.globus.cog.abstraction.interfaces.ServiceContact;
import org.globus.gsi.GlobusCredential;
import org.globus.gsi.gssapi.GlobusGSSCredentialImpl;
import org.globus.gsi.gssapi.auth.Authorization;
import org.globus.gsi.gssapi.auth.HostAuthorization;
import org.gridforum.jgss.ExtendedGSSManager;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.GSSManager;

public class GlobusSecurityContextImpl extends SecurityContextImpl implements Delegation {
    public static final Logger logger = Logger.getLogger(GlobusSecurityContextImpl.class);
    
    public static final int XML_ENCRYPTION = 1;
    public static final int XML_SIGNATURE = 2;
    
    public static final String PROXY_HOST_PATH_MAPPING_FILE = System.getProperty("user.home") + 
        File.separator + ".globus" + File.separator + "proxy.mapping"; 
    
    public static final int DEFAULT_CREDENTIAL_REFRESH_INTERVAL = 30000;
    private static Map<String, GSSCredential> cachedCredentials = new HashMap<String, GSSCredential>();
    private static Map<String, Long> credentialTimes = new HashMap<String, Long>();

    private static Properties proxyPaths;

    public GlobusSecurityContextImpl() {
    }
    
    public GlobusSecurityContextImpl(String proxyPath) {
        if (proxyPath == null) {
            setCredentials(getDefaultCredentials());
        }
        else {
            setCredentials(loadProxyFromFile(proxyPath));
        }
    }

    public void setAuthorization(Authorization authorization) {
        setAttribute("authorization", authorization);
    }
    
    public Authorization getAuthorization() {
        Authorization authorization = (Authorization) getAttribute("authorization");
        if (authorization == null) {
            authorization = HostAuthorization.getInstance();
        }
        return authorization;
    }

    public void setXMLSec(int xml_security) {
        setAttribute("xml_security", new Integer(xml_security));
    }

    public int getXMLSec() {
        Integer value = (Integer) getAttribute("xml_security");
        if (value == null) {
            return GlobusSecurityContextImpl.XML_SIGNATURE;
        }
        return value.intValue();
    }

    public void setDelegation(int delegation) {
        setAttribute("delegation", new Integer(delegation));
    }

    public int getDelegation() {
        Integer value = (Integer) getAttribute("delegation");
        if (value == null) {
            return GlobusSecurityContextImpl.PARTIAL_DELEGATION;
        }
        return value.intValue();
    }
    
    public GSSCredential getDefaultCredentials() {
        return _getDefaultCredential(getServiceContact());
    }
    
    @Override
    public Object getCredentials() {
        Object credentials = super.getCredentials();
        if (credentials == null) {
            return getDefaultCredentials();
        }
        else {
            return credentials;
        }
    }

    public static GSSCredential _getDefaultCredential(ServiceContact serviceContact) {
        String host = null;
        if (serviceContact != null) {
            // null is OK
            host = serviceContact.getHost();
        }
        loadProxyPaths();
        synchronized (cachedCredentials) {
            GSSCredential cachedCredential = cachedCredentials.get(host);
            Long credentialTime = credentialTimes.get(host);
            long now = System.currentTimeMillis();
            if (cachedCredential == null || (now - credentialTime) > DEFAULT_CREDENTIAL_REFRESH_INTERVAL) {
                if (cachedCredential == null) {
                    if (logger.isInfoEnabled()) {
                        logger.info("No cached credentials for " + host + ".");
                    }
                }
                else {
                    if (logger.isInfoEnabled()) {
                        logger.info("Credentials for " + host + " need refreshing.");
                    }
                }
                credentialTimes.put(host, now);
                cachedCredential = loadCredential(host);
                cachedCredentials.put(host, cachedCredential);
            }
            return cachedCredential;
        }
    }

    private static GSSCredential loadCredential(String host) {
        String proxyPath = null;
                
        if (host != null) {
            proxyPath = (String) proxyPaths.get(host);
        }
                
        if (proxyPath == null) {
            if (logger.isInfoEnabled()) {
                logger.info("No proxy mapping found for " + host + ". Loading default.");
            }
            return loadDefaultProxy();
        }
        else {
            if (logger.isInfoEnabled()) {
                logger.info("Proxy mapping found for " + host + ": " + proxyPath);
            }
            return loadProxyFromFile(proxyPath);
        }
    }

    private static GSSCredential loadProxyFromFile(String proxyPath) {
        try {
            GlobusCredential cred = new GlobusCredential(proxyPath);
            return new GlobusGSSCredentialImpl(cred, GSSCredential.INITIATE_AND_ACCEPT);
        }
        catch (Exception e) {
            throw new SecurityException(e);
        }
    }

    private static GSSCredential loadDefaultProxy() {
        GSSManager manager = ExtendedGSSManager.getInstance();
        try {
            return manager.createCredential(GSSCredential.INITIATE_AND_ACCEPT);
        }
        catch (GSSException e) {
            throw new SecurityException(e);
        }
    }

    private static synchronized void loadProxyPaths() {
        if (proxyPaths == null) {
            proxyPaths = new Properties();
            try {
                proxyPaths.load(new FileInputStream(PROXY_HOST_PATH_MAPPING_FILE));
            }
            catch (FileNotFoundException e) {
                // no mapping
            }
            catch (IOException e) {
                logger.warn("Could not load host-proxy mapping file", e);
            }
        }
    }
}