/*
 * Swift Parallel Scripting Language (http://swift-lang.org)
 * Code from Java CoG Kit Project (see notice below) with modifications.
 *
 * Copyright 2005-2014 University of Chicago
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// ----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Oct 8, 2004
 */
package org.globus.cog.abstraction.impl.file;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.common.AbstractionFactory;
import org.globus.cog.abstraction.impl.common.ProviderMethodException;
import org.globus.cog.abstraction.impl.common.task.InvalidProviderException;
import org.globus.cog.abstraction.impl.common.task.InvalidSecurityContextException;
import org.globus.cog.abstraction.interfaces.FileResource;
import org.globus.cog.abstraction.interfaces.Service;

public class FileResourceCache {
    private static Logger logger = Logger.getLogger(FileResourceCache.class);

    private static FileResourceCache defaultFileResourceCache;

    private static int DEFAULT_MAX_IDLE_RESOURCES = 20;
    private static long DEFAULT_MAX_IDLE_TIME = 120000;

    public static FileResourceCache getDefault() {
        if (defaultFileResourceCache == null) {
            defaultFileResourceCache = new FileResourceCache();
        }
        return defaultFileResourceCache;
    }

    private LinkedList<FileResource> order;
    private Map<FileResource, Long> releaseTimes;
    private Map<Service, List<FileResource>> fileResources;
    private Map<FileResource, Service> services;
    private Set<FileResource> invalid;
    private Set<FileResource> inUse;
    private Timer timer;
    private int maxIdleResources = DEFAULT_MAX_IDLE_RESOURCES;
    private long maxIdleTime = DEFAULT_MAX_IDLE_TIME;
    private ResourceStopper stopper;

    public FileResourceCache() {
        fileResources = new HashMap<Service, List<FileResource>>();
        services = new HashMap<FileResource, Service>();
        inUse = new HashSet<FileResource>();
        invalid = new HashSet<FileResource>();
        order = new LinkedList<FileResource>();
        releaseTimes = new HashMap<FileResource, Long>();
        stopper = new ResourceStopper();
    }

    public FileResource getResource(Service service)
            throws InvalidProviderException, ProviderMethodException,
            IllegalHostException, InvalidSecurityContextException,
            FileResourceException, IOException {
        if (logger.isDebugEnabled()) {
            logger.debug("Got request for resource for " + service);
        }
        FileResource fileResource;
        synchronized (this) {
            checkTimer();
            if (fileResources.containsKey(service)) {
                List<FileResource> resources = fileResources.get(service);
                Iterator<FileResource> i = resources.iterator();
                while (i.hasNext()) {
                    fileResource = i.next();
                    if (!inUse.contains(fileResource)) {
                        inUse.add(fileResource);
                        order.remove(fileResource);
                        releaseTimes.remove(fileResource);
                        if (logger.isDebugEnabled()) {
                            logger.debug("Found cached resource ("
                                    + fileResource + ")");
                        }
                        return fileResource;
                    }
                }
            }
            fileResource = newResource(service);
        }
        synchronized (fileResource) {
            if (!fileResource.isStarted()) {
                fileResource.start();
            }
        }
        return fileResource;
    }

    private FileResource newResource(Service service)
            throws InvalidProviderException, ProviderMethodException,
            IllegalHostException, InvalidSecurityContextException,
            FileResourceException, IOException {
        // TODO Are file resources reentrant?
        if (logger.isDebugEnabled()) {
            logger.debug("Instantiating new resource for " + service);
        }
        String provider = service.getProvider();
        if (provider == null) {
            throw new InvalidProviderException("Provider is null");
        }
        FileResource fileResource = AbstractionFactory
                .newFileResource(provider);
        fileResource.setService(service);
        List<FileResource> resources;
        if (fileResources.containsKey(service)) {
            resources = fileResources.get(service);
        }
        else {
            resources = new LinkedList<FileResource>();
            fileResources.put(service, resources);
        }
        resources.add(fileResource);
        services.put(fileResource, service);
        inUse.add(fileResource);
        return fileResource;
    }

    private Throwable lastRelease;

