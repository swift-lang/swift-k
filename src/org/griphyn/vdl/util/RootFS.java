/*
 * Copyright 2012 University of Chicago
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * Created on Mar 23, 2016
 */
package org.griphyn.vdl.util;

import org.globus.cog.abstraction.interfaces.Service;
import org.griphyn.vdl.mapping.AbsFile;

public class RootFS {
    private final Service service;
    private final AbsFile path;
    private final boolean _default;
    
    public RootFS(Service service, AbsFile path) {
        this.service = service;
        if (path == null && service == null) {
            _default = true;
        }
        else {
            _default = false;
        }
        if (service != null) {
            if (path == null) {
                this.path = new AbsFile(service.getProvider(), null, -1, null, null);
            }
            else if (path.getProtocol() == null) {
                this.path = new AbsFile(service.getProvider(), path.getHost(), path.getPort(), path.getDirectory(), path.getName());
            }
            else {
                this.path = path;
            }
        }
        else {
            this.path = path;
        }
    }
    
    public Service getService() {
        return service;
    }
        
    public AbsFile getPath() {
        return path;
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((path == null) ? 0 : path.hashCode());
        result = prime * result + ((service == null) ? 0 : service.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        RootFS other = (RootFS) obj;
        if (path == null) {
            if (other.path != null)
                return false;
        }
        else if (!path.equals(other.path))
            return false;
        if (service == null) {
            if (other.service != null)
                return false;
        }
        else if (!service.equals(other.service))
            return false;
        return true;
    }

    public String getProtocol() {
        return path.getProtocol();
    }

    public String getHost() {
        return path.getHost();
    }

    public boolean isDefault() {
        return _default;
    }

    public int getPort() {
        return path.getPort();
    }
    
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("RootFS[service: ");
        sb.append(service);
        sb.append(", path: ");
        sb.append(path);
        sb.append("]");
        return sb.toString();
    }
}
