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

package org.globus.cog.abstraction.impl.common;

import java.util.Date;

import org.globus.cog.abstraction.interfaces.Status;

public class StatusImpl implements Status {
    private int curStatus;
    private int prevStatus;
    private Exception exception = null;
    private String message = null;
    private Date time;
    
    public StatusImpl(int status, String message, Exception exception, int prev) {
        this.curStatus = status;
        this.prevStatus = prev;
        this.message = message;
        this.exception = exception;
        this.time = new Date();
    }
    
    public StatusImpl(int status, String message, Exception exception) {
        this(status, message, exception, Status.UNKNOWN);
    }

    public StatusImpl() {
        this.curStatus = Status.UNSUBMITTED;
        this.prevStatus = Status.UNSUBMITTED;
        this.time = new Date();
    }

    public StatusImpl(int curStatus) {
        this();
        this.curStatus = curStatus;
    }

    public void setStatusCode(int status) {
        this.curStatus = status;
        this.time = new Date();
    }

    public int getStatusCode() {
        return this.curStatus;
    }

    public void setPrevStatusCode(int status) {
        this.prevStatus = status;
    }

    public int getPrevStatusCode() {
        return this.prevStatus;
    }

    public void setException(Exception exception) {
        this.exception = exception;
    }

    public Exception getException() {
        return this.exception;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return this.message;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public Date getTime() {
        return this.time;
    }

    public boolean isTerminal() {
        return isTerminal(curStatus);
    }
    
    public static boolean isTerminal(int status) {
        return status == COMPLETED || status == FAILED
                || status == CANCELED;
    }

    public String getStatusString() {
        return code2String(this.curStatus);
    }

    public String getPrevStatusString() {
        return code2String(this.prevStatus);
    }

    private String code2String(int statusCode) {
        switch (statusCode) {
            case Status.ACTIVE:
                return "Active";

            case Status.CANCELED:
                return "Canceled";

            case Status.COMPLETED:
                return "Completed";

            case Status.FAILED:
                return "Failed";

            case Status.RESUMED:
                return "Resumed";

            case Status.SUBMITTED:
                return "Submitted";

            case Status.SUBMITTING:
                return "Submitting";
                
            case Status.SUSPENDED:
                return "Suspended";

            case Status.UNSUBMITTED:
                return "Unsubmitted";

            case Status.STAGE_IN:
                return "Stagein";
                
            case Status.STAGE_OUT:
                return "Stageout";
                
            default:
                return "Unknown (" + statusCode + ")";
        }
    }

    public String toString() {
        if (message != null) {
            return code2String(curStatus) + " " + message;
        }
        if (exception != null) {
            return code2String(curStatus) + " " + exception.getMessage();
        }
        return code2String(curStatus);
    }
    
    public static final class Unmodifiable extends StatusImpl {

        public Unmodifiable() {
            super();
        }

        public Unmodifiable(int status, String message, Exception exception, int prev) {
            super(status, message, exception, prev);
        }

        public Unmodifiable(int status, String message, Exception exception) {
            super(status, message, exception);
        }

        public Unmodifiable(int curStatus) {
            super(curStatus);
        }

        @Override
        public void setStatusCode(int status) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setPrevStatusCode(int status) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setException(Exception exception) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setMessage(String message) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setTime(Date time) {
            throw new UnsupportedOperationException();
        }
    }
}
