// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.abstraction.impl.fileTransfer;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.common.AbstractDelegatedTaskHandler;
import org.globus.cog.abstraction.impl.common.AbstractionFactory;
import org.globus.cog.abstraction.impl.common.ProviderMethodException;
import org.globus.cog.abstraction.impl.common.StatusImpl;
import org.globus.cog.abstraction.impl.common.task.FileTransferSpecificationImpl;
import org.globus.cog.abstraction.impl.common.task.IllegalSpecException;
import org.globus.cog.abstraction.impl.common.task.InvalidProviderException;
import org.globus.cog.abstraction.impl.common.task.InvalidSecurityContextException;
import org.globus.cog.abstraction.impl.common.task.InvalidServiceContactException;
import org.globus.cog.abstraction.impl.common.task.ServiceContactImpl;
import org.globus.cog.abstraction.impl.common.task.ServiceImpl;
import org.globus.cog.abstraction.impl.common.task.TaskImpl;
import org.globus.cog.abstraction.impl.common.task.TaskSubmissionException;
import org.globus.cog.abstraction.impl.file.DirectoryNotFoundException;
import org.globus.cog.abstraction.impl.file.FileFragmentImpl;
import org.globus.cog.abstraction.impl.file.FileNotFoundException;
import org.globus.cog.abstraction.impl.file.FileResourceException;
import org.globus.cog.abstraction.interfaces.FileFragment;
import org.globus.cog.abstraction.interfaces.FileResource;
import org.globus.cog.abstraction.interfaces.FileTransferSpecification;
import org.globus.cog.abstraction.interfaces.ProgressMonitor;
import org.globus.cog.abstraction.interfaces.SecurityContext;
import org.globus.cog.abstraction.interfaces.Service;
import org.globus.cog.abstraction.interfaces.Status;
import org.globus.cog.abstraction.interfaces.Task;
import org.globus.cog.abstraction.interfaces.TaskHandler;
import org.globus.io.urlcopy.UrlCopyListener;

