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
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

package org.globus.cog.util;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;

import org.apache.log4j.Logger;

/**
 * Utility class to read a stream and report when a pattern is found
 * @author wozniak
 */
public class StreamProcessor extends Streamer {

    public static final Logger logger =
        Logger.getLogger(StreamProcessor.class);

    /**
       Object to notify when pattern is found
     */
    Object object;

    String pattern;

    boolean matched = false;

    public StreamProcessor(InputStream istream, OutputStream ostream,
                           Object object, String pattern) {
        super(istream, ostream);
        this.object = object;
        this.pattern = pattern;

        setName("StreamProcessor["+pattern+"]");
        logger.debug(getName());
    }

    @Override
    public void run() {
        status = Status.ACTIVE;
        matched = false;

        BufferedReader reader =
            new BufferedReader(new InputStreamReader(istream));
        PrintStream writer =
            new PrintStream(new BufferedOutputStream(ostream));

        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                logger.debug("read: " + line);
                if (line.contains(pattern)) {
                    writer.flush();
                    matched = true;
                    synchronized(object) {
                        object.notify();
                        break;
                    }
                }
                else
                    writer.println(line);
            }
            writer.flush();
        }
        catch (IOException e) {
            status = Status.FAILED;
            e.printStackTrace();
        }
        status = Status.COMPLETED;
    }

    public boolean matched() {
        return matched;
    }
}
