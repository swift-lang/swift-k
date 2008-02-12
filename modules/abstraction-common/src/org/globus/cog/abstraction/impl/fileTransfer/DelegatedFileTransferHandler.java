// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.abstraction.impl.fileTransfer;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Enumeration;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.common.AbstractionFactory;
import org.globus.cog.abstraction.impl.common.ProviderMethodException;
import org.globus.cog.abstraction.impl.common.StatusImpl;
import org.globus.cog.abstraction.impl.common.task.FileTransferSpecificationImpl;
import org.globus.cog.abstraction.impl.common.task.IllegalSpecException;
import org.globus.cog.abstraction.impl.common.task.InvalidProviderException;
import org.globus.cog.abstraction.impl.common.task.InvalidSecurityContextException;
import org.globus.cog.abstraction.impl.common.task.InvalidServiceContactException;
import org.globus.cog.abstraction.impl.common.task.SecurityContextImpl;
import org.globus.cog.abstraction.impl.common.task.ServiceContactImpl;
import org.globus.cog.abstraction.impl.common.task.ServiceImpl;
import org.globus.cog.abstraction.impl.common.task.TaskImpl;
import org.globus.cog.abstraction.impl.common.task.TaskSubmissionException;
import org.globus.cog.abstraction.impl.file.DirectoryNotFoundException;
import org.globus.cog.abstraction.impl.file.FileNotFoundException;
import org.globus.cog.abstraction.impl.file.FileResourceException;
import org.globus.cog.abstraction.interfaces.DelegatedTaskHandler;
import org.globus.cog.abstraction.interfaces.FileResource;
import org.globus.cog.abstraction.interfaces.FileTransferSpecification;
import org.globus.cog.abstraction.interfaces.ProgressMonitor;
import org.globus.cog.abstraction.interfaces.SecurityContext;
import org.globus.cog.abstraction.interfaces.Service;
import org.globus.cog.abstraction.interfaces.Status;
import org.globus.cog.abstraction.interfaces.Task;
import org.globus.cog.abstraction.interfaces.TaskHandler;
import org.globus.gsi.gssapi.auth.Authorization;
import org.globus.gsi.gssapi.auth.HostAuthorization;
import org.globus.io.urlcopy.UrlCopy;
import org.globus.io.urlcopy.UrlCopyException;
import org.globus.io.urlcopy.UrlCopyListener;
import org.globus.util.GlobusURL;
import org.ietf.jgss.GSSCredential;

