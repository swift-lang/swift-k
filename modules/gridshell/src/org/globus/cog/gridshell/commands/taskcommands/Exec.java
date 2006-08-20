/*
 * 
 */
package org.globus.cog.gridshell.commands.taskcommands;

import java.net.URI;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.globus.cog.gridshell.commands.gsh.Group;
import org.globus.cog.gridshell.getopt.app.GetOptImpl;
import org.globus.cog.gridshell.getopt.app.OptionImpl;
import org.globus.cog.gridshell.getopt.interfaces.GetOpt;
import org.globus.cog.gridshell.interfaces.Scope;
import org.globus.cog.gridshell.tasks.ExecTask;

/**
 * 
 */
public class Exec extends AbstractTaskCommand {
    private static final Logger logger = Logger.getLogger(Exec.class);
    
    public Object execute() throws Exception {                
        String command = (String)getGetOpt().getOption("command").getValue();
        String args = (String)getGetOpt().getOption("args").getValue();
        Boolean isBatch = (Boolean)getGetOpt().getOption("isBatch").getValue();
        Boolean isRedirected = (Boolean)getGetOpt().getOption("isRedirected").getValue();
        String stdErr = (String)getGetOpt().getOption("stderr").getValue();
        String stdOut = (String)getGetOpt().getOption("stdout").getValue();
        
        if(getGetOpt().isOptionSet("uri")) {
            logger.info("doing uri");
            URI uri = (URI)getGetOpt().getOption("uri").getValue();
            executeUri(uri,command,args,isBatch,isRedirected,stdErr,stdOut);
        }else if(getGetOpt().isOptionSet("group")) {
            logger.info("doing group");
            String group = (String)getGetOpt().getOption("group").getValue();
            executeGroup(group,command,args,isBatch,isRedirected,stdErr,stdOut);
        }else {
            this.setStatusFailed("URI or Group is required");
            return null;
        }
        
        return super.execute();
    }
    
    public void executeUri(URI uri,String command,String args, Boolean isBatch,
            Boolean isRedirected, String stdErr, String stdOut) throws Exception {
        Object credentials = getCredentials();
        addExec(uri,credentials,command,args,isBatch,isRedirected,stdErr,stdOut);
    }
    
    public void executeGroup(String group,String command,String args, Boolean isBatch,
            Boolean isRedirected, String stdErr, String stdOut) throws Exception {
            Object groupObj = Group.getGroups().get(group);
            if(groupObj==null) {
                throw new RuntimeException("Error group '"+group+"' is not defined");
            }
            
            Iterator iGroupItems = ((Group.GroupContainer)groupObj).getGroupItems().iterator();
            while(iGroupItems.hasNext()) {
                Group.GroupItem gItem = (Group.GroupItem)iGroupItems.next();
                URI uri = gItem.getURI();
                Object credentials = gItem.getCredentials();
                addExec(uri,credentials,command,args,isBatch,isRedirected,stdErr,stdOut);
            }
    }
    
    public void addExec(URI uri,Object credential,String command, String args, Boolean isBatch,
            Boolean isRedirected, String stdErr, String stdOut) throws Exception {        
        String provider = (uri.getScheme()==null) ? "gt2": uri.getScheme();
        String serviceContact = (uri.getHost()==null)? String.valueOf(uri) : uri.getHost();
        int port = uri.getPort();
        
        if(logger.isDebugEnabled()) {
            logger.debug("uri="+uri);
            logger.debug("provider="+provider);
            logger.debug("serviceContact="+serviceContact);
            logger.debug("port="+port);
        }   
        
        ExecTask task = new ExecTask(this.toString(),credential,provider,serviceContact,port,command,args);
        task.isBatch(isBatch.booleanValue());
        task.isRedirected(isRedirected.booleanValue());
        task.setStdErr(stdErr);
        task.setStdOut(stdOut);
        addTask(task);        
    }

    /* (non-Javadoc)
     * @see org.globus.cog.gridshell.commands.taskcommands.AbstractTaskCommand#getTaskOutput()
     */
    public Object getTaskOutput() {
        logger.debug("getting taskoutput");
        synchronized(bufferedResult) {
            return bufferedResult;
        }
    }

    /* (non-Javadoc)
     * @see org.globus.cog.gridshell.commands.AbstractShellCommand#createGetOpt(org.globus.cog.gridshell.interfaces.Scope)
     */
    public GetOpt createGetOpt(Scope scope) {
        GetOpt result = new GetOptImpl(scope);
        result.addOption(new OptionImpl("the uri of where to execute",URI.class,false,"u","uri",false));
        result.addOption(new OptionImpl("the group to execute",String.class,false,"g","group",false));
        result.addOption(new OptionImpl("the command to execute",String.class,true,"c","command",false));
        result.addOption(new OptionImpl("arguments to the command to execute",String.class,false,"a","args",false));
        result.addOption(OptionImpl.createFlag("should run in batch","b","isBatch"));
        result.addOption(OptionImpl.createFlag("should be redirected",Boolean.TRUE,"r","isRedirected"));
        result.addOption(new OptionImpl("standard output",String.class,false,null,"stdout",false));
        result.addOption(new OptionImpl("standard error",String.class,false,null,"stderr",false));
        result.addOption(new OptionImpl("username",String.class,false,null,"username",false));
        result.addOption(new OptionImpl("the location of a certificate",String.class,false,null,"certificate",false));
        result.addOption(OptionImpl.createFlag("signals you wish to enter a password","p","password"));
        return result;
    }

}
