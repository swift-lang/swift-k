package org.globus.transfer.reliable.client.credential;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.globus.common.CoGProperties;
import org.globus.gsi.CertUtil;
import org.globus.gsi.GlobusCredential;
import org.globus.util.Util;


public class CredManager {
	private static Log logger = LogFactory.getLog(CredManager.class);
	
	public static ProxyInfo getProxyInfo() throws Exception {
		GlobusCredential proxy = null;
        String file = null;

        try {            
            file = CoGProperties.getDefault().getProxyFile();            
            proxy = new GlobusCredential(file);
        } catch (Exception e) {
            logger.debug("Unable to load the user proxy : "
                    + e.getMessage());
            throw e;       
        }
        
        ProxyInfo ret = new ProxyInfo(CertUtil.toGlobusID(proxy.getSubject()),
        		proxy.getStrength(),Util.formatTimeSec(proxy.getTimeLeft()));
        return ret;
	}
}
