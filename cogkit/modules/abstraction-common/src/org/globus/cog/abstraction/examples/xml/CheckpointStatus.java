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

package org.globus.cog.abstraction.examples.xml;

import java.io.File;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.common.StatusEvent;
import org.globus.cog.abstraction.impl.common.task.GenericTaskHandler;
import org.globus.cog.abstraction.impl.common.task.IllegalSpecException;
import org.globus.cog.abstraction.impl.common.task.InvalidSecurityContextException;
import org.globus.cog.abstraction.impl.common.task.InvalidServiceContactException;
import org.globus.cog.abstraction.impl.common.task.TaskSubmissionException;
import org.globus.cog.abstraction.interfaces.Status;
import org.globus.cog.abstraction.interfaces.StatusListener;
import org.globus.cog.abstraction.interfaces.Task;
import org.globus.cog.abstraction.xml.TaskUnmarshaller;
import org.globus.cog.util.ArgumentParser;
import org.globus.cog.util.ArgumentParserException;

public class CheckpointStatus implements StatusListener {
    static Logger logger = Logger.getLogger(CheckpointStatus.class.getName());
    private Task task;
    private String checkpointFile = null;

    private void unmarshal() {
        try {
            File xmlFile = new File(this.checkpointFile);
            this.task = TaskUnmarshaller.unmarshal(xmlFile);
            this.task.addStatusListener(this);
        } catch (Exception e) {
            logger.error("Cannot unmarshal task", e);
        }
    }

    private void submit() {
        GenericTaskHandler handler = new GenericTaskHandler();
        try {
            handler.submit(this.task);
        } catch (InvalidSecurityContextException ise) {
            logger.error("Security Exception");
            ise.printStackTrace();
            System.exit(1);
        } catch (TaskSubmissionException tse) {
            logger.error("TaskSubmission Exception");
            tse.printStackTrace();
            System.exit(1);
        } catch (IllegalSpecException ispe) {
            logger.error("Specification Exception");
            ispe.printStackTrace();
            System.exit(1);
        } catch (InvalidServiceContactException isce) {
            logger.error("Service Contact Exception");
            isce.printStackTrace();
            System.exit(1);
        }
    }

    public void statusChanged(StatusEvent event) {
        Status status = event.getStatus();
        System.out.println("Current status = " + status.getStatusString());
        if (status.getStatusCode() == Status.FAILED) {
            logger.debug("Error = " + this.task.getStdError());
            this.task.getStatus().getException().printStackTrace();
        }
        System.exit(0);
    }

    public static void main(String args[]) {
        new CheckpointStatus();

        ArgumentParser ap = new ArgumentParser();
        ap.setExecutableName("cog-checkpoint-status");
        ap.addOption("checkpoint", "Input checkpoint file", "fileName",
                ArgumentParser.NORMAL);
        ap.addAlias("checkpoint", "c");
        ap.addFlag("verbose",
                "If enabled, display information about what is being done");
        ap.addAlias("verbose", "v");
        ap.addFlag("help", "Display usage");
        ap.addAlias("help", "h");
        try {
            ap.parse(args);
            if (ap.isPresent("help")) {
                ap.usage();
            } else {
                ap.checkMandatory();
                try {
                    CheckpointStatus xml2task = new CheckpointStatus();
                    xml2task.setCheckpointFile(ap.getStringValue("checkpoint"));
                    xml2task.unmarshal();
                    xml2task.submit();
                } catch (Exception e) {
                    logger.error("Exception in main", e);
                }
            }
        } catch (ArgumentParserException e) {
            System.err.println("Error parsing arguments: " + e.getMessage());
            ap.usage();
        }
    }

    public String getCheckpointFile() {
        return checkpointFile;
    }

    public void setCheckpointFile(String file) {
        this.checkpointFile = file;
    }
}
