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


// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------


package org.globus.cog.util;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This class provides a string split function similar to jdk 1.4
 * String.split(). Be warned that the split() method in this class does not use
 * regular expressions.
 *
 */
public class StringUtil {

	/**
	 * Splits a string into an array of tokens
	 *
	 * @param string
	 *            The string to be split
	 * @param separator
	 *            The separator for the tokens
	 * @return an array with the tokens generated
	 */
	public static String[] split(String string, String separator) {

		int count = 0;
		for (int last = -1, next = 0;
			next != -1;
			next = string.indexOf(separator, last + 1), count++, last = next) {
		}
		String[] tokens = new String[count];
		int lastIndex = -1;
		int nextIndex = 0;
		int token = 0;
		do {
			nextIndex = string.indexOf(separator, lastIndex + 1);
			if (nextIndex == -1) {
				String last;
				if (lastIndex == string.length() - 1) {
					last = "";
				}
				else {
					last = string.substring(lastIndex + 1);
				}
				tokens[token++] = last;
			}
			else {
				tokens[token++] = string.substring(lastIndex + 1, nextIndex);
			}
			lastIndex = nextIndex;
		}
		while (lastIndex != -1);

		return tokens;
	}

	public static String wordWrap(String arg, int lead, int width) {
		StringBuffer tmp = new StringBuffer();
		String[] words = arg.split(" ");
		int crtwidth = 0;
		String sLead = StringUtil.repeat(" ", lead);
		tmp.append(sLead);
		for (int i = 0; i < words.length; i++) {
			if (crtwidth == 0) {
				if (words[i].length() > width) {
					String[] split = StringUtil.split(words[i], width);
					for (int j = 0; j < split.length - 1; j++) {
						tmp.append(split[j]);
						tmp.append("\n");
						tmp.append(sLead);
					}
					tmp.append(split[split.length - 1]);
					if (split[split.length - 1].length() < width) {
						tmp.append(" ");
						crtwidth = split[split.length - 1].length() + 1;
					}
					else {
						tmp.append("\n");
						tmp.append(sLead);
						crtwidth = 0;
					}
				}
				else {
					tmp.append(words[i]);
					tmp.append(" ");
					crtwidth += words[i].length() + 1;
				}
			}
			else {
				int len = words[i].length();
				if (crtwidth + len < width) {
					tmp.append(words[i]);
					tmp.append(" ");
					crtwidth += len + 1;
				}
				else if (crtwidth + len == width) {
					tmp.append(words[i]);
					tmp.append("\n");
					tmp.append(sLead);
					crtwidth = 0;
				}
				else {
					tmp.append("\n");
					tmp.append(sLead);
					tmp.append(words[i]);
					tmp.append(" ");
					crtwidth = len + 1;
				}
			}
		}
		return tmp.toString();
	}

	public static String repeat(String s, int times) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < times; i++) {
			sb.append(s);
		}
		return sb.toString();
	}

	public static String[] split(String s, int length) {
		if (length == 0) {
			return new String[0];
		}
		int len = s.length() / length + 1;
		String[] result = new String[len];
		for (int i = 0; i < len; i++) {
			result[i] = s.substring(i * length, Math.min((i + 1) * length, s.length()));
		}
		return result;
	}

	public static String concat(String[] tokens, int start) {
	    return concat(tokens, start, " ");
	}

	public static String concat(String[] tokens, int start,
	                            String separator) {
        StringBuilder sb = new StringBuilder();
        for (int i = start; i < tokens.length; i++) {
            sb.append(tokens[i]);
            if (i < tokens.length-1)
                sb.append(separator);
        }
        return sb.toString();
    }

	public static String concat(String... strings) {
	    return concat(' ', strings);
	}

	public static String concat(char c, String... strings) {
	    StringBuilder sb = new StringBuilder();
	    for (int i = 0; i < strings.length; i++) {
	        sb.append(strings[i]);
	        if (i < strings.length-1)
	            sb.append(c);
	    }
	    return sb.toString();
	}

	/**
	   Could be replaced by Arrays.copyOf in Java 1.6
	 */
    public static String[] subset(String[] tokens, int offset) {
        int length = tokens.length - offset;
        String[] result = new String[length];
        for (int i = 0; i < length; i++)
            result[i] = tokens[i+offset];
        return result;
    }

    /**
       @see {@link Arrays.asList}
     */
    public static void addSome(List<String> dest, String[] src,
                               int i) {
        for (int j = i; j < src.length; j++)
            dest.add(src[j]);
    }

    /**
       Like Java Map.toString() but no curly braces
     */
    public static String toString(Map<String,? extends Object> map) {

    	if (map == null)
    		return "null";

    	StringBuilder sb = new StringBuilder(map.size()*128);
    	Set<String> keys = map.keySet();
    	Iterator<String> it = keys.iterator();
    	while (it.hasNext()) {
    		String key = it.next();
    		Object value = map.get(key);
    		sb.append(key);
    		sb.append('=');
    		sb.append(value);
    		if (it.hasNext())
    			sb.append(',');
    	}
    	return sb.toString();
    }
    
    public static String toString(List<? extends Object> l) {
        if (l == null) {
            return "null";
        }
        return l.toString();
    }
}
