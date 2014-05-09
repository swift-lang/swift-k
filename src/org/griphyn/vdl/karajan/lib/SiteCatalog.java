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
import org.globus.cog.abstraction.interfaces.ExecutionService;
import org.globus.cog.abstraction.interfaces.Service;
import org.globus.cog.abstraction.interfaces.ServiceContact;
import org.globus.cog.karajan.analyzer.ArgRef;
import org.globus.cog.karajan.analyzer.Param;
import org.globus.cog.karajan.compiled.nodes.functions.AbstractSingleValuedFunction;
import org.globus.cog.karajan.util.BoundContact;
import org.globus.cog.karajan.util.ContactSet;
import org.globus.swift.catalog.site.SiteCatalogParser;
import org.globus.swift.catalog.site.SwiftContact;
import org.griphyn.vdl.util.FQN;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class SiteCatalog extends AbstractSingleValuedFunction {
    private ArgRef<String> fileName;
    
    private enum Version {
        V1, V2;
    }

    @Override
    protected Param[] getParams() {
        return params("fileName");
    }

    @Override
    public Object function(Stack stack) {
        String fn = fileName.getValue(stack);
        SiteCatalogParser p = new SiteCatalogParser(fn);
        try {
            Document doc = p.parse();
            return buildResources(doc);
        }
        catch (Exception e) {
            throw new ExecutionException(this, "Failed to parse site catalog", e);
        }
    }

    private Object buildResources(Document doc) {
        Node root = getRoot(doc);
        
        if (root.getLocalName().equals("config")) {
            return parse(root, Version.V1);
        }
        else if (root.getLocalName().equals("sites")) {
            return parse(root, Version.V1);
        }
        else {
            throw new IllegalArgumentException("Illegal sites file root node: " + root.getLocalName());
        }
    }
    
    

    private Object parse(Node config, Version v) {
        ContactSet cs = new ContactSet();
        NodeList pools = config.getChildNodes();
        for (int i = 0; i < pools.getLength(); i++) {
            Node n = pools.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                try {
                    BoundContact bc = pool(n, v);
                    if (bc != null) {
                        cs.addContact(bc);
                    }
                }
                catch (Exception e) {
                    throw new ExecutionException(this, "Invalid site entry '" + poolName(n, v) + "': ", e);
                }
            }
        }
        return cs;
    }

    private String poolName(Node site, Version v) {
       if (site.getLocalName().equals("pool")) {
           return attr(site, "handle");
       }
       else if (site.getLocalName().equals("site")) {
           return attr(site, "name");
       }
       else {
           throw new IllegalArgumentException("Invalid node: " + site.getLocalName());
       }
    }

    private Node getRoot(Document doc) {
        NodeList l = doc.getChildNodes();
        for (int i = 0; i < l.getLength(); i++) {
            if (l.item(i).getNodeType() == Node.ELEMENT_NODE) {
                return l.item(i);
            }
        }
        throw new IllegalArgumentException("Missing root element");
    }

    private BoundContact pool(Node n, Version v) throws InvalidProviderException, ProviderMethodException {
        if (n.getNodeType() != Node.ELEMENT_NODE) {
            return null;
        }
        String name = poolName(n, v);
        SwiftContact bc = new SwiftContact(name);
        
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
            
            if (v == Version.V1 && ctype.equals("gridftp")) {
                bc.addService(gridftp(c));
            }
            else if (v == Version.V1 && ctype.equals("jobmanager")) {
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
            else if (v == Version.V2 && ctype.equals("application")) {
                application(bc, c);
            }
            else {
                throw new IllegalArgumentException("Unknown node type: " + ctype);
            }
        }
        return bc;
    }

    private void application(BoundContact bc, Node c) {
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
        if (jobManager == null) {
            jobManager = attr(n, "jobmanager", null);
        }
        
        ExecutionService s = new ExecutionServiceImpl();
        s.setProvider(provider);
        ServiceContact contact = null;
        if (url != null) {
            contact = new ServiceContactImpl(url);
            s.setServiceContact(contact);
            s.setSecurityContext(AbstractionFactory.newSecurityContext(provider, contact));
        }
        
        if (jobManager != null) {
            s.setJobManager(jobManager);
        }
        
        return s;
    }

    private Service filesystem(Node n) throws InvalidProviderException, ProviderMethodException {
        String provider = attr(n, "provider");
        String url = attr(n, "url", null);
        
        Service s = new ServiceImpl();
        s.setType(Service.FILE_OPERATION);
        s.setProvider(provider);
        
        ServiceContact contact = null;
        if (url != null) {
            contact = new ServiceContactImpl(url);
            s.setServiceContact(contact);
            s.setSecurityContext(AbstractionFactory.newSecurityContext(provider, contact));
        }
        
        return s;
    }

    private void env(SwiftContact bc, Node n) {
        String key = attr(n, "key");
        String value = text(n);
        
        bc.addProfile(new FQN("env", key), value);
    }

    private void profile(SwiftContact bc, Node n) {
        String ns = attr(n, "namespace");
        String key = attr(n, "key");
        String value = text(n);
        
        if (value == null) {
            throw new IllegalArgumentException("No value for profile " + ns + ":" + key);
        }
        if (ns.equals("karajan")) {
            bc.addProperty(key, value);
        }
        else {
        	bc.addProfile(new FQN(ns, key), value);
        }
    }

    private String text(Node n) {
        if (n.getFirstChild() != null) {
            return n.getFirstChild().getNodeValue();
        }
        else {
            return null;
        }
    }

    private String attr(Node n, String name) {
        NamedNodeMap attrs = n.getAttributes();
        if (attrs != null) {
            Node attr = attrs.getNamedItem(name);
            if (attr == null) {
                throw new IllegalArgumentException("Missing " + name);
            }
            else {
                return expandProps(attr.getNodeValue());
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
                return expandProps(attr.getNodeValue());
            }
        }
        else {
            return defVal;
        }
    }

    private String expandProps(String v) {
        if (v == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        int li = -1;
        for (int i = 0; i < v.length(); i++) {
            char c = v.charAt(i);
            switch (c) {
                case '{':
                    if (li != -1) {
                        li = -1;
                        sb.append('{');
                    }
                    else {
                        li = i;
                    }
                    break;
                case '}':
                    if (li != -1) {
                        sb.append(System.getProperty(v.substring(li + 1, i)));
                        li = -1;
                    }
                    else {
                        sb.append(c);
                    }
                    break;
                default:
                    if (li == -1) {
                        sb.append(c);
                    }
            }
        }
        return sb.toString();
    }
}
