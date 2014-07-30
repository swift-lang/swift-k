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
