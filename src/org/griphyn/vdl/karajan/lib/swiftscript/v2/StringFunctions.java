//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Mar 7, 2015
 */
package org.griphyn.vdl.karajan.lib.swiftscript.v2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import k.rt.Stack;

import org.globus.cog.karajan.analyzer.ArgRef;
import org.globus.cog.karajan.analyzer.Signature;
import org.griphyn.vdl.type.Field;

public class StringFunctions {
    
    public static class Strcat extends FTypes.VargsReducerString<Object, StringBuilder> {
        @Override
        protected StringBuilder initial(Stack stack) {
            return new StringBuilder();
        }

        @Override
        protected StringBuilder reduceOne(StringBuilder current, Object value) {
            current.append(value);
            return current;
        }

        @Override
        protected String getValue(StringBuilder current) {
            return current.toString();
        }
    }
    
    public static class Length extends FTypes.SwiftFunction {
        private ArgRef<String> s;

        @Override
        public Object function(Stack stack) {
            String s = this.s.getValue(stack);
            return s.length();
        }

        @Override
        protected Signature getSignature() {
            return new Signature(params("s"));
        }

        @Override
        protected Field getReturnType() {
            return Field.GENERIC_INT;
        }
    }
    
    public static class Split extends FTypes.SwiftFunction {
        private ArgRef<String> s;
        private ArgRef<String> delimiter;

        @Override
        protected Field getReturnType() {
            return Field.GENERIC_STRING_ARRAY;
        }
        
        public Object function(Stack stack) {
            return function(stack, Integer.MAX_VALUE);
        }
        
        public Object function(Stack stack, int maxSplit) {
            String s = this.s.getValue(stack);
            String delimiter = this.delimiter.getValue(stack);
            List<String> l = new ArrayList<String>();
            int dl = delimiter.length();
            int index = -dl;
            while (index < s.length() && l.size() < maxSplit - 1) {
            	int i = s.indexOf(delimiter, index + dl);
            	if (i == -1) {
            		l.add(s.substring(index + dl));
            		index = s.length();
            	}
            	else if (i - index == dl) {
            		index = i;
            	}
            	else {
            		l.add(s.substring(index + dl, i));
            		index = i;
            	}
            }
            if (index < s.length()) {
            	l.add(s.substring(index + dl));
            }
            return l;
        }

        @Override
        protected Signature getSignature() {
            return new Signature(params("s", "delimiter"));
        }
    }
    
    public static class Split2 extends Split {
        private ArgRef<Integer> maxSplit;

        @Override
        public Object function(Stack stack) {
            int maxSplit = this.maxSplit.getValue(stack);
            return function(stack, maxSplit);
        }
        
        @Override
        protected Signature getSignature() {
            return new Signature(params("s", "delimiter", "maxSplit"));
        }
    }
    
    public static class SplitRe extends FTypes.SwiftFunction {
        private ArgRef<String> s;
        private ArgRef<String> regexp;

        @Override
        protected Field getReturnType() {
            return Field.GENERIC_STRING_ARRAY;
        }
        
        public Object function(Stack stack) {
            return function(stack, Integer.MAX_VALUE);
        }
        
        public Object function(Stack stack, int maxSplit) {
            String s = this.s.getValue(stack);
            String regexp = this.regexp.getValue(stack);
            return Arrays.asList(s.split(regexp, maxSplit));
        }

        @Override
        protected Signature getSignature() {
            return new Signature(params("s", "regexp"));
        }
    }

    public static class SplitRe2 extends SplitRe {
        private ArgRef<Integer> maxSplit;

        @Override
        public Object function(Stack stack) {
            int maxSplit = this.maxSplit.getValue(stack);
            return function(stack, maxSplit);
        }
        
        @Override
        protected Signature getSignature() {
            return new Signature(params("s", "regexp", "maxSplit"));
        }
    }
    
    public static class Trim extends FTypes.StringPString {
        @Override
        protected String v(String arg) {
            return arg.trim();
        }
    }

    public static class Substring extends FTypes.SwiftFunction {
        private ArgRef<String> s;
        private ArgRef<Integer> start;

        @Override
        public Object function(Stack stack) {
            String s = this.s.getValue(stack);
            Integer start = this.start.getValue(stack);
            return s.substring(start);
        }

        @Override
        protected Signature getSignature() {
            return new Signature(params("s", "start"));
        }

