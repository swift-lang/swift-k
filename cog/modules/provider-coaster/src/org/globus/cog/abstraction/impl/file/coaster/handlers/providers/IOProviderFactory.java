//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jan 1, 2010
 */
package org.globus.cog.abstraction.impl.file.coaster.handlers.providers;

import java.util.HashMap;
import java.util.Map;

public class IOProviderFactory {
    private static final IOProviderFactory DEFAULT = new IOProviderFactory();
    
    public static IOProviderFactory getDefault() {
        return DEFAULT;
    }
    
    private Map classes, instances;
    
    public IOProviderFactory() {
        classes = new HashMap();
        instances = new HashMap();
        initializeProviders();
    }
    
    private void initializeProviders() {
        classes.put("file", LocalIOProvider.class);
        classes.put("coaster", ProxyIOProvider.class);
        classes.put("copy", LocalCopyIOProvider.class);
    }
    
    public IOProvider instance(String protocol) throws InvalidIOProviderException {
        if (instances.containsKey(protocol)) {
            return (IOProvider) instances.get(protocol);
        }
        else if (classes.containsKey(protocol)) {
            Class cls = (Class) classes.get(protocol);
            try {
                return (IOProvider) cls.newInstance();
            }
            catch (Exception e) {
                throw new InvalidIOProviderException(e);
            }
        }
        else {
            throw new InvalidIOProviderException("Unknown protocol: " + protocol);
        }
    }
}
