//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jan 7, 2013
 */
package org.griphyn.vdl.karajan.lib;

import java.util.ArrayList;
import java.util.List;

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
import org.globus.swift.catalog.site.Application;
import org.globus.swift.catalog.site.SiteCatalogParser;
import org.globus.swift.catalog.site.SwiftContact;
import org.globus.swift.catalog.site.SwiftContactSet;
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
        SiteCatalogParser p = new SiteCatalogParser(fn);
        try {
            Document doc = p.parse();
            return buildResources(doc);
        }
        catch (Exception e) {
            throw new ExecutionException(this, "Failed to parse site catalog", e);
        }
    }
    
    private static class KVPair {
        public final String key;
        public final String value;
        
        public KVPair(String key, String value) {
            this.key = key;
            this.value = value;
        }
    }

    private Object buildResources(Document doc) {
        Node root = getRoot(doc);
        
        if (root.getLocalName().equals("config")) {
            throw new IllegalArgumentException("Old sites file format. Please upgrade your sites file to the new format.");
        }
        else if (root.getLocalName().equals("sites")) {
            return parse(root);
        }
        else {
            throw new IllegalArgumentException("Illegal sites file root node: " + root.getLocalName());
        }
    }
       
    private Object parse(Node config) {
        SwiftContactSet cs = new SwiftContactSet();
        NodeList pools = config.getChildNodes();
        for (int i = 0; i < pools.getLength(); i++) {
            Node n = pools.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                String ctype = n.getNodeName();
                if (ctype.equals("site")) {
                    try {
                        SwiftContact bc = pool(n);
                        if (bc != null) {
                            cs.addContact(bc);
                        }
                    }
                    catch (Exception e) {
                        throw new ExecutionException(this, "Invalid site entry '" + poolName(n) + "': ", e);
                    }
                }
                else if (ctype.equals("apps")) {
                    SwiftContact dummy = new SwiftContact();
                    apps(dummy, n);
                    for (Application app : dummy.getApplications()) {
                        cs.addApplication(app);
                    }
                }
                else {
                    throw new IllegalArgumentException("Invalid node: " + ctype);
                }
            }
        }
        return cs;
    }

    private String poolName(Node site) {
       if (site.getLocalName().equals("site")) {
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

    private SwiftContact pool(Node n) throws InvalidProviderException, ProviderMethodException {
        if (n.getNodeType() != Node.ELEMENT_NODE) {
            return null;
        }
        String name = poolName(n);
        SwiftContact bc = new SwiftContact(name);
        
        String sysinfo = attr(n, "sysinfo", null);
        if (sysinfo != null) {
            bc.setProperty("sysinfo", sysinfo);
        }
                
        NodeList cs = n.getChildNodes();
        
        for (int i = 0; i < cs.getLength(); i++) {
            Node c = cs.item(i);
            if (c.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }
            String ctype = c.getNodeName();
            
            if (ctype.equals("execution")) {
                bc.addService(execution(c));
            }
            else if (ctype.equals("filesystem")) {
                bc.addService(filesystem(c));
            }
            else if (ctype.equals("workdirectory")) {
                bc.setProperty("workdir", text(c));
            }
            else if (ctype.equals("scratch")) {
                bc.setProperty("scratch", text(c));
            }
            else if (ctype.equals("property")) {
                bc.setProperty(attr(c, "name"), text(c));
            }
            else if (ctype.equals("apps")) {
                apps(bc, c);
            }
            else {
                throw new IllegalArgumentException("Unknown node type: " + ctype);
            }
        }
        return bc;
    }

    private void apps(SwiftContact bc, Node n) {
        NodeList cs = n.getChildNodes();
        
        List<KVPair> envs = new ArrayList<KVPair>();
        List<KVPair> props = new ArrayList<KVPair>();
        for (int i = 0; i < cs.getLength(); i++) {
            Node c = cs.item(i);
            if (c.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }
            String ctype = c.getNodeName();
            
            if (ctype.equals("app")) {
                bc.addApplication(application(c));
            }
            else if (ctype.equals("env")) {
                envs.add(env(c));
            }
            else if (ctype.equals("property")) {
                props.add(this.property(c));
            }
            else {
                throw new IllegalArgumentException("Unknown node type: " + ctype);
            }
        }
        
        mergeEnvsToApps(bc, envs);
        mergePropsToApps(bc, props);
    }

    private void mergeEnvsToApps(SwiftContact bc, List<KVPair> envs) {
        for (Application app : bc.getApplications()) {
            for (KVPair kvp : envs) {
                if (!app.getEnv().containsKey(kvp.key)) {
                    // only merge if app does not override
                    app.setEnv(kvp.key, kvp.value);
                }
            }
        }
    }
    
    private void mergePropsToApps(SwiftContact bc, List<KVPair> props) {
        for (Application app : bc.getApplications()) {
            for (KVPair kvp : props) {
                if (!app.getProperties().containsKey(kvp.key)) {
                    app.addProperty(kvp.key, kvp.value);
                }
            }
        }
    }

    private Application application(Node n) {
        Application app = new Application();
        app.setName(attr(n, "name"));
        app.setExecutable(attr(n, "executable"));
        
        NodeList cs = n.getChildNodes();
        
        for (int i = 0; i < cs.getLength(); i++) {
            Node c = cs.item(i);
            if (c.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }
            String ctype = c.getNodeName();
            
            if (ctype.equals("env")) {
                KVPair env = env(c);
                app.setEnv(env.key, env.value);
            }
            else if (ctype.equals("property")) {
                KVPair prop = property(c);
                app.addProperty(prop.key, prop.value);
            }
            else {
                throw new IllegalArgumentException("Unknown node type: " + ctype);
            }
        }
        
        return app;
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
        
        properties(s, n);
                
        return s;
    }

    private void properties(Service s, Node n) {
        NodeList cs = n.getChildNodes();
        
        for (int i = 0; i < cs.getLength(); i++) {
            Node c = cs.item(i);
            if (c.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }
            String ctype = c.getNodeName();
            
            if (ctype.equals("property")) {
                property(s, c);
            }
            else {
                throw new IllegalArgumentException("Unknown node type: " + ctype);
            }
        }

    }

    private void property(Service s, Node c) {
        s.setAttribute(attr(c, "name"), text(c));
    }
    
    private KVPair property(Node c) {
        return new KVPair(attr(c, "name"), text(c));
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
        
        properties(s, n);
        
        return s;
    }
    
    private KVPair env(Node n) {
        String key = attr(n, "name");
        String value = text(n);
        
        return new KVPair(key, value);
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
