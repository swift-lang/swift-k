// ----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

package org.globus.cog.abstraction.impl.invocation.ws;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.wsdl.Definition;
import javax.wsdl.Input;
import javax.wsdl.Operation;
import javax.wsdl.Output;
import javax.wsdl.Part;
import javax.wsdl.PortType;
import javax.wsdl.Service;
import javax.xml.namespace.QName;

import org.apache.log4j.Logger;
import org.apache.wsif.WSIFException;
import org.apache.wsif.WSIFMessage;
import org.apache.wsif.WSIFOperation;
import org.apache.wsif.WSIFPort;
import org.apache.wsif.WSIFService;
import org.apache.wsif.WSIFServiceFactory;
import org.apache.wsif.util.WSIFUtils;
import org.globus.cog.abstraction.impl.common.StatusImpl;
import org.globus.cog.abstraction.interfaces.Status;
import org.globus.cog.abstraction.interfaces.Task;
import org.globus.cog.abstraction.interfaces.WSInvocationSpecification;

public class InvocationThread extends Thread {
    static Logger logger = Logger.getLogger(InvocationThread.class.getName());
    private Task task = null;

    public InvocationThread(Task task) {
        this.task = task;
    }

    public void run() {
        try {
            this.task.setStatus(Status.ACTIVE);
            org.globus.cog.abstraction.interfaces.Service service = this.task
                    .getService(0);
            WSInvocationSpecification spec = (WSInvocationSpecification) this.task
                    .getSpecification();
            String method = spec.getMethod();
            String args[] = spec.getArgumentsAsArray();
            HashMap map = invokeMethod(service.getServiceContact().getContact()
                    + "?wsdl", method, null, null, null, "", args, 0);
            String output = null;
            for (Iterator iterator = map.keySet().iterator(); iterator
                    .hasNext();) {
                String name = (String) iterator.next();
                if (output == null) {
                    output = map.get(name) + "\n";
                } else {
                    output += map.get(name) + "\n";
                }
            }
            this.task.setStdOutput(output);
            this.task.setStatus(Status.COMPLETED);
        } catch (Exception e) {
            Status newStatus = new StatusImpl();
            Status oldStatus = this.task.getStatus();
            newStatus.setPrevStatusCode(oldStatus.getStatusCode());
            newStatus.setStatusCode(Status.FAILED);
            newStatus.setException(e);
            this.task.setStatus(newStatus);
        }
    }

