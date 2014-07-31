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

// ----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

package org.globus.cog.abstraction.impl.fileTransfer;

import java.io.File;
import java.io.IOException;

import org.globus.cog.abstraction.impl.common.ProviderMethodException;
import org.globus.cog.abstraction.impl.common.task.InvalidProviderException;
import org.globus.cog.abstraction.impl.common.task.InvalidSecurityContextException;
import org.globus.cog.abstraction.impl.file.FileResourceCache;
import org.globus.cog.abstraction.impl.file.FileResourceException;
import org.globus.cog.abstraction.impl.file.IrrecoverableResourceException;
import org.globus.cog.abstraction.interfaces.FileResource;
import org.globus.cog.abstraction.interfaces.Service;

public class CachingDelegatedFileTransferHandler extends
        DelegatedFileTransferHandler {

    protected synchronized FileResource startResource(Service service)
            throws InvalidProviderException, ProviderMethodException,
            InvalidSecurityContextException, FileResourceException, IOException {
        return FileResourceCache.getDefault().getResource(service);
    }

    protected synchronized void stopResources() {
        if (getSourceResource() != null) {
            FileResourceCache.getDefault().releaseResource(getSourceResource());
            setSourceResource(null);
        }
        if (getDestinationResource() != null) {
            FileResourceCache.getDefault().releaseResource(
                    getDestinationResource());
            setDestinationResource(null);
        }
    }

    protected void doDestination(File localSource, Service service)
    throws FileResourceException, InvalidProviderException, 
    ProviderMethodException, InvalidSecurityContextException {
        try {
            super.doDestination(localSource, service);
        }
        catch (IrrecoverableResourceException e) {
            FileResourceCache.getDefault().invalidateResource(
                    getDestinationResource());
            throw e;
        }
    }

    protected File doSource(Service service, File localDestination)
            throws FileResourceException, IOException,
            InvalidProviderException, ProviderMethodException {
        try {
            return super.doSource(service, localDestination);
        }
        catch (IrrecoverableResourceException e) {
            FileResourceCache.getDefault().invalidateResource(
                    getSourceResource());
            throw e;
        }
    }
}