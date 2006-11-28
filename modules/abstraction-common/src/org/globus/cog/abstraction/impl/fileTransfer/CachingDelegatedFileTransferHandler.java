// ----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

package org.globus.cog.abstraction.impl.fileTransfer;

import java.io.IOException;

import org.globus.cog.abstraction.impl.common.ProviderMethodException;
import org.globus.cog.abstraction.impl.common.task.InvalidProviderException;
import org.globus.cog.abstraction.impl.common.task.InvalidSecurityContextException;
import org.globus.cog.abstraction.impl.file.FileResourceCache;
import org.globus.cog.abstraction.impl.file.FileResourceException;
import org.globus.cog.abstraction.interfaces.FileResource;
import org.globus.cog.abstraction.interfaces.Service;

public class CachingDelegatedFileTransferHandler extends DelegatedFileTransferHandler {

	protected FileResource startResource(Service service) throws InvalidProviderException,
			ProviderMethodException, InvalidSecurityContextException, FileResourceException,
			IOException {
		return FileResourceCache.getDefault().getResource(service);
	}

	protected void stopResources() {
		if (getSourceResource() != null) {
			FileResourceCache.getDefault().releaseResource(getSourceResource());
			setSourceResource(null);
		}
		if (getDestinationResource() != null) {
			FileResourceCache.getDefault().releaseResource(getDestinationResource());
			setDestinationResource(null);
		}
	}
}