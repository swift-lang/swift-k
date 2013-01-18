//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Mar 12, 2008
 */
package org.globus.cog.abstraction.coaster.service.job.manager;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import org.globus.cog.abstraction.coaster.service.CoasterService;

public class IDGenerator {
    private SecureRandom sr;
    
    public IDGenerator() {
        try {
            sr = SecureRandom.getInstance("SHA1PRNG");
        }
        catch (NoSuchAlgorithmException e) {
        	CoasterService.error(30, "Cannot get SHA1PRNG instance", e);
        }
    }

    public synchronized int nextInt() {
        return sr.nextInt();
    }
}
