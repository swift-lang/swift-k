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



package org.globus.swift.data;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.globus.swift.data.policy.Broadcast;
import org.globus.swift.data.policy.Policy;
import org.globus.swift.data.util.LineReader;
import org.griphyn.vdl.util.SwiftConfig;

/**
 * Manages CDM policies for files based on pattern matching.
 * Initialized when given a CDM file to read. 
 * @author wozniak
 * */

public class Director {
    private static final Logger logger = 
        Logger.getLogger(Director.class);

    /**
       Has a CDM policy file been provided?
    */
    static boolean enabled = false;

    /**
       Save the location of the given CDM policy file
     */
    static File policyFile;

    /**
       Maps from Patterns to Policies for fs.data rules
     */
    static Map<Pattern,Policy> map = 
        new LinkedHashMap<Pattern, Policy>();

    /**
       Maps from String names to String values for fs.data properties
    */
    static Map<String,String> properties = 
        new HashMap<String, String>();

    /**
       Remember the files we have broadcasted.
       Map from allocations to filenames.
       The keys represent the list of known allocations.
       NOTE: must be accessed only using synchronized Director methods
     */
    private static Map<String,Set<String>> broadcasted =
        new LinkedHashMap<String,Set<String>>();

    /**
       Set of files to be broadcasted.
       NOTE: must be accessed only using synchronized Director methods
    */
    private static Set<String> broadcastWork = new LinkedHashSet<String>();

    public static boolean isEnabled()
    {
        return enabled;
    }

    /** 
       How to broadcast: either "file" or "f2cn"
     */
    public static String broadcastMode = null;
    
    /** 
       Convenience reference to the Swift logfile name
     */
    public static String logfile = null;
    
    /**
       Read in the user-supplied CDM policy file.
    */
    public static void load(File file, SwiftConfig config) throws IOException {
        logger.debug("CDM file: " + file);
        Director.policyFile = file;
        List<String> list = LineReader.read(file);
        for (String s : list)
            addLine(s);
        init(config);
    }

    static void init(SwiftConfig config) throws IOException
    {        
        broadcastMode = config.getCDMBroadcastMode();
        logfile = config.getCDMFile();
        
        if (broadcastMode.equals("file")) 
            broadcasted.put("LOCAL_FILE", new HashSet<String>());
        
        enabled = true;
    }
    
    /**
       A line is either a rule or a property.
    */
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

    /**
       A rule has a pattern, a policy token, and extra arguments.
       <br>
       E.g.: rule .*.txt BROADCAST arg arg arg
    */
    static void addRule(String[] tokens) {
        Pattern pattern = Pattern.compile(tokens[1]);
        Policy policy = Policy.valueOf(tokens[2]);
        List<String> tokenList = Arrays.asList(tokens);
        policy.settings(tokenList.subList(3,tokenList.size()));
        map.put(pattern, policy);
    }

    /**
       A property has a name and a value.
       Properties can be overwritten.
       <br>
       E.g.: property X 3
    */
    static void addProperty(String[] tokens) {
        String name = tokens[1];
        String value = concat(tokens, 2);
        properties.put(name, value);
    }

    /**
       Utility to concatenate all strings from array {@link tokens}
       starting at index {@link start}.
    */
    static String concat(String[] tokens, int start) {
        StringBuilder result = new StringBuilder();
        for (int i = start; i < tokens.length; i++) {
            result.append(tokens[i]);
        }
        return result.toString();
    }

    /**
       Obtain the CDM policy for a given file.
    */
    public static Policy lookup(String file) {
        logger.debug("Director.lookup(): map: " + map);
    	for (Pattern pattern : map.keySet()) {
            Matcher matcher = pattern.matcher(file);
            if (matcher.matches())
                return map.get(pattern);
    	}

    	return Policy.DEFAULT;
    }

    /**
       Obtain the value of a CDM property.
    */
    public static String property(String name) {
        String result = properties.get(name);
        if (result == null)
            result = "UNSET";
        return result;
    }

    /**
       Add a file to the list of files to be broadcasted.
    */
    public static synchronized void addBroadcast(String srcdir, 
                                                 String srcfile) {
        logger.debug("addBroadcast(): " + srcdir + " " + srcfile);
        String path = srcdir+"/"+srcfile;
        broadcastWork.add(path);
    }

    /**
       Add a Coasters allocation to the list of allocations.
       If the location is added twice, the second addition
       is considered to be an empty allocation with no CDM state.
    */
    public static synchronized void addAllocation(String blockId) {
        logger.debug("addAllocation(): " + blockId);
        broadcasted.put(blockId, new HashSet<String>());
        doBroadcast();
    }

    /**
       Create a batch of broadcast work to do and send it to be performed.
    */
    public static synchronized void doBroadcast() {
        logger.debug("doBroadcast: broadcasted: " + broadcasted);
        // Map from locations to files
        Map<String,List<String>> batch = getBroadcastBatch();
        if (batch.size() == 0)
            return;
        logger.debug("doBroadcast(): batch: " + batch);
        Broadcast.perform(batch);
        noteBroadcasts(batch);
        logger.debug("marked: " + broadcasted);
    }

    /**
       Obtain a map from allocations to files.
       For each allocation, its corresponding files should
       be broadcasted to it.
       Should only be called by {@link doBroadcast}.
    */
    private static Map<String,List<String>> getBroadcastBatch() {
        logger.debug("getBroadcastBatch(): ");
        Map<String,List<String>> batch = 
            new LinkedHashMap<String,List<String>>();
        for (String file : broadcastWork) {
            logger.debug("file: " + file);
            for (Map.Entry<String,Set<String>> entry : 
                broadcasted.entrySet()) {
                String allocation = entry.getKey();
                Set<String> files = entry.getValue();
                logger.debug("files: " + files);
                if (! files.contains(file)) {
                    logger.debug("adding: " + file + 
                                 " to: " + allocation);
                    List<String> work = batch.get(allocation);
                    if (work == null) {
                        work = new ArrayList<String>();
                        batch.put(allocation, work);
                    }
                    work.add(file);
                }
            }
        }
        return batch;
    }

    /**
       Note that the files in the given batch have been successfully 
       broadcasted by adding them to Set broadcasted.
       Should only be called by {@link doBroadcast}.
    */
    private static void noteBroadcasts(Map<String,
                                       List<String>> batch) {
        logger.debug("markBroadcasts: batch: " + batch);
        for (Map.Entry<String,List<String>> entry : 
            batch.entrySet()) {
            String location = entry.getKey();
            logger.debug("markBroadcasts: location: " + location);
            List<String> files = entry.getValue();
            for (String file : files) {
                Set<String> contents = broadcasted.get(location);
                assert(! contents.contains(file));
                logger.debug("markBroadcasts: add: " + file);
                contents.add(file);
            }
        }
    }

    /*
    public static boolean broadcasted(String file, String dir) {
        return broadcasted.contains(dir+"/"+file);
    }
    */

    /**
        Check the policy effect of name with respect to policy_file
        @param args {name, policy_file}
    */
    public static void main(String[] args) {
        if (args.length != 2) {
            logger.debug("Incorrect args");
            System.exit(1);
        }

        try {
            String name = args[0];
            File policyFile = new File(args[1]);
            if (! policyFile.exists()) {
                logger.debug("Policy file does not exist: " +
                    args[1]);
            }
            load(policyFile, SwiftConfig.getDefault());
            Policy policy = lookup(name);
            logger.debug(name + ": " + policy);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(2);
        }
    }
}