        @Override
        protected Field getReturnType() {
            return Field.GENERIC_STRING;
        }
    }
    
    public static class Substring2 extends FTypes.SwiftFunction {
        private ArgRef<String> s;
        private ArgRef<Integer> start;
        private ArgRef<Integer> end;

        @Override
        public Object function(Stack stack) {
            String s = this.s.getValue(stack);
            int start = this.start.getValue(stack);
            int end = this.end.getValue(stack);
            return s.substring(start, end);
        }

        @Override
        protected Signature getSignature() {
            return new Signature(params("s", "start", "end"));
        }

        @Override
        protected Field getReturnType() {
            return Field.GENERIC_STRING;
        }
    }
    
    public static class ToUpper extends FTypes.StringPString {
        @Override
        protected String v(String arg) {
            return arg.toUpperCase();
        }
    }
    
    public static class ToLower extends FTypes.StringPString {
        @Override
        protected String v(String arg) {
            return arg.toLowerCase();
        }
    }
    
    private static class JoinState {
        public String delimiter;
        public StringBuilder sb;
    }
    
    public static class Join extends FTypes.ArrayReducerString<String, JoinState> {
        private ArgRef<String> delimiter;

        @Override
        protected JoinState initial(Stack stack) {
            JoinState s = new JoinState();
            s.sb = new StringBuilder();
            s.delimiter = this.delimiter.getValue(stack);
            return s;
        }

        @Override
        protected JoinState reduceOne(JoinState current, String value) {
            if (current.sb.length() > 0) {
                current.sb.append(current.delimiter);
            }
            current.sb.append(value);
            return current;
        }

        @Override
        protected String getValue(JoinState current) {
            return current.sb.toString();
        }

        @Override
        protected Signature getSignature() {
            return new Signature(params("array", "delimiter"));
        }
    }
    
    public static class ReplaceAll extends FTypes.SwiftFunction {
        private ArgRef<String> s;
        private ArgRef<String> find;
        private ArgRef<String> replacement;

        @Override
        protected Field getReturnType() {
            return Field.GENERIC_STRING;
        }

        @Override
        public Object function(Stack stack) {
            String s = this.s.getValue(stack);
            String find = this.find.getValue(stack);
            String replacement = this.replacement.getValue(stack);
            return replaceAll(s, find, replacement, stack);
        }

        protected Object replaceAll(String s, String find, String replacement, Stack stack) {
            return s.replace(find, replacement);
        }

        @Override
        protected Signature getSignature() {
            return new Signature(params("s", "find", "replacement"));
        }
    }
    
    public static class ReplaceAll2 extends ReplaceAll {
        private ArgRef<Integer> start;
        private ArgRef<Integer> end;

        @Override
        protected Object replaceAll(String s, String find, String replacement, Stack stack) {
            int start = this.start.getValue(stack);
            int end = this.end.getValue(stack);
            StringBuilder sb = new StringBuilder();
            sb.append(s.substring(0, start));
            sb.append(s.substring(start, end).replace(find, replacement));
            sb.append(s.substring(end));
            return sb.toString();
        }

        @Override
        protected Signature getSignature() {
            return new Signature(params("s", "find", "replacement", "start", "end"));
        }
    }
    
    public static class ReplaceAllRe extends FTypes.SwiftFunction {
        private ArgRef<String> s;
        private ArgRef<String> findRe;
        private ArgRef<String> replacementRe;

        @Override
        protected Field getReturnType() {
            return Field.GENERIC_STRING;
        }

        @Override
        public Object function(Stack stack) {
            String s = this.s.getValue(stack);
            String findRe = this.findRe.getValue(stack);
            String replacementRe = this.replacementRe.getValue(stack);
            
            return replaceAll(s, findRe, replacementRe, stack);
        }

        protected Object replaceAll(String s, String findRe, String replacementRe, Stack stack) {
            return s.replaceAll(findRe, replacementRe);
        }

        @Override
        protected Signature getSignature() {
            return new Signature(params("s", "findRe", "replacementRe"));
        }
    }
    
    public static class ReplaceAllRe2 extends ReplaceAllRe {
        private ArgRef<Integer> start;
        private ArgRef<Integer> end;

