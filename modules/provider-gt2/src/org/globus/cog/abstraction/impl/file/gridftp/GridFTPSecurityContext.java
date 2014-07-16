//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Nov 23, 2005
 */
package org.globus.cog.abstraction.impl.file.gridftp;

import org.globus.cog.abstraction.impl.execution.gt2.GlobusSecurityContextImpl;
import org.globus.cog.abstraction.interfaces.SecurityContext;

/**
 * A specific security context for GridFTP which allows
 */
public class GridFTPSecurityContext extends GlobusSecurityContextImpl implements GridFTPConstants {
	public void setDataChannelAuthentication(DataChannelAuthenticationType dcau) {
		setAttribute(ATTR_DATA_CHANNEL_AUTHENTICATION, dcau);
	}

	public void setDataChannelAuthentication(boolean dcau) {
		if (dcau) {
			setAttribute(ATTR_DATA_CHANNEL_AUTHENTICATION, DataChannelAuthenticationType.SELF);
		}
		else {
			setAttribute(ATTR_DATA_CHANNEL_AUTHENTICATION, DataChannelAuthenticationType.NONE);
		}
	}

	public DataChannelAuthenticationType getDataChannelAuthentication() {
		return GridFTPSecurityContext.getDataChannelAuthentication(this);
	}

	public static DataChannelAuthenticationType getDataChannelAuthentication(SecurityContext sc) {
		Object dcau = sc.getAttribute(ATTR_DATA_CHANNEL_AUTHENTICATION);
		if (dcau instanceof DataChannelAuthenticationType) {
			return ((DataChannelAuthenticationType) dcau);
		}
		if (dcau instanceof Boolean) {
			return ((Boolean) dcau).booleanValue() ? DataChannelAuthenticationType.SELF
					: DataChannelAuthenticationType.NONE;
		}
		return null;
	}

	public void setDataChannelProtection(DataChannelProtectionType prot) {
		setAttribute(ATTR_DATA_CHANNEL_PROTECTION, prot);
	}

	public DataChannelProtectionType getDataChannelProtection() {
		return GridFTPSecurityContext.getDataChannelProtection(this);
	}

	public static DataChannelProtectionType getDataChannelProtection(SecurityContext sc) {
		Object prot = sc.getAttribute(ATTR_DATA_CHANNEL_PROTECTION);
		if (prot instanceof DataChannelProtectionType) {
			return ((DataChannelProtectionType) prot);
		}
		return null;
	}
}
