
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------



package org.globus.cog.portlet.remoteExecution;

public class JobInfo {
	private String Name = null;
	private String URL = null;
	private boolean LocalExecutable = false;
	private String Executable = null;
	private String ExecutableName = null;
	private String Arguments = null;
	private String Directory = null;
	private boolean Redirected = false;
	private String StdOutput = null;
	private String StdError = null;
	private String Status = null;

	public JobInfo() {
		this.Name = "";
		this.URL = "";
		this.LocalExecutable = false;
		this.Executable = "";
		this.ExecutableName = "";
		this.Arguments = "";
		this.Directory = "";
		this.Redirected = false;
		this.StdOutput = "";
		this.StdError = "";
		this.Status = "";
	}
	public JobInfo(String Name, 
					String URL, 
					boolean LocalExecutable, String Executable, String ExecutableName, 
					String Arguments, String Directory,
					boolean Redirected, String StdOutput, String StdError,
					String Status) {
		this.Name = Name;
		this.URL = URL;
		this.LocalExecutable = LocalExecutable;
		this.Executable = Executable;
		this.ExecutableName = ExecutableName;
		this.Arguments = Arguments;
		this.Directory = Directory;
		this.Redirected = Redirected;
		this.StdOutput = StdOutput;
		this.StdError = StdError;
		this.Status = Status;
	}
	public void setName(String Name) {
		this.Name = Name;
	}
	public String getName() {
		return this.Name;
	}
	public void setURL(String URL) {
		this.URL = URL;
	}
	public String getURL() {
		return this.URL;
	}
	public void setLocalExecutable(boolean LocalExecutable) {
		this.LocalExecutable = LocalExecutable;
	}
	public boolean getLocalExecutable() {
		return this.LocalExecutable;
	}
	public void setExecutable(String Executable) {
		this.Executable = Executable;
	}
	public String getExecutable() {
		return this.Executable;
	}
	public void setExecutableName(String ExecutableName) {
		this.ExecutableName = ExecutableName;
	}
	public String getExecutableName() {
		return this.ExecutableName;
	}
	public void setArguments(String Arguments) {
		this.Arguments = Arguments;
	}
	public String getArguments() {
		return this.Arguments;
	}
	public void setDirectory(String Directory) {
		this.Directory = Directory;
	}
	public String getDirectory() {
		return this.Directory;
	}
	public void setRedirected(boolean Redirected) {
		this.Redirected = Redirected;
	}
	public boolean getRedirected() {
		return this.Redirected;
	}
	public void setStdOutput(String StdOutput) {
		this.StdOutput = StdOutput;
	}
	public String getStdOutput() {
		return this.StdOutput;
	}
	public void setStdError(String StdError) {
		this.StdError = StdError;
	}
	public String getStdError() {
		return this.StdError;
	}
	public void setStatus(String Status) {
		this.Status = Status;
	}
	public String getStatus() {
		return this.Status;
	}
} /* end clsJobInfo */
