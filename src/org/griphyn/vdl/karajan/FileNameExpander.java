//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Aug 3, 2014
 */
package org.griphyn.vdl.karajan;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import k.rt.ExecutionException;

import org.griphyn.vdl.karajan.lib.PathUtils;
import org.griphyn.vdl.mapping.AbsFile;
import org.griphyn.vdl.mapping.DSHandle;
import org.griphyn.vdl.mapping.DependentException;
import org.griphyn.vdl.mapping.HandleOpenException;
import org.griphyn.vdl.mapping.Mapper;
import org.griphyn.vdl.mapping.Path;
import org.griphyn.vdl.mapping.PathComparator;
import org.griphyn.vdl.mapping.RootHandle;

public class FileNameExpander {
    public enum MultiMode {
        COMBINED, SEPARATE
    }
    
    public enum Transform {
        NONE, RELATIVE
    }
    
    private final DSHandle var;
    private final MultiMode mode;
    private final Transform transform;
    
    public FileNameExpander(DSHandle var) {
        this(var, MultiMode.COMBINED, Transform.RELATIVE);
    }

    public FileNameExpander(DSHandle var, MultiMode mode, Transform transform) {
        this.var = var;
        this.mode = mode;
        this.transform = transform;
    }

    @Override
    public String toString() {
        if (mode == MultiMode.COMBINED) {
            return "filename(" + var + ")";
        }
        else {
            return "filenames(" + var + ")";
        }
    }
    
    public String toCombinedString() {
        return combine(map(), this.transform == Transform.RELATIVE);
    }
    
    public String[] toStringArray() {
        boolean remote = (this.transform == Transform.RELATIVE);
        List<AbsFile> l = map();
        String[] r = new String[l.size()];
        for (int i = 0; i < l.size(); i++) {
            AbsFile f = l.get(i);
            r[i] = getPath(f, remote);
        }
        return r;
    }
    
    public List<String> toStringList() {
        return Arrays.asList(toStringArray());
    }

    public void toString(Collection<Object> ret, boolean direct) {
        boolean remote = (this.transform == Transform.RELATIVE) && !direct;
        if (mode == MultiMode.COMBINED) {
            ret.add(combine(map(), remote));
        }
        else {
            addAll(ret, map(), remote);
        }
    }

    private void addAll(Collection<Object> ret, List<AbsFile> l, boolean remote) {
        for (int i = 0; i < l.size(); i++) {
            AbsFile f = l.get(i);
            ret.add(getPath(f, remote));
        }
    }

    private String combine(List<AbsFile> l, boolean remote) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < l.size(); i++) {
            AbsFile f = l.get(i);
            if (i > 0) {
                sb.append(' ');
            }
            sb.append(getPath(f, remote));
        }
        return sb.toString();
    }

    private String getPath(AbsFile f, boolean remote) {
        if (isDirect(f)) {
            return new File(f.getPath()).getAbsolutePath();
        }
        else if (remote) {
            return remoteName(f);
        }
        else {
            return f.getPath();
        }
    }

    private String remoteName(AbsFile f) {
        if ("file".equals(f.getProtocol())) {
            return PathUtils.remotePathName(f.getPath());
        }
        else {
            return PathUtils.remotePathName(f.getHost() + "/" + f.getPath());
        }
    }

    public String getSingleLocalPath() {
        if (var.getType().isArray() || var.getType().isComposite() || var.getType().isPrimitive()) {
            throw new ExecutionException("Expected a non-composite mapped type instead of " + var);
        }
        AbsFile f = mapSingle();
        if (!isLocal(f)) {
            throw new ExecutionException("Expected a variable mapped to a local file");
        }
        return f.getPath();
    }
    
    private boolean isLocal(AbsFile f) {
        return "file".equals(f.getProtocol()) || "direct".equals(f.getProtocol());
    }
    
    private boolean isDirect(AbsFile f) {
        return "direct".equals(f.getProtocol());
    }

    public String[] getURLsAsArray() throws ExecutionException {
        return getURLArray(map());
    }
    
    private String[] getURLArray(List<AbsFile> l) {
        String[] ret = new String[l.size()];
        for (int i = 0; i < l.size(); i++) {
            AbsFile f = l.get(i);
            if (isLocal(f)) {
                ret[i] = f.getPath();
            }
            else {
                ret[i] = f.getURIAsString();
            }
        }
        return ret;
    }

    private List<AbsFile> map() {
        try {
            if (var.getType().isComposite()) {
                return mapMultiple();
            }
            else {
                return Collections.singletonList(mapSingle());
            }
        }
        catch (DependentException e) {
            return Collections.emptyList();
        }
        catch (HandleOpenException e) {
            throw new ExecutionException("The current implementation should not throw this exception", e);
        }
    }

    private AbsFile mapSingle() {
        return (AbsFile) var.map();
    }

    public static final String[] EMPTY_STRING_ARRAY = new String[0];

    private List<AbsFile> mapMultiple() throws HandleOpenException {
        RootHandle root = var.getRoot();
        Mapper mapper = root.getMapper();
                        
        if (mapper == null) {
            throw new ExecutionException(var.getType() + " is not a mapped type");
        }
        
        List<AbsFile> l = new ArrayList<AbsFile>();
        Collection<Path> fp = var.getFringePaths();
        List<Path> src;
        if (fp instanceof List) {
            src = (List<Path>) fp;
        }
        else {
            src = new ArrayList<Path>(fp);
        }
        Collections.sort(src, new PathComparator());
        
        for (Path p : src) {
            l.add((AbsFile) mapper.map(p));
        }
        return l;
    }
}
