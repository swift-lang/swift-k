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
 * Created on Apr 7, 2005
 */
package org.globus.cog.karajan.compiled.nodes.grid;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import k.rt.ExecutionException;
import k.rt.Stack;

import org.globus.cog.abstraction.impl.common.StatusEvent;
import org.globus.cog.abstraction.interfaces.FileOperationSpecification;
import org.globus.cog.abstraction.interfaces.GridFile;
import org.globus.cog.abstraction.interfaces.Task;
import org.globus.cog.karajan.analyzer.ArgRef;
import org.globus.cog.karajan.analyzer.ChannelRef;
import org.globus.cog.karajan.analyzer.Signature;
import org.globus.cog.karajan.util.BoundContact;

public class FileList extends AbstractFileOperation {
	private ArgRef<String> dir;
	private ChannelRef<Object> cr_vargs;

	@Override
	protected Signature getSignature() {
		return new Signature(
				params("dir", optional("host", BoundContact.LOCALHOST),
						optional("securityContext", null),
						optional("provider", "local")),
				returns(channel("...", DYNAMIC))
		);
	}

	protected String getOperation(Stack stack) throws ExecutionException {
		return FileOperationSpecification.LS;
	}

	protected String[] getArguments(Stack stack) throws ExecutionException {
		String dir = this.dir.getValue(stack);
		if (dir != null) {
			return new String[] { dir };
		}
		else {
			return new String[] { };
		}
	}

	protected void taskCompleted(StatusEvent e, Stack stack) throws ExecutionException {
		try {
			Task task = (Task) e.getSource();
			@SuppressWarnings("unchecked")
			Collection<GridFile> list = (Collection<GridFile>) task.getAttribute("output");
			List<String> files = new ArrayList<String>();
			for (GridFile gf : list) {
				files.add(gf.getName());
			}
			cr_vargs.append(stack, files);
		}
		catch (Exception ex) {
			throw new ExecutionException("Exception caugh while retrieving output", ex);
		}
		super.taskCompleted(e, stack);
	}
}
