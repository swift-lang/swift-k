// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.karajan.workflow.nodes.grid;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.common.task.CachingFileTransferTaskHandler;
import org.globus.cog.abstraction.impl.common.task.FileTransferSpecificationImpl;
import org.globus.cog.abstraction.impl.common.task.TaskImpl;
import org.globus.cog.abstraction.impl.file.gridftp.GridFTPConstants;
import org.globus.cog.abstraction.interfaces.Service;
import org.globus.cog.abstraction.interfaces.StatusListener;
import org.globus.cog.abstraction.interfaces.Task;
import org.globus.cog.abstraction.interfaces.TaskHandler;
import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.scheduler.Scheduler;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.util.BoundContact;
import org.globus.cog.karajan.util.Contact;
import org.globus.cog.karajan.util.TypeUtil;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.globus.cog.karajan.workflow.events.MonitoringEvent;
import org.globus.cog.karajan.workflow.events.ProgressMonitoringEvent;
import org.globus.cog.karajan.workflow.events.ProgressMonitoringEventType;
import org.globus.cog.karajan.workflow.nodes.FlowNode;

public class GridTransfer extends AbstractGridNode implements StatusListener {
	public static final Logger logger = Logger.getLogger(GridTransfer.class);

	public static final Arg A_SRCFILE = new Arg.Optional("srcfile");
	public static final Arg A_SRCDIR = new Arg.Optional("srcdir", "");
	public static final Arg A_SRCHOST = new Arg.Optional("srchost");
	public static final Arg A_SRCPROVIDER = new Arg.Optional("srcprovider");
	public static final Arg A_SRCOFFSET = new Arg.Optional("srcoffset");

	public static final Arg A_DESTFILE = new Arg.Optional("destfile");
	public static final Arg A_DESTDIR = new Arg.Optional("destdir", "");
	public static final Arg A_DESTHOST = new Arg.Optional("desthost");
	public static final Arg A_DESTPROVIDER = new Arg.Optional("destprovider");
	public static final Arg A_DESTOFFSET = new Arg.Optional("destoffset");

	public static final Arg A_PROVIDER = new Arg.Optional("provider");
	public static final Arg A_THIRDPARTY = new Arg.Optional("thirdparty", Boolean.FALSE);
	public static final Arg A_LENGTH = new Arg.Optional("length");

	public static final Arg A_TCP_BUFSZ = new Arg.Optional("tcpBufferSize");

	private static Timer timer;
	private static HashMap pollTasks;
	static {
		pollTasks = new HashMap();
	}

	static {
		setArguments(GridTransfer.class, new Arg[] { A_SRCFILE, A_SRCDIR, A_DESTFILE, A_DESTDIR,
				A_SRCHOST, A_DESTHOST, A_PROVIDER, A_SRCPROVIDER, A_DESTPROVIDER, A_THIRDPARTY,
				A_SRCOFFSET, A_LENGTH, A_DESTOFFSET, A_TCP_BUFSZ });
	}

	public GridTransfer() {
		setElementType("gridTransfer");
	}

	public void submitTask(VariableStack stack) throws ExecutionException {
		try {
			FileTransferSpecificationImpl fs = new FileTransferSpecificationImpl();
			Task task = new TaskImpl();
			Scheduler scheduler = getScheduler(stack);
			Contact sourceContact, destinationContact;

			String srcfile = TypeUtil.toString(A_SRCFILE.getValue(stack));
			String destfile = TypeUtil.toString(A_DESTFILE.getValue(stack, srcfile));

			String srcprovider = null, destprovider = null;

			if (A_PROVIDER.isPresent(stack)) {
				srcprovider = TypeUtil.toString(A_PROVIDER.getValue(stack));
				destprovider = srcprovider;
			}
			srcprovider = TypeUtil.toString(A_SRCPROVIDER.getValue(stack, srcprovider));
			destprovider = TypeUtil.toString(A_SRCPROVIDER.getValue(stack, destprovider));

			fs.setSourceFile(srcfile);
			fs.setDestinationFile(destfile);

			fs.setSourceDirectory(TypeUtil.toString(A_SRCDIR.getValue(stack)));
			fs.setDestinationDirectory(TypeUtil.toString(A_DESTDIR.getValue(stack)));

			if (A_SRCHOST.isPresent(stack)) {
				sourceContact = getHost(stack, A_SRCHOST, scheduler, srcprovider);
			}
			else {
				sourceContact = BoundContact.LOCALHOST;
			}

			if (A_DESTHOST.isPresent(stack)) {
				destinationContact = getHost(stack, A_DESTHOST, scheduler, destprovider);
			}
			else {
				destinationContact = BoundContact.LOCALHOST;
			}

			if (A_THIRDPARTY.isPresent(stack)) {
				fs.setThirdParty(TypeUtil.toBoolean(A_THIRDPARTY.getValue(stack)));
			}

			if (A_SRCOFFSET.isPresent(stack)) {
				fs.setSourceOffset(TypeUtil.toNumber(A_SRCOFFSET.getValue(stack)).longValue());
			}

			if (A_DESTOFFSET.isPresent(stack)) {
				fs.setDestinationOffset(TypeUtil.toNumber(A_DESTOFFSET.getValue(stack)).longValue());
			}

			if (A_LENGTH.isPresent(stack)) {
				fs.setSourceLength(TypeUtil.toNumber(A_LENGTH.getValue(stack)).longValue());
			}

			if (A_TCP_BUFSZ.isPresent(stack)) {
				fs.setAttribute(GridFTPConstants.ATTR_TCP_BUFFER_SIZE,
						TypeUtil.toString(A_TCP_BUFSZ.getValue(stack)));
			}

			if (sourceContact.equals(BoundContact.LOCALHOST) && fs.getSourceDirectory().equals("")) {
				fs.setSourceDirectory(stack.getExecutionContext().getBasedir());
			}

			if (destinationContact.equals(BoundContact.LOCALHOST)
					&& fs.getDestinationDirectory().equals("")) {
				fs.setDestinationDirectory(stack.getExecutionContext().getBasedir());
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
			if (stack.getExecutionContext().isMonitoringEnabled()) {
				TransferProgressPoll tpp = new TransferProgressPoll(this, task, stack.copy());
				synchronized (pollTasks) {
					pollTasks.put(task, tpp);
				}
				getTimer().schedule(tpp, 5000, 5000);
			}
		}
		catch (ExecutionException e) {
			throw e;
		}
		catch (Exception e) {
			throw new ExecutionException("Exception caught while submitting job", e);
		}
	}

	private static synchronized Timer getTimer() {
		if (timer == null) {
			timer = new Timer();
		}
		return timer;
	}

	protected void removeTask(Task task) {
		super.removeTask(task);
		TransferProgressPoll tpp = null;
		synchronized (pollTasks) {
			tpp = (TransferProgressPoll) pollTasks.remove(task);
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
		private final VariableStack stack;

		private final FlowNode element;

		private final Task task;

		public TransferProgressPoll(FlowNode element, Task task, VariableStack stack) {
			this.element = element;
			this.task = task;
			this.stack = stack;
		}

		public void run() {
			try {
				long crt = TypeUtil.toNumber(task.getAttribute("transferedBytes")).longValue();
				long total = TypeUtil.toNumber(task.getAttribute("totalBytes")).longValue();
				MonitoringEvent me = new ProgressMonitoringEvent(element,
						ProgressMonitoringEventType.TRANSFER_PROGRESS, stack, crt, total);
				element.fireMonitoringEvent(me);
			}
			catch (Exception e) {
				logger.warn("Exception caught while sending monitoring event", e);
			}
		}
	}
}