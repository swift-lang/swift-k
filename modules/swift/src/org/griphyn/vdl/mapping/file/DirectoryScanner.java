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
 * Created on Mar 17, 2013
 */
package org.griphyn.vdl.mapping.file;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import org.griphyn.vdl.mapping.AbsFile;

public class DirectoryScanner implements Iterator<AbsFile> {
    private Stack<Iterator<AbsFile>> paths;
    
    public DirectoryScanner(AbsFile root) {
        paths = new Stack<Iterator<AbsFile>>();
        paths.push(Collections.singletonList(root).iterator());
    }

    @Override
    public boolean hasNext() {
        while (!paths.isEmpty() && !paths.peek().hasNext()) {
            paths.pop();
        }
        if (paths.isEmpty()) {
            return false;
        }
        else {
            return true;
        }
    }

    @Override
    public AbsFile next() {
        AbsFile nextdir = paths.peek().next();
        List<AbsFile> subdirs = nextdir.listDirectories(null);
        if (subdirs != null) {
            paths.push(subdirs.iterator());
        }
        return nextdir;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }
    
    public static void main(String[] args) {
        AbsFile f = new AbsFile(".");
        DirectoryScanner s = new DirectoryScanner(f);
        while (s.hasNext()) {
            System.out.println(s.next());
        }
    }
}
