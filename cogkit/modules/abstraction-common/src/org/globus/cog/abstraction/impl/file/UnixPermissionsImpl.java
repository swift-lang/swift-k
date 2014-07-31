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
 * Created on Jun 10, 2005
 */
package org.globus.cog.abstraction.impl.file;

import org.globus.cog.abstraction.interfaces.Permissions;

public class UnixPermissionsImpl implements Permissions {
    private int value;

    public UnixPermissionsImpl(int value) {
        this.value = value;
    }

    public UnixPermissionsImpl(char c) {
        this.value = c - '0';
    }

    public void setRead(boolean canRead) {
        if (canRead) {
            value |= 4;
        }
        else {
            value &= 3;
        }
    }

    public boolean getRead() {
        return (value & 4) != 0;
    }

    public void setWrite(boolean canWrite) {
        if (canWrite) {
            value |= 2;
        }
        else {
            value &= 5;
        }
    }

    public boolean getWrite() {
        return (value & 2) != 0;
    }

    public void setExecute(boolean canExecute) {
        if (canExecute) {
            value |= 1;
        }
        else {
            value &= 6;
        }
    }

    public boolean getExecute() {
        return (value & 1) != 0;
    }

    public int toDigit() {
        return value;
    }   
}
