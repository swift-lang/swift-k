package org.globus.cog.karajan.workflow.nodes.restartLog;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.channels.FileLock;

/**
 * A writer that will sync buffers to disks when flush() is called.
 * 
 */
public class FlushableLockedFileWriter extends OutputStreamWriter {
	private final FileOutputStream fos;
	private File file;
	private FileLock lock;

	public FlushableLockedFileWriter(File f, boolean append) throws IOException {
		this(new FileOutputStream(f, append));
		this.file = f;
		this.lock = fos.getChannel().tryLock();
	}

	private FlushableLockedFileWriter(FileOutputStream fos) {
		super(fos);
		this.fos = fos;
	}

	public void flush() throws IOException {
		super.flush();
		fos.getFD().sync();
	}

	public File getFile() {
		return file;
	}

	public void close() throws IOException {
		lock.release();
		super.close();
	}
	
	public boolean isLocked() {
		return lock != null && lock.isValid();
	}
}