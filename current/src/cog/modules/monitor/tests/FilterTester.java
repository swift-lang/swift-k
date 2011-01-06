package org.globus.cog.monitor.guss;
import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.Assert;
import junit.framework.TestSuite;
import java.util.Date;

public class FilterTester extends TestCase {

    public FilterTester(String name) {
	super(name);
    }

    public void testToStringAndBack() {
	Filter originalFilter = new DateFilter(DateFilter.BEFORE, new Date(2004,10,31));
	String stringifiedFilter = originalFilter.toString();
	Filter newFilter = Filter.newFilterFromString(stringifiedFilter);
	Assert.assertTrue(originalFilter.equals(newFilter));
    }

    public void testAcceptReject() {
	Filter originalFilter = new DateFilter(DateFilter.BEFORE, new Date(2004,10,31));
	GFTPRecord aRecord = new GFTPRecord(new Date(2004,10,30), new Date(2004, 10, 30), "foobar.baz.edu", GFTPRecord.STOR, 54290);
	GFTPRecord bRecord = new GFTPRecord(new Date(2004,11,1), new Date(2004, 11, 1), "foobar.baz.edu", GFTPRecord.STOR, 54290);
	
	Assert.assertTrue(originalFilter.passes(aRecord));
	Assert.assertFalse(originalFilter.passes(bRecord));
    }

}
