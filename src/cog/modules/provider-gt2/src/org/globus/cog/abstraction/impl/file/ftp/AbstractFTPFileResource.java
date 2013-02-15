//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jan 27, 2007
 */
package org.globus.cog.abstraction.impl.file.ftp;

import java.io.IOException;

import org.globus.cog.abstraction.impl.file.AbstractFileResource;
import org.globus.cog.abstraction.impl.file.FileResourceException;
import org.globus.cog.abstraction.impl.file.IrrecoverableResourceException;
import org.globus.cog.abstraction.interfaces.SecurityContext;
import org.globus.cog.abstraction.interfaces.ServiceContact;
import org.globus.ftp.exception.DataChannelException;
import org.globus.ftp.exception.FTPException;
import org.globus.ftp.exception.ServerException;

public abstract class AbstractFTPFileResource extends AbstractFileResource {

    protected AbstractFTPFileResource(String name, String protocol, ServiceContact serviceContact, SecurityContext securityContext) {
        super(name, protocol, serviceContact, securityContext);        
    }

    protected FileResourceException translateException(Exception e) {
        return translateException(null, e);
    }

    protected FileResourceException translateException(String message, Exception e) {
        if (e instanceof IOException) {
            return new IrrecoverableResourceException(message, e);
        }
        else if (e instanceof FTPException) {
            if (e instanceof ServerException) {
                ServerException se = (ServerException) e;
                if (se.getCode() == ServerException.REPLY_TIMEOUT) {
                    return new IrrecoverableResourceException(message, e);
                }
                else if (se.getCode() == ServerException.PREVIOUS_TRANSFER_ACTIVE) {
                	return new IrrecoverableResourceException(message, e);
                }
                else {
                    return new FileResourceException(message, e);
                }
            }
            else if (e instanceof DataChannelException) {
                return new IrrecoverableResourceException(message, e);
            }
            else {
                return new FileResourceException(message, e);
            }
        }
        else {
            return new IrrecoverableResourceException(message, e);
        }
    }

}
