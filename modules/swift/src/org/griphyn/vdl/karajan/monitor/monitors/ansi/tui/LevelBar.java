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
    private float value, other;
    private String text;
    private int textColor, otherColor;
    
    public LevelBar() {
        bgColor = ANSI.BLACK;
        fgColor = ANSI.RED;
        textColor = ANSI.WHITE;
        otherColor = ANSI.MAGENTA;
    }

    public float getValue() {
        return value;
    }

    public void setValue(float value) {
        this.value = value;
    }
    
    public void setOtherValue(float ov) {
        this.other = ov;
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
        int crt;
        crt = spaces(context, 0, other * width, otherColor);
        crt = spaces(context, crt, (value - other) * width, fgColor);
        crt = spaces(context, crt, width, bgColor);
    }
    
    private int spaces(ANSIContext context, int pos, float count, int color) throws IOException {
        int c = (int) count;
        context.bgColor(color);
        context.spaces(Math.min(c, width - pos));
        return pos + c;
    }
    
    private void drawWithText(ANSIContext context) throws IOException {
        int textPos = (width - text.length()) / 2;
        StringBuilder sb = new StringBuilder();
        spaces(sb, textPos);
        sb.append(text);
        spaces(sb, width - textPos - text.length());
        String s = sb.toString();
        
        context.moveTo(sx, sy);
        context.fgColor(textColor);
        
        int crt;
        crt = text(context, s, 0, other * width, otherColor);
        crt = text(context, s, crt, (value - other) * width, fgColor);
        crt = text(context, s, crt, width, bgColor);
    }
    
    private int text(ANSIContext context, String str, int pos, float count, int color) throws IOException {
        int c = (int) count;
        context.bgColor(color);
        for (int i = 0; i < c && pos + i < str.length(); i++) {
            context.putChar(str.charAt(pos + i));
        }
        return pos + c;
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
