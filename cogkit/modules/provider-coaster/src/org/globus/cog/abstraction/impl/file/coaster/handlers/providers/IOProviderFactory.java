/*
 * Swift Parallel Scripting Language (http://swift-lang.org)
 * Code from Java CoG Kit Project (see notice below) with modifications.
 *
 * Copyright 2005-2014 University of Chicago
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
