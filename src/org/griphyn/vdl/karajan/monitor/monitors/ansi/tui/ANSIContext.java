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
 * Created on Jan 31, 2007
 */
package org.griphyn.vdl.karajan.monitor.monitors.ansi.tui;

import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.LinkedList;
import java.util.List;

import jline.Terminal;
import jline.UnixTerminal;

import org.apache.log4j.Logger;

public class ANSIContext {
    public static final Logger logger = Logger.getLogger(ANSIContext.class);

    private OutputStreamWriter os;
    private InputStream is;
    private Screen screen;
    private ScreenBuffer buf;
    private boolean doubleBuffered;
    private int lock;
    private boolean done, unicode, initialized;

    private Terminal terminal;
    private boolean redraw;
    private UnicodeDrawingScheme uds;
    private Dialog errorDialog;
    private TextArea errorTA;

    public ANSIContext(OutputStream os, InputStream is) {
        unicode = "true".equals(System.getProperty("tui.use.unicode"));
        try {
            this.os = new OutputStreamWriter(os, "UTF8");
        }
        catch (UnsupportedEncodingException e) {
            logger.warn("UTF8 not supported here");
            this.os = new OutputStreamWriter(os);
            unicode = false;
        }
        this.is = is;
        doubleBuffered = true;
        if (unicode) {
            uds = new UnicodeDrawingScheme.RoundedLight();
        }
        else {
            uds = new UnicodeDrawingScheme.ASCII();
        }
    }

    public void moveTo(int x, int y) throws IOException {
        if (doubleBuffered) {
            buf.moveTo(x, y);
        }
        else {
            os.write(ANSI.moveTo(x, y));
        }
    }

    public void clear() throws IOException {
        if (doubleBuffered) {
            buf.clear();
        }
        else {
            os.write(ANSI.clear());
        }
    }

    public void fgColor(int color) throws IOException {
        if (doubleBuffered) {
            buf.fgColor(color);
        }
        else {
            os.write(ANSI.fgColor(color));
        }
    }

    public void bgColor(int color) throws IOException {
        if (doubleBuffered) {
            buf.bgColor(color);
        }
        else {
            os.write(ANSI.bgColor(color));
        }
    }

    public void underline(boolean underline) throws IOException {
        if (doubleBuffered) {
            buf.underline(underline);
        }
        else {
            os.write(ANSI.underline(underline));
        }
    }

    public void bold(boolean bold) throws IOException {
        if (doubleBuffered) {
            buf.bold(bold);
        }
        else {
            os.write(ANSI.bold(bold));
        }
    }

    public void text(String text) throws IOException {
        if (doubleBuffered) {
            buf.write(text);
        }
        else {
            os.write(text);
        }
    }

