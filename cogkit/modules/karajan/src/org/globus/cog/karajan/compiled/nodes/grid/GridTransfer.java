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
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.karajan.compiled.nodes.grid;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import k.rt.ExecutionException;
import k.rt.Stack;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.common.task.CachingFileTransferTaskHandler;
import org.globus.cog.abstraction.impl.common.task.FileTransferSpecificationImpl;
import org.globus.cog.abstraction.impl.common.task.TaskImpl;
import org.globus.cog.abstraction.impl.file.gridftp.GridFTPConstants;
import org.globus.cog.abstraction.interfaces.Service;
import org.globus.cog.abstraction.interfaces.Task;
import org.globus.cog.abstraction.interfaces.TaskHandler;
import org.globus.cog.karajan.analyzer.ArgRef;
import org.globus.cog.karajan.analyzer.Scope;
import org.globus.cog.karajan.analyzer.Signature;
import org.globus.cog.karajan.analyzer.VarRef;
import org.globus.cog.karajan.compiled.nodes.Node;
import org.globus.cog.karajan.scheduler.Scheduler;
import org.globus.cog.karajan.util.BoundContact;
import org.globus.cog.karajan.util.Contact;
import org.globus.cog.karajan.util.TypeUtil;

public class GridTransfer extends AbstractGridNode {
	public static final Logger logger = Logger.getLogger(GridTransfer.class);
	
	private ArgRef<String> srcfile;
	private ArgRef<String> srcdir;
	private ArgRef<Object> srchost;
	private ArgRef<String> srcprovider;
	private ArgRef<Number> srcoffset;
	
	private ArgRef<String> destfile;
	private ArgRef<String> destdir;
	private ArgRef<Object> desthost;
	private ArgRef<String> destprovider;
	private ArgRef<Number> destoffset;
	
	private ArgRef<String> provider;
	private ArgRef<Boolean> thirdparty;
	
	private ArgRef<Number> length;
	private ArgRef<Number> tcpBufferSize;

	private VarRef<String> cwd;
	
	@Override
	protected Signature getSignature() {
		return new Signature(
				params(
						"srcfile", optional("srcdir", null), optional("destdir", null), 
						optional("destfile", null),	optional("srchost", null),
						optional("desthost", null), optional("provider", null),
						optional("srcprovider", null), optional("destprovider", null),
						optional("thirdparty", null),
						optional("srcoffset", null), optional("length", null),
						optional("destoffset", null), optional("tcpBufferSize", null)
				)
		);
	}

	private static Timer timer;
	private static Map<Task, TransferProgressPoll> pollTasks;
	static {
		pollTasks = new HashMap<Task, TransferProgressPoll>();
	}
	
	@Override
	protected void addLocals(Scope scope) {
		super.addLocals(scope);
		cwd = scope.getVarRef("CWD");
	}

