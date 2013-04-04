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
import java.util.Iterator;
import java.util.List;

import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.util.TypeUtil;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.globus.cog.karajan.workflow.nodes.functions.FunctionsCollection;
import org.globus.swift.catalog.types.Os;
import org.griphyn.vdl.mapping.AbsFile;

public class PathUtils extends FunctionsCollection {

	public static final Arg PATH = new Arg.Positional("path");

	static {
		setArguments("vdl_dirname", new Arg[] { PATH });
	}

	public String vdl_dirname(VariableStack stack) throws ExecutionException {
		String path = TypeUtil.toString(PATH.getValue(stack));
		return new AbsFile(path).getDir();
	}
    
    static {
        setArguments("vdl_reldirname", new Arg[] { PATH });
    }

    public String vdl_reldirname(VariableStack stack) throws ExecutionException {
        String path = TypeUtil.toString(PATH.getValue(stack));
        String dir = new AbsFile(path).getDir();
        return remotePathName(dir);
    }
    
    private static final char EOL = '\0';
    /**
     * Replace leading slash if present with "__root__" and replace
     * parent dir references with "__parent__"
     */
    public static String remotePathName(String dir) {
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

    static {
        setArguments("vdl_basename", new Arg[] { PATH });
    }

    public String vdl_basename(VariableStack stack) throws ExecutionException {
    	String path = TypeUtil.toString(PATH.getValue(stack));
        return new AbsFile(path).getName();
    }
    
    static {
        setArguments("vdl_provider", new Arg[] { PATH });
    }

    public String vdl_provider(VariableStack stack) throws ExecutionException {
    	String path = TypeUtil.toString(PATH.getValue(stack));
        return new AbsFile(path).getProtocol();
    }
    
    static {
        setArguments("vdl_hostname", new Arg[] { PATH });
    }

    public String vdl_hostname(VariableStack stack) throws ExecutionException {
    	String path = TypeUtil.toString(PATH.getValue(stack));
        return new AbsFile(path).getHost();
    }
    
    public static final Arg DIR = new Arg.Positional("dir");
    public static final Arg OS = new Arg.Optional("os");
    
    static {
        setArguments("vdl_dircat", new Arg[] { DIR, PATH, OS });
    }

    public String vdl_dircat(VariableStack stack) throws ExecutionException {
        String dir = TypeUtil.toString(DIR.getValue(stack));
    	String path = TypeUtil.toString(PATH.getValue(stack));
    	boolean windows = false;
    	if (OS.isPresent(stack)) {
    		Os os = (Os) OS.getValue(stack);
    		windows = Os.WINDOWS.equals(os);
    	}
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
    
    private String windowsify(String path, boolean windows) {
		if (windows) {
			return path.replace('/', '\\');
		}
		else {
			return path;
		}
	}

	public static final Arg FILES = new Arg.Positional("files");
    static {
        setArguments("vdl_pathnames", new Arg[] { FILES });
    }

    public Object[] vdl_pathnames(VariableStack stack) throws ExecutionException {
        List<String> l = new ArrayList<String>();
        Iterator<?> i = TypeUtil.toIterator(FILES.getValue(stack));
        while (i.hasNext()) {
        	l.add(remotePathName(new AbsFile((String) i.next()).getPath()));
        }
        return l.toArray(new String[0]);
    }
}
