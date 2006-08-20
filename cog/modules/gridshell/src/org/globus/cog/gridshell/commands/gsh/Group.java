/*
 * 
 */
package org.globus.cog.gridshell.commands.gsh;

import java.beans.PropertyChangeEvent;
import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import org.globus.cog.gridshell.commands.AbstractShellCommand;
import org.globus.cog.gridshell.getopt.app.GetOptImpl;
import org.globus.cog.gridshell.getopt.app.OptionImpl;
import org.globus.cog.gridshell.getopt.interfaces.GetOpt;
import org.globus.cog.gridshell.interfaces.Scope;

/**
 * 
 */
public class Group extends AbstractShellCommand {
    public transient static final String ACTION_DEFAULT = "show groups";
    public transient static final String ACTION_PRINT = "print";
    public transient static final String ACTION_ADD = "add";
    public transient static final String ACTION_REMOVE = "remove";
    
    private transient static final String EOL = System.getProperty("line.separator");
    
    private static Map groups = new HashMap();
    
    public static class GroupContainer {
        private Collection groupItems = new LinkedList();
        
        public void addGroupItem(GroupItem item) {
            synchronized(groupItems) {
                groupItems.add(item);
            }
        }
        public void removeGroupItem(GroupItem item) {
            synchronized(groupItems) {
                groupItems.remove(item);            
            }
        }
        public Collection getGroupItems() {
            synchronized(groupItems) {
                return java.util.Collections.unmodifiableCollection(groupItems);
            }
        }
        public String toString() {
            StringBuffer result = new StringBuffer();
            Iterator iGItems = groupItems.iterator();
            while(iGItems.hasNext()) {
                result.append(iGItems.next());
                result.append(EOL);
            }            
            return result.toString();
        }
    }
    
    public static class GroupItem {
        private static final String DEFAULT_PROVIDER = "gt2";
        
        private URI uri;
        private Object credentials;
        
        public GroupItem(URI uri) {
            this(uri,null);
        }
        public GroupItem(URI uri, Object credentials) {
            this.uri = uri;
            this.credentials = credentials;
        }
        
        public URI getURI() { return uri; }
        public Object getCredentials() { return credentials; }
        public void removeCredntails() { credentials = null; }
        
        public String toString() {
            return uri.toString();
        }
    }

    
    public static Map getGroups() {
        return Collections.unmodifiableMap(groups);
    }
    /* (non-Javadoc)
     * @see org.globus.cog.gridshell.commands.AbstractShellCommand#createGetOpt(org.globus.cog.gridshell.interfaces.Scope)
     */
    public GetOpt createGetOpt(Scope scope) {
        GetOpt result = new GetOptImpl(scope);
        result.addOption(new OptionImpl("the group name",String.class,false,"g","group",false));
        result.addOption(new OptionImpl("the action [print | add | remove]",String.class,ACTION_DEFAULT,"a","action",false));
        result.addOption(new OptionImpl("the uri",URI.class,false,"u","uri",false));
        result.addOption(new OptionImpl("username",String.class,false,null,"username",false));
        result.addOption(new OptionImpl("password",String.class,false,"p","password"));
        result.addOption(new OptionImpl("certificate",String.class,false,null,"certificate",false));
        return result;
    }

    /* (non-Javadoc)
     * @see org.globus.cog.gridshell.interfaces.Command#execute()
     */
    public Object execute() throws Exception {
        
        String action = (String)getGetOpt().getOption("action").getValue();
        String group = (String)getGetOpt().getOption("group").getValue();
        URI uri = (URI)getGetOpt().getOption("uri").getValue();
        
        if(ACTION_DEFAULT.equals(action)) {
            print(group);
        }else if(ACTION_ADD.equals(action)) {
            addToGroup(group,uri);
        }else if(ACTION_REMOVE.equals(action)) {
            
        }else if(ACTION_PRINT.equals(action)) {
            print(group);            
        }else {
            this.setStatusFailed("Invalid action defined '"+action+"'");
        }
        
        return null;
    }
    
    private void printGroups() {
        StringBuffer result = new StringBuffer();
        if(groups.size()==0) {
            result.append("No groups to display");
            result.append(EOL);
        }
        Iterator iGroups = groups.keySet().iterator();
        while(iGroups.hasNext()) {
            String groupName = (String) iGroups.next();
            result.append(groupName);
            result.append(EOL);
        }
        setResult(result.toString());
        this.setStatusCompleted();
    }
    private void print(String group) {
        if(group==null) {
            printGroups();            
            return;
        }
        if(groups.containsKey(group)) {
            setResult(groups.get(group));            
            this.setStatusCompleted();            
        }else {
            this.setStatusFailed("Group '"+group+"' is not defined");
        }
    }
    private void addToGroup(String group,URI uri) {
        if(!groups.containsKey(group)) {
            groups.put(group,new GroupContainer());
        }
        
        GroupContainer container = (GroupContainer)groups.get(group);        
        container.addGroupItem(new GroupItem(uri,getCredentials()));
        this.setStatusCompleted();
    }
    
    /* (non-Javadoc)
     * @see org.globus.cog.gridshell.interfaces.Command#destroy()
     */
    public Object destroy() throws Exception {
        // do nothing method
        return null;
    }

    /* (non-Javadoc)
     * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent evt) {
        // do nothing method   
    }

}
