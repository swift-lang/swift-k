
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.test;

import java.io.*;

public class HTMLPrintStream extends PrintWriter {
    public HTMLPrintStream(OutputStream os) {
        super(os);
    }

    public HTMLPrintStream text(String text) {
        println(text);
        flush();
        return this;
    }

    public HTMLPrintStream br() {
        return text("<br>");
    }

    public HTMLPrintStream beginTable(int border) {
        return text("<table border=\"" + String.valueOf(border) + "\">");
    }

    public HTMLPrintStream beginTable() {
        return text("<table>");
    }

    public HTMLPrintStream endTable() {
        return text("</table>");
    }

    public HTMLPrintStream beginTableData() {
        return text("<td>");
    }

    public HTMLPrintStream beginTableData(String bgcolor) {
        return text("<td bgcolor=\"" + bgcolor + "\">");
    }

    public HTMLPrintStream endTableData() {
        return text("</td>");
    }

    public HTMLPrintStream tableData(String contents) {
        return text("<td>" + contents + "</td>");
    }

    public HTMLPrintStream tableData(String contents, String bgcolor) {
        return text("<td bgcolor=\"" + bgcolor + "\">" + contents + "</td>");
    }

    public HTMLPrintStream tableData(String contents, String bgcolor, int colspan) {
        return text("<td bgcolor=\"" + bgcolor + "\" colspan=\"" + String.valueOf(colspan) + "\">" + contents + "</td>");
    }

    public HTMLPrintStream link(String href, String name) {
        return text("<a href=\"" + href + "\">" + name + "</a>");
    }

    public HTMLPrintStream link(String href, String anchor, String name) {
        return text("<a href=\"" + href + "#" + anchor + "\">" + name + "</a>");
    }

    public HTMLPrintStream anchor(String name) {
        return text("<a name=\"" + name + "\"></a>");
    }

    public HTMLPrintStream paragraph() {
        return text("<p>");
    }

    public HTMLPrintStream beginHeading(int n) {
        return text("<h" + String.valueOf(n) + ">");
    }

    public HTMLPrintStream endHeading(int n) {
        return text("</h" + String.valueOf(n) + ">");
    }

    public HTMLPrintStream heading(int n, String text) {
        beginHeading(n);
        text(text);
        return endHeading(n);
    }

    public HTMLPrintStream beginPreformatted() {
        return text("<pre>");
    }

    public HTMLPrintStream endPreformatted() {
        return text("</pre>");
    }

    public HTMLPrintStream beginTableRow() {
        return text("<tr>");
    }

    public HTMLPrintStream endTableRow() {
        return text("</tr>");
    }

    public HTMLPrintStream beginHTMLDocument() {
        return text("<html>");
    }

    public HTMLPrintStream endHTMLDocument() {
        return text("</html>");
    }

    public HTMLPrintStream beginHeader() {
        return text("<head>");
    }

    public HTMLPrintStream endHeader() {
        return text("</head>");
    }

    public HTMLPrintStream title(String title) {
        return text("<title>" + title + "</title>");
    }

    public HTMLPrintStream beginBody() {
        return text("<body>");
    }

    public HTMLPrintStream endBody() {
        return text("</body>");
    }
}