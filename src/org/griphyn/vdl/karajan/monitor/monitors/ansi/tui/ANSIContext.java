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
    private boolean done;

    private Terminal terminal;
    private boolean redraw;

    public ANSIContext(OutputStream os, InputStream is) {
        this.os = new OutputStreamWriter(os);
        this.is = is;
        doubleBuffered = true;
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

    public void lineArt(boolean enabled) throws IOException {
        if (doubleBuffered) {
            buf.lineArt(enabled);
        }
        else {
            os.write(ANSI.lineArt(enabled));
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
        terminal = Terminal.setupTerminal();
        try {
            terminal.initializeTerminal();
            os.write(ANSI.cursorVisible(false));
            os.flush();
            Runtime.getRuntime().addShutdownHook(new Thread() {
                public void run() {
                    exit();
                }
            });
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return terminal.isANSISupported();
    }

    public int[] querySize() throws IOException {
        os.write(ANSI.AESC + "18t");
        os.flush();
        try {
            expect(ANSI.AESC, 250);
            List nums = readNums();
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
            sz[0] = ((Integer) nums.get(1)).intValue();
            sz[1] = ((Integer) nums.get(0)).intValue();
            return sz;
        }
        catch (UnsupportedOperationException e) {
            return null;
        }
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

    protected void expect(String what, int wait) throws IOException {
        if (wait > 0) {
            try {
                Thread.sleep(wait);
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

    protected List readNums() throws IOException {
        List nums = new LinkedList();
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
                        try {
                            if (doubleBuffered) {
                                // buf.invalidate();
                            }
                            screen.keyboardEvent(key);
                            if (screen != null) {
                                screen.redraw();
                            }
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    //screen.status(key.toString());
                }
            }
            catch (Exception e) {
                if (!done) {
                    logger.warn("Rendering exception", e);
                    moveTo(1, 1);
                    bgColor(ANSI.RED);
                    fgColor(ANSI.WHITE);
                    CharArrayWriter cr = new CharArrayWriter();
                    PrintWriter wr = new PrintWriter(cr);
                    e.printStackTrace(wr);
                    text(cr.toString());
                    sync();
                }
            }
        }
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
        if (c < 32 || c >= 240) {
            c = '.';
        }
        if (doubleBuffered) {
            buf.write(c);
        }
        else {
            os.write(c);
        }
    }

    public void putChar(int c) throws IOException {
        if (c < 32 || c >= 240) {
            c = '.';
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
        lineArt(true);
        moveTo(x, y);
        putChar(ANSI.GCH_UL_CORNER);
        for (int i = 0; i < width - 2; i++) {
            putChar(ANSI.GCH_H_LINE);
        }
        putChar(ANSI.GCH_UR_CORNER);
        for (int i = 0; i < height - 2; i++) {
            moveTo(x, y + i + 1);
            putChar(ANSI.GCH_V_LINE);
            moveTo(x + width - 1, y + i + 1);
            putChar(ANSI.GCH_V_LINE);
        }
        moveTo(x, y + height - 1);
        putChar(ANSI.GCH_LL_CORNER);
        for (int i = 0; i < width - 2; i++) {
            putChar(ANSI.GCH_H_LINE);
        }
        putChar(ANSI.GCH_LR_CORNER);
        lineArt(false);
    }

    public void filledFrame(int x, int y, int width, int height)
            throws IOException {
        lineArt(true);
        moveTo(x, y);
        putChar(ANSI.GCH_UL_CORNER);
        for (int i = 0; i < width - 2; i++) {
            putChar(ANSI.GCH_H_LINE);
        }
        putChar(ANSI.GCH_UR_CORNER);
        for (int i = 0; i < height - 2; i++) {
            moveTo(x, y + i + 1);
            putChar(ANSI.GCH_V_LINE);
            spaces(width - 2);
            putChar(ANSI.GCH_V_LINE);
        }
        moveTo(x, y + height - 1);
        putChar(ANSI.GCH_LL_CORNER);
        for (int i = 0; i < width - 2; i++) {
            putChar(ANSI.GCH_H_LINE);
        }
        putChar(ANSI.GCH_LR_CORNER);
        lineArt(false);
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
