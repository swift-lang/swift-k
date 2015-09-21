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


/*
 * Created on Aug 3, 2014
 */
package org.griphyn.vdl.karajan;

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
import org.griphyn.vdl.mapping.InvalidPathException;
import org.griphyn.vdl.mapping.Mapper;
import org.griphyn.vdl.mapping.Path;
import org.griphyn.vdl.mapping.PathComparator;
import org.griphyn.vdl.mapping.RootHandle;

public class FileNameResolver {
    public enum MultiMode {
        COMBINED, SEPARATE
    }
    
    public enum Transform {
        NONE, REMOTE, ABSOLUTE
    }
    
    private final DSHandle var;
    private final MultiMode mode;
    private Transform transform;
    private String defaultScheme;
    
    public FileNameResolver(DSHandle var) {
        this(var, MultiMode.COMBINED, Transform.REMOTE);
    }

    public FileNameResolver(DSHandle var, MultiMode mode, Transform transform) {
        this.var = var;
        this.mode = mode;
        this.transform = transform;
    }

    public Transform getTransform() {
        return transform;
    }

    public void setTransform(Transform transform) {
        this.transform = transform;
    }

    public String getDefaultScheme() {
        return defaultScheme;
    }

    public void setDefaultScheme(String defaultScheme) {
        this.defaultScheme = defaultScheme;
    }

    @Override
    public String toString() {
        return toCombinedString();
    }
    
    public String toCombinedString() {
        return combine(map());
    }
    
    public String[] toStringArray() {
        List<AbsFile> l = map();
        String[] r = new String[l.size()];
        for (int i = 0; i < l.size(); i++) {
            AbsFile f = l.get(i);
            r[i] = getPath(f);
        }
        return r;
    }
    
    public List<String> toStringList() {
        return Arrays.asList(toStringArray());
    }

    public void toString(Collection<Object> ret) {
        if (mode == MultiMode.COMBINED) {
            ret.add(combine(map()));
        }
        else {
            addAll(ret, map());
        }
    }

    private void addAll(Collection<Object> ret, List<AbsFile> l) {
        for (int i = 0; i < l.size(); i++) {
            AbsFile f = l.get(i);
            ret.add(getPath(f));
        }
    }

    private String combine(List<AbsFile> l) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < l.size(); i++) {
            AbsFile f = l.get(i);
            if (i > 0) {
                sb.append(' ');
            }
            sb.append(getPath(f));
        }
        return sb.toString();
    }

    private String getPath(AbsFile f) {
        if (this.transform == Transform.ABSOLUTE || isDirect(f)) {
            return f.getAbsolutePath();
        }
        else if (this.transform == Transform.REMOTE) {
            return PathUtils.remotePathName(f);
        }
        else if (isLocal(f) || f.getHost() == null) {
            return f.getPath();
        }
        else {
            return f.getHost() + "/" + f.getPath();
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
        return f.getProtocol() == null || "file".equals(f.getProtocol()) || "direct".equals(f.getProtocol());
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
        catch (InvalidPathException e) {
            throw new ExecutionException("Cannot map " + var, e);
        }
        catch (HandleOpenException e) {
            throw new ExecutionException("The current implementation should not throw this exception", e);
        }
    }

    private AbsFile mapSingle() {
        AbsFile f = (AbsFile) var.map();
        if (defaultScheme != null && f.getProtocol() == null) {
            f = new AbsFile(defaultScheme, f.getHost(), f.getPort(), f.getDirectory(), f.getName());
        }
        return f;
    }

    public static final String[] EMPTY_STRING_ARRAY = new String[0];

    private List<AbsFile> mapMultiple() throws HandleOpenException, InvalidPathException {
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
            AbsFile f = (AbsFile) mapper.map(p);
            if (defaultScheme != null && f.getProtocol() == null) {
                f = new AbsFile(defaultScheme, f.getHost(), f.getPort(), f.getDirectory(), f.getName());
            }
            l.add(f);
        }
        return l;
    }
}
