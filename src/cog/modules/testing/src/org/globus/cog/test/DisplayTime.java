
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.test;

import java.util.Calendar;

public class DisplayTime extends AbstractTest{
    public void test(String machine) {
        Calendar cal = Calendar.getInstance();
        String dateString = "";
        dateString = dateString.concat(String.valueOf(cal.get(Calendar.HOUR)));
        dateString = dateString.concat(":");
        dateString = dateString.concat(String.valueOf(cal.get(Calendar.MINUTE)));
        dateString = dateString.concat(":");
        dateString = dateString.concat(String.valueOf(cal.get(Calendar.SECOND)));
        output.printField(dateString);
    }

    public String getServiceName() {
        return null;
    }

    public String getColumnName() {
        return "Time";
    }

}
