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

//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Sep 6, 2005
 */
package org.globus.cog.coaster.channels;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.net.URI;
import java.util.EnumSet;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

import org.apache.log4j.Logger;
import org.globus.cog.coaster.FallbackAuthorization;
import org.globus.cog.coaster.GSSService;
import org.globus.cog.coaster.RequestManager;
import org.globus.cog.coaster.UserContext;
import org.globus.cog.coaster.channels.ChannelOptions.CompressionType;
import org.globus.cog.coaster.channels.ChannelOptions.Type;
import org.globus.cog.coaster.commands.ChannelConfigurationCommand;
import org.globus.cog.coaster.commands.ShutdownCommand;
import org.globus.gsi.GSIConstants;
import org.globus.gsi.gssapi.GSSConstants;
import org.globus.gsi.gssapi.GlobusGSSManagerImpl;
import org.globus.gsi.gssapi.auth.Authorization;
import org.globus.gsi.gssapi.auth.HostAuthorization;
import org.globus.gsi.gssapi.auth.SelfAuthorization;
import org.globus.gsi.gssapi.net.GssSocket;
import org.globus.gsi.gssapi.net.GssSocketFactory;
import org.gridforum.jgss.ExtendedGSSContext;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSManager;

public class GSSChannel extends AbstractTCPChannel {
	private static final Logger logger = Logger.getLogger(GSSChannel.class);
	
	private static final boolean streamCompression;
	
	static {
	    boolean st;
	    try {
	        DeflaterOutputStream.class.getConstructor(OutputStream.class, boolean.class);
	        st = true;
	    }
	    catch (Exception e) {
	        logger.info("Exception caught trying to find DeflaterOutputStream(OutputStream, boolean) " +
	        		"constructor. Disabling stream compression.");
	        st = false;
	    }
	    streamCompression = st;
	}

	private GssSocket socket;
	private String peerId;
	private boolean shuttingDown;
	private Exception startException;
	private int id;
	private static int sid = 1;

	public GSSChannel(GssSocket socket, RequestManager requestManager, UserContext userContext)
			throws IOException {
		super(requestManager, userContext, false);
		setSocket(socket);
		this.socket = socket;
		init();
	}

	public GSSChannel(URI contact, RequestManager requestManager, UserContext userContext) {
		super(requestManager, userContext, true);
		setContact(contact);
		init();
	}

	private void init() {
		id = sid++;
	}

	public void start() throws ChannelException {
		connect();
		super.start();
	}

	protected void connect() throws ChannelException {
		try {
			if (getContact() != null) {
				HostAuthorization hostAuthz = new HostAuthorization("host");

				Authorization authz = new FallbackAuthorization(new Authorization[] { hostAuthz,
						SelfAuthorization.getInstance() });

				GSSCredential cred = this.getUserContext().getCredential();
				if (cred == null) {
					cred = GSSService.initializeCredentials(true, null, null);
				}

				GSSManager manager = new GlobusGSSManagerImpl();
				ExtendedGSSContext gssContext = (ExtendedGSSContext) manager.createContext(null,
						GSSConstants.MECH_OID, cred, cred.getRemainingLifetime());

				gssContext.requestAnonymity(false);
				gssContext.requestCredDeleg(false);
				//gssContext.requestConf(false);
				gssContext.setOption(GSSConstants.GSS_MODE, GSIConstants.MODE_SSL);
				gssContext.setOption(GSSConstants.DELEGATION_TYPE,
						GSIConstants.DELEGATION_TYPE_LIMITED);
				URI contact = getContact();
				socket = (GssSocket) GssSocketFactory.getDefault().createSocket(contact.getHost(),
						contact.getPort(), gssContext);

				socket.setKeepAlive(true);
				socket.setSoTimeout(0);
				socket.setWrapMode(GSIConstants.MODE_SSL.intValue());
				socket.setAuthorization(authz);
				setSocket(socket);

				logger.info("Connected to " + contact);

				setName(contact.toString());
				
				if (streamCompression) {
				    EnumSet<CompressionType> t = EnumSet.noneOf(CompressionType.class);
				    t.add(CompressionType.NONE);
				    t.add(CompressionType.DEFLATE);
				    ChannelConfigurationCommand ccc = new ChannelConfigurationCommand(t);
				    ccc.execute(this);
				}
			}
		}
		catch (Exception e) {
			throw new ChannelException("Failed to start channel " + this, e);
		}
	}

