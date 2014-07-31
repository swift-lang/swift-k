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
 * Created on Mar 24, 2014
 */
package org.griphyn.vdl.mapping;

import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

public interface FileSystemLister {

    List<AbsFile> listFiles(AbsFile dir, FilenameFilter filter);
    
    FileSystemLister DEFAULT = new Default();
    
    public static class Default implements FileSystemLister {
        @Override
        public List<AbsFile> listFiles(AbsFile dir, FilenameFilter filter) {
            return dir.listFiles(filter);
        }   
    }
    
    public static class FileList implements FileSystemLister {
        private FSTree tree;
        
        public FileList(Collection<AbsFile> l) {
            tree = buildTree(l);
        }

        private FSTree buildTree(Collection<AbsFile> l) {
            FSTree tree = new FSTree(null);
            FSTree root = tree.getDir("absolute");
            root.setParent(root);
            
            for (AbsFile f : l) {
                String path = f.getPath();
                boolean absolute = path.startsWith("/");
                
                FSTree crt = absolute ? tree.getDir("absolute") : tree.getDir("relative");
                
                StringTokenizer st = new StringTokenizer(path, "/", false);
                while (st.hasMoreTokens()) {
                    String token = st.nextToken();
                    
                    if (!st.hasMoreTokens()) {
                        // file
                        crt.addFile(token);
                    }
                    else if (token.equals("..")) {
                        crt = crt.getParent();
                    }
                    else if (token.equals(".")) {
                        // skip
                    }
                    else {
                        crt = crt.getDir(token);
                    }
                }
            }
            return tree;
        }

        @Override
        public List<AbsFile> listFiles(AbsFile dir, FilenameFilter filter) {
            String path = dir.getPath();
            
            boolean absolute = path.startsWith("/");
                
            FSTree crt = absolute ? tree.getDir("absolute") : tree.getDir("relative");
                
            StringTokenizer st = new StringTokenizer(path, "/", false);
            while (st.hasMoreTokens() && crt != null) {
                String token = st.nextToken();
                
                if (token.equals("..")) {
                    crt = crt.getParent();
                }
                else if (token.equals(".")) {
                    // skip
                }
                else {
                    crt = crt.findDir(token);
                }
            }
            if (crt == null) {
                throw new RuntimeException("No such directory: " + path);
            }
            
            List<AbsFile> l = new ArrayList<AbsFile>();
            for (String name : crt.listFiles()) {
                if (filter.accept(null, name)) {
                    l.add(new AbsFile(dir, name));
                }
            }
            
            return l;
        }
    }
    
    public static class FSTree {
        private List<String> files;
        private List<FSTreeDirEntry> dirs;
        private FSTree parent;
        
        public FSTree(FSTree parent) {
            this.parent = parent;
        }

        public List<String> listFiles() {
            if (files == null) {
                return Collections.emptyList();
            }
            else {
                return files;
            }
        }

        public FSTree getParent() {
            if (parent == null) {
                parent = findDir("..");
            }
            return parent;
        }
        
        public void setParent(FSTree parent) {
            this.parent = parent;
        }

        public void addFile(String name) {
            if (files == null) {
                files = new ArrayList<String>();
            }
            files.add(name);
        }

        public FSTree getDir(String name) {
            FSTree d = findDir(name);
            if (d == null) {
                if (dirs == null) {
                    dirs = new LinkedList<FSTreeDirEntry>();
                }
                d = new FSTree(this);
                dirs.add(new FSTreeDirEntry(name, d));
            }
            return d;
        }

        public FSTree findDir(String name) {
            if (dirs == null) {
                return null;
            }
            for (FSTreeDirEntry e : dirs) {
                if (e.name.equals(name)) {
                    return e.files;
                }
            }
            return null;
        }
    }
    
    public static class FSTreeDirEntry {
        public final String name;
        public final FSTree files;
        
        public FSTreeDirEntry(String name, FSTree files) {
            this.name = name;
            this.files = files;
        }
    }
}
