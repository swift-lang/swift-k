
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.test;


import java.io.*;
import java.util.*;

/*
* The columns are ";" separated
* The names may include a number range:
*	dumy[0-10] means dummy0, dummy1,... dummy10
* The columns are as follows:
* Machine name; OS version; processor; service running with version; port number (may be empty); location on the local fs
*/

public class MachineListParser {

    Hashtable Machines;
    Vector MachinesList;
    BufferedReader is;

    /*
    * Does NOT work as String.split in 1.4.x
    * The separator is not a regexp
    */
    private static String[] split(String string, String separator) {

        int count = 0;
        for (int last = -1, next = 0; next != -1; next = string.indexOf(separator, last + 1), count++, last = next) {
        }
        String[] tokens = new String[count];
        int lastIndex = -1;
        int nextIndex = 0;
        int token = 0;
        do {
            nextIndex = string.indexOf(separator, lastIndex + 1);
            if (nextIndex == -1) {
                String last;
                if (lastIndex == string.length() - 1) {
                    last = "";
                }
                else {
                    last = string.substring(lastIndex + 1);
                }
                tokens[token++] = last;
            }
            else {
                tokens[token++] = string.substring(lastIndex + 1, nextIndex);
            }
            lastIndex = nextIndex;
        }
        while (lastIndex != -1);

        return tokens;
    }

    public MachineListParser(String filename) throws Exception {
        is = new BufferedReader(new InputStreamReader(new FileInputStream(filename)));
        Machines = new Hashtable();
        MachinesList = new Vector();
        try {
            parse();
        }
        catch (Exception e) {
            throw new Exception("Error in machine list (" + filename + ") " + e.getMessage());
        }
    }

