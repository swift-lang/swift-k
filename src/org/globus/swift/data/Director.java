
package org.globus.swift.data;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
       Maps from Patterns to Policies for fs.data rules
     */
    static Map<Pattern,Policy> map = new LinkedHashMap();
    
    /**
       Maps from String names to String values for fs.data properties
    */
    static Map<String,String> properties = new HashMap();

    /** 
       Remember the files we have broadcasted
     */
    static Set<String> broadcasted = new HashSet<String>();
    
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
        String type = tokens[0];
        if (type.equals("rule")) { 
            addRule(tokens);
        }
        else if (type.equals("property")) { 
            addProperty(tokens);
        }
       
    }

    static void addRule(String[] tokens) { 
        Pattern pattern = Pattern.compile(tokens[1]);
        Policy policy = Policy.valueOf(tokens[2]);
        List<String> tokenList = Arrays.asList(tokens);        
        policy.settings(tokenList.subList(3,tokenList.size()));
        map.put(pattern, policy);
    }
    
    static void addProperty(String[] tokens) { 
        String name = tokens[1];
        String value = concat(tokens, 2);        
        properties.put(name, value);
    }
    
    static String concat(String[] tokens, int start) {
        StringBuilder result = new StringBuilder();
        for (int i = start; i < tokens.length; i++) { 
            result.append(tokens[i]);
        }
        return result.toString();
    }
    
    public static Policy lookup(String file) {
    	for (Pattern pattern : map.keySet()) {
    		Matcher matcher = pattern.matcher(file);
    		if (matcher.matches())
    			return map.get(pattern);
    	}
    	return Policy.DEFAULT;
    }
    
    public static String property(String name) {
        String result = properties.get(name);
        if (result == null) 
            result = "UNSET";
        return result;
    }
    
    public static boolean broadcasted(String file, String dir) {
        return broadcasted.contains(dir+"/"+file);
    }
    
    public static void broadcast(String file, String dir) {
        broadcasted.add(dir+"/"+file);
    }
    
    /** 
     * Check the policy effect of name with respect to policy_file
     * @param args {name, policy_file} 
     */
    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Incorrect args");
            System.exit(1);
        }
            
        try {
       
            String name = args[0]; 
            File policyFile = new File(args[1]);
            if (! policyFile.exists()) {
                System.out.println("Policy file does not exist: " + 
                    args[1]);
            }
            load(policyFile);
            Policy policy = lookup(name);
            System.out.println(name + ": " + policy);
        } catch (Exception e) { 
            e.printStackTrace();
            System.exit(2);
        }
    }
}