    public void printReply() throws IOException {
        try {
            Thread.sleep(100);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
        while (is.available() > 0) {
            int c = is.read();
            System.err.println(((char) c) + " - " + c);
        }
    }

    public boolean init() {
        logger.info("Initializing terminal");
        terminal = Terminal.setupTerminal();
        try {
            terminal.initializeTerminal();
            if (!this.vt100CodesSupported()) {
                return false;
            }
            os.write(ANSI.cursorVisible(false));
            os.flush();
            Runtime.getRuntime().addShutdownHook(new Thread() {
                public void run() {
                    exit();
                }
            });
            initialized = true;
            return true;
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean isInitialized() {
        return initialized;
    }



    private boolean querySizeWorks = true, alternate = false;

    public int[] querySize() throws IOException {
        if (!querySizeWorks) {
            return null;
        }
        if (alternate) {
            return new int[] {terminal.getTerminalWidth(), terminal.getTerminalHeight() };
        }
        os.write(ANSI.AESC + "18t");
        os.flush();
        try {
            expect(ANSI.AESC, 250);
            List<Integer> nums = readNums();
            if (nums == null || nums.size() < 2) {
                if (buf != null) {
                    return new int[] { buf.getWidth(), buf.getHeight() };
                }
                else {
                    return new int[] { 80, 24 };
                }
            }
            if (nums.size() == 3) {
                nums.remove(0);
            }
            int[] sz = new int[2];
            sz[0] = nums.get(1).intValue();
            sz[1] = nums.get(0).intValue();
            if (logger.isDebugEnabled()) {
                logger.debug("Terminal size is " + sz[0] + "x" + sz[1]);
            }
            return sz;
        }
        catch (Exception e) {
            logger.info("Could not query terminal size", e);
            if (terminal.getTerminalWidth() != 0) {
                alternate = true;
                return new int[] {terminal.getTerminalWidth(), terminal.getTerminalHeight() };
            }
            querySizeWorks = false;
            return null;
        }
    }
    
    public boolean vt100CodesSupported() throws IOException {
        os.write(ANSI.AESC + "c");
        os.flush();
        String reply = readReply(100);
        logger.debug("Terminal status code: " + reply);
        return reply.length() > 0;
    }

    protected void expect(char c) throws IOException {
        if (is.read() != c) {
            throw new IOException("Error communicating with terminal emulator");
        }
    }

    protected void expect(String what) throws IOException {
        for (int i = 0; i < what.length(); i++) {
            expect(what.charAt(i));
        }
    }
    
    protected String readReply(int wait) throws IOException {
        StringBuilder sb = new StringBuilder();
        while (wait > 0 && is.available() == 0) {
            try {
                Thread.sleep(1);
                wait--;
            }
            catch (InterruptedException e) {
                throw new IOException("Interrupted");
            }
        }
        while (is.available() > 0) {
            sb.append((char) is.read());
        }
        return sb.toString();
    }

    protected void expect(String what, int wait) throws IOException {
        while (wait > 0 && is.available() == 0) {
            try {
                Thread.sleep(1);
                wait--;
            }
            catch (InterruptedException e) {
                throw new IOException("Interrupted");
            }
        }
        if (is.available() == 0) {
            throw new UnsupportedOperationException("No reply");
        }
        for (int i = 0; i < what.length(); i++) {
            expect(what.charAt(i));
        }
    }

    protected int readNum(char end) throws IOException {
        StringBuffer sb = new StringBuffer();
        int c;
        do {
            c = is.read();
            if (c == end) {
                return Integer.parseInt(sb.toString());
            }
            else {
                sb.append((char) c);
            }
        } while (true);
    }

    protected List<Integer> readNums() throws IOException {
        List<Integer> nums = new LinkedList<Integer>();
        StringBuffer sb = new StringBuffer();
        int c;
        do {
            c = is.read();
            if (!Character.isDigit((char) c)) {
                try {
                    nums.add(new Integer(sb.toString()));
                }
                catch (NumberFormatException e) {
                    return null;
                }
                sb = new StringBuffer();
                if (c != ';') {
                    return nums;
                }
            }
            else {
                sb.append((char) c);
            }
        } while (true);
    }

    public void sync() throws IOException {
        if (doubleBuffered) {
            buf.sync();
        }
        else {
            os.flush();
        }
    }

    int count = 10;

    private void checkSize() throws IOException {
        count--;
        if (count == 0) {
            count = 10;
            int[] size = querySize();
            if (size != null
                    && (size[0] != buf.getWidth() || size[1] != buf.getHeight())) {
                buf.resize(size[0], size[1]);
                screen.setSize(size[0], size[1]);
                screen.invalidate();
            }
        }
    }

    public void reset() throws IOException {
        fgColor(ANSI.DEFAULT);
        bgColor(ANSI.DEFAULT);
        clear();
        sync();
    }

    public void run() throws IOException {
        while (!done) {
            try {
                Key key;
                int c = read();
                if (c == 27) {
                    c = read();
                    if (c == 27) {
                        key = new Key(0, 27);
                    }
                    else if (c >= '1' && c <= '5') {
                        key = new Key(0, Key.F1 + c - '1');
                    }
                    else if (c >= '6' && c <= '9') {
                        key = new Key(0, Key.F6 + c - '6');
                    }
                    else if (c == '[') {
                        int c0 = read();
                        if (c0 <= 56) {
                            int c1 = read();
                            if (c1 == '~') {
                                key = new Key(0, Key.KEYPAD2 + c0);
                            }
                            else {
                                int c2 = read();
                                if (c2 == '~') {
                                    key = new Key(c0, c1, 0);
                                }
                                else {
                                    key = new Key(c0, c1, is.read());
                                    read();
                                }
                            }
                        }
                        else {
                            key = new Key(0, Key.KEYPAD + c0);
                        }
                    }
                    else if (c == 'O') {
                        // OS X F1 - F4
                        int c0 = read();
                        key = new Key(0, Key.F1 + (c0 - 'P'));
                    }
                    else {
                        key = new Key(Key.MOD_ALT, c);
                    }
                }
                else if (c < 32 && c != 0x0a && c != 0x0d) {
                    if (c == 9) {
                        key = new Key(Key.TAB);
                    }
                    else {
                        key = new Key(Key.MOD_CTRL, c + 96);
                    }
                }
                else if (c > 128) {
                    // XTerm
                    key = new Key(Key.MOD_ALT, c - 128);
                }
                else {
                    key = new Key(0, c);
                }

                if (key != null) {
                    if (screen != null) {
                        if (doubleBuffered) {
                            // buf.invalidate();
                        }
                        screen.keyboardEvent(key);
                        if (screen != null) {
                            screen.redraw();
                        }
                    }
                    //screen.status(key.toString());
                }
            }
            catch (Exception e) {
                if (!done) {
                    logger.info("Rendering exception", e);
                    displayErrorDialog(e);
                }
            }
        }
    }

    private void displayErrorDialog(Exception e) {
        CharArrayWriter cr = new CharArrayWriter();
        PrintWriter wr = new PrintWriter(cr);
        e.printStackTrace(wr);
        
        synchronized(this) {
            if (errorDialog == null) {
                createErrorDialog();
            }
            if (errorTA.getText() == null) {
                errorTA.setText(cr.toString());
            }
            else {
                errorTA.setText(errorTA.getText() + "\n" + cr.toString());
            }
        }
    }

    private void createErrorDialog() {
        Dialog d = new Dialog();
        d.setFgColor(ANSI.RED);
        errorDialog = d;
        d.setTitle("Error");
        d.setSize(getScreen().getWidth() * 3 / 4, getScreen().getHeight() * 3 / 4);
        errorTA = new TextArea();
        d.add(errorTA);
        errorTA.setFgColor(ANSI.RED);
        errorTA.setLocation(1, 1);
        errorTA.setSize(d.getWidth() - 2, d.getHeight() - 3);
        errorTA.setScrollBarVisible(true);
        Button close = new Button("Close");
        d.add(close);
        close.setLocation((d.getWidth() - close.getWidth()) / 2, d.getHeight() - 1);
        close.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(Component source) {
                synchronized(ANSIContext.this) {
                    errorDialog.close();
                    errorDialog = null;
                }
            }  
        });
        
        d.center(getScreen());
        d.display(getScreen());
    }

    private int read() throws IOException {
        while (is.available() == 0) {
            if (redraw) {
                redraw = false;
                screen.redraw();
            }
            else {
                if (done) {
                    return 0;
                }
                try {
                    checkSize();
                    Thread.sleep(10);
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        int c = is.read();
        //logger.warn("CONSOLE IN: " + c + " - " + (char) c);
        return c;
    }

    public void spaces(int count) throws IOException {
        if (doubleBuffered) {
            buf.spaces(count);
        }
        else {
            for (int i = 0; i < count; i++) {
                os.write(' ');
            }
        }
    }

    public char getChar(int x, int y) {
        if (doubleBuffered) {
            buf.moveTo(x, y);
            return buf.getChar();
        }
        else {
            return '?';
        }
    }

    public void putChar(char c) throws IOException {
        if (!unicode) {
            if (c < 32 || c >= 240) {
                c = '.';
            }
        }
        if (doubleBuffered) {
            buf.write(c);
        }
        else {
            os.write(c);
        }
    }

    public void putChar(int c) throws IOException {
        if (!unicode) {
            if (c < 32 || c >= 240) {
                c = '.';
            }
        }
        if (doubleBuffered) {
            buf.write(c);
        }
        else {
            os.write(c);
        }
    }

    public void println(String string) throws IOException {
        if (doubleBuffered) {
            buf.writeln(string);
        }
        else {
            os.write(string);
            os.write('\n');
        }
    }

    public void frame(int x, int y, int width, int height) throws IOException {
        moveTo(x, y);
        lineArt(ANSI.GCH_UL_CORNER);
        for (int i = 0; i < width - 2; i++) {
            lineArt(ANSI.GCH_H_LINE);
        }
        lineArt(ANSI.GCH_UR_CORNER);
        for (int i = 0; i < height - 2; i++) {
            moveTo(x, y + i + 1);
            lineArt(ANSI.GCH_V_LINE);
            moveTo(x + width - 1, y + i + 1);
            lineArt(ANSI.GCH_V_LINE);
        }
        moveTo(x, y + height - 1);
        lineArt(ANSI.GCH_LL_CORNER);
        for (int i = 0; i < width - 2; i++) {
            lineArt(ANSI.GCH_H_LINE);
        }
        lineArt(ANSI.GCH_LR_CORNER);
    }

    public void filledFrame(int x, int y, int width, int height)
            throws IOException {
        moveTo(x, y);
        lineArt(ANSI.GCH_UL_CORNER);
        for (int i = 0; i < width - 2; i++) {
            lineArt(ANSI.GCH_H_LINE);
        }
        lineArt(ANSI.GCH_UR_CORNER);
        for (int i = 0; i < height - 2; i++) {
            moveTo(x, y + i + 1);
            lineArt(ANSI.GCH_V_LINE);
            spaces(width - 2);
            lineArt(ANSI.GCH_V_LINE);
        }
        moveTo(x, y + height - 1);
        lineArt(ANSI.GCH_LL_CORNER);
        for (int i = 0; i < width - 2; i++) {
            lineArt(ANSI.GCH_H_LINE);
        }
        lineArt(ANSI.GCH_LR_CORNER);
    }
    
    public void lineArt(int code) throws IOException {
        if (unicode) {
            putChar(uds.getChar(code));
        }
        else {
            lineArt(true);
            putChar(uds.getChar(code));
            lineArt(false);
        }
    }
    
    public void lineArt(boolean enabled) throws IOException {
        if (!unicode) {
            if (doubleBuffered) {
                buf.lineArt(enabled);
            }
            else {
                os.write(ANSI.lineArt(enabled));
            }
        }
    }

    public void filledRect(int x, int y, int width, int height)
            throws IOException {
        moveTo(x, y);
        for (int i = 0; i < height; i++) {
            moveTo(x, y + i);
            spaces(width);
        }
    }

    public Screen getScreen() {
        return screen;
    }

    public void setScreen(Screen screen) {
        this.screen = screen;
        if (doubleBuffered) {
            this.buf = new ScreenBuffer(this, screen.getWidth(), screen
                .getHeight());
        }
    }

    public void echo(boolean b) throws IOException {
        if (b) {
            terminal.enableEcho();
        }
        else {
            terminal.disableEcho();
        }
    }

    public OutputStreamWriter getOutputStream() {
        return os;
    }

    public synchronized void lock() {
        if (!doubleBuffered) {
            while (lock > 0) {
                try {
                    wait();
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            lock++;
        }
    }

    public synchronized void unlock() {
        if (!doubleBuffered) {
            lock--;
            if (lock == 0) {
                notifyAll();
            }
        }
    }

    public void exit() {
        done = true;
        try {
            Screen scr = getScreen();
            if (screen != null) {
                moveTo(0, getScreen().getHeight() - 1);
            }
            os.write(ANSI.cursorVisible(true));
            os.flush();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        screen = null;
        if (terminal instanceof UnixTerminal) {
            try {
                ((UnixTerminal) terminal).restoreTerminal();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        terminal.enableEcho();
    }

    public void redrawLater() {
        this.redraw = true;
    }

    public boolean isDoubleBuffered() {
        return doubleBuffered;
    }

    public void setDoubleBuffered(boolean doubleBuffered) {
        this.doubleBuffered = doubleBuffered;
    }

    public void setCursorVisible(boolean b) throws IOException {
        os.write(ANSI.cursorVisible(b));
        os.flush();
    }
}
