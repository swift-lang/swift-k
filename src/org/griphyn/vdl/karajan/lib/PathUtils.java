/*
 * Copyright 2012 University of Chicago
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.griphyn.vdl.karajan.lib;

import java.util.ArrayList;
import java.util.List;

import k.rt.Stack;

import org.globus.cog.karajan.analyzer.ArgRef;
import org.globus.cog.karajan.analyzer.Param;
import org.globus.cog.karajan.compiled.nodes.functions.AbstractSingleValuedFunction;
import org.globus.swift.catalog.types.Os;
import org.griphyn.vdl.mapping.AbsFile;

public class PathUtils {

    public static class DirName extends AbstractSingleValuedFunction {
        private ArgRef<String> path;

        @Override
        protected Param[] getParams() {
            return params("path");
        }

        @Override
        public Object function(Stack stack) {
            return new AbsFile(path.getValue(stack)).getDir();
        }
    }
    
    public static class RelDirName extends AbstractSingleValuedFunction {
        private ArgRef<String> path;

        @Override
        protected Param[] getParams() {
            return params("path");
        }

        @Override
        public Object function(Stack stack) {
            String dir = new AbsFile(path.getValue(stack)).getDir();
            if (dir.startsWith("/")) {
                return dir.substring(1);
            }
            else {
                return dir;
            }
        }
    }

    public static class BaseName extends AbstractSingleValuedFunction {
        private ArgRef<String> path;

        @Override
        protected Param[] getParams() {
            return params("path");
        }

        @Override
        public Object function(Stack stack) {
            return new AbsFile(path.getValue(stack)).getName();
        }
    }
    
    public static class Provider extends AbstractSingleValuedFunction {
        private ArgRef<String> path;

        @Override
        protected Param[] getParams() {
            return params("path");
        }

        @Override
        public Object function(Stack stack) {
            return new AbsFile(path.getValue(stack)).getProtocol();
        }
    }

    public static class HostName extends AbstractSingleValuedFunction {
        private ArgRef<String> path;

        @Override
        protected Param[] getParams() {
            return params("path");
        }

        @Override
        public Object function(Stack stack) {
            return new AbsFile(path.getValue(stack)).getHost();
        }
    }
    
    public static class DirCat extends AbstractSingleValuedFunction {
        private ArgRef<String> dir;
        private ArgRef<String> path;
        private ArgRef<Os> os;

        @Override
        protected Param[] getParams() {
            return params("dir", "path", optional("os", Os.LINUX));
        }

        @Override
        public Object function(Stack stack) {
            String dir = this.dir.getValue(stack);
            String path = this.path.getValue(stack);
            boolean windows = this.os.getValue(stack).equals(Os.WINDOWS);
            if (dir.equals("")) {
                return windowsify(path, windows);
            }
            else if (dir.endsWith("/")) {
                return windowsify(dir + path, windows);
            }
            else {
                return windowsify(dir + '/' + path, windows);
            }
        }
    }
    
    private static String windowsify(String path, boolean windows) {
		if (windows) {
			return path.replace('/', '\\');
		}
		else {
			return path;
		}
	}
    
    public static class PathNames extends AbstractSingleValuedFunction {
        private ArgRef<List<String>> files;

        @Override
        protected Param[] getParams() {
            return params("files");
        }

        @Override
        public String[] function(Stack stack) {
            List<String> l = new ArrayList<String>();
            for (String f : files.getValue(stack)) {
                l.add(new AbsFile(f).getPath());
            }
            return l.toArray(new String[0]);
        }
    }
}
