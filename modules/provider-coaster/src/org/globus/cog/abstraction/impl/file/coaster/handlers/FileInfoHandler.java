//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Sep 26, 2008
 */
package org.globus.cog.abstraction.impl.file.coaster.handlers;

import java.io.File;
import java.util.Date;

import org.globus.cog.abstraction.impl.file.PermissionsImpl;
import org.globus.cog.abstraction.interfaces.GridFile;
import org.globus.cog.abstraction.interfaces.Permissions;
import org.globus.cog.coaster.ProtocolException;

public class FileInfoHandler extends CoasterFileRequestHandler {

    public void requestComplete() throws ProtocolException {
    	File f = normalize(getInDataAsString(0));
        
        addOutData(f.getAbsolutePath());
        addOutData(f.isDirectory() ? GridFile.DIRECTORY : GridFile.FILE);
        addOutData(new Date(f.lastModified()).toString());
        addOutData(f.length());
        
        Permissions p = new PermissionsImpl(f.canRead(), f.canWrite(), false);
        addOutData(p.toDigit());
        addOutData(p.toDigit());
        addOutData(p.toDigit());
        sendReply();
    }
}
