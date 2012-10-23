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



package org.globus.swift.data.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

public class LineReader {

    public LineReader()
    {}

    public static List<String> read(File file) 
    throws FileNotFoundException {
        BufferedReader reader =
            new BufferedReader(new FileReader(file));
        return read(reader);
    }

    public static List<String> read(String s) {
        BufferedReader reader =
            new BufferedReader(new StringReader(s));
        return read(reader);
    }

    public static List<String> read(BufferedReader reader) {
        List<String> result = new ArrayList<String>();
        try
        {
            String prevline = "";
            String line = "";
            while ((line = reader.readLine()) != null) {
                int hash = line.indexOf("#");
                if (hash >= 0)
                    line = line.substring(0,hash);
                line = (prevline + " " + line).trim();
                if (line.endsWith("\\")) {
                    line = line.substring(0, line.length()-2);
                    prevline = line;
                    continue;
                }
                else {
                    prevline = "";
                    line = line.trim();
                    if (line.length() > 0)
                        result.add(line);
                }
            }
            reader.close();
        }
        catch (IOException e)
        {
            System.out.println("LineReader: I/O problem.");
            return null;
        }
        return result;
    }

    public static String[] tokenize(String line) {
        if (line == null)
            return null;
        List<String> words = new ArrayList<String>();
        String[] ws = line.split("\\s");
        for (int i = 0; i < ws.length; i++)
            if (ws[i].length() > 0)
                words.add(ws[i]);
        String[] result = new String[words.size()];
        for (int i = 0; i < words.size(); i++)
            result[i] = words.get(i);
        return result;
    }
}