    private void parse() throws Exception {
        String line = null;
        int lineIndex = 0;
        do {
            line = is.readLine();
            lineIndex++;
            if (lineIndex == 1) {
                continue;
            }
            if (line != null) {
                try {
                    parseLine(line);
                }
                catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("Error on line " +
                            String.valueOf(lineIndex) + ": " + line + "\n" + e.getMessage());
                }
            }
        }
        while (line != null);
    }


    private void parseLine(String line) throws Exception {
        String[] tokens = split(line, ";");
        if (tokens.length != 6) {
            throw new Exception("Wrong number of columns.");
        }
        for (int i = 0; i < 6; i++) {
            tokens[i] = tokens[i].trim();
        }
	//validate the host name
	if (tokens[0].length() == 0) {
	    throw new Exception("Invalid hostname:" + tokens[0]);
	} 
        for (int i = 0; i < tokens[0].length(); i++) {
	    char c = tokens[0].charAt(i);
	    if (Character.isLetterOrDigit(c)) continue;
	    if (c == '.') continue;
	    if (c == ':') continue;
	    if (c == '-') continue;
	    if (c == '_') continue;
	    throw new Exception("Invalid hostname:" + tokens[0]);
	}
	if (tokens[0].indexOf('[') != -1) {
            expand(tokens);
        }
        else {
            add(tokens);
        }
    }

    private void expand(String[] tokens) throws Exception {
        String exp = tokens[0];
        int start = exp.indexOf('[');
        int end = exp.indexOf(']');
        int sep = exp.indexOf('-', start);
        if (!((start < sep) && (sep < end))) {
            throw new Exception("Invalid expansion group: " + exp);
        }
        int first = Integer.parseInt(exp.substring(start + 1, sep));
        int last = Integer.parseInt(exp.substring(sep + 1, end));
        String heads = exp.substring(0, start);
        String tail = exp.substring(end + 1);
        for (int i = first; i <= last; i++) {
            tokens[0] = heads + String.valueOf(i) + tail;
            add(tokens);
        }
    }

    private void add(String[] tokens) throws Exception {
        Hashtable services, ports;

        /*
        * Machinedata[0] - hashtable with the services
        * Machinedata[1] - os
        * Machinedata[2] - cpu
        * Machinedata[3] - RAM
        */
        Object[] Machinedata;

        if (Machines.containsKey(tokens[0])) {
            Machinedata = (Object[]) Machines.get(tokens[0]);
            services = (Hashtable) Machinedata[0];
        }
        else {
            Machinedata = new Object[4];
            services = new Hashtable();
            Machinedata[0] = services;
            Machinedata[1] = tokens[1];
            Machinedata[2] = tokens[2];
            Machinedata[3] = tokens[3];
            Machines.put(tokens[0], Machinedata);

            MachinesList.add(tokens[0]);
        }

        String[] service = split(tokens[4], " ");

        /*
        * Servicedata[0] - version
        */
        String[] Servicedata = new String[1];
        if (service.length != 2) {
            Servicedata[0] = "N/A";
        }
        else {
            Servicedata[0] = service[1];
        }
        if (services.containsKey(service[0])) {
            ports = (Hashtable) services.get(service[0]);
        }
        else {
            ports = new Hashtable();
            services.put(service[0], ports);
        }

        //check if it's a valid number
        try {
            Integer.parseInt(tokens[5]);
        }
        catch (Exception e) {
            tokens[5] = "";
        }

        ports.put(tokens[5], Servicedata);
    }

    private Object[] getMachinedata(String machine) {
        if (!Machines.containsKey(machine)) {
	    throw new RuntimeException("Invalid machine name: " + machine);
	}
	return (Object[]) Machines.get(machine);
    }

    private Hashtable getServicesHT(String machine) {
        return (Hashtable) getMachinedata(machine)[0];
    }

    private Hashtable getServicePortsHT(String machine, String service) {
        Hashtable services = getServicesHT(machine);
	if (!services.containsKey(service)) {
	    throw new RuntimeException(machine+" does not have a service named "+service);
	}
	return (Hashtable) services.get(service);
    }

    private String[] getServicedata(String machine, String service, String port) {
        Hashtable serviceports = getServicePortsHT(machine, service);
	if (!serviceports.containsKey(port)) {
	    throw new RuntimeException("No '"+service+"' service on machine '"+machine+"' on port "+port);
	}
	return (String[]) serviceports.get(port);
    }

    public Enumeration getMachines() {
        return MachinesList.elements();
    }

    public Enumeration getServices(String machine) {
        return getServicesHT(machine).keys();
    }

    public Enumeration getServicePorts(String machine, String service) {
        Hashtable ports = getServicePortsHT(machine, service);
        return ports.keys();
    }

    public String getFirstPort(String machine, String service) {
        Hashtable ports = getServicePortsHT(machine, service);
        return (String) ports.keys().nextElement();
    }

    public String getFirstPortWithName(String machine, String service){
        String port = getFirstPort(machine, service);
        if (port.equals("")){
            return machine;
        }
        else{
            return machine + ":" + port;
        }
    }

    public boolean hasService(String machine, String service) {
        return getServicesHT(machine).containsKey(service);
    }

    public Enumeration getMachinesWithService(String service) {
        Enumeration e = Machines.keys();
        Vector v = new Vector();
        while (e.hasMoreElements()) {
            String machine = (String) e.nextElement();
            Hashtable services = getServicesHT(machine);
            if (services.containsKey(service)) {
                v.add(machine);
            }
        }
        return v.elements();
    }

    public String getOS(String machine) {
        Object[] Machinedata = getMachinedata(machine);
        return (String) Machinedata[1];
    }

    public String getCPU(String machine) {
        Object[] Machinedata = getMachinedata(machine);
        return (String) Machinedata[2];

    }

    public String getRAM(String machine) {
        Object[] Machinedata = getMachinedata(machine);
        return (String) Machinedata[3];
    }

    public String getServiceVersion(String machine, String service, String port) {
        String[] Servicedata = getServicedata(machine, service, port);
        return Servicedata[0];
    }

    public static String getMachine(String MP) {
        String[] mp = split(MP, ":");
        return mp[0];
    }

    public static String getHost(String MP) {
        return getMachine(MP);
    }

    public static String getPort(String MP) {
        String[] mp = split(MP, ":");
        if (mp.length == 2) {
            return mp[1];
        }
        else {
            return "";
        }
    }

    public static int getPortAsInt(String MP){
        String sport = getPort(MP);
        if (sport.equals("")){
            return 0;
        }
        else{
            return Integer.parseInt(sport);
        }
    }

    public static String combine(String machine, String port) {
        if (port.equals("")) {
            return machine;
        }
        else {
            return machine + ":" + port;
        }
    }
}
