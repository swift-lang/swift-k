package org.globus.cog.karajan.compiled.nodes.restartLog;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.channels.FileLock;

import org.apache.log4j.Logger;

/**
 * A writer that will sync buffers to disks when flush() is called.
 * 
 */
public class FlushableLockedFileWriter extends OutputStreamWriter {
	public static final Logger logger = Logger.getLogger(FlushableLockedFileWriter.class);

	private final FileOutputStream fos;
	private File file;
	private FileLock lock;
	private SyncThread syncThread;

	public FlushableLockedFileWriter(File f, boolean append) throws IOException {
		this(new FileOutputStream(f, append));
		this.file = f;
		try {
			this.lock = fos.getChannel().tryLock();
		}
		catch (IOException e) {
			logger.info("Could not acquire lock on " + f, e);
		}
	}

	private FlushableLockedFileWriter(FileOutputStream fos) {
		super(fos);
		this.fos = fos;
		syncThread = new SyncThread(this);
		syncThread.start();
	}

	public void flush() throws IOException {
		syncThread.flush();
	}

	public void actualFlush() throws IOException {
		if (isLocked()) {
			super.flush();
			fos.getFD().sync();
		}
	}

	public File getFile() {
		return file;
	}

	public void close() throws IOException {
		if (lock != null) {
			lock.release();
		}
		super.close();
	}

	public boolean isLocked() {
		return lock != null && lock.isValid();
	}

	public boolean lockExists() {
		return lock != null;
	}
}