	protected void initializeConnection() {
		try {
			if (socket.getContext().isEstablished()) {
				UserContext uc = new UserContext();
				// TODO Credentials should be associated with each
				// individual instance

				// X509Certificate[] chain = (X509Certificate[])
				// ((ExtendedGSSContext)
				// socket.getContext()).inquireByOid(GSSConstants.X509_CERT_CHAIN);
				if (socket.getContext().getCredDelegState()) {
					uc.setCredential(socket.getContext().getDelegCred());
				}
				else {
					uc.setCredential(GSSService.initializeCredentials(true, null, null));
				}
				peerId = uc.getName();
				logger.debug(getContact() + "Peer identity: " + peerId);
			}
			else {
				throw new IOException("Context not established");
			}
		}
		catch (Exception e) {
			logger.warn(getContact() + "Could not get client identity", e);
		}
	}

	public void shutdown() {
		synchronized (this) {
			if (isClosed()) {
				return;
			}
			if (!isLocalShutdown() && isClient()) {
				try {
					ShutdownCommand sc = new ShutdownCommand();
					logger.debug(getContact() + "Initiating remote shutdown");
					sc.execute(this);
					logger.debug(getContact() + "Remote shutdown ok");
				}
				catch (Exception e) {
					logger.warn(getContact() + "Failed to shut down channel nicely", e);
				}
				super.shutdown();
				close();
			}
		}
	}

    @Override
    public boolean supportsOption(Type type, Object value) {
        if (type.equals(ChannelOptions.Type.COMPRESSION)) {
            if (value == null) {
                return false;
            }
            else if (value.equals(CompressionType.NONE)) {
                return true;
            }
            else if (value.equals(CompressionType.DEFLATE)) {
                return streamCompression;
            }
            else {
                return false;
            }
        }
        else {
            return super.supportsOption(type, value);
        }
    }

    @Override
    public void setOption(Type type, Object value) {
        if (type.equals(ChannelOptions.Type.COMPRESSION) || 
                type.equals(ChannelOptions.Type.UP_COMPRESSION)  || 
                type.equals(ChannelOptions.Type.DOWN_COMPRESSION)) {
            
            if (CompressionType.NONE.equals(value)) {
                // do nothing
            }
            else if (CompressionType.DEFLATE.equals(value)) {
                if (type.equals(ChannelOptions.Type.COMPRESSION) || 
                        type.equals(ChannelOptions.Type.UP_COMPRESSION)) {
                    compressOutput();
                }
                if (type.equals(ChannelOptions.Type.COMPRESSION) || 
                        type.equals(ChannelOptions.Type.DOWN_COMPRESSION)) {
                    compressInput();
                }
            }
            else {
                throw new IllegalArgumentException("Unsupported compression type: " + value);
            }
        }
        else {
            super.setOption(type, value);
        }
    }

    private void compressInput() {
        setInputStream(new InflaterInputStream(getInputStream()));
    }

    private void compressOutput() {
        try {
            /*
             * Instantiate DeflaterOutputStream(out, syncFlush) using reflection
             * since it is only available in 1.7, but we still want this
             * to compile on 1.6. 
             */
            Constructor<DeflaterOutputStream> cons = 
                DeflaterOutputStream.class.getConstructor(OutputStream.class, boolean.class);
            DeflaterOutputStream dos = cons.newInstance(getOutputStream(), true);
            setOutputStream(dos);
        }
        catch (Exception e) {
            logger.warn("Failed to instantiate DeflaterOutputStream", e);
            throw new RuntimeException(e);
        }
    }

    protected void register() {
		getMultiplexer(SLOW).register(this);
	}

	public String getPeerId() {
		return peerId;
	}
}
