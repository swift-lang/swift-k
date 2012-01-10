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


package org.griphyn.vdl.mapping;

import java.util.ArrayList;
import java.util.List;

import org.griphyn.vdl.mapping.Path.Entry;

public class PathParser {
    private String str;
    private int crt = 0;
    
    public PathParser(String str) {
        this.str = str;
    }
    
    /*
     *  path = emptyPath | nonEmptyPath
     *  emptyPath = <nothing> | '$'
     *  nonEmptyPath = element['.' field|indexElement]*
     *  element = field | indexElement
     *  indexElement = '[' index ']'
     *  index = number | '"' string '"' | wildcard
     *  wildcard = '*'
     *  # TODO: this would need to be modified to include complex objects if
     *  # supported as array keys
     *  number = [+|-]('.' digit+)|(digit+['.' digit+])
     *  digit = '0' - '9'
     *  string = (character | escapeSequence)*
     *  escapeSequence = '\' ('\' | '"' | 'n' | 't')
     *  id = idFirstChar [idChar]
     *  idFirstChar = 'a' - 'z' | 'A' - 'Z' | '_'
     *  idChar = idFirstChar | digit 
     */
    public Path parse() {
        return path();
    }

    private Path path() {
        if (str.length() == 0 || str.equals("$")) {
            return Path.EMPTY_PATH;
        }
        else {
            return nonEmptyPath();
        }
    }

    private Path nonEmptyPath() {
        List<Path.Entry> l = new ArrayList<Path.Entry>();
        l.add(element());
        while (!done()) {
            if (matchChar('[')) {
                l.add(arrayElement());
            }
            else {
                expect('.');
                l.add(field());
            }
        }
        return new Path(l);
    }
    
    private Entry element() {
       if (matchChar('[')) {
           return arrayElement();
       }
       else {
           return field();
       }
    }
    
    private Entry field() {
        return new Path.Entry(id(), false);
    }

    private Entry arrayElement() {
        Path.Entry index = index();
        expect(']');
        return index;
    }

    private Comparable<?> id() {
        int start = crt;
        expectIdFirstChar();
        
        while (idChar()) {
            crt++;
        }
        return str.substring(start, crt);
    }

    private boolean idChar() {
        if (crt >= str.length()) {
            return false;
        }
        char c = str.charAt(crt);
        return isLetter(c) || c == '_' || isDigit(c);
    }

    private void expectIdFirstChar() {
        checkCrt();
        char c = str.charAt(crt);
        if (isLetter(c) || (c == '_')) {
            crt++;
        }
        else {
            throw invalidPath("Unexpected character '" + c + "'");
        }
    }
    
    private boolean isLetter(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
    }
    
    private boolean isDigit(char c) {
        return (c >= '0' && c <= '9');
    }

    private Path.Entry index() {
        if (matchChar('"')) {
            String s = string();
            expect('"');
            return new Path.Entry(s, true);
        }
        else if (matchChar('*')) {
            return new Path.Entry.Wildcard();
        }
        else {
            return new Path.Entry(number(), true);
        }
    }

    private String string() {
        StringBuilder sb = new StringBuilder();
        while (!matchChar('"')) {
            checkCrt();
            char c = str.charAt(crt++);
            if (c == '\\') {
                checkCrt();
                c = str.charAt(crt++);
                switch(c) {
                    case '"':
                        c = '"';
                        break;
                    case '\\':
                        c = '\\';
                        break;
                    case 'n':
                        c = '\n';
                        break;
                    case 't':
                        c = '\t';
                        break;
                    default:
                        throw invalidPath("Illegal escape sequence '\\" + c + "'"); 
                }
            }
            sb.append(c);
        }
        crt--;
        return sb.toString();
    }

    /*
     * number = [+|-]('.' digit+)|(digit+['.' digit+])
     */
    private Comparable<?> number() {
        checkCrt();
        int start = crt;
        skip('-');
        skip('+');
        boolean fp = false;
        if (matchChar('.')) {
            fp = true;
            digits();
        }
        else {
            digits();
            if (matchChar('.')) {
                fp = true;
                digits();
            }
        }
        if (fp) {
            return Double.parseDouble(str.substring(start, crt));
        }
        else {
            return Integer.parseInt(str.substring(start, crt));
        }
    }

    private void digits() {
        checkCrt();
        char c = str.charAt(crt++);
        if (!isDigit(c)) {
            throw invalidPath("Expected digit but got '" + c + "'");
        }
        while (crt < str.length() && isDigit(str.charAt(crt))) {
            crt++;
        }
    }

    private void expect(char c) {
        checkCrt();
        if (!matchChar(c)) {
            throw invalidPath("Expected '" + c + "'");
        }
    }

    private boolean matchChar(char c) {
        if (crt < str.length() && str.charAt(crt) == c) {
            crt++;
            return true;
        }
        else {
            return false;
        }
    }
    
    private void skip(char c) {
        if (crt < str.length() && str.charAt(crt) == c) {
            crt++;
        }
    }

    private void checkCrt() {
        if (crt >= str.length()) {
            throw new PathParsingException("Premature end of path");
        }
    }

    private boolean done() {
        return crt >= str.length();
    }
    
    private PathParsingException invalidPath(String msg) {
        return new PathParsingException(msg + " in \"" + str + "\" col " + (crt + 1));
    }
    
    private static int failed, total;
    
    public static void main(String[] args) {
        testCorrect("a");
        testCorrect("a.b");
        testCorrect("a[0]");
        testCorrect("a[1.0]");
        testCorrect("a.b.c");
        testCorrect("a[\"string\"].c");
        testCorrect("a[123].b[\"123\"].d");
        testCorrect("[\"str\\\"rts\"]");
        testCorrect("x[-.23]");
        testCorrect("[*]");
        testInvalid("b.-");
        testInvalid("a[1");
        testInvalid("a[\"aa]");
        testInvalid("[\"invalid\\escape\"]");
        System.out.println("Total tests: " + total + ", failed tests: " + failed);
    }

    private static void testInvalid(String p) {
        total++;
        try {
            System.out.print(p + ":");
            System.out.print(spaces(32 - p.length()));
            System.out.println("FAILED: " + new PathParser(p).parse().stringForm());
            failed++;
        }
        catch (PathParsingException e) {
            System.out.println("OK: " + e.getMessage());
        }
    }

    private static void testCorrect(String p) {
        total++;
        try {
            System.out.print(p + ":");
            System.out.print(spaces(32 - p.length()));
            Path pp = new PathParser(p).parse();
            if (pp.equals(new PathParser(pp.stringForm()).parse())) {
                System.out.println("OK: " + pp.stringForm());
            }
            else {
                System.out.println("FAILED2: " + pp.stringForm());
            }
        }
        catch (PathParsingException e) {
            System.out.println("FAILED: " + e.getMessage());
            failed++;
        }
    }
    
    private static String spaces(int count) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            sb.append(' ');
        }
        return sb.toString();
    }
}
