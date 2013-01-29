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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.globus.cog.abstraction.impl.file.GridFileImpl;
import org.globus.cog.abstraction.impl.file.PermissionsImpl;
import org.globus.cog.abstraction.interfaces.GridFile;
import org.globus.cog.coaster.ProtocolException;
import org.globus.cog.coaster.commands.Command;

public class ListCommand extends Command {
    public static final String NAME = "LIST";

    public ListCommand(String name) {
        super(NAME);
        addOutData(name);
    }
    
    public Collection<GridFile> getResult() throws ProtocolException {
        List<GridFile> l = new ArrayList<GridFile>();
        int i = 0;
        int sz = getInDataSize();
        if (sz % 7 != 0) {
            throw new ProtocolException("Reply size mismatch");
        }

        while (i < sz) {
            GridFile f = new GridFileImpl();
            f.setAbsolutePathName(getInDataAsString(i++));
            f.setName(new File(f.getAbsolutePathName()).getName());
            f.setLastModified(getInDataAsString(i++));
            f.setFileType((byte) getInDataAsInt(i++));
            f.setSize(getInDataAsLong(i++));
            f.setUserPermissions(new PermissionsImpl(getInDataAsInt(i++)));
            f.setGroupPermissions(new PermissionsImpl(getInDataAsInt(i++)));
            f.setWorldPermissions(new PermissionsImpl(getInDataAsInt(i++)));
            l.add(f);
        }
        return l;
    }
}
