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
        if (dir.startsWith("/")) {
            return dir.substring(1);
        }
        else {
            return dir;
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
        List l = new ArrayList();
        Iterator i = TypeUtil.toIterator(FILES.getValue(stack));
        while (i.hasNext()) {
        	l.add(new AbsFile((String) i.next()).getPath());
        }
        return l.toArray(new String[0]);
    }
}
