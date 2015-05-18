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

/*
 * Created on Apr 19, 2015
 */
package org.griphyn.vdl.toolkit;

import java.io.InputStream;

import org.apache.log4j.Logger;
import org.globus.swift.parser.SwiftScriptLexer;
import org.globus.swift.parser.SwiftScriptParser;
import org.globus.swift.parsetree.Program;

public class SwiftParser {
    private static final Logger logger = Logger.getLogger(SwiftParser.class);
        
    public static Program parse(InputStream in) throws ParsingException {
        try {
            SwiftScriptLexer lexer = new SwiftScriptLexer(in);
            SwiftScriptParser parser = new SwiftScriptParser(lexer);
            parser.setSwiftLexer(lexer);
            Program code = parser.program();
            return code;
        } 
        catch(Exception e) {
            logger.error("Could not compile SwiftScript source: "+e);
            logger.debug("Full parser exception",e);
            throw new ParsingException("Could not compile SwiftScript source: " + e.getMessage(), e);
        }
    }

    static public class ParsingException extends Exception {
        public ParsingException() {
            super();
        }

        public ParsingException(String message, Throwable cause) {
            super(message, cause);
        }

        public ParsingException(String message) {
            super(message); 
        }

        public ParsingException(Throwable cause) {
            super(cause);
        }
    }
}
