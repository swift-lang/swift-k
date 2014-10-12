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

import k.rt.Channel;
import k.rt.Stack;
import k.thr.LWThread;

import org.globus.cog.karajan.analyzer.ArgRef;
import org.globus.cog.karajan.analyzer.ChannelRef;
import org.globus.cog.karajan.analyzer.Param;
import org.globus.cog.karajan.analyzer.Signature;
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
            String dir = new AbsFile(path.getValue(stack)).getDirectory();
            if (dir == null) {
                return ".";
            }
            else {
                return dir;
            }
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
            String dir = new AbsFile(path.getValue(stack)).getDirectory();
            return function(dir);
        }
        
        public static String function(String dir) {
            if (dir != null) {
                return remotePathName(dir);
            }
            else {
                return "";
            }
        }
    }
    
    public static String remoteDirName(AbsFile f) {
        if ("file".equals(f.getProtocol())) {
            return remotePathName(f.getDirectory());
        }
        else if ("direct".equals(f.getProtocol())) {
            return f.getAbsoluteDir();
        }
        else {
            if (f.getHost() == null) {
                return remotePathName(f.getDirectory());
            }
            else {
                return remotePathName(f.getHost() + "/" + f.getDirectory());
            }
        }
    }
    
    public static String remotePathName(AbsFile f) {
        if ("file".equals(f.getProtocol())) {
            return remotePathName(f.getPath());
        }
        else if ("direct".equals(f.getProtocol())) {
            return f.getAbsolutePath();
        }
        else {
            if (f.getHost() == null || f.getHost().equals("localhost")) {
                return remotePathName(f.getPath());
            }
            else {
                return remotePathName(f.getHost() + "/" + f.getPath());
            }
        }
    }
        
    private static final char EOL = '\0';
    /**
     * Replace leading slash if present with "__root__" and replace
     * parent dir references with "__parent__".
     * 
     */

    public static String remotePathName(String dir) {
        if (dir == null) {
            return "";
        }
        if (dir.length() == 0) {
            return dir;
        }
        StringBuilder sb = null;
        
        // parse it by hand to avoid creating too much string object garbage
        boolean modified = false;
        boolean nondot = false;
        int dotcount = 0;
        int start = 0;
        for (int i = 0; i <= dir.length(); i++) {
            boolean skip = false;
            char c;
            if (i == dir.length()) {
                c = EOL;
            }
            else {
                c = dir.charAt(i);
            }
            switch (c) {
                case EOL:
                case '/':
                    if (i == 0) {
                        sb = new StringBuilder();
                        sb.append("__root__/");
                        skip = true;
                        modified = true;
                    }
                    else if (nondot) {
                        // do nothing
                    }
                    else {
                        // only dots. If zero or one, remove completely ("//", "/./")
                        switch (dotcount) {
                            case 0:
                            case 1:
                            case 2:
                                modified = true;
                                skip = true;
                                if (sb == null) {
                                    sb = new StringBuilder();
                                    append(sb, dir, 0, i - dotcount);
                                }
                                if (dotcount == 2) {
                                    sb.append("__parent__");
                                    if (c != EOL) {
                                        sb.append('/');
                                    }
                                }
                            default:
                                // pass along
                        }
                    }
                    nondot = false;
                    dotcount = 0;
                    break;
                case '.':
                    if (nondot) {
                        // a path element containing a dot among other things
                        // so leave it alone
                    }
                    else {
                        dotcount++;
                        skip = true;
                    }
                    break;
                default:
                    nondot = true;
                    if (dotcount > 0) {
                        if (modified) {
                            for (int j = 0; j < dotcount; j++) {
                                sb.append('.');
                            }
                        }
                        dotcount = 0;
                    }
            }
            if (modified) {
                if (sb == null) {
                    sb = new StringBuilder();
                    append(sb, dir, 0, i - dotcount);
                }
                if (!skip && c != EOL) {
                    sb.append(c);
                }
            }
        }
        if (modified) {
            return sb.toString();
        }
        else {
            return dir;
        }
    }
    
    public static void testMakeRelative(String str, String expected, boolean samestr) {
        String result = remotePathName(str);
        if (!result.equals(expected)) {
            throw new RuntimeException("input: '" + str + "', expected: '" + expected + "', result: '" + result + "'");
        }
        if (samestr && (str != result)) {
            throw new RuntimeException("Expected same string for '" + str + "'");
        }
        System.out.println("OK '" + str + "' -> '" + result + "'");
    }
    
    public static void main(String[] args) {
        testMakeRelative("onething", "onething", true);
        testMakeRelative("two/things", "two/things", true);
        testMakeRelative("/absolute/path", "__root__/absolute/path", false);
        testMakeRelative("../in/the/beginning", "__parent__/in/the/beginning", false);
        testMakeRelative("in/the/../middle", "in/the/__parent__/middle", false);
        // nonsensical, but meh
        testMakeRelative("/../in/the/beginning/absolute", "__root__/__parent__/in/the/beginning/absolute", false);
        testMakeRelative("/in/the/../middle/absolute", "__root__/in/the/__parent__/middle/absolute", false);
        testMakeRelative("../in/../many/../places", "__parent__/in/__parent__/many/__parent__/places", false);
        testMakeRelative("/../in/../many/../places/../absolute", "__root__/__parent__/in/__parent__/many/__parent__/places/__parent__/absolute", false);
        testMakeRelative("a/single/./dot", "a/single/dot", false);
        testMakeRelative("double//slash", "double/slash", false);
        testMakeRelative("./single/dot/at/start", "single/dot/at/start", false);
        testMakeRelative("/./single/dot/at/start/absolute", "__root__/single/dot/at/start/absolute", false);
        testMakeRelative("multiple/single/././././dots", "multiple/single/dots", false);
        // technically this isn't valid, but that's not our problem
        testMakeRelative("three/.../dots", "three/.../dots", true);
        testMakeRelative("two/..valid/dots", "two/..valid/dots", true);
        testMakeRelative("more/val..id/dots", "more/val..id/dots", true);
        testMakeRelative("/everything/./in/../../one//single../../path", "__root__/everything/in/__parent__/__parent__/one/single../__parent__/path", false);
        testMakeRelative("..", "__parent__", false);
        testMakeRelative("ends/in/..", "ends/in/__parent__", false);
    }

    private static void append(StringBuilder sb, String str, int begin, int end) {
        for (int i = begin; i < end; i++) {
            sb.append(str.charAt(i));
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
            return function(dir, path, windows);
        }
        
        public static String function(String dir, String path, boolean windows) {
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
                l.add(remotePathName(new AbsFile(f).getPath()));
            }
            return l.toArray(new String[0]);
        }
    }
    
    /**
     * (provider, dhost, rdir, bname, ldir) = splitFileURL(file, dir)
     * 
     * Implements this functionality that used to be in swift-int.k:
     * 
     * provider := provider(file)
     * dhost := hostname(file)
     * rdir := dircat(dir, reldirname(file))
     * bname := basename(file)
     * ldir := swift:dirname(file)
     *
     */
    public static class SplitFileURL extends SwiftFunction {
       private ArgRef<AbsFile> file;
       private ArgRef<String> dir;
       private ArgRef<String> destdir;
       private ChannelRef<Object> cr_vargs;
    
        @Override
        protected Signature getSignature() {
            return new Signature(params("file", "dir", optional("destdir", null)), returns(channel("...", DYNAMIC)));
        }
        
        @Override
        public void runBody(LWThread thr) {
            super.runBody(thr);
        }

        @Override
        public Object function(Stack stack) {
            AbsFile f = this.file.getValue(stack);
            String dir = this.dir.getValue(stack);
            String destdir = this.destdir.getValue(stack);
            Channel<Object> ret = cr_vargs.get(stack);
            
            ret.add(f.getProtocol("file"));
            ret.add(f.getHost("localhost"));
            if (destdir == null) {
                ret.add(DirCat.function(dir, remoteDirName(f), false));
            }
            else {
                ret.add(DirCat.function(dir, destdir, false));
            }
            ret.add(f.getName());
            String fdir = f.getDirectory();
            ret.add(fdir == null ? "" : fdir);
            
            return null;
        }
    }

}