        @Override
        protected Object replaceAll(String s, String find, String replacement, Stack stack) {
            int start = this.start.getValue(stack);
            int end = this.end.getValue(stack);
            StringBuilder sb = new StringBuilder();
            sb.append(s.substring(0, start));
            sb.append(s.substring(start, end).replaceAll(find, replacement));
            sb.append(s.substring(end));
            return sb.toString();
        }

        @Override
        protected Signature getSignature() {
            return new Signature(params("s", "findRe", "replacementRe", "start", "end"));
        }
    }

    public static class IndexOf extends FTypes.SwiftFunction {
        private ArgRef<String> s;
        private ArgRef<String> find;
        private ArgRef<Integer> start;

        @Override
        protected Field getReturnType() {
            return Field.GENERIC_INT;
        }

        @Override
        public Object function(Stack stack) {
            String s = this.s.getValue(stack);
            String find = this.find.getValue(stack);
            int start = this.start.getValue(stack);
            
            return indexOf(s, find, start, stack);
        }

        protected Object indexOf(String s, String find, int start, Stack stack) {
            return s.indexOf(find, start);
        }

        @Override
        protected Signature getSignature() {
            return new Signature(params("s", "find", "start"));
        }
    }
    
    public static class IndexOf2 extends IndexOf {
        private ArgRef<Integer> end;

        @Override
        protected Object indexOf(String s, String find, int start, Stack stack) {
            int end = this.end.getValue(stack);
            int index = s.indexOf(find, start);
            if (index < end) {
                return index;
            }
            else {
                return -1;
            }
        }

        @Override
        protected Signature getSignature() {
            return new Signature(params("s", "find", "start", "end"));
        }
    }

    public static class LastIndexOf extends FTypes.SwiftFunction {
        private ArgRef<String> s;
        private ArgRef<String> find;
        private ArgRef<Integer> start;

        @Override
        protected Field getReturnType() {
            return Field.GENERIC_INT;
        }

        @Override
        public Object function(Stack stack) {
            String s = this.s.getValue(stack);
            String find = this.find.getValue(stack);
            int start = this.start.getValue(stack);
            
            return lastIndexOf(s, find, start, stack);
        }

        protected Object lastIndexOf(String s, String find, int start, Stack stack) {
            return s.lastIndexOf(find, start == -1 ? s.length() : start);
        }

        @Override
        protected Signature getSignature() {
            return new Signature(params("s", "find", "start"));
        }
    }
    
    public static class LastIndexOf2 extends LastIndexOf {
        private ArgRef<Integer> end;

        @Override
        protected Object lastIndexOf(String s, String find, int start, Stack stack) {
            int end = this.end.getValue(stack);
            int index = s.lastIndexOf(find, start == -1 ? s.length() : start);
            if (index < end) {
                return -1;
            }
            else {
                return index;
            }
        }

        @Override
        protected Signature getSignature() {
            return new Signature(params("s", "find", "start", "end"));
        }
    }

    public static class Matches extends FTypes.SwiftFunction {
        private ArgRef<String> s;
        private ArgRef<String> regexp;

        @Override
        protected Field getReturnType() {
            return Field.GENERIC_BOOLEAN;
        }

        @Override
        public Object function(Stack stack) {
            String s = this.s.getValue(stack);
            String regexp = this.regexp.getValue(stack);
            
            return s.matches(regexp);
        }
        
        @Override
        protected Signature getSignature() {
            return new Signature(params("s", "regexp"));
        }
    }
    
    public static class FindAllRe extends FTypes.SwiftFunction {
        private ArgRef<String> s;
        private ArgRef<String> regexp;

        @Override
        protected Field getReturnType() {
            return Field.GENERIC_STRING_ARRAY;
        }

        @Override
        public Object function(Stack stack) {
            String s = this.s.getValue(stack);
            String regexp = this.regexp.getValue(stack);
            
            List<String> groups = new ArrayList<String>();
            Pattern p = Pattern.compile(regexp);
            Matcher m = p.matcher(s);
            while (m.find()) {
                for (int i = 0; i < m.groupCount(); i++) {
                	// this is tricky, since it is possible for a group to fail a match,
                	// while the pattern succeeds. Matcher returns null in such cases. 
                	// We'll just use an empty string, but that's not quite legit
                	String g = m.group(i);
                	if (g == null) {
                		groups.add("");
                	}
                	else {
                		groups.add(g);
                	}
                }
            }
            return groups;
        }
        
        @Override
        protected Signature getSignature() {
            return new Signature(params("s", "regexp"));
        }
    }
}
