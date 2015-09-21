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
 * Created on Sep 24, 2008
 */
package org.globus.cog.abstraction.impl.file.coaster.commands;

import java.io.File;
import java.util.Date;

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
        f.setLastModified(new Date(Long.parseLong(getInDataAsString(2))));
        f.setName(new File(f.getAbsolutePathName()).getName());
        f.setSize(getInDataAsLong(3));
        f.setUserPermissions(PermissionsImpl.instance(getInDataAsInt(4)));
        f.setGroupPermissions(PermissionsImpl.instance(getInDataAsInt(5)));
        f.setWorldPermissions(PermissionsImpl.instance(getInDataAsInt(6)));
        return f;
    }
}
