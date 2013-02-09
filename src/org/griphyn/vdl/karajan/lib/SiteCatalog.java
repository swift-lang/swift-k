//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jan 7, 2013
 */
package org.griphyn.vdl.karajan.lib;

import k.rt.ExecutionException;
import k.rt.Stack;

import org.globus.cog.abstraction.impl.common.AbstractionFactory;
import org.globus.cog.abstraction.impl.common.ProviderMethodException;
import org.globus.cog.abstraction.impl.common.task.ExecutionServiceImpl;
import org.globus.cog.abstraction.impl.common.task.InvalidProviderException;
import org.globus.cog.abstraction.impl.common.task.ServiceContactImpl;
import org.globus.cog.abstraction.impl.common.task.ServiceImpl;
import org.globus.cog.abstraction.interfaces.Service;
import org.globus.cog.abstraction.interfaces.ServiceContact;
import org.globus.cog.karajan.analyzer.ArgRef;
import org.globus.cog.karajan.analyzer.Param;
import org.globus.cog.karajan.compiled.nodes.functions.AbstractSingleValuedFunction;
import org.globus.cog.karajan.util.BoundContact;
import org.globus.cog.karajan.util.ContactSet;
import org.globus.swift.catalog.site.Parser;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class SiteCatalog extends AbstractSingleValuedFunction {
    private ArgRef<String> fileName;

    @Override
    protected Param[] getParams() {
        return params("fileName");
    }

    @Override
    public Object function(Stack stack) {
        String fn = fileName.getValue(stack);
        Parser p = new Parser(fn);
        try {
            Document doc = p.parse();
            return buildResources(doc);
        }
        catch (Exception e) {
            throw new ExecutionException(this, "Failed to parse site catalog", e);
        }
    }

    private Object buildResources(Document doc) {
        ContactSet cs = new ContactSet();
        NodeList pools = doc.getElementsByTagName("config").item(0).getChildNodes();
        for (int i = 0; i < pools.getLength(); i++) {
            try {
                BoundContact bc = pool(pools.item(i));
                if (bc != null) {
                    cs.addContact(bc);
                }
            }
            catch (Exception e) {
                throw new ExecutionException(this, "Invalid pool entry '" + attr(pools.item(i), "handle") + "': ", e);
            }
        }
        return cs;
    }

    private BoundContact pool(Node n) throws InvalidProviderException, ProviderMethodException {
        if (n.getNodeType() != Node.ELEMENT_NODE) {
            return null;
        }
        String name = attr(n, "handle");
        BoundContact bc = new BoundContact(name);
        
        String sysinfo = attr(n, "sysinfo", null);
        if (sysinfo != null) {
            bc.addProperty("sysinfo", sysinfo);
        }
        
        NodeList cs = n.getChildNodes();
        
        for (int i = 0; i < cs.getLength(); i++) {
            Node c = cs.item(i);
            if (c.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }
            String ctype = c.getNodeName();
            
            if (ctype.equals("gridftp")) {
                bc.addService(gridftp(c));
            }
            else if (ctype.equals("jobmanager")) {
                bc.addService(jobmanager(c));
            }
            else if (ctype.equals("execution")) {
                bc.addService(execution(c));
            }
            else if (ctype.equals("filesystem")) {
                bc.addService(filesystem(c));
            }
            else if (ctype.equals("workdirectory")) {
                bc.addProperty("workdir", text(c));
            }
            else if (ctype.equals("scratch")) {
                bc.addProperty("scratch", text(c));
            }
            else if (ctype.equals("env")) {
                env(bc, c);
            }
            else if (ctype.equals("profile")) {
                profile(bc, c);
            }
            else {
                System.err.println("Unknown node type: " + ctype);
            }
        }
        return bc;
    }

    private Service jobmanager(Node n) throws InvalidProviderException, ProviderMethodException {
        String provider;
        String url = attr(n, "url");
        String major = attr(n, "major");
        if (url.equals("local://localhost")) {
            provider = "local";
        }
        else if (url.equals("pbs://localhost")) {
            provider = "pbs";
        }
        else if ("2".equals(major)) {
            provider = "gt2";
        }
        else if ("4".equals(major)) {
            provider = "gt4";
        }
        else {
            throw new IllegalArgumentException("Unknown job manager version: " + major + ", url = '" + url + "'");
        }
        
        ServiceContact contact = new ServiceContactImpl(url);
            return new ServiceImpl(provider, Service.EXECUTION, 
                contact, AbstractionFactory.newSecurityContext(provider, contact));
    }

    private Service gridftp(Node n) throws InvalidProviderException, ProviderMethodException {
        String url = attr(n, "url");
        if (url.equals("local://localhost")) {
            return new ServiceImpl("local", Service.FILE_OPERATION, new ServiceContactImpl("localhost"), null);
        }
        else {
            ServiceContact contact = new ServiceContactImpl(url);
            return new ServiceImpl("gsiftp", Service.FILE_OPERATION, 
                contact, AbstractionFactory.newSecurityContext("gsiftp", contact));
        }
    }

    private Service execution(Node n) throws InvalidProviderException, ProviderMethodException {
        String provider = attr(n, "provider");
        String url = attr(n, "url", null);
        String jobManager = attr(n, "jobManager", null);
        ServiceContact contact = null;
        if (url != null) {
            contact = new ServiceContactImpl(url);
        }
        else if (provider.equals("local")) {
            contact = new ServiceContactImpl("localhost");
        }
        else {
            throw new IllegalArgumentException("Missing URL");
        }
        return new ExecutionServiceImpl(provider, contact, 
            AbstractionFactory.newSecurityContext(provider, contact), jobManager);
    }

    private Service filesystem(Node n) throws InvalidProviderException, ProviderMethodException {
        String provider = attr(n, "provider");
        String url = attr(n, "url", null);
        ServiceContact contact = null;
        if (url != null) {
            contact = new ServiceContactImpl(url);
        }
        else if (provider.equals("local")) {
            contact = new ServiceContactImpl("localhost");
        }
        else {
            throw new IllegalArgumentException("Missing URL");
        }
        return new ServiceImpl(provider, Service.FILE_OPERATION, 
            contact, AbstractionFactory.newSecurityContext(provider, contact));
    }

    private void env(BoundContact bc, Node n) {
        String key = attr(n, "key");
        String value = text(n);
        
        bc.addProperty("env:" + key, value);
    }

    private void profile(BoundContact bc, Node n) {
        String ns = attr(n, "namespace");
        String key = attr(n, "key");
        String value = text(n);
        
        if (ns.equals("karajan")) {
            bc.addProperty(key, value);
        }
        else {
            bc.addProperty(ns + ":" + key, value);
        }
    }

    private String text(Node n) {
        return n.getFirstChild().getNodeValue();
    }

    private String attr(Node n, String name) {
        NamedNodeMap attrs = n.getAttributes();
        if (attrs != null) {
            Node attr = attrs.getNamedItem(name);
            if (attr == null) {
                throw new IllegalArgumentException("Missing " + name);
            }
            else {
                return attr.getNodeValue();
            }
        }
        else {
            throw new IllegalArgumentException("Missing " + name);
        }
    }
    
    private String attr(Node n, String name, String defVal) {
        NamedNodeMap attrs = n.getAttributes();
        if (attrs != null) {
            Node attr = attrs.getNamedItem(name);
            if (attr == null) {
                return defVal;
            }
            else {
                return attr.getNodeValue();
            }
        }
        else {
            return defVal;
        }
    }
}
