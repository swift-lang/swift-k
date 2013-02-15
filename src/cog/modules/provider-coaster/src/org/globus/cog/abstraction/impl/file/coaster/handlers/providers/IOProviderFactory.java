//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jan 1, 2010
 */
package org.globus.cog.abstraction.impl.file.coaster.handlers.providers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.globus.cog.abstraction.impl.common.AbstractionProperties;

public class IOProviderFactory {
    private static final IOProviderFactory DEFAULT = new IOProviderFactory();

    public static IOProviderFactory getDefault() {
        return DEFAULT;
    }

    private Map<String, Class<IOProvider>> classes;
    private Map<String, IOProvider> instances;

    public IOProviderFactory() {
        classes = new HashMap<String, Class<IOProvider>>();
        instances = new HashMap<String, IOProvider>();
        initializeProviders();
    }

    private void initializeProviders() {
        instances.put("file", new LocalIOProvider());
        instances.put("proxy", new ProxyIOProvider());
        instances.put("copy", new LocalCopyIOProvider());
        IOProvider resource = new CoGResourceIOProvider();
        List<String> providers =
                AbstractionProperties.getProviders(AbstractionProperties.TYPE_FILE_RESOURCE);
        List<String> all = new ArrayList<String>();
        all.addAll(providers);
        for (String provider : providers) {
            all.addAll(AbstractionProperties.getAliases(provider));
        }
        for (String name: all) {
            if (!instances.containsKey(name)) {
                instances.put(name, resource);
            }
        }
    }

    public IOProvider instance(String protocol) throws InvalidIOProviderException {
        if (instances.containsKey(protocol)) {
            return instances.get(protocol);
        }
        else if (classes.containsKey(protocol)) {
            Class<IOProvider> cls = classes.get(protocol);
            try {
                return cls.newInstance();
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
