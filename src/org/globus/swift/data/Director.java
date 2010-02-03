
package org.globus.swift.data;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.globus.swift.data.policy.Policy;
import org.globus.swift.data.util.LineReader;
import org.griphyn.vdl.karajan.Loader;

/**
 * Manages CDM policies for files based on pattern matching.
 * @author wozniak
 * */

public class Director {
    private static final Logger logger = Logger.getLogger(Director.class);

    /**
       Save the location of the given CDM policy file
     */
    static File policyFile;

    /**
       Maps from Patterns to Policies
     */
    static Map map;

    static {
    	map = new LinkedHashMap();
    }

    public static void load(File policyFile) throws IOException {
        logger.info("loading: " + policyFile);
        Director.policyFile = policyFile;
        LineReader lines = new LineReader();
        List list = lines.read(policyFile);
        for (Iterator it = list.iterator(); it.hasNext(); ) {
            String s = (String) it.next();
            addLine(s);
        }
    }

    static void addLine(String s) {
        String[] tokens = LineReader.tokenize(s);
        Pattern pattern = Pattern.compile(tokens[0]);
        Policy policy = Policy.valueOf(tokens[1]);
        map.put(pattern, policy);
    }

    public static Policy lookup(String file) {
    	for (Iterator it = map.keySet().iterator(); it.hasNext(); ) {
    	    Pattern pattern = (Pattern) it.next();
    		Matcher matcher = pattern.matcher(file);
    		if (matcher.matches())
    			return (Policy) map.get(pattern);
    	}
    	return Policy.DEFAULT;
    }
}
