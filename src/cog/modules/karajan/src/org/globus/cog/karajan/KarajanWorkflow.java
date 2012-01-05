//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Apr 6, 2006
 */
package org.globus.cog.karajan;

import java.io.File;
import java.io.IOException;

import org.globus.cog.karajan.workflow.ElementTree;
import org.globus.cog.karajan.workflow.ExecutionContext;

/**
 * A basic interface to embedding workflows from Java
 */
public class KarajanWorkflow {
	private ElementTree tree;
	private ExecutionContext ec;

	/**
	 * Default constructor
	 */
	public KarajanWorkflow() {

	}

	/**
	 * Sets and parses the workflow contained in the given argument
	 * 
	 * @throws SpecificationException
	 *             if there is something wrong with the specification
	 */
	public void setSpecification(String spec) throws SpecificationException {
		tree = Loader.loadFromString(spec);
	}

	/**
	 * Sets and parses the workflow contained in the given file
	 * 
	 * @throws IOException
	 *             if an error occurs while reading the file
	 * @throws SpecificationException
	 *             if there is something wrong with the specification
	 */
	public void setSpecification(File spec) throws SpecificationException, IOException {
		tree = Loader.load(spec.getAbsolutePath());
	}

	/**
	 * Starts the workflow
	 * 
	 * @throws SpecificationException
	 *             if no specification was set
	 * @throws IllegaleStateException
	 *             if this workflow was started before
	 */
	public synchronized void start() throws IllegalStateException, SpecificationException {
		if (tree == null) {
			throw new SpecificationException("No specification");
		}
		if (ec != null) {
			throw new IllegalStateException();
		}
		ec = new ExecutionContext(tree);
		ec.start();
	}

	/**
	 * Waits for the workflow to finish
	 * 
	 * @throws IllegalStateException
	 *             if the workflow was never started
	 * @throws InterruptedException
	 */
	public synchronized void waitFor() throws IllegalStateException, InterruptedException {
		if (ec == null) {
			throw new IllegalStateException();
		}
		ec.waitFor();
	}
	
	/**
	 * Checks if the workflow has finished executing
	 */
	public boolean isDone() {
		return (ec != null && ec.done());
	}
	
	/**
	 * Check if the workflow has failed
	 */
	public boolean isFailed() {
		return (ec != null && ec.isFailed());
	}
	
	/**
	 * Get the throwable that caused the workflow to fail. Returns
	 * <code>null</code> if the workflow has not failed
	 */
	public Throwable getFailure() {
		return ec != null?ec.getFailure():null;
	}
}
