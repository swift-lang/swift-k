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
 * Created on Mar 25, 2014
 */
package org.globus.cog.abstraction.impl.file;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.globus.cog.abstraction.interfaces.FileResource;
import org.globus.cog.abstraction.interfaces.GridFile;
import org.globus.cog.abstraction.interfaces.RemoteFile;

public class SimplePathExpansion {
    
    /**
     * Expands in parallel the paths in src and dst using the 
     * specified file resource. In contrast to PathExpansion,
     * this class only expands patterns in the file name, not
     * directories.
     * 
     * @throws FileResourceException 
     */
    public static Collection<String[]> expand(RemoteFile srf, RemoteFile drf, FileResource sres) throws FileResourceException {
        SimplePathExpansion pe = new SimplePathExpansion(srf, drf, sres);
        return pe.glob();
    }
    
    private RemoteFile srf;
    private RemoteFile drf;
    private FileResource fr;
    
    public SimplePathExpansion(RemoteFile srf, RemoteFile drf, FileResource sres) {
        this.srf = srf;
        this.drf = drf;
                
        this.fr = sres;
    }


    protected Collection<String[]> glob() throws FileResourceException {
        List<String[]> results = new ArrayList<String[]>();
        Pattern p = Pattern.compile(toRegexp(srf.getName()));
        Collection<GridFile> files = list(fr, srf.getDirectory(), p);
        for (GridFile f : files) {
            Matcher m = p.matcher(f.getName());
            if (!m.matches()) {
                throw new IllegalStateException("'" + f.getName() + "' was supposed to match '" + srf.getName() + "'");
            }
            
            String[] result = new String[2];
            RemoteFile rf = new RemoteFile(srf);
            rf.setName(f.getName());
            result[0] = rf.getURIAsString();
            result[1] = substituteGroups(drf, m);
            results.add(result);
        }
        return results;
    }
    

    private Collection<GridFile> list(FileResource fr, String dir, final Pattern pattern) throws FileResourceException {
        return fr.list(dir, new FileResourceFileFilter() {
            @Override
            public boolean accept(GridFile f) {
                return pattern.matcher(f.getName()).matches();
            }
        });
    }
    
    private String toRegexp(String s) {
        StringBuilder sb = new StringBuilder();
        boolean lastWasWildcard = false;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '?':
                    if (!lastWasWildcard) {
                        lastWasWildcard = true;
                        sb.append("(");
                    }
                    sb.append('.');
                    break;
                case '*':
                    if (!lastWasWildcard) {
                        lastWasWildcard = true;
                        sb.append("(");
                    }
                    sb.append(".*");
                    break;
                case '.':
                case '\\':
                case '[':
                case ']':
                case '(':
                case ')':
                case '^':
                case '$':
                case '|':
                case '{':
                case '}':
                    if (lastWasWildcard) {
                        lastWasWildcard = false;
                        sb.append(')');
                    }
                    sb.append('\\');
                    sb.append(c);
                    break;
                default:
                    if (lastWasWildcard) {
                        lastWasWildcard = false;
                        sb.append(')');
                    }
                    sb.append(c);
                    break;
            }
        }
        if (lastWasWildcard) {
            sb.append(')');
        }
        return sb.toString();
    }

    private String substituteGroups(RemoteFile drf, Matcher m) {
        String dst = drf.getName();
        StringBuilder sb = new StringBuilder();
        boolean lastWasWildcard = false;
        int begin = 0;
        int cg = 0;
        for (int i = 0; i < dst.length(); i++) {
            char c = dst.charAt(i);
            switch (c) {
                case '?':
                case '*':
                    if (!lastWasWildcard) {
                        // begin group
                        sb.append(dst.substring(begin, i));
                        cg++;
                    }
                    lastWasWildcard = true;
                    break;
                default:
                    if (lastWasWildcard) {
                        // end group
                        sb.append(m.group(cg));
                        lastWasWildcard = false;
                        begin = i;
                    }
            }
        }
        if (lastWasWildcard) {
            sb.append(m.group(cg));
        }
        else {
            sb.append(dst.substring(begin));
        }
        RemoteFile rf = new RemoteFile(drf);
        rf.setName(sb.toString());
        return rf.getURIAsString();
    }
}
