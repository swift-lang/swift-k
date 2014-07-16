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
 * Created on Feb 22, 2007
 */
package org.griphyn.vdl.karajan.monitor.monitors.ansi.tui;

import java.util.Iterator;

public class Dialog extends Frame {
    private Component sfocus;

    public Dialog() {
        bgColor = ANSI.WHITE;
        fgColor = ANSI.BLACK;
        setFilled(true);
    }

    public void display(Screen screen) {
        sfocus = screen.getFocusedComponent();
        screen.add(this);
        focusFirst();
    }

    public void close() {
        getScreen().remove(this);
        setVisible(false);
        if (sfocus != null) {
            sfocus.focus();
        }
    }

    public void center(Container c) {
        x = (c.getWidth() - width) / 2;
        y = (c.getHeight() - height) / 2;
    }

    public boolean keyboardEvent(Key key) {
        if (key.getKey() == Key.ESC) {
            close();
            return true;
        }
        else {
            return super.keyboardEvent(key);
        }
    }

    public static int displaySimpleDialog(Screen screen, String title,
            String msg, String[] buttons) {
        final Dialog d = new Dialog();
        d.setTitle(title);
        Label l = new Label();
        l.setText(msg);
        d.setSize(10 + msg.length(), 6);
        l.setLocation(5, 2);
        l.setSize(msg.length(), 1);
        l.setBgColor(d.getBgColor());
        l.setFgColor(d.getFgColor());
        d.add(l);
        final Button[] bs = new Button[buttons.length];
        final int[] r = new int[1];
        for (int i = 0; i < buttons.length; i++) {
            bs[i] = new Button();
            bs[i].setLabel(buttons[i]);
            bs[i].setLocation((d.getWidth() - 10) * i / 3 + 5, 4);
            bs[i].addActionListener(new ActionListener() {
                public void actionPerformed(Component source) {
                    for (int i = 0; i < bs.length; i++) {
                        if (source == bs[i]) {
                            r[0] = i;
                        }
                    }
                    d.close();
                }
            });
            d.add(bs[i]);
        }
        d.center(screen);
        d.display(screen);
        bs[0].focus();
        while (d.isVisible()) {
            try {
                Thread.sleep(50);
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return r[0];
    }

    public boolean focusNext() {
        if (focused == null) {
            return focusFirst();
        }
        else if (focused.focusNext()) {
            return true;
        }
        Iterator<Component> i = components.iterator();
        while (i.hasNext()) {
            if (i.next() == focused) {
                while (i.hasNext()) {
                    Component comp = i.next();
                    if (comp.focusFirst()) {
                        return true;
                    }
                }
            }
        }
        return focusFirst();
    }
}
