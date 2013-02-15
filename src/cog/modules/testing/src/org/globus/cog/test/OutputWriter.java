
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.test;

import java.util.List;
import java.util.Iterator;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;

public class OutputWriter {
    private HTMLPrintStream out, log;
    private String logname;
    private int anchorCount;
    private List columns;
    private int crtCol;

    public OutputWriter(String dir, String outname, String logname, List columns){
        try {
            this.out = new HTMLPrintStream(new FileOutputStream(dir+outname));
            this.log = new HTMLPrintStream(new FileOutputStream(dir+logname));
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
            System.exit(102);
        }

        this.logname = logname;
        this.columns = columns;
        anchorCount = 0;
        log.beginHTMLDocument().beginHeader().title("Test log").endHeader().beginBody();
        out.beginTable(1);
        out.beginTableRow();
        Iterator i = columns.listIterator();
        while (i.hasNext()){
            String h = (String) i.next();
            out.beginTableData();
            out.link("help.html", h, h);
            out.endTableData();
        }
        out.endTableRow();
        crtCol = 0;
        out.beginTableRow();
    }

    private void nextColumn(){
        crtCol++;
        if (crtCol == columns.size()){
            out.endTableRow().beginTableRow();
            crtCol = 0;
        }
    }

    private void log(String test, String machine, String output, String passed){
        anchorCount++;
        log.paragraph().anchor(String.valueOf(anchorCount));
        log.beginHeading(2).link("help.html", test, test).text(" test "+passed+" on " + machine).endHeading(2);
        log.beginPreformatted();
        log.println(output);
        log.endPreformatted();
    }

    public void printResult(String test, String machine, String output, boolean ok){
        if (ok){
            log(test, machine, output, "passed");
            out.beginTableData("#33ff33");
            out.link(logname, String.valueOf(anchorCount), "Ok");
            out.endTableData();
        }
        else{
            log(test, machine, output, "failed");
            out.beginTableData("#ff0000");
            out.link(logname, String.valueOf(anchorCount), "Failed");
            out.endTableData();
        }
        nextColumn();
    }

    public void printResult(String test, String machine, String output, String ok){
        log(test, machine, output, "results");
        out.beginTableData("#33ff33");
        out.link(logname, String.valueOf(anchorCount), ok);
        out.endTableData();
        nextColumn();
    }

    public void printField(String s) {
        out.tableData(s);
        nextColumn();
    }

    public void printField(String s, String color){
        out.tableData(s, color);
        nextColumn();
    }

    public void printField() {
        out.tableData("N/A", "#aaaaaa");
        nextColumn();
    }

    public void printRow(String message, String color){
        int colspan = columns.size() - crtCol;
        out.tableData(message, color, colspan);
        crtCol = 0;
        out.endTableRow();
        out.beginTableRow();
    }

    public void close(){
        out.endTableRow().endTable();
        out.close();
        log.endBody().endHTMLDocument();
    }
}
