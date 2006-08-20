// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.broker.interfaces;

import java.io.File;
import java.io.FileNotFoundException;

import condor.classad.RecordExpr;

public interface ClassAd {

    public void setAd(File classAdFile) throws FileNotFoundException;
    public void setAd(RecordExpr recordExpr);
    public RecordExpr getAd();
    public String toXML();
}
