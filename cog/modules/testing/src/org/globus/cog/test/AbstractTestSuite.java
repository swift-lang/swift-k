
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.test;

import org.globus.common.CoGProperties;
import org.globus.gsi.GlobusCredential;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public abstract class AbstractTestSuite implements TestSuiteInterface {

    protected OutputWriter output;
    protected String machinelist, dir, prefix;
    public MachineListParser machines;
    private List tests;
    private int timeout;
    private String name;
    private String serviceName;

    public AbstractTestSuite(String dir, String prefix, String machinelist, int timeout) {
        this.dir = dir;
        this.prefix = prefix;
        this.machinelist = machinelist;
        this.tests = new LinkedList();
        this.timeout = timeout;
        try {
            machines = new MachineListParser(machinelist);
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
        }
        if (!machines.getMachines().hasMoreElements()) {
        	System.out.println("Warning! No machines specified. No tests will be performed");
        }
    }

    protected String getStackTrace(Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        return sw.toString();
    }

    /*Test if the proxy is valid and not expired
        * Returns null if the proxy is ok; an error message otherwise
        */

    public static String testProxy() {
        String proxyFile = CoGProperties.getDefault().getProxyFile();
        GlobusCredential proxy;
        try {
            proxy = new GlobusCredential(proxyFile);
            if (proxy.getTimeLeft() == 0) {
                return "Proxy has expired";
            }
            return null;
        }
        catch (Exception e) {
            return "Cannot load proxy: " + e.getMessage();
        }
    }

    public void tests() {
        LinkedList cols = new LinkedList();
        Iterator i = tests.listIterator();
        while (i.hasNext()) {
            TestInterface test = (TestInterface) i.next();
            cols.add(test.getColumnName());
        }

	output = new OutputWriter(dir, prefix + getName() + "-output.html", prefix + getName() + "-log.html", cols);

	i = tests.listIterator();
	while (i.hasNext()) {
	    TestInterface test = (TestInterface) i.next();
	    test.setOutputWriter(output);
	    test.setMachines(machines);
	}

        String message = testProxy();
        if (message != null) {
            output.printRow(message, "#ff0000");
        }
        else {
            try {
                Enumeration e = machines.getMachines();
                while (e.hasMoreElements()) {
                    String machine = (String) e.nextElement();
		    if (getServiceName() == null) {
			doTests(machine);
		    }
		    else {
			Enumeration f = machines.getServicePorts(machine, getServiceName());
			while (f.hasMoreElements()) {
			    String port = (String) f.nextElement();
			    doTests(machine + ":" + port);
			}
		    }
                }
            }
            catch (Exception e) {
		e.printStackTrace();
            }
        }
        output.close();
    }
    
    private void doTests(String machine) {
	int port = MachineListParser.getPortAsInt(machine);
	String m = machine;
	Iterator i = tests.listIterator();
        while (i.hasNext()) {
            TestInterface test = (TestInterface) i.next();
            try {
		if (test.getServiceName() == null) {
            	    test.test(machine);
        	}
        	else if (machines.hasService(MachineListParser.getHost(machine), test.getServiceName())) {
		    if (!Pinger.ping(machine)) {
			output.printRow("machine down", "#808080");
			return;
		    }
		    m = machine;
		    if (port == 0) {
			String p = machines.getFirstPort(machine, test.getServiceName());
			m = m + ":" + p;
		    }
		    System.out.println(test.getColumnName()+"("+m+")");
		    Wrapper w = new Wrapper(test, m, timeout);
		    if (!w.execute()) {
            	        output.printField("timeout", "#ffaa00");
            	    }
        	}
        	else {
            	    output.printField();
        	}
	    }
	    catch (Exception e) {
		output.printResult(test.getColumnName(), m, Util.getStackTrace(e), false);
	    }
        }
    }

    public void addTest(TestInterface test) {
        tests.add(test);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    public String getServiceName() {
	return serviceName;
    }
    
    public void setServiceName(String serviceName) {
	this.serviceName = serviceName;
    }
}
