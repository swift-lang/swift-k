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
import java.util.Iterator;

public class TabbedContainer extends Container {
    public static final int TABS_AT_TOP = 0;
    public static final int TABS_AT_BOTTOM = 1;

    private int activeBgColor, activeFgColor;
    private Tab active;
    private int tabPosition, labelLines;

    public TabbedContainer() {
        activeBgColor = ANSI.CYAN;
        activeFgColor = ANSI.BLACK;
        bgColor = ANSI.BLUE;
        fgColor = ANSI.WHITE;
    }

    public void setTabPosition(int pos) {
        this.tabPosition = pos;
    }

    public int getActiveBgColor() {
        return activeBgColor;
    }

    public void setActiveBgColor(int activeBgColor) {
        this.activeBgColor = activeBgColor;
    }

    public int getActiveFgColor() {
        return activeFgColor;
    }

    public void setActiveFgColor(int activeFgColor) {
        this.activeFgColor = activeFgColor;
    }

    public void addTab(Tab tab) {
        super.add(tab);
        invalidate();
    }

    public void add(Component comp) {
        if (comp instanceof Tab) {
            super.add(comp);
            invalidate();
        }
        else {
            throw new ClassCastException("Component must be a Tab");
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

        int currentLabelPos = 0;
        int currentLine = 0;

        context.lock();
        try {
            context.moveTo(sx, sy);
            context.bgColor(bgColor);
            context.fgColor(fgColor);
            context.spaces(width);
            Iterator<Component> i = components.iterator();
            while (i.hasNext()) {
                Tab c = (Tab) i.next();
                int newLabelPos = currentLabelPos + c.getLabel().getLabelSize() + 3;
                if (newLabelPos > width) {
                    currentLine++;
                    context.spaces(width - currentLabelPos);
                    currentLabelPos = 0;
                }
                if (tabPosition == TABS_AT_TOP) {
                    context.moveTo(sx + currentLabelPos, sy + currentLine);
                }
                else {
                    context.moveTo(sx + currentLabelPos, sy + height - labelLines + currentLine);
                }
                currentLabelPos += c.getLabel().getLabelSize() + 3;
                context.bgColor(ANSI.BLACK);
                context.spaces(1);
                if (c != active) {
                    context.bgColor(bgColor);
                    context.fgColor(fgColor);
                    context.bold(false);
                }
                else {
                    context.bgColor(activeBgColor);
                    context.fgColor(activeFgColor);
                    context.bold(true);
                }
                context.spaces(1);
                c.drawTitle(context);
                context.spaces(1);

                if (i.hasNext()) {
                    context.bgColor(ANSI.BLACK);
                    context.spaces(1);
                }
            }
            context.bgColor(ANSI.BLACK);
            context.spaces(width - currentLabelPos);
            context.bold(false);

            if (active != null) {
                context.bgColor(activeBgColor);
                context.fgColor(activeFgColor);
                active.drawTree(context);
            }
        }
        finally {
            context.unlock();
        }
    }

    protected void validate() {
        if (isValid()) {
            return;
        }
        Iterator<Component> i;
        i = components.iterator();
        int totalLabelWidth = 0;
        while (i.hasNext()) {
            Tab c = (Tab) i.next();
            totalLabelWidth += c.getLabel().getLabelSize() + 3;
        }

        labelLines = (totalLabelWidth - 1) / width + 1;

        i = components.iterator();
        while (i.hasNext()) {
            Tab c = (Tab) i.next();
            if (tabPosition == TABS_AT_TOP) {
                c.setLocation(0, labelLines);
            }
            else {
                c.setLocation(0, 0);
            }
            c.setSize(width, height - labelLines);
        }
        if (active == null && components.size() > 0) {
            active = (Tab) components.get(0);
            active.setVisible(true);
            active.validate();
        }
        setValid(true);
    }

    public boolean childFocused(Component component) {
        boolean f = true;
        if (focused != null) {
            f = focused.unfocus();
            if (f) {
                focused.focusLost();
            }
        }
        if (f) {
            focused = component;
            focused.focusGained();
        }
        return f;
    }

    public void focusGained() {
    }

    public void setActive(Tab tab) {
        if (active != null) {
            active.setVisible(false);
        }
        active = tab;
        if (active != null) {
            active.setVisible(true);
            active.focus();
        }
    }
}