public class DelegatedFileTransferHandler implements DelegatedTaskHandler,
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

    protected Task task;
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
        if (this.task != null) {
            throw new TaskSubmissionException(
                    "DelegatedFileTransferHandler cannot handle two active transfers simultaneously");
        }
        else {
            this.task = task;
            this.spec = (FileTransferSpecification) task.getSpecification();
            prepareTransfer();
            if (oldStyle) {
                oldStyleHandler.submit(task);
            }
            else {
                Thread thread = new Thread(this);
                this.task.setStatus(Status.SUBMITTED);
                thread.start();
            }
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
        if (this.task.getStatus().getStatusCode() == Status.UNSUBMITTED) {
            this.task.setStatus(Status.CANCELED);
        }
        else {
            try {
                stopResources();
                this.task.setStatus(Status.CANCELED);
            }
            catch (Exception e) {
                throw new TaskSubmissionException("Cannot cancel task", e);
            }
        }
    }

    private void prepareTransfer() throws IllegalSpecException,
            InvalidSecurityContextException, InvalidServiceContactException,
            TaskSubmissionException {

        try {
            this.spec = (FileTransferSpecification) this.task
                    .getSpecification();
        }
        catch (Exception e) {
            throw new IllegalSpecException(
                    "Exception while retreiving FileTransferSpecification", e);
        }

        Service sourceService = this.task
                .getService(Service.FILE_TRANSFER_SOURCE_SERVICE);
        Service destinationService = this.task
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
            if ((sourceService.getProvider().equalsIgnoreCase("gridftp")
                    || sourceService.getProvider().equalsIgnoreCase("gsiftp") || sourceService
                    .getProvider().equalsIgnoreCase("gridftp-old"))
                    && (destinationService.getProvider().equalsIgnoreCase(
                            "gridftp")
                            || destinationService.getProvider()
                                    .equalsIgnoreCase("gsiftp") || destinationService
                            .getProvider().equalsIgnoreCase("gridftp-old"))) {
                this.thirdparty = true;
            }
            else if (spec.isThirdParty()) {
                throw new IllegalSpecException(
                        "Third party transfers between providers "
                                + sourceService.getProvider() + " and "
                                + destinationService.getProvider()
                                + " is not supported");
            }
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
        else {
            return null;
        }
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
                if (this.sourceResource.isDirectory(spec.getSource())) {
                    if (localDestination == null) {
                        localDestination = File.createTempFile(Long
                                .toString(this.task.getIdentity().getValue()),
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
                        localDestination = File.createTempFile(Long
                                .toString(this.task.getIdentity().getValue()),
                                null);
                    }
                    if (logger.isDebugEnabled()) {
                        logger.debug("File transfer with resource remote->tmp");
                    }
                    this.sourceResource.getFile(spec.getSource(),
                            localDestination.getAbsolutePath(),
                            new ProgressMonitor() {
                                public void progress(long current, long total) {
                                    task.setStdOutput(current + "/" + total);
                                }
                            });
                }
                return localDestination;
            }
            else {
                if (localDestination == null) {
                    localDestination = File
                            .createTempFile(Long.toString(this.task
                                    .getIdentity().getValue()), null);
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
            throws FileResourceException, IOException,
            InvalidProviderException, ProviderMethodException,
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
                    fr.putFile(localSource.getAbsolutePath(), spec
                            .getDestination());
                }
            }
            finally {
                fr.stop();
            }
        }
        else {
            if (this.destinationResource != null) {
                if (localSource.isDirectory()) {
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
                    this.destinationResource.putFile(localSource
                            .getAbsolutePath(), spec.getDestination(),
                            new ProgressMonitor() {
                                public void progress(long current, long total) {
                                    task.setStdOutput(current + "/" + total);
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

            Enumeration e = spec.getAllAttributes();
            while (e.hasMoreElements()) {
                String name = (String) e.nextElement();
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
        this.task.setStatus(Status.ACTIVE);
        // todo retreive progress markers
        if (this.thirdparty) {
            doThirdPartyTransfer();
        }
        else {
            try {
                /*
                 * The preparations are for detecting general problems with the
                 * services before anything time-costly is done. There is no
                 * point in transfering source->local if the destination service
                 * has issues
                 */
                Service sourceService = this.task
                        .getService(Service.FILE_TRANSFER_SOURCE_SERVICE);
                Service destinationService = this.task
                        .getService(Service.FILE_TRANSFER_DESTINATION_SERVICE);
                this.sourceResource = prepareService(sourceService);
                this.destinationResource = prepareService(destinationService);
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
                stopResources();
                transferCompleted();
            }
            catch (Exception e) {
                logger.debug("Exception in transfer", e);
                stopResources();
                transferFailed(e);
            }
        }
    }

    private void doThirdPartyTransfer() {
        UrlCopy urlCopy = new UrlCopy();
        GlobusURL sourceURL = null;
        GlobusURL destinationURL = null;

        logger.debug("Performing third party transfer");
        try {
            String url = this.task.getService(
                    Service.FILE_TRANSFER_SOURCE_SERVICE).getServiceContact()
                    .getContact()
                    + "/" + spec.getSource();
            if (!url.startsWith("gsiftp://")) {
                url = "gsiftp://" + url;
            }
            logger.debug("Source URL: " + url);
            sourceURL = new GlobusURL(url);

            url = this.task.getService(
                    Service.FILE_TRANSFER_DESTINATION_SERVICE)
                    .getServiceContact().getContact()
                    + "/" + spec.getDestination();
            if (!url.startsWith("gsiftp://")) {
                url = "gsiftp://" + url;
            }
            logger.debug("Destination URL: " + url);
            destinationURL = new GlobusURL(url);
        }
        catch (MalformedURLException mue) {
            transferFailed(mue);
            return;
        }
        try {
            urlCopy.setSourceUrl(sourceURL);
            urlCopy.setDestinationUrl(destinationURL);
        }
        catch (UrlCopyException uce) {
            transferFailed(uce);
            return;
        }
        SecurityContext securityContext = this.task.getService(
                Service.FILE_TRANSFER_SOURCE_SERVICE).getSecurityContext();
        try {
            urlCopy.setCredentials((GSSCredential) securityContext
                    .getCredentials());
            Authorization authorization = (Authorization) securityContext
                    .getAttribute("authorization");
            if (authorization == null) {
                authorization = HostAuthorization.getInstance();
            }
            urlCopy.setSourceAuthorization(authorization);
            urlCopy.setDestinationAuthorization(authorization);
        }
        catch (Exception se) {
            transferFailed(se);
            return;
        }
        if (spec.getSourceOffset() != FileTransferSpecification.OFFSET_FILE_START) {
            urlCopy.setSourceFileOffset(spec.getSourceOffset());
        }
        if (spec.getDestinationOffset() != FileTransferSpecification.OFFSET_FILE_START) {
            urlCopy.setDestinationOffset(spec.getDestinationOffset());
        }
        if (spec.getSourceLength() != FileTransferSpecification.LENGTH_ENTIRE_FILE) {
            urlCopy.setSourceFileLength(spec.getSourceLength());
        }
        urlCopy.setUseThirdPartyCopy(true);
        urlCopy.addUrlCopyListener(this);
        urlCopy.run();
    }

    private void transferFailed(Exception e) {
        Status status = new StatusImpl();
        status.setPrevStatusCode(this.task.getStatus().getStatusCode());
        status.setStatusCode(Status.FAILED);
        status.setException(e);
        this.task.setStatus(status);
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
                this.task
                        .setStdOutput("<third party file transfers: progress not available>");
            }
            else {
                if (logger.isDebugEnabled()) {
                    logger.debug(current + " bytes transfered");
                }
                this.task.setStdOutput(Long.toString(current)
                        + " bytes transferred");
            }
        }
        else {
            if (logger.isDebugEnabled()) {
                logger.debug(current + " out of " + total
                        + " bytes transferred");
            }
            this.task.setStdOutput(current + " out of " + total
                    + " bytes transfered");
        }
    }

    public void transferError(Exception error) {
        transferFailed(error);
    }

    public void transferCompleted() {
        if (!this.failed) {
            this.task.setStatus(Status.COMPLETED);
        }
    }

    private SecurityContext getSecurityContext(Service service) {
        SecurityContext securityContext = service.getSecurityContext();
        if (securityContext == null) {
            // create default credentials
            securityContext = new SecurityContextImpl();
        }
        return securityContext;
    }
}