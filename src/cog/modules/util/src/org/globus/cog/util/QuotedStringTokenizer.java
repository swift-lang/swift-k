/*
 * Portions of this file Copyright 1999-2005 University of Chicago
 * Portions of this file Copyright 1999-2005 The University of Southern California.
 *
 * This file or a portion of this file is licensed under the
 * terms of the Globus Toolkit Public License, found at
 * http://www.globus.org/toolkit/download/license.html.
 * If you redistribute this file, with or without
 * modifications, you must include this notice in the file.
 */
package org.globus.cog.util;

import java.util.Enumeration;

/*
 * This class is not thread safe.
 */
public class QuotedStringTokenizer implements Enumeration {
    
    private int limit;
    private int start;
    private String str;

    public QuotedStringTokenizer(String str) {
	this.str = str;
	start = 0;
	limit = str.length();
    }

    public Object nextElement() {
	return nextToken();
    }

    public String nextToken() {
	while ((start < limit) && (str.charAt(start) <= ' ')) {
	    start++;	// eliminate leading whitespace
	}

	if (start == limit) return null;

	StringBuffer buf = new StringBuffer(limit-start);
	char ch;
	char quote = str.charAt(start);
	if (quote == '"' || quote == '\'') {
	    start++;
	    for (int i=start;i<limit;i++) {
		ch = str.charAt(i);
		start++;
		if (ch == quote) {
		    break;
		} else if (ch == '\\') {
		    buf.append( str.charAt(++i) );
		    start++;
		} else {
		    buf.append(ch);
		}
	    }
	    return buf.toString();
	} else {
	    for (int i=start;i<limit;i++) {
		ch = str.charAt(i);
		start++;
		if (ch == ' ') {
		    break;
		} else {
		    buf.append(ch);
		}
	    }
	}
	
	return buf.toString();
    }

    public boolean hasMoreElements() {
	return hasMoreTokens();
    }

    public boolean hasMoreTokens() {
	while ((start < limit) && (str.charAt(start) <= ' ')) {
	    start++;	// eliminate leading whitespace
	}
	
	return (start != limit);
    }

    public int countTokens() {
	int localStart = start;
	int i = 0;
	while( nextToken() != null ) {
	    i++; 
	}
	start = localStart;
	return i;
    }
    
}
