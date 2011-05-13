/*
 * Created on Jun 12, 2006
 */
package org.griphyn.vdl.karajan;

import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.interfaces.FileOperationSpecification;
import org.globus.cog.abstraction.interfaces.FileTransferSpecification;
import org.globus.cog.abstraction.interfaces.JobSpecification;
import org.globus.cog.abstraction.interfaces.Service;
import org.globus.cog.abstraction.interfaces.Task;
import org.globus.cog.karajan.scheduler.TaskTransformer;
import org.globus.cog.karajan.util.BoundContact;
import org.globus.cog.karajan.util.Contact;
import org.globus.cog.karajan.workflow.KarajanRuntimeException;
import org.globus.swift.catalog.TCEntry;
import org.globus.swift.catalog.types.TCType;
import org.griphyn.vdl.util.FQN;
import org.griphyn.vdl.util.VDL2Config;

public class VDSTaskTransformer implements TaskTransformer {
	public static final Logger logger = Logger.getLogger(VDSTaskTransformer.class);

	private TaskTransformer impl;

	public VDSTaskTransformer(TCCache tc) {
		this.impl = new TCTransformer(tc);
	}

	public void transformTask(Task task, Contact[] contacts, Service[] services) {
		impl.transformTask(task, contacts, services);
	}

	public static abstract class AbstractTransformer implements TaskTransformer {

		public void transformTask(Task task, Contact[] contacts, Service[] services) {
			if (task.getType() == Task.JOB_SUBMISSION) {
				applyJobWorkDirectory(task, contacts);
				applyTCEntry(task, contacts);
			}
			else if (task.getType() == Task.FILE_TRANSFER) {
				applyTransferWorkDirectory(task, contacts);
			}
			else if (task.getType() == Task.FILE_OPERATION) {
				applyFileOpWorkDirectory(task, contacts);
			}
		}

		private static Set opsWithDirInFirstArg = new HashSet();
		static {
			Set s = opsWithDirInFirstArg;
			s.add(FileOperationSpecification.LS);
			s.add(FileOperationSpecification.MKDIR);
			s.add(FileOperationSpecification.MKDIRS);
			s.add(FileOperationSpecification.RMDIR);
			s.add(FileOperationSpecification.RMFILE);
			s.add(FileOperationSpecification.ISDIRECTORY);
			s.add(FileOperationSpecification.EXISTS);
			s.add(FileOperationSpecification.CHMOD);
		}

		private void applyFileOpWorkDirectory(Task task, Contact[] contacts) {
			FileOperationSpecification spec = (FileOperationSpecification) task.getSpecification();
			BoundContact bc = (BoundContact) contacts[0];
			String workdir = (String) bc.getProperty("workdir");
			if (workdir != null) {
				String op = spec.getOperation();
				if (opsWithDirInFirstArg.contains(op)) {
					if (spec.getArgumentSize() >= 1) {
						String arg = spec.getArgument(0);
						if (arg != null && !arg.startsWith("/")) {
							spec.setArgument(workdir + '/' + arg, 0);
						}
					}
				}
			}
		}

		private void applyTransferWorkDirectory(Task task, Contact[] contacts) {
			FileTransferSpecification spec = (FileTransferSpecification) task.getSpecification();
			BoundContact src = (BoundContact) contacts[0];
			String srcdir = spec.getSourceDirectory();
			if (srcdir == null || !srcdir.startsWith("/")) {
				String workdir = (String) src.getProperty("workdir");
				if (workdir != null) {
					spec.setSourceDirectory(workdir + '/' + srcdir);
				}
			}

			BoundContact dst = (BoundContact) contacts[1];
			String dstdir = spec.getDestinationDirectory();
			if (dstdir == null || !dstdir.startsWith("/")) {
				String workdir = (String) dst.getProperty("workdir");
				if (workdir != null) {
					spec.setDestinationDirectory(workdir + '/' + dstdir);
				}
			}
		}

		private void applyJobWorkDirectory(Task task, Contact[] contacts) {
			JobSpecification spec = (JobSpecification) task.getSpecification();
			String dir = spec.getDirectory();
			BoundContact bc = (BoundContact) contacts[0];
			String workdir = (String) bc.getProperty("workdir");
			if (dir == null || !dir.startsWith("/")) {
				if (workdir != null) {
					if (dir == null) {
						spec.setDirectory(workdir);
					}
					else {
						spec.setDirectory(workdir + '/' + dir);
					}
				}
			}
			List l =   spec.getArgumentsAsList();
			// perhaps should check for /bin/bash in the executable, or some other way of detecting we need to do a substitution here... or equally could assume that the second parameter always needs to undergo this substitution...
			String executable = (String)l.get(0);

			try {
				VDL2Config config = VDL2Config.getConfig();

				if(config.getProperty("wrapper.invocation.mode", bc).equals("absolute")
			 	 &&(executable.endsWith("shared/_swiftwrap")
			   	  || executable.endsWith("shared/_swiftseq"))) {

				String s  = spec.getDirectory()+"/"+executable;
				l.set(0,s);
				}
			} 
			catch(IOException ioe) {
				throw new KarajanRuntimeException("Could not determine wrapper invocation mode", ioe);
			}
		}

		protected abstract void applyTCEntry(Task task, Contact[] contacts);
	}

	public static class TCTransformer extends AbstractTransformer {
		private TCCache tc;
		private Set warnset = new HashSet();

		public TCTransformer(TCCache tc) {
			this.tc = tc;
		}

		protected void applyTCEntry(Task task, Contact[] contacts) {
			JobSpecification spec = (JobSpecification) task.getSpecification();
			BoundContact bc = (BoundContact) contacts[0];

			FQN fqn = new FQN(spec.getExecutable());
			List l;
			try {
				l = tc.getTCEntries(fqn, bc.getHost(), TCType.INSTALLED);
			}
			catch (Exception e) {
				throw new KarajanRuntimeException(e);
			}
			if (l == null || l.isEmpty()) {
				return;
			}
			if (l.size() > 1) {
				synchronized (warnset) {
					LinkedList wl = new LinkedList();
					wl.add(fqn);
					wl.add(bc);
					if (!warnset.contains(wl)) {
						logger.warn("Multiple entries found for " + fqn + " on " + bc
								+ ". Using the first one");
						warnset.add(wl);
					}
				}
			}

			TCEntry tce = (TCEntry) l.get(0);
			spec.setExecutable(tce.getPhysicalTransformation());
		}
	}
}
