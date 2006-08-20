//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

package org.globus.cog.broker.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.StringWriter;

import org.globus.cog.broker.interfaces.ClassAd;

import condor.classad.ClassAdParser;
import condor.classad.ClassAdWriter;
import condor.classad.Constant;
import condor.classad.Expr;
import condor.classad.RecordExpr;

public class ClassAdImpl implements ClassAd {

    private RecordExpr recordExpr = null;

    public ClassAdImpl(File classAdFile) throws FileNotFoundException {
        FileReader reader = new FileReader(classAdFile);
        ClassAdParser parser = new ClassAdParser(reader);
        this.recordExpr = (RecordExpr) parser.parse();

        Expr rank = this.recordExpr.lookup("Rank");
        if (rank == null) {
            // if no rank is  specified in the class ad, default it to 0
            this.recordExpr.insertAttribute("Rank", Constant.getInstance(0));
        }

        Expr requirements = this.recordExpr.lookup("Requirements");
        if (requirements == null) {
            // if no requirements is  specified in the class ad, default it to true
            this.recordExpr.insertAttribute(
                "Requirements",
                Constant.getInstance(true));
        }
    }

    public ClassAdImpl(RecordExpr recordExpr) {
        this.recordExpr = recordExpr;

        Expr rank = this.recordExpr.lookup("Rank");
        if (rank == null) {
            // if no rank is  specified in the class ad, default it to 0
            this.recordExpr.insertAttribute("Rank", Constant.getInstance(0));
        }

        Expr requirements = this.recordExpr.lookup("Requirements");
        if (requirements == null) {
            // if no requirements is  specified in the class ad, default it to true
            this.recordExpr.insertAttribute(
                "Requirements",
                Constant.getInstance(true));
        }
    }

    public void setAd(File classAdFile) throws FileNotFoundException {
        FileReader reader = new FileReader(classAdFile);
        ClassAdParser parser = new ClassAdParser(reader);
        this.recordExpr = (RecordExpr) parser.parse();

        Expr rank = this.recordExpr.lookup("Rank");
        if (rank == null) {
            // if no rank is  specified in the class ad, default it to 0
            this.recordExpr.insertAttribute("Rank", Constant.getInstance(0));
        }

        Expr requirements = this.recordExpr.lookup("Requirements");
        if (requirements == null) {
            // if no requirements is  specified in the class ad, default it to true
            this.recordExpr.insertAttribute(
                "Requirements",
                Constant.getInstance(true));
        }
    }

    public void setAd(RecordExpr recordExpr) {
        this.recordExpr = recordExpr;

        Expr rank = this.recordExpr.lookup("Rank");
        if (rank == null) {
            // if no rank is  specified in the class ad, default it to 0
            this.recordExpr.insertAttribute("Rank", Constant.getInstance(0));
        }

        Expr requirements = this.recordExpr.lookup("Requirements");
        if (requirements == null) {
            // if no requirements is  specified in the class ad, default it to true
            this.recordExpr.insertAttribute(
                "Requirements",
                Constant.getInstance(true));
        }
    }

    public RecordExpr getAd() {
        return this.recordExpr;
    }

    public String toString() {
        StringWriter writer = new StringWriter();
        ClassAdWriter out =
            new ClassAdWriter(writer, ClassAdWriter.NATIVE, true);
        out.setFormatFlags(ClassAdWriter.MULTI_LINE_ADS);
        out.println(this.recordExpr);
        return writer.toString();
    }

    public String toXML() {
        StringWriter writer = new StringWriter();
        ClassAdWriter out = new ClassAdWriter(writer, ClassAdWriter.XML, true);
        out.setFormatFlags(ClassAdWriter.MULTI_LINE_ADS);
        out.println(this.recordExpr);
        return writer.toString();
    }

    public static int[] match(ClassAd classAd1, ClassAd classAd2) {
        return condor.classad.ClassAd.match(classAd1.getAd(), classAd2.getAd());
    }

}