	public void submitTask(Stack stack) {
		try {
			FileTransferSpecificationImpl fs = new FileTransferSpecificationImpl();
			Task task = new TaskImpl();
			Scheduler scheduler = getScheduler(stack);
			Contact sourceContact, destinationContact;

			String srcfile = this.srcfile.getValue(stack);
			String destfile = this.destfile.getValue(stack);
			if (destfile == null) {
				destfile = srcfile;
			}

			String srcprovider = this.srcprovider.getValue(stack); 
			String destprovider = this.destprovider.getValue(stack);
			String provider = this.provider.getValue(stack);

			if (provider != null) {
				if (srcprovider == null) {
					srcprovider = provider;
				}
				if (destprovider == null) {
					destprovider = provider;
				}
			}
			
			fs.setSourceFile(srcfile);
			fs.setDestinationFile(destfile);

			fs.setSourceDirectory(this.srcdir.getValue(stack));
			fs.setDestinationDirectory(this.destdir.getValue(stack));

			Object srchost = this.srchost.getValue(stack);
			if (srchost != null) {
				sourceContact = getHost(srchost, scheduler, srcprovider);
			}
			else {
				sourceContact = BoundContact.LOCALHOST;
			}
			
			Object desthost = this.desthost.getValue(stack);

			if (desthost != null) {
				destinationContact = getHost(desthost, scheduler, destprovider);
			}
			else {
				destinationContact = BoundContact.LOCALHOST;
			}

			Boolean thirdparty = this.thirdparty.getValue(stack);
			if (thirdparty != null) {
				fs.setThirdParty(thirdparty);
			}
			else {
				fs.setThirdPartyIfPossible(true);
			}

			Number srcoffset = this.srcoffset.getValue(stack);
			if (srcoffset != null) {
				fs.setSourceOffset(srcoffset.longValue());
			}
			
			Number destoffset = this.destoffset.getValue(stack);
			if (destoffset != null) {
				fs.setDestinationOffset(destoffset.longValue());
			}
			
			Number length = this.length.getValue(stack);
			if (length != null) {
				fs.setSourceLength(length.longValue());
			}

			Number tcpBufsz = this.tcpBufferSize.getValue(stack);
			if (tcpBufsz != null) {
				fs.setAttribute(GridFTPConstants.ATTR_TCP_BUFFER_SIZE, String.valueOf(tcpBufsz));
			}

			if (sourceContact.equals(BoundContact.LOCALHOST)
					&& isRelative(fs.getSourceDirectory())) {
				fs.setSourceDirectory(pathcat(cwd.getValue(stack),
						fs.getSourceDirectory()));
			}

			if (destinationContact.equals(BoundContact.LOCALHOST)
					&& isRelative(fs.getDestinationDirectory())) {
				fs.setDestinationDirectory(pathcat(cwd.getValue(stack),
						fs.getDestinationDirectory()));
			}

			task.setType(Task.FILE_TRANSFER);
			task.setRequiredService(2);
			task.setSpecification(fs);

			if (scheduler == null) {
				TaskHandler handler = new CachingFileTransferTaskHandler();
				Service s, d;
				s = getService((BoundContact) sourceContact, srcprovider);

				if (s == null) {
					throw new ExecutionException(
							"Could not find a valid transfer/operation provider for "
									+ sourceContact);
				}

				d = getService((BoundContact) destinationContact, destprovider);

				if (d == null) {
					throw new ExecutionException(
							"Could not find a valid transfer/operation provider for "
									+ destinationContact);
				}

				setSecurityContextIfNotLocal(s, getSecurityContext(stack, s.getProvider()));
				setSecurityContextIfNotLocal(d, getSecurityContext(stack, d.getProvider()));

				task.setService(Service.FILE_TRANSFER_SOURCE_SERVICE, s);
				task.setService(Service.FILE_TRANSFER_DESTINATION_SERVICE, d);
				submitUnscheduled(handler, task, stack);
			}
			else {
				submitScheduled(scheduler, task, stack, new Contact[] { sourceContact,
						destinationContact });
			}

			TransferProgressPoll tpp = new TransferProgressPoll(this, task);
			synchronized (pollTasks) {
				pollTasks.put(task, tpp);
			}
			getTimer().schedule(tpp, 5000, 5000);
		}
		catch (Exception e) {
			throw new ExecutionException("Exception caught while submitting job", e);
		}
	}

	private boolean isRelative(String dir) {
		return dir == null || !new File(dir).isAbsolute();
	}
	
	private String pathcat(String a, String b) {
		if (b == null) {
			return a;
		}
		else {
			return a + File.separator + b;
		}
	}

	private static synchronized Timer getTimer() {
		if (timer == null) {
			timer = new Timer();
		}
		return timer;
	}

	protected void removeTask(Task task) {
		TransferProgressPoll tpp = null;
		synchronized (pollTasks) {
			tpp = pollTasks.remove(task);
		}
		if (tpp != null) {
			tpp.cancel();
		}
	}

	protected Service getService(BoundContact contact, String provider) throws ExecutionException {
		if (contact.equals(BoundContact.LOCALHOST)) {
			return contact.getService(Service.FILE_OPERATION, "local");
		}
		else {
			Service s = contact.getService(Service.FILE_OPERATION, provider);
			if (s == null) {
				return contact.getService(Service.FILE_TRANSFER, provider);
			}
			else {
				return s;
			}
		}
	}

	private class TransferProgressPoll extends TimerTask {
		private final Node element;

		private final Task task;

		public TransferProgressPoll(Node element, Task task) {
			this.element = element;
			this.task = task;
		}

		public void run() {
			try {
				Object attrcrt = task.getAttribute("transferedBytes");
				if (attrcrt == null) {
					return;
				}
				long crt = TypeUtil.toNumber(attrcrt).longValue();
				Object attrtotal = task.getAttribute("totalBytes");
				if (attrtotal == null) {
					return;
				}
				long total = TypeUtil.toNumber(attrtotal).longValue();
			}
			catch (Exception e) {
				logger.warn("Exception caught while sending monitoring event", e);
			}
		}
	}
}