public class DelegatedFileTransferHandler extends AbstractDelegatedTaskHandler implements 
        Runnable, UrlCopyListener {
    static Logger logger = Logger.getLogger(DelegatedFileTransferHandler.class
            .getName());

    private static final Service LOCAL_SERVICE;
    static {
        Service s = new ServiceImpl();
        s.setProvider("local");
        s.setServiceContact(new ServiceContactImpl("localhost"));
        LOCAL_SERVICE = s;
    }

    private FileTransferSpecification spec;
    private FileResource sourceResource;
    private FileResource destinationResource;
    private boolean thirdparty = false;
    private boolean failed = false;
    private boolean oldStyle;
    private TaskHandler oldStyleHandler;

    public void submit(Task task) throws IllegalSpecException,
            InvalidSecurityContextException, InvalidServiceContactException,
            TaskSubmissionException {
        checkAndSetTask(task);
        
        this.spec = (FileTransferSpecification) task.getSpecification();
        prepareTransfer();
        if (oldStyle) {
            oldStyleHandler.submit(task);
        }
        else {
            Thread thread = new Thread(this);
            task.setStatus(Status.SUBMITTED);
            thread.start();
        }
    }

    public void suspend() throws InvalidSecurityContextException,
            TaskSubmissionException {
    }

    public void resume() throws InvalidSecurityContextException,
            TaskSubmissionException {
    }
    
    public void cancel() throws InvalidSecurityContextException, 
            TaskSubmissionException {
        cancel("Canceled");
    }

    public void cancel(String message) throws InvalidSecurityContextException,
            TaskSubmissionException {
        if (getTask().getStatus().getStatusCode() == Status.UNSUBMITTED) {
            getTask().setStatus(new StatusImpl(Status.CANCELED, message, null));
        }
        else {
            try {
                stopResources();
                getTask().setStatus(new StatusImpl(Status.CANCELED, message, null));
            }
            catch (Exception e) {
                throw new TaskSubmissionException("Cannot cancel task", e);
            }
        }
    }

    private void prepareTransfer() throws IllegalSpecException,
            InvalidServiceContactException,
            TaskSubmissionException {

        try {
            this.spec = (FileTransferSpecification) getTask()
                    .getSpecification();
        }
        catch (Exception e) {
            throw new IllegalSpecException(
                    "Exception while retreiving FileTransferSpecification", e);
        }

        Service sourceService = getTask()
                .getService(Service.FILE_TRANSFER_SOURCE_SERVICE);
        Service destinationService = getTask()
                .getService(Service.FILE_TRANSFER_DESTINATION_SERVICE);
        if (sourceService == null || destinationService == null) {
            throw new TaskSubmissionException(
                    "Invalid source or destination service");
        }

        if (sourceService.getProvider() == null) {
            throw new InvalidServiceContactException("Invalid source provider");
        }

        if (destinationService.getProvider() == null) {
            throw new InvalidServiceContactException(
                    "Invalid destination provider");
        }

        if (spec.isThirdParty() || spec.isThirdPartyIfPossible()) {
            this.thirdparty = true;
        }

        if (!this.thirdparty
                && (!canHandle(sourceService) || !canHandle(destinationService))) {
            throw new TaskSubmissionException(
                    "Could not find appropriate providers to handle a "
                            + sourceService.getProvider() + " -> "
                            + destinationService.getProvider() + " transfer");
        }
    }

    private boolean isLocal(Service service) {
        return service.getProvider().equalsIgnoreCase("local");
    }

    protected boolean canHandle(Service service) {
        return isLocal(service)
                || AbstractionFactory.hasObject(service.getProvider(),
                        "fileResource")
                || AbstractionFactory.hasObject(service.getProvider(),
                        "fileTransferTaskHandler");
    }

    protected File getLocalDestination(Service service) {
        if (isLocal(service)) {
            return new File(spec.getDestination());
        }
        return null;
    }

    protected FileResource prepareService(Service service)
            throws InvalidProviderException, ProviderMethodException,
            InvalidSecurityContextException, FileResourceException, IOException {
        if (isLocal(service)) {
            return null;
        }
        else if (AbstractionFactory.hasObject(service.getProvider(),
                "fileResource")) {
            if (logger.isDebugEnabled()) {
                logger.debug("Starting service on "
                        + service.getServiceContact());
            }
            return startResource(service);
        }
        else {
            return null;
        }
    }

    /**
     * Do the first part of the transfer and return the intermediate
     * file/directory
     * 
     * @param service
     *            the source service
     * @param localDestination
     *            a target local destination. If <tt>null</tt> a temporary
     *            file will be created. This allows the whole process to avoid
     *            using temporary files if the destination is local, by using
     *            the right file in the first place
     * @throws DirectoryNotFoundException
     *             if a directory is being transfered and it was not found at
     *             the source
     * @throws FileNotFoundException
     *             if a file is being transfered and it was not found at the
     *             source
     */
    protected File doSource(Service service, File localDestination)
            throws FileResourceException, IOException,
            InvalidProviderException, ProviderMethodException {
        if (isLocal(service)) {
            return new File(spec.getSource());
        }
        else {
            if (this.sourceResource != null) {
                if (spec.isRecursive() && this.sourceResource.isDirectory(spec.getSource())) {
                    if (localDestination == null) {
                        localDestination = File.createTempFile(getTask().getIdentity().getValue(),
                                null);
                        localDestination.delete();
                        localDestination.mkdir();
                    }
                    else if (localDestination.exists()
                            && !localDestination.isDirectory()) {
                        throw new FileResourceException(
                                "A directory transfer was requested, but the destination ("
                                        + localDestination.getAbsolutePath()
                                        + ") already exists and is a file");

                    }
                    if (logger.isDebugEnabled()) {
                        logger
                                .debug("Directory transfer with resource remote->tmp");
                    }
                    this.sourceResource.getDirectory(spec.getSource(),
                            localDestination.getAbsolutePath());
                }
                else {
                    if (localDestination == null) {
                        localDestination = File.createTempFile(getTask().getIdentity().getValue(),
                                null);
                    }
                    if (logger.isDebugEnabled()) {
                        logger.debug("File transfer with resource remote->tmp");
                    }
                    this.sourceResource.getFile(new FileFragmentImpl(spec.getSource()),
                            new FileFragmentImpl(localDestination.getAbsolutePath()),
                            new ProgressMonitor() {
                                public void progress(long current, long total) {
                                    getTask().setStdOutput(current + "/" + total);
                                    getTask().setAttribute("transferedBytes", new Long(current));
                                    getTask().setAttribute("totalBytes", new Long(current));
                                }
                            });
                }
                return localDestination;
            }
            else {
                if (localDestination == null) {
                    localDestination = File
                            .createTempFile(getTask()
                                    .getIdentity().getValue(), null);
                }
                transferWithHandler(service.getProvider(), service,
                        LOCAL_SERVICE, spec.getSource(), localDestination);

                return localDestination;
            }
        }
    }

    /**
     * Do the second part of the transfer
     * 
     * @param localSource
     *            the local file to be transfered. It cannot be null. At this
     *            point A 3rd party transfer was done, in which case this method
     *            should not have been reached, a simulated 3rd party was done,
     *            in which case we have a local temp file, or we upload from
     *            local, in which case the localSource should reflect the actual
     *            source
     * @param service
     *            the service to upload to
     * @throws InvalidSecurityContextException
     */
    protected void doDestination(File localSource, Service service)
            throws FileResourceException, InvalidProviderException, ProviderMethodException,
            InvalidSecurityContextException {
        if (isLocal(service)) {
            FileResource fr = AbstractionFactory.newFileResource("local");
            fr.start();
            try {
                if (localSource.isDirectory()) {
                    if (logger.isDebugEnabled()) {
                        logger
                                .debug("Directory transfer with resource local->local");
                    }
                    fr.putDirectory(localSource.getAbsolutePath(), spec
                            .getDestination());
                }
                else {
                    if (logger.isDebugEnabled()) {
                        logger
                                .debug("Directory transfer with resource local->local");
                    }
                    fr.createDirectories(spec.getDestinationDirectory());
                    fr.putFile(new FileFragmentImpl(localSource.getAbsolutePath()), 
                        new FileFragmentImpl(spec.getDestination()));
                }
            }
            finally {
                fr.stop();
            }
        }
        else {
            if (this.destinationResource != null) {
                if (spec.isRecursive() && localSource.isDirectory()) {
                    if (logger.isDebugEnabled()) {
                        logger
                                .debug("Directory transfer with resource local->remote");
                    }
                    this.destinationResource.putDirectory(localSource
                            .getAbsolutePath(), spec.getDestination());
                }
                else {
                    if (logger.isDebugEnabled()) {
                        logger
                                .debug("File transfer with resource local->remote");
                    }
                    
                    this.destinationResource.createDirectories(spec.getDestinationDirectory());
                    this.destinationResource.putFile(new FileFragmentImpl(localSource
                            .getAbsolutePath()), new FileFragmentImpl(spec.getDestination()),
                            new ProgressMonitor() {
                                public void progress(long current, long total) {
                                    getTask().setStdOutput(current + "/" + total);
                                }
                            });
                }
            }
            else {
                if (logger.isDebugEnabled()) {
                    logger.debug("File transfer with handler local->remote");
                }
                transferWithHandler(service.getProvider(), LOCAL_SERVICE,
                        service, localSource, spec.getDestination());
            }
        }
    }

    protected void transferWithHandler(String provider, Service sourceService,
            Service destinationService, Object source, Object destination)
            throws FileResourceException, InvalidProviderException,
            ProviderMethodException {
        TaskHandler th;

        th = AbstractionFactory.newFileTransferTaskHandler(provider);

        try {
            Task t = new TaskImpl();
            t.setType(Task.FILE_TRANSFER);
            t.setService(Service.FILE_TRANSFER_SOURCE_SERVICE, sourceService);
            t.setService(Service.FILE_TRANSFER_DESTINATION_SERVICE,
                    destinationService);
            FileTransferSpecification tspec = new FileTransferSpecificationImpl();

            if (source instanceof File) {
                tspec.setSourceDirectory(((File) source).getParentFile()
                        .getAbsolutePath());
                tspec.setSourceFile(((File) source).getName());
            }
            else if (source instanceof String) {
                tspec.setSource((String) source);
            }
            else {
                throw new IllegalArgumentException("Invalid source: " + source);
            }

            if (destination instanceof File) {
                tspec.setDestinationDirectory(((File) destination)
                        .getParentFile().getAbsolutePath());
                tspec.setDestinationFile(((File) destination).getName());
            }
            else if (destination instanceof String) {
                tspec.setDestination((String) destination);
            }
            else {
                throw new IllegalArgumentException("Invalid destination: "
                        + destination);
            }

            for (String name : spec.getAttributeNames()) {
                tspec.setAttribute(name, spec.getAttribute(name));
            }

            t.setSpecification(tspec);
            th.submit(t);
            t.waitFor();
            if (t.getStatus().getStatusCode() == Status.FAILED) {
                if (t.getStatus().getException() != null) {
                    throw t.getStatus().getException();
                }
                else if (t.getStatus().getMessage() != null) {
                    throw new Exception(t.getStatus().getMessage());
                }
                else {
                    throw new Exception(
                            "Unknown error occured while transfering file");
                }
            }
        }
        catch (Exception e) {
            throw new FileResourceException("Could not transfer file: "
                    + e.getMessage(), e);
        }
    }

    protected void cleanTemporaries(File tmp) {
        if (tmp.exists()) {
            if (tmp.isDirectory()) {
                File[] files = tmp.listFiles();
                for (int i = 0; i < files.length; i++) {
                    cleanTemporaries(files[i]);
                }
            }
            tmp.delete();
        }
    }

    protected FileResource startResource(Service service)
            throws InvalidProviderException, ProviderMethodException,
            InvalidSecurityContextException, IOException, FileResourceException {
        FileResource resource;
        resource = AbstractionFactory.newFileResource(service.getProvider()
                .trim());
        resource.setServiceContact(service.getServiceContact());
        resource.setSecurityContext(getSecurityContext(service));
        resource.start();
        return resource;
    }

    public void run() {
        getTask().setStatus(Status.ACTIVE);
        // todo retreive progress markers
        try {
            /*
             * The preparations are for detecting general problems with the
             * services before anything time-costly is done. There is no
             * point in transfering source->local if the destination service
             * has issues
             */
            Service sourceService = getTask()
                    .getService(Service.FILE_TRANSFER_SOURCE_SERVICE);
            Service destinationService = getTask()
                    .getService(Service.FILE_TRANSFER_DESTINATION_SERVICE);
            this.sourceResource = prepareService(sourceService);
            this.destinationResource = prepareService(destinationService);

            boolean canDoThirdParty = false;
            if (this.sourceResource != null && this.destinationResource != null) {
                canDoThirdParty = this.sourceResource.supportsThirdPartyTransfers() &&
                    this.sourceResource.getProtocol().equals(this.destinationResource.getProtocol());
            }
            if (spec.isThirdParty()) {
                if (!canDoThirdParty) {
                    throw new TaskSubmissionException("Cannot do third party transfer between " + 
                        this.sourceResource.getName() + " and " + this.getDestinationResource().getName());
                }
                else {
                    doThirdPartyTransfer();
                }
            }
            else if (spec.isThirdPartyIfPossible() && canDoThirdParty) {
                doThirdPartyTransfer();
            }
            else {
                // TODO clean temporary files if doSource() fails somewhere
                // after they have been created
                File intermediate = doSource(sourceService,
                        getLocalDestination(destinationService));
                try {
                    doDestination(intermediate, destinationService);
                }
                finally {
                    if (!isLocal(sourceService) && !isLocal(destinationService)) {
                        cleanTemporaries(intermediate);
                    }
                }
            }
            transferCompleted();
        }
        catch (Exception e) {
            logger.debug("Exception in transfer", e);
            transferFailed(e);
        }
        finally {
            stopResources();
        }
    }

    private void doThirdPartyTransfer() {
        int buffsz = -1;
        if (System.getProperty("tcp.buffer.size") != null) {
            buffsz = Integer.parseInt(System.getProperty("tcp.buffer.size"));
        }
        Object tcpBuffSz = spec.getAttribute("TCPBufferSize"); 
        if (tcpBuffSz instanceof String) {
            buffsz = Integer.parseInt((String) tcpBuffSz);
        }
        else if (tcpBuffSz instanceof Number) {
            buffsz = ((Number) tcpBuffSz).intValue();
        }
        
        if (buffsz != -1) {
            this.destinationResource.setAttribute("tcp.buffer.size", buffsz);
        }
        logger.debug("Performing third party transfer");
        
        try {
            this.destinationResource.createDirectories(spec.getDestinationDirectory());
            this.destinationResource.thirdPartyTransfer(this.sourceResource, 
                makeFragment(spec.getSource(), spec.getSourceOffset(), spec.getSourceLength()), 
                makeFragment(spec.getDestination(), spec.getDestinationOffset(), spec.getSourceLength()));
        }
        catch (Exception se) {
            transferFailed(se);
        }
    }

    private FileFragment makeFragment(String file,
            long poffset, long plen) {
        long offset, len;
        if (poffset == FileTransferSpecification.OFFSET_FILE_START) {
            offset = FileFragment.FILE_START;
        }
        else {
            offset = poffset;
        }
        if (plen == FileTransferSpecification.LENGTH_ENTIRE_FILE) {
            len = FileFragment.MAX_LENGTH;
        }
        else {
            len = plen;
        }
        return new FileFragmentImpl(file, offset, len);
    }

    private void transferFailed(Exception e) {
        failTask(null, e);
        stopResources();
        if (logger.isDebugEnabled()) {
            logger.debug("File transfer failed", e);
        }
        this.failed = true;
    }

    protected void stopResources() {
        try {
            if (this.sourceResource != null) {
                this.sourceResource.stop();
            }
            if (this.destinationResource != null) {
                this.destinationResource.stop();
            }
        }
        catch (Exception e) {
            logger.warn("Could not stop resources ", e);
        }
    }

    protected void setDestinationResource(FileResource destinationResource) {
        this.destinationResource = destinationResource;
    }

    protected void setSourceResource(FileResource sourceResource) {
        this.sourceResource = sourceResource;
    }

    protected FileResource getDestinationResource() {
        return destinationResource;
    }

    protected FileResource getSourceResource() {
        return sourceResource;
    }

    public void transfer(long current, long total) {
        if (total == -1) {
            if (current == -1) {
                logger
                        .debug("<third party file transfers: progress not available>");
                getTask()
                        .setStdOutput("<third party file transfers: progress not available>");
            }
            else {
                if (logger.isDebugEnabled()) {
                    logger.debug(current + " bytes transfered");
                }
                getTask().setStdOutput(Long.toString(current)
                        + " bytes transferred");
            }
        }
        else {
            if (logger.isDebugEnabled()) {
                logger.debug(current + " out of " + total
                        + " bytes transferred");
            }
            getTask().setStdOutput(current + " out of " + total
                    + " bytes transfered");
        }
    }

    public void transferError(Exception error) {
        transferFailed(error);
    }

    public void transferCompleted() {
        if (!this.failed) {
            getTask().setStatus(Status.COMPLETED);
        }
    }

    private SecurityContext getSecurityContext(Service service) throws InvalidProviderException, ProviderMethodException {
        SecurityContext securityContext = service.getSecurityContext();
        if (securityContext == null) {
            return AbstractionFactory.getSecurityContext(service.getProvider(), service.getServiceContact());
        }
        else {
            return securityContext;
        }
    }
}