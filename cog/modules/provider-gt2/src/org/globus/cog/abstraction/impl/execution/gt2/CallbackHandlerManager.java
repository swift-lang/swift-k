// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.abstraction.impl.execution.gt2;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.globus.common.CoGProperties;
import org.globus.gram.Gram;
import org.ietf.jgss.GSSCredential;

public class CallbackHandlerManager {
    public static final Logger logger = Logger.getLogger(CallbackHandlerManager.class);
    
    private static Map count = new HashMap();
    private static String cogIP = CoGProperties.getDefault().getIPAddress();

    public static synchronized void increaseUsageCount(GSSCredential cred) {
        Integer i = (Integer) count.get(cred);
        if (i == null) {
            i = new Integer(1);
        } else {
            i = new Integer(i.intValue() + 1);
        }
        count.put(cred, i);
    }

    public static synchronized void decreaseUsageCount(GSSCredential cred) {
        Integer i = (Integer) count.get(cred);
        if (i == null) {
            logger.warn("No registered callback handler for "
                    + cred );
        } else if (i.intValue() == 1) {
            count.remove(cred);
            Gram.deactivateCallbackHandler(cred);
        } else {
            count.put(cred, new Integer(i.intValue() - 1));
        }
    }
}