    public void releaseResource(FileResource resource) {
        if (resource == null) {
            return;
        }
        synchronized (this) {
            if (logger.isDebugEnabled()) {
                logger.debug("Releasing resource for "
                        + resource.getServiceContact() + " (" + resource + ")");
            }
            /*
             * if (!inUse.contains(resource)) { throw new
             * RuntimeException("Attempted to release resource that is not in
             * use"); }
             */
            if (inUse.remove(resource)) {
                if (invalid.remove(resource)) {
                    removeResource(resource);
                }
                else {
                    order.addLast(resource);
                    releaseTimes.put(resource, new Long(System
                            .currentTimeMillis()));
                    if (logger.isDebugEnabled()) {
                        logger.debug("Resource (" + resource
                                + ") successfully released");
                        lastRelease = new Throwable();
                    }
                }
            }
            else {
                if (logger.isDebugEnabled()) {
                    logger.debug("Resource was previously released",
                            new Throwable());
                    logger.debug("Previous release: ", lastRelease);
                }
            }
            checkIdleResourceCount();
        }
    }

    public synchronized void invalidateResource(FileResource resource) {
        invalid.add(resource);
    }

    private void removeResource(FileResource resource) {
        synchronized (this) {
            Service service = services.remove(resource);
            if (service == null) {
                return;
            }
            if (fileResources.containsKey(service)) {
                List<FileResource> resources = fileResources.get(service);
                resources.remove(resource);
                stopper.addResource(resource);
            }
            else {
                if (logger.isDebugEnabled()) {
                    logger
                            .debug("removeResource called with resource not in set");
                }
            }
        }
    }

    private void checkIdleResourceCount() {
        while (order.size() > maxIdleResources) {
            FileResource fileResource = order.removeFirst();
            if (logger.isDebugEnabled()) {
                logger
                        .debug("Idle resource count exceeded. Removing resource for "
                                + fileResource.getServiceContact());
            }
            removeResource(fileResource);
        }
    }

    public int getMaxIdleResources() {
        return maxIdleResources;
    }

    public void setMaxIdleResources(int maxResources) {
        this.maxIdleResources = maxResources;
    }

    public long getMaxIdleTime() {
        return maxIdleTime;
    }

    public void setMaxIdleTime(long maxIdleTime) {
        this.maxIdleTime = maxIdleTime;
    }

    private void checkTimer() {
        if (timer == null) {
            timer = new Timer(true);
            timer.schedule(new ResourceSwipe(this), 60000, 60000);
        }
    }

    private synchronized void checkIdleResourceAges() {
        long threshold = System.currentTimeMillis() - maxIdleTime;
        long last;
        do {
            if (order.size() == 0) {
                return;
            }
            FileResource resource = order.getFirst();
            last = releaseTimes.get(resource);
            if (last < threshold) {
                order.removeFirst();
                releaseTimes.remove(resource);
                if (logger.isDebugEnabled()) {
                    logger
                            .debug("Maximum idle time exceeded. Removing resource for "
                                    + resource.getServiceContact());
                }
                removeResource(resource);
            }
        } while (last < threshold);
    }

    private class ResourceSwipe extends TimerTask {
        private FileResourceCache cache;

        public ResourceSwipe(FileResourceCache cache) {
            this.cache = cache;
        }

        public void run() {
            cache.checkIdleResourceAges();
        }
    }

    public static class ResourceStopper implements Runnable {
        private LinkedList<FileResource> resources;
        private boolean running;

        public ResourceStopper() {
            resources = new LinkedList<FileResource>();
            running = false;
        }

        public void addResource(FileResource fr) {
            synchronized (this) {
                resources.add(fr);
                if (!running) {
                    running = true;
                    Thread t = new Thread(this);
                    t.setName("File resource stopper");
                    t.setDaemon(true);
                    t.start();
                }
            }
        }

        public void run() {
            FileResource fr = nextResource();
            while (fr != null) {
                try {
                    fr.stop();
                }
                catch (Exception e) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Failed to stop resource", e);
                    }
                    else {
                        logger.info("Failed to stop resource");
                    }
                }
                fr = nextResource();
            }
            synchronized (this) {
                running = false;
            }
        }

        private FileResource nextResource() {
            synchronized (this) {
                if (resources.isEmpty()) {
                    return null;
                }
                else {
                    return resources.removeFirst();
                }
            }
        }
    }
}