    private HashMap invokeMethod(String wsdlLocation, String operationName,
            String inputName, String outputName, String portName,
            String protocol, String[] args, int argShift) throws Exception {
        String serviceNS = null;
        String serviceName = null;
        String portTypeNS = null;
        String portTypeName = null;

        logger.debug("Reading WSDL document from '" + wsdlLocation + "'");
        Definition def = WSIFUtils.readWSDL(null, wsdlLocation);

        Service service = WSIFUtils.selectService(def, serviceNS, serviceName);

        Map portTypes = WSIFUtils.getAllItems(def, "PortType");
        if (portTypes.size() > 1 && portName != null) {
            for (Iterator i = portTypes.keySet().iterator(); i.hasNext();) {
                QName qn = (QName) i.next();
                if (portName.equals(qn.getLocalPart())) {
                    portTypeName = qn.getLocalPart();
                    portTypeNS = qn.getNamespaceURI();
                    break;
                }
            }
        }
        PortType portType = WSIFUtils.selectPortType(def, portTypeNS,
                portTypeName);

        WSIFServiceFactory factory = WSIFServiceFactory.newInstance();
        WSIFService dpf = factory.getService(def, service, portType);
        WSIFPort port = null;
        if (portName == null)
            port = dpf.getPort();
        else
            port = dpf.getPort(portName);

        if (inputName == null && outputName == null) {
            // retrieve list of operations
            List operationList = portType.getOperations();

            // try to find input and output names for the operation specified
            boolean found = false;
            for (Iterator i = operationList.iterator(); i.hasNext();) {
                Operation op = (Operation) i.next();
                String name = op.getName();
                if (!name.equals(operationName)) {
                    continue;
                }
                if (found) {
                    throw new RuntimeException(
                            "Operation '"
                                    + operationName
                                    + "' is overloaded. "
                                    + "Please specify the operation in the form "
                                    + "'operationName:inputMessageName:outputMesssageName' to distinguish it");
                }
                found = true;
                Input opInput = op.getInput();
                inputName = (opInput.getName() == null) ? null : opInput
                        .getName();
                Output opOutput = op.getOutput();
                outputName = (opOutput.getName() == null) ? null : opOutput
                        .getName();
            }
        }

        WSIFOperation operation = port.createOperation(operationName,
                inputName, outputName);
        WSIFMessage input = operation.createInputMessage();
        WSIFMessage output = operation.createOutputMessage();
        WSIFMessage fault = operation.createFaultMessage();

        // retrieve list of names and types for input and names for output
        List operationList = portType.getOperations();

        // find portType operation to prepare in/out message w/ parts
        boolean found = false;
        String[] outNames = new String[0];
        Class[] outTypes = new Class[0];
        for (Iterator i = operationList.iterator(); i.hasNext();) {
            Operation op = (Operation) i.next();
            String name = op.getName();
            if (!name.equals(operationName)) {
                continue;
            }
            if (found) {
                throw new RuntimeException(
                        "overloaded operations are not supported");
            }
            found = true;
            Input opInput = op.getInput();
            String[] inNames = new String[0];
            Class[] inTypes = new Class[0];
            if (opInput != null) {
                List parts = opInput.getMessage().getOrderedParts(null);
                unWrapIfWrappedDocLit(parts, name, def);
                int count = parts.size();
                inNames = new String[count];
                inTypes = new Class[count];
                retrieveSignature(parts, inNames, inTypes);
            }
            for (int pos = 0; pos < inNames.length; ++pos) {
                String arg = args[pos + argShift];
                Object value = null;
                Class c = inTypes[pos];
                if (c.equals(String.class)) {
                    value = arg;
                } else if (c.equals(Double.TYPE)) {
                    value = new Double(arg);
                } else if (c.equals(Float.TYPE)) {
                    value = new Float(arg);
                } else if (c.equals(Integer.TYPE)) {
                    value = new Integer(arg);
                } else if (c.equals(Boolean.TYPE)) {
                    value = new Boolean(arg);
                } else {
                    throw new RuntimeException("Cannot convert '" + arg
                            + "' to " + c);
                }

                input.setObjectPart(inNames[pos], value);
            }

            Output opOutput = op.getOutput();
            if (opOutput != null) {
                List parts = opOutput.getMessage().getOrderedParts(null);
                unWrapIfWrappedDocLit(parts, name + "Response", def);
                int count = parts.size();
                outNames = new String[count];
                outTypes = new Class[count];
                retrieveSignature(parts, outNames, outTypes);
            }

        }
        if (!found) {
            throw new RuntimeException("no operation " + operationName
                    + " was found in port type " + portType.getQName());
        }

        logger.debug("Executing operation " + operationName);
        operation.executeRequestResponseOperation(input, output, fault);

        HashMap map = new HashMap();
        for (int pos = 0; pos < outNames.length; ++pos) {
            String name = outNames[pos];
            map.put(name, output.getObjectPart(name));
        }

        return map;
    }

    private void retrieveSignature(List parts, String[] names, Class[] types) {
        // get parts in correct order
        for (int i = 0; i < names.length; ++i) {
            Part part = (Part) parts.get(i);
            names[i] = part.getName();
            QName partType = part.getTypeName();
            if (partType == null) {
                partType = part.getElementName();
            }
            if (partType == null) {
                throw new RuntimeException("part " + names[i]
                        + " must have type name declared");
            }
            // only limited number of types is supported
            // cheerfully ignoring schema namespace ...
            String s = partType.getLocalPart();
            if ("string".equals(s)) {
                types[i] = String.class;
            } else if ("double".equals(s)) {
                types[i] = Integer.TYPE;
            } else if ("float".equals(s)) {
                types[i] = Float.TYPE;
            } else if ("int".equals(s)) {
                types[i] = Integer.TYPE;
            } else if ("boolean".equals(s)) {
                types[i] = Boolean.TYPE;
            } else {
                throw new RuntimeException("part type " + partType
                        + " not supported in this sample");
            }
        }
    }

    private void unWrapIfWrappedDocLit(List parts, String operationName,
            Definition def) throws WSIFException {
        Part p = WSIFUtils.getWrappedDocLiteralPart(parts, operationName);
        if (p != null) {
            List unWrappedParts = WSIFUtils.unWrapPart(p, def);
            parts.remove(p);
            parts.addAll(unWrappedParts);
        }
    }

}