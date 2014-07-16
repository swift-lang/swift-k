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

import java.io.IOException;

public class Component {
    public static final int BOTTOM_LAYER = 0;
    public static final int NORMAL_LAYER = 1;
    public static final int TOP_LAYER = 2;

    protected int x, y, width, height, bgColor, fgColor, focusedBgColor;
    protected int sx, sy;
    private boolean visible, valid, focus, focusable;
    private Screen screen;
    private Container parent;
    private int layer;

    public Component() {
        visible = true;
        focusable = true;
        focusedBgColor = ANSI.YELLOW;
    }

    protected void redraw() {
        if (isBranchVisible()) {
            Screen scr = getScreen();
            if (scr == null) {
                return;
            }
            ANSIContext ctx = scr.getContext();
            scr.redrawLater();
        }
    }

    protected boolean isBranchVisible() {
        Component parent = getParent();
        if (parent == null) {
            return false;
        }
        else {
            return visible && parent.isBranchVisible();
        }
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        if (this.visible != visible) {
            this.visible = visible;
            if (getScreen() != null) {
                getScreen().redraw();
            }
        }
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int h) {
        this.height = h;
        redraw();
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int w) {
        this.width = w;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public void setLocation(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void setSize(int w, int h) {
        if (this.width != w || this.height != h) {
            valid = false;
        }
        this.width = w;
        this.height = h;
    }

    public int getBgColor() {
        return bgColor;
    }

    public void setBgColor(int bgColor) {
        this.bgColor = bgColor;
    }

    public int getFgColor() {
        return fgColor;
    }

    public void setFgColor(int fgColor) {
        this.fgColor = fgColor;
    }

    public int getFocusedBgColor() {
        return focusedBgColor;
    }

    public void setFocusedBgColor(int focusedBgColor) {
        this.focusedBgColor = focusedBgColor;
    }

    protected void draw(ANSIContext context) throws IOException {

    }

    public Container getParent() {
        return parent;
    }

    public void setParent(Container parent) {
        this.parent = parent;
    }

    public Screen getScreen() {
        if (parent == null) {
            return null;
        }
        else if (screen != null) {
            return screen;
        }
        else {
            return screen = parent.getScreen();
        }
    }

    protected void invalidate() {
        valid = false;
        Component parent = getParent();
        if (parent != null) {
            parent.invalidate();
        }
    }

    protected void drawTree(ANSIContext context) throws IOException {
        validate();
        Component parent = getParent();
        if (parent != null) {
            sx = parent.sx + x;
            sy = parent.sy + y;
        }
        draw(context);
    }

    protected void validate() {
        valid = true;
    }

    protected void setValid(boolean valid) {
        this.valid = valid;
    }

    protected boolean isValid() {
        return valid;
    }

    protected void status(String msg) {
        Screen scr = getScreen();
        if (scr != null) {
            scr.status(msg);
        }
    }

    public boolean hasFocus() {
        return focus;
    }

    public boolean focus() {
        if (!focus) {
            Container parent = getParent();
            if (parent == null) {
                return false;
            }
            if (parent.focus()) {
                focus = parent.childFocused(this);
            }
            redraw();
        }
        return focus;
    }

    public boolean unfocus() {
        if (focus) {
            focus = false;
            Container parent = getParent();
            if (parent != null) {
                parent.childUnfocused(this);
            }
            redraw();
        }
        return true;
    }

    public void focusLost() {
    }

    public void focusGained() {
    }

    public boolean keyboardEvent(Key e) {
        return false;
    }

    public boolean isFocusable() {
        return focusable;
    }

    public void setFocusable(boolean focusable) {
        this.focusable = focusable;
    }

    public void setAbsoluteLocation(int sx, int sy) {
        this.sx = sx;
        this.sy = sy;
    }

    public int getLayer() {
        return layer;
    }

    public void setLayer(int layer) {
        this.layer = layer;
    }

    public boolean focusNext() {
        return false;
    }

    public boolean focusFirst() {
        if (isFocusable()) {
            return focus();
        }
        else {
            return false;
        }
    }
}
