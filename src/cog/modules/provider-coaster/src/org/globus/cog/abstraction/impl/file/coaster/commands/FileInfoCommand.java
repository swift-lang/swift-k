//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Sep 24, 2008
 */
package org.globus.cog.abstraction.impl.file.coaster.commands;

import java.io.File;

import org.globus.cog.abstraction.impl.file.GridFileImpl;
import org.globus.cog.abstraction.impl.file.PermissionsImpl;
import org.globus.cog.abstraction.interfaces.GridFile;
import org.globus.cog.coaster.commands.Command;

public class FileInfoCommand extends Command {
    public static final String NAME = "FILEINFO"; 

    public FileInfoCommand(String name) {
        super(NAME);
        addOutData(name);
    }

    public GridFile getResult() {
        GridFile f = new GridFileImpl();
        f.setAbsolutePathName(getInDataAsString(0));
        f.setFileType((byte) getInDataAsInt(1));
        f.setLastModified(getInDataAsString(2));
        f.setName(new File(f.getAbsolutePathName()).getName());
        f.setSize(getInDataAsLong(3));
        f.setUserPermissions(PermissionsImpl.instance(getInDataAsInt(4)));
        f.setGroupPermissions(PermissionsImpl.instance(getInDataAsInt(5)));
        f.setWorldPermissions(PermissionsImpl.instance(getInDataAsInt(6)));
        return f;
    }
}
