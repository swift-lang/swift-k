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

//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Dec 30, 2008
 */
package org.globus.cog.karajan.parser;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.util.LinkedList;
import java.util.Stack;


public final class NativeParser {
    public static final boolean DEBUG = false;

    private final Stack<WrapperNode> stack;
    private final SimpleLexer lex;
    private final String name;
    private int currentPriority;

    public NativeParser(String name, Reader source) throws IOException {
        this.name = name;
        this.stack = new Stack<WrapperNode>();
        lex = new SimpleLexer(source);
    }

    private void node(WrapperNode node) {
        stack.push(node);
    }

    private void node(String type) {
        WrapperNode n = new WrapperNode();
        n.setNodeType(type);
        n.setProperty(WrapperNode.LINE, lex.getLineNumber());
        stack.push(n);
    }

    private void node(String type, String text) {
        WrapperNode n = new WrapperNode();
        n.setNodeType(type);
        n.setProperty(WrapperNode.TEXT, text);
        n.setProperty(WrapperNode.LINE, lex.getLineNumber());
        stack.push(n);
    }
    
    private void node(String type, int count) {
    	node(type, count, lex.getLineNumber());
    }

    private void node(String type, int count, int line) {
        LinkedList<WrapperNode> l = new LinkedList<WrapperNode>();
        WrapperNode n = new WrapperNode();
        n.setNodeType(type);
        for (int i = 0; i < count; i++) {
            l.addFirst(stack.pop());
        }
        n.setNodes(l);
        n.setProperty(WrapperNode.LINE, line);
        stack.push(n);
    }

    private void startMarker() {
        stack.push(null);
    }

    private void swap() {
        WrapperNode n1 = stack.pop();
        WrapperNode n2 = stack.pop();
        stack.push(n1);
        stack.push(n2);
    }

    private void rot() {
        WrapperNode n1 = stack.pop();
        WrapperNode n2 = stack.pop();
        WrapperNode n3 = stack.pop();
        stack.push(n1);
        stack.push(n3);
        stack.push(n2);
    }

    private void addChild() {
        WrapperNode c = stack.pop();
        WrapperNode p = stack.peek();
        p.addNode(c);
    }

    private void extractChild() {
        WrapperNode c = stack.peek();
        WrapperNode n = c.removeNode(c.nodeCount() - 1);
        stack.push(n);
    }

    private void wrapInBlock() {
        WrapperNode c = stack.pop();
        WrapperNode p = stack.peek();
        if (!c.getNodeType().equals("k:block")) {
            WrapperNode b = this.newNode("k:block");
            b.addNode(c);
            c = b;
        }
        p.addNode(c);
    }

    private void start(String type) {
        WrapperNode n = new WrapperNode();
        n.setNodeType(type);
        n.setProperty(WrapperNode.LINE, lex.getLineNumber());
        stack.push(n);
        startMarker();
    }

    private void end() {
        LinkedList<WrapperNode> l = new LinkedList<WrapperNode>();
        WrapperNode n = stack.pop();
        while (n != null) {
            l.addFirst(n);
            n = stack.pop();
        }
        WrapperNode p = stack.peek();
        p.setNodes(l);
    }

    private void end(String custom) {
        LinkedList<WrapperNode> l = new LinkedList<WrapperNode>();
        WrapperNode n = stack.pop();
        while (n != null) {
            l.addFirst(n);
            n = stack.pop();
        }
        WrapperNode p = new WrapperNode();
        p.setNodeType(custom);
        p.setNodes(l);
        p.setProperty(WrapperNode.LINE, lex.getLineNumber());
        stack.push(p);
    }

    private WrapperNode newNode(String type) {
        WrapperNode n = new WrapperNode();
        n.setNodeType(type);
        n.setProperty(WrapperNode.LINE, lex.getLineNumber());
        return n;
    }

    private String popText() {
        WrapperNode n = stack.pop();
        return (String) n.getProperty(WrapperNode.TEXT);
    }
    
    private String peekText() {
        WrapperNode n = stack.peek();
        return (String) n.getProperty(WrapperNode.TEXT);
    }

    private String peekType() {
        return stack.peek().getNodeType();
    }

