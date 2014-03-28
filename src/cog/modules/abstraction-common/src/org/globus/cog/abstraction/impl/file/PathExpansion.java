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
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.globus.cog.abstraction.interfaces.FileResource;
import org.globus.cog.abstraction.interfaces.GridFile;

public class PathExpansion {
    
    private static class PathElement {
        public final String e;
        public final int groupCount;
        
        public PathElement(String e, int groupCount) {
            this.e = e;
            this.groupCount = groupCount;
        }
        
        public String toString() {
            return e + ":" + groupCount;
        }
    }
    
    private static class ResultElement {
        public final String e;
        public final int groupIndex;
        
        public ResultElement(String e) {
            this.e = e;
            this.groupIndex = -1;
        }
        
        public ResultElement(int groupIndex) {
            this.e = null;
            this.groupIndex = groupIndex;
        }
        
        public String toString() {
            if (this.e == null) {
                return "\\" + this.groupIndex;
            }
            else {
                return this.e;
            }
        }
    }
    
    /**
     * Expands in parallel the paths in src and dst using the 
     * specified file resource. 
     * The source and destination must have the same pattern structure.
     * 
     * For example: src = aaa???.b?bb, dst = xxxzy???.c?cc
     * 
     * Returns a list of string pairs (src1, dst1) with the patterns
     * replaced in both src and dst by files matched by src.
     * For example, sres can find the following files:
     * aaa001.bAbb, aaa002.bCbb
     * 
     * then this method will return:
     * (aaa001.bAbb, xxxzy001.cAcc)
     * (aaa002.bCbb, xxxzy002.cCcc)
     * @throws FileResourceException 
     */
    public static Collection<String[]> expand(String src, String dst, FileResource sres) throws FileResourceException {
        PathExpansion pe = new PathExpansion(src, dst, sres);
        pe.globRecursive(0, 0, "");
        return pe.results;
    }
    
    private List<PathElement> srcs;
    private List<ResultElement> dsts;
    private String[] captureGroups;
    private List<String[]> results;
    private FileResource fr;
    
    public PathExpansion(String src, String dst, FileResource sres) {
        this.srcs = buildSrcList(src);
        this.dsts = buildDstList(dst);
        
        int groupCount = countCaptureGroups(srcs);
        captureGroups = new String[groupCount];
        results = new ArrayList<String[]>();
        
        this.fr = sres;
    }

    private int countCaptureGroups(List<PathElement> srcs) {
        int s = 0;
        for (PathElement pe : srcs) {
            s += pe.groupCount;
        }
        return s;
    }

    protected void globRecursive(int srcIndex,  int groupIndex, String partialPath) 
            throws FileResourceException {
        
        if (srcIndex == srcs.size()) {
            String[] result = new String[2];
            result[0] = partialPath;
            result[1] = substituteGroups();
            results.add(result);
            return;
        }
        PathElement pe = srcs.get(srcIndex);
        if (pe.groupCount == 0) {
            globRecursive(srcIndex + 1, groupIndex, partialPath + pe.e);
        }
        else {
            Collection<GridFile> files = list(fr, partialPath, pe.e);
            Pattern p = Pattern.compile(pe.e);
            for (GridFile f : files) {
                Matcher m = p.matcher(f.getName());
                if (!m.matches()) {
                    throw new IllegalStateException("'" + f.getName() + "' was supposed to match '" + pe.e + "'");
                }
                for (int j = 0; j < pe.groupCount; j++) {
                    captureGroups[groupIndex + j] = m.group(j + 1);
                }
                boolean last = srcIndex + 1 == srcs.size();
                if (last) {
                    globRecursive(srcIndex + 1, groupIndex + pe.groupCount, partialPath + f.getName());
                }
                else {
                    globRecursive(srcIndex + 1, groupIndex + pe.groupCount, partialPath + f.getName() + "/");
                }
            }
        }
    }

    private String substituteGroups() {
        StringBuilder sb = new StringBuilder();
        for (ResultElement re : dsts) {
            if (re.groupIndex > 0) {
                sb.append(captureGroups[re.groupIndex - 1]);
            }
            else {
                sb.append(re.e);
            }
        }
        return sb.toString();
    }

    private Collection<GridFile> list(FileResource fr, String dir, String pattern) throws FileResourceException {
        final Pattern p = Pattern.compile(pattern);
        return fr.list(dir, new FileResourceFileFilter() {
            @Override
            public boolean accept(GridFile f) {
                return p.matcher(f.getName()).matches();
            }
        });
    }

    /**
     * Splits a path into path elements. Each path element is either a
     * contiguous sequence in the path that contains no glob patterns
     * or a single path element that contains glob patterns.
     * 
     * For example, the path "/a/b/?/d?e?f/g" will be split into:
     * ["/a/b/", "?/", "d?e?f/", "g"]
     */
    private List<PathElement> buildSrcList(String src) {
        List<PathElement> l = new LinkedList<PathElement>();
        int runStart = 0, lastSeparator = 0;
        boolean lastWasWildcard = false;
        int wildcardsInLastRun = 0;
        
        for (int i = 0; i < src.length(); i++) {
            char c = src.charAt(i);
            switch (c) {
                case '/':
                    lastWasWildcard = false;
                    if (wildcardsInLastRun > 0) {
                        l.add(new PathElement(toRegexp(src.substring(runStart, i + 1)), wildcardsInLastRun));
                        runStart = i + 1;
                    }
                    lastSeparator = i;
                    continue;
                case '?':
                case '*':
                    if (!lastWasWildcard) {
                        wildcardsInLastRun++;
                        l.add(new PathElement(src.substring(runStart, lastSeparator + 1), 0));
                        runStart = lastSeparator + 1;
                    }
                    lastWasWildcard = true;
                    break;
                default:
                    lastWasWildcard = false;
            }
        }
        if (wildcardsInLastRun > 0) {
            l.add(new PathElement(toRegexp(src.substring(runStart)), wildcardsInLastRun));
        }
        else {
            l.add(new PathElement(src.substring(runStart), 0));
        }
        return l;
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
                case '+':
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

    private List<ResultElement> buildDstList(String dst) {
        List<ResultElement> l = new ArrayList<ResultElement>();
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
                        l.add(new ResultElement(dst.substring(begin, i)));
                        cg++;
                    }
                    lastWasWildcard = true;
                    break;
                default:
                    if (lastWasWildcard) {
                        // end group
                        l.add(new ResultElement(cg));
                        lastWasWildcard = false;
                        begin = i;
                    }
            }
        }
        if (lastWasWildcard) {
            l.add(new ResultElement(cg));
        }
        else {
            l.add(new ResultElement(dst.substring(begin)));
        }
        return l;
    }
}
