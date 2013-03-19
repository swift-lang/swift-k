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


package org.griphyn.vdl.karajan.monitor.monitors.ansi.tui;

import java.io.IOException;

public class LevelBar extends Component {
    private float value;
    private String text;
    private int textColor;
    
    public LevelBar() {
        bgColor = ANSI.BLACK;
        fgColor = ANSI.RED;
        textColor = ANSI.WHITE;
    }

    public float getValue() {
        return value;
    }

    public void setValue(float value) {
        this.value = value;
    }

    protected void draw(ANSIContext context) throws IOException {
        if (text == null) {
            drawWithoutText(context);
        }
        else {
            drawWithText(context);
        }
    }

    private void drawWithoutText(ANSIContext context) throws IOException {
        context.moveTo(sx, sy);
        context.bgColor(fgColor);
        int c = (int) (value * width);
        context.spaces(c);
        context.bgColor(bgColor);
        context.spaces(width - c);
    }
    
    private void drawWithText(ANSIContext context) throws IOException {
        int textPos = (width - text.length()) / 2;
        StringBuilder sb = new StringBuilder();
        spaces(sb, textPos);
        sb.append(text);
        spaces(sb, width - textPos - text.length());
        String s = sb.toString();
        
        context.moveTo(sx, sy);
        context.bgColor(fgColor);
        context.fgColor(textColor);
        int c = (int) (value * width);
        context.text(s.substring(0, c));
        context.bgColor(bgColor);
        context.text(s.substring(c));
    }
    
     private void spaces(StringBuilder sb, int count) {
        for(int i = 0; i < count; i++) {
            sb.append(' ');
        }
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getTextColor() {
        return textColor;
    }

    public void setTextColor(int textColor) {
        this.textColor = textColor;
    }
}