    private String spaces(final int count) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            sb.append(' ');
        }
        return sb.toString();
    }

    private ParsingException buildException(String message) {
        return new ParsingException(name + ", line " + lex.getLineNumber()
                + " column " + lex.getColumn() + "\n" + lex.currentLine()
                + "\n" + spaces(lex.getColumn() - 1) + "^\n" + message
                + "\nStack: " + stack);
    }

    private boolean eof() {
        return lex.peekChar() == SimpleLexer.EOF;
    }

    private boolean priorityCheck(final int p) {
        return p > currentPriority;
    }

    private int resetPriority(int value) {
        int ps = currentPriority;
        currentPriority = value;
        return ps;
    }

    private void skipWhitespace() {
        while (lex.isWhitespace()) {
            lex.skipChar();
        }
    }

    private void skipHWhitespace() {
        char c = lex.peekChar();
        while (c == ' ' || c == '\t') {
            lex.nextChar();
            c = lex.peekChar();
        }
    }

    private boolean parenAfterVWhitespace() {
        Object mark = lex.mark();
        char c = lex.peekChar();
        boolean any = false;
        while (c == '\n') {
            lex.nextChar();
            c = lex.peekChar();
            any = true;
        }
        if (any) {
            if (lex.peekChar() == '(') {
                lex.reset(mark);
                return true;
            }
            lex.reset(mark);
        }
        return false;
    }

    private void skipToNewLine() {
        char c = lex.nextChar();
        while (c != '\n' && c != '\r' && c != 0) {
            c = lex.nextChar();
        }
    }

    private void skipTo(String str) {
        int index = 0;
        Object mark = null;
        while (true) {
            char c = lex.peekChar();
            if (c == str.charAt(index)) {
                if (index == 0) {
                    mark = lex.mark();
                }
                index++;
                if (index == str.length()) {
                    lex.reset(mark);
                    return;
                }
            }
            else {
                index = 0;
            }
            lex.nextChar();
        }
    }

    private boolean isVWhitespace() {
        char c = lex.peekChar();
        if (c == '\n' || c == '\r') {
            lex.nextChar();
            return true;
        }
        else {
            return false;
        }
    }

    private void skip(char c) throws ParsingException {
        char l = lex.nextChar();
        if (c != l) {
            throw buildException("Expected " + c);
        }
    }

    private void skip(String str) throws ParsingException {
        for (int i = 0; i < str.length(); i++) {
            char l = lex.peekChar();
            if (str.charAt(i) != l) {
                throw buildException("Expected " + str);
            }
            lex.nextChar();
        }
    }

    private boolean match(char c) {
        char l = lex.peekChar();
        if (c == l) {
            lex.nextChar();
            return true;
        }
        else {
            return false;
        }
    }

    public WrapperNode parse() throws ParsingException {
        start("k:main");
        expressionList();
        end();
        stack.peek().setProperty(WrapperNode.FILENAME, name);
        WrapperNode n = stack.pop();
        dumpTree(n, new File(name).getName() + ".parse.tree");
        return n;
    }

    public static void dumpTree(WrapperNode n, String name) {
        if (DEBUG) {
            try {
                BufferedWriter br = new BufferedWriter(new FileWriter(name));
                writeNode(br, n, 0);
                br.close();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void writeNode(BufferedWriter br, WrapperNode n, int l)
            throws IOException {
        for (int i = 0; i < l; i++) {
            br.write("  ");
        }
        br.write(n.getNodeType());
        if (n.getProperty(WrapperNode.TEXT) != null) {
            br.write("(" + n.getProperty(WrapperNode.TEXT) + ")");
        }
        br.write('\n');
        for (WrapperNode s : n.nodes()) {
            writeNode(br, s, l + 1);
        }
    }

    private void expressionList() throws ParsingException {
        while (!eof()) {
            skipWhitespace();
            if (!eof()) {
                currentPriority = 10;
                expression();
                if (!eof()) {
                    itemSeparator();
                }
            }
        }
    }

    private void expression() throws ParsingException {
        switch (lex.peekChar()) {
        	case '.':
        		vargs();
        		break;
            case '/':
                comment();
                return;
            case '[':
                list();
                break;
            case '\'':
                quotedIdentifier();
                break;
            case '{':
                block();
                break;
            case '(':
                seq();
                maybeBinaryOperator();
                break;
            case '+':
                unaryOperator("+", "+");
                break;
            case '-':
                unaryOperator("-", "neg");
                break;
            case '!':
                unaryOperator("!", "!");
                break;
            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
                numericLiteral();
                maybeBinaryOperator();
                break;
            case '"':
                stringLiteral();
                maybeBinaryOperator();
                break;
            default:
                if (firstIdentifierChar()) {
                    identifier();
                    action();
                }
                else {
                    throw buildException("Expected expression or separator but got '"
                            + lex.peekToken() + "'");
                }
        }
        skipHWhitespace();
    }

    private void signedNumericLiteral() throws ParsingException {
        char sign = lex.nextChar();
        numericLiteral();
        node("k:num", sign + popText());
    }
    
    private void vargs() throws ParsingException {
    	skip("...");
        node("k:var", "...");
    }

    private void signedIntegerLiteral() {
        char sign = lex.nextChar();
        integerLiteral();
        node("k:num", sign + popText());
    }

    private void integerLiteral() {
        node("k:num", digits());
    }
    
    private static class Var {
        public final String name;
        
        public Var(String name) {
            this.name = name;
        }
    }

    private void stringLiteral() throws ParsingException {
        skip("\"");
        LinkedList<Object> l = null;
        boolean inVar = false;
        StringBuilder sb = new StringBuilder();
        out:
        while (true) {
            char c = lex.peekChar();
            switch (c) {
                case '\\':
                    char c2 = lex.peekNextChar();
                    switch (c2) {
                        case 'n':
                            sb.append('\n');
                            break;
                        case 't':
                            sb.append('\t');
                            break;
                        case 'f':
                        	sb.append('\f');
                        	break;
                        case 'r':
                        	sb.append('\r');
                        	break;
                        case 'b':
                        	sb.append('\b');
                        	break;
                        case '\\':
                        	sb.append('\\');
                        	break;
                        case '"':
                            sb.append('"');
                            break;
                        case '{':
                            sb.append('{');
                            break;
                        default:
                            throw new ParsingException("Unrecognized escape sequence '\\" + c2 + "'");
                    }
                    lex.skipChar();
                    break;
                case '{':
                    if (inVar) {
                        throw new ParsingException("Invalid character '{' in variable name");
                    }
                    if (l == null) {
                        l = new LinkedList<Object>();
                    }
                    l.add(sb.toString());
                    sb = new StringBuilder();
                    inVar = true;
                    break;
                case '}':
                    if (inVar) {
                        l.add(new Var(sb.toString()));
                        sb = new StringBuilder();
                        inVar = false;
                    }
                    else {
                        sb.append(c);
                    }
                    break;
                case '"':
                	lex.skipChar();
                    break out;
                case SimpleLexer.EOF:
                	throw new ParsingException("Unexpected end of file while scanning string literal");
                default:
                    sb.append(c);
            }
            lex.skipChar();
        }
        if (l == null) {
            node("k:str", sb.toString());
        }
        else {
        	if (sb.length() > 0) {
        		l.add(sb.toString());
        	}
        	for (Object o : l) {
        	    if (o instanceof String) {
        	        node("k:str", (String) o);
        	    }
        	    else {
        	        node("k:var", ((Var) o).name);
        	    }
        	}
        	node("k:concat", l.size());
        }
    }

	private void numericLiteral() throws ParsingException {
        if (lex.peekChar() == '-' || lex.peekChar() == '+') {
            signedIntegerLiteral();
        }
        else {
            integerLiteral();
        }
        if (lex.peekChar() == '.') {
            fractionalPart();
        }
        if (lex.peekChar() == 'e') {
            exponent();
        }
    }

    private void exponent() throws ParsingException {
        skip("e");
        char c = lex.peekChar();
        if (c == '-' || c == '+') {
            signedIntegerLiteral();
        }
        else {
            integerLiteral();
        }
        String exp = popText();
        node("k:num", popText() + "e" + exp);
    }

    private void fractionalPart() throws ParsingException {
        skip(".");
        node("k:num", popText() + "." + digits());
    }

    private String digits() {
        Object mark = lex.mark();
        while (Character.isDigit(lex.peekChar())) {
            lex.nextChar();
        }
        return lex.region(mark);
    }
    
    private void maybeBinaryOperator() throws ParsingException {
    	Object mark = lex.mark();
        skipHWhitespace();
        switch (lex.peekChar()) {
            case '=':
                if (lex.peekNextChar() == '=') {
                    binaryOperator("==", "==");
                }
                else {
                    throw new ParsingException("Unexpected character: '" + lex.peekChar() + "'");
                }
                break;
            case ':':
            	colonOp();
            	break;
            case '<':
                less();
                break;
            case '>':
                greater();
                break;
            case '|':
                binaryOperator("|", "|", 6);
                break;
            case '&':
                binaryOperator("&", "&", 5);
                break;
            case '-':
                binaryOperator("-", "-", 2);
                break;
            case '+':
                binaryOperator("+", "+", 2);
                break;
            case '*':
                binaryOperator("*", "*", 1);
                break;
            case '/':
                char c = lex.peekNextChar();
                if (c == '/' || c == '*') {
                    return;
                }
                else {
                    binaryOperator("/", "/", 1);
                }
                break;
            case '%':
                binaryOperator("%", "%", 1);
                break;
            default:
                lex.reset(mark);
        }
    }

    private boolean action() throws ParsingException {
        Object mark = lex.mark();
        skipWhitespace();
        switch (lex.peekChar()) {
            case '(':
                invocation();
                break;
            case '{':
                invocationWithBlock();
                break;
            case ':':
                colonOp();
                break;
            case '!':
                negationOp();
                break;
            case '=':
                if (lex.peekNextChar() == '=') {
                    binaryOperator("==", "==");
                }
                else {
                    named();
                }
                break;
            case '<':
                less();
                break;
            case '>':
                greater();
                break;
            case '|':
                binaryOperator("|", "|", 6);
                break;
            case '&':
                binaryOperator("&", "&", 5);
                break;
            case '-':
                binaryOperator("-", "-", 2);
                break;
            case '+':
                binaryOperator("+", "+", 2);
                break;
            case '*':
                binaryOperator("*", "*", 1);
                break;
            case '/':
                char c = lex.peekNextChar();
                if (c == '/' || c == '*') {
                	lex.reset(mark);
                    return false;
                }
                else {
                    binaryOperator("/", "/", 1);
                }
                break;
            case '%':
                binaryOperator("%", "%", 1);
                break;
            default:
                lex.reset(mark);
                return false;
        }
        return true;
    }

    private void less() throws ParsingException {
        switch (lex.peekNextChar()) {
            case '=':
                binaryOperator("<=", "<=", 3);
                break;
            default:
                binaryOperator("<", "<", 3);
        }
    }

    private void greater() throws ParsingException {
        switch (lex.peekNextChar()) {
            case '=':
                binaryOperator(">=", ">=", 3);
                break;
            default:
                binaryOperator(">", ">", 3);
        }
    }

    private void colonOp() throws ParsingException {
        switch (lex.peekNextChar()) {
            case '=':
                binaryOperator(":=", "k:assign", 7);
                break;
            default:
            	throw buildException("Unexpected operator: ':" + lex.peekNextChar() + "'");
        }
    }

    private void negationOp() throws ParsingException {
        switch (lex.peekNextChar()) {
            case '=':
                binaryOperator("!=", "!=", 4);
                break;
            default:
                throw buildException("Unexpected operator: !");
        }
    }

    private void maybeComment() throws ParsingException {
        if (lex.peekChar() == '/') {
            switch (lex.peekNextChar()) {
                case '/':
                    skip("//");
                    skipToNewLine();
                    skipHWhitespace();
                    break;
                case '*':
                    skip("/*");
                    skipTo("*/");
                    skip("*/");
                    skipWhitespace();
                    break;
                default:
                    throw buildException("Unexpected character: "
                            + lex.peekNextChar());
            }
            maybeComment();
        }
    }

    private void comment() throws ParsingException {
        switch (lex.peekNextChar()) {
            case '/':
                skip("//");
                skipToNewLine();
                break;
            case '*':
                skip("/*");
                skipTo("*/");
                skip("*/");
                break;
            default:
                throw buildException("Unexpected character: "
                        + lex.peekNextChar());
        }
        if (lex.peekChar() == SimpleLexer.EOF) {
            return;
        }
        skipWhitespace();
        expression();
    }

    private void binaryOperator(String op, String fn) throws ParsingException {
        binaryOperator(op, fn, 10);
    }

    private void binaryOperator(String op, String fn, final int priority)
            throws ParsingException {
        if (priorityCheck(priority)) {
            return;
        }
        int line = lex.getLineNumber();
        skip(op);
        skipWhitespace();
        int ps = resetPriority(priority);
        expression();
        resetPriority(ps);
        node(fn, 2, line);
        skipHWhitespace();
        maybeBinaryOperator();
    }

    private void binaryLOperator(String op, String fn, final int priority)
            throws ParsingException {
        if (priorityCheck(priority)) {
            return;
        }
        skip(op);
        skipWhitespace();
        int ps = resetPriority(priority);
        identifier();
        node(fn, 2);
        resetPriority(ps);
        skipHWhitespace();
        action();
    }

    private void unaryOperator(String op, String fn) throws ParsingException {
        skip(op);
        skipWhitespace();
        expression();
        node(fn, 1);
    }

    private void named() throws ParsingException {
        binaryOperator("=", "k:named", 8);
    }

    private void invocation() throws ParsingException {
        if (priorityCheck(1)) {
            return;
        }
        if (peekType().equals("k:var")) {
            String type = popText();
            startMarker();
            pairedContainer('(', ')', type);
            scanBlock();
        }
        else {
            startMarker();
            swap();
            pairedContainer('(', ')', "k:apply");
        }
        skipHWhitespace();
        if (parenAfterVWhitespace()) {
            // chained invocation disambiguation
            return;
        }
        else {
            action();
        }
    }
    
    private void invocationWithBlock() throws ParsingException {
        if (priorityCheck(1)) {
            return;
        }
        if (peekType().equals("k:var")) {
            stack.peek().setNodeType(peekText());
            startMarker();
            pairedContainer('{', '}', "k:block");
            addChild();
            scanBlock();
        }
        skipHWhitespace();
        if (parenAfterVWhitespace()) {
            // chained invocation disambiguation
            return;
        }
        else {
            action();
        }
    }

    private void scanBlock() throws ParsingException {
        Object mark = lex.mark();
        skipWhitespace();
        if (lex.peekChar() == '{') {
            block();
            addChild();
        }
        else {
            lex.reset(mark);
        }
        scanElse();
    }

    private void scanElse() throws ParsingException {
        Object mark = lex.mark();
        skipWhitespace();
        if ("else".equals(lex.peekToken())) {
            _else();
            wrapInBlock();
            scanElse();
        }
        else {
            lex.reset(mark);
        }
    }

    private void identifier() {
        Object mark = lex.mark();
        while (identifierChar()) {
            lex.nextChar();
        }
        node("k:var", lex.region(mark));
    }

    private boolean firstIdentifierChar() {
        char c = lex.peekChar();
        return Character.isLetter(c) || c == '_' || c == '$' || c == '#'
                || c == '?';
    }

    private boolean identifierChar() {
        char c = lex.peekChar();
        // hack to deal with namespace separators
        if (c == ':') {
        	if (identifierChar(lex.peekNextChar())) {
        		return true;
        	}
        	else {
        		return false;
        	}
        }
        else {
        	return identifierChar(c);
        }
        
    }

    private boolean identifierChar(char c) {
		switch (c) {
            case '.':
            case '_':
            case '$':
            case '#':
            case '?':
            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
                return true;
            default:
                return Character.isLetter(c);
        }
	}

	private void _else() throws ParsingException {
        skip("else");
        skipWhitespace();
        expression();
    }

    private void seq() throws ParsingException {
        startMarker();
        pairedContainer('(', ')', "k:sequential");
    }

    private void block() throws ParsingException {
        startMarker();
        pairedContainer('{', '}', "k:block");
    }

    private void quotedIdentifier() throws ParsingException {
        skip("'");
        Object mark = lex.mark();
        skipTo("'");
        node("k:var", lex.region(mark));
        skip("'");
    }

    private boolean itemSeparator() throws ParsingException {
    	maybeComment();
        skipHWhitespace();
        if (match(',')) {
            skipWhitespace();
            return true;
        }
        else if (isVWhitespace()) {
            skipWhitespace();
            return true;
        }
        else {
            return false;
        }
    }

    private void list() throws ParsingException {
        startMarker();
        pairedContainer('[', ']', "k:slist");
    }

    private void pairedContainer(char start, char end, String name)
            throws ParsingException {
        int ps = resetPriority(10);
        int line = lex.getLineNumber();
        skip(start);
        skipWhitespace();
        maybeComment();
        
        while (!match(end)) {
            expression();
            maybeComment();
            if (!itemSeparator()) {
                skip(end);
                break;
            }
        }
        end(name);
        stack.peek().setProperty(WrapperNode.LINE, line);
        resetPriority(ps);
    }
}
