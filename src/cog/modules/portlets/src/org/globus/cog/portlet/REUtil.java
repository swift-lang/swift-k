// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------



package org.globus.cog.portlet;

// turbine
//import org.apache.turbine.util.RunData;

// jglobus
import org.globus.util.ConfigUtil;

// apache
import org.apache.log4j.Logger;

// java
import java.io.IOException;
import java.io.File;
import java.util.Enumeration;
import java.util.Properties;

/**
 * methods:
 *
 */
public final class REUtil {

	// setup logging
	static Logger logger = Logger.getLogger(REUtil.class.getName());

	public static final String REMOTE_EXECUTION_DIR = ".remote_execution";
	public static final String TASK_DIR_NAME_PRE = "task_";
	public static final String RE_EXECUTABLE_NAME = "RE_executable_name";
	public static final String RE_LOCAL_EXECUTABLE = "RE_local_executable";
	public static final String RE_REDIRECTED = "RE_redirected";
	public static final String RE_STD_IN_FILE_NAME = "RE_std_in";
	public static final String RE_STD_OUT_FILE_NAME = "RE_std_out.html";
	public static final String RE_STD_ERR_FILE_NAME = "RE_std_err.html";

	public static final String RE_STD_OUT_HTML_TITLE = "Standard Output";
	public static final String RE_STD_ERR_HTML_TITLE = "Standard Error";
	public static final String RE_FILES_HTML_HEADER_1 = "<html><head><title>";
	public static final String RE_FILES_HTML_HEADER_2 = "</title></head><body><pre><xmp>";
	public static final String RE_FILES_HTML_FOTTER = "</xmp></pre></body></html>";

	public static final String FILE_SEPARATOR_PROPERTY = "file.separator";
	public static final String JAVA_TEMP_DIR = "java.io.tmpdir";
	public static final String USER_HOME = "user.dir";

	public static final String COMMAND_CHMOD = "/bin/chmod";
	public static final String CHMOD_700 = "700";
	public static final String COMMAND_LS = "/bin/ls";
	public static final String LS_LA = "-la";
	public static final String SEP_PIPE = "|";
	public static final String COMMAND_GREP = "/bin/grep";
	public static final String COMMAND_CUT = "/bin/cut";
	public static final String CUT_C4 = "-c4";
	public static final String COMMAND_CMD = "cmd.exe";
	public static final String COMMAND_DIR = "dir";
	public static final String CMD_SLASHC = "/c";
	public static final String CMD_AMP = "&";
	public static final String CMD_DCOLON = ":";
	public static final String WHITESPACE = " ";

	public static final String HEADER_USER_AGENT = "User-Agent";
	public static final String USER_AGENT_WIN = "Win";
	public static final String SEP_WIN = "\\";
	public static final String SEP_UNIX = "/";

	public static final String REGEX_START = "(";
	public static final String REGEX_STOP = ")+";

	public static final String JOB_NAME_GT2 = "GT2";
	public static final String JOB_NAME_GT3 = "GT3";
	public static final int JOB_TYPE_GT2 = 2;
	public static final int JOB_TYPE_GT3 = 3;
	public static final String ATTRIBUTE_ENVIRONMENT = "environment";

	private static REUtil _objREUtil = null;
	private static int iSysType;
	private static String strPathSep = null;
	private static String strRemExeDir = null;
	private static String strUserHome = null;
	private static boolean _boolInitDone = false;

	/**
	 * private constructor
	 */
	protected REUtil() {
	}

	/**
	 * static so that this is done once and the first time this
	 * object is accessed.
	 *
	 */
//	public static void init(RunData objRunData) {
	public static void init(String strInitDir) {

//		String strInitDir = null;
		Properties propSysProps = null;
		Enumeration enuSysPropNames = null;

		if (_boolInitDone) {
			return;
		}

		/* initialize */
		propSysProps = null;
		enuSysPropNames = null;
		try {
			propSysProps = System.getProperties();
			enuSysPropNames = propSysProps.propertyNames();

			/*
			 * get system type, and according to that work with paths
			 * permissions etc
			 */
			iSysType = ConfigUtil.getOS();
			strPathSep = propSysProps.getProperty(FILE_SEPARATOR_PROPERTY);
			strUserHome = propSysProps.getProperty(USER_HOME);

			logger.debug("Info: REUtil: init: user.home = \"" + strUserHome + "\"");

			/*
			 * TODO
			 * currently constructing webaplication_real_path|.remote_execution
			 * but this should be something that is configurable
			 * through either a .properties file, or administration option
			 */

			try {
//				strInitDir = objRunData.getServletContext().getRealPath(strPathSep).trim();

//				logger.debug("Info: REUtil: init: init dir = \"" + strInitDir + "\"");

				strInitDir = strInitDir + strPathSep + REMOTE_EXECUTION_DIR;

//				logger.debug("Info: REUtil: init: init dir = \"" + strInitDir + "\"");

				/* subtract the user.home from the real path */
				// TODO will this work everywhere?
				/* it does not !!!
				strInitDir = strInitDir.substring(strUserHome.length()+1);
				*/

//				logger.debug("Info: REUtil: init: init dir = \"" + strInitDir + "\"");

				strInitDir = uniqueSep(strInitDir);

//				logger.debug("Info: REUtil: init: init dir = \"" + strInitDir + "\"");
				
				initREDir(strInitDir);

				// lets save this
				strRemExeDir = strInitDir;

				/* set that init is done */
				_boolInitDone = true;
			} catch (Exception excpE) {
				throw new Exception("Error: REUtil: init: could not initialize RE directory");
			}

			logger.debug("Info: REUtil: init: remote execution dir = \"" + strRemExeDir + "\"");

		} catch (SecurityException excpSE) {
			System.err.println("Error: REUtil: init:");
			System.err.println(excpSE.toString());
		} catch (NullPointerException excpNPE) {
			System.err.println("Error: REUtil: init:");
			System.err.println(excpNPE.toString());
		} catch (Exception excpE) {
			System.err.println("Error: REUtil: init:");
			System.err.println(excpE.toString());
		} finally {
		}
	}

	private static void initREDir(String strDirName) 
		throws Exception {

//		String[] strArrDirList = null;

		boolean boolDeleted = false;
		File flDir;

		if ((strDirName == null) || (strDirName.trim().equals(""))) {
			throw new Exception("Error: REUtil: initREDir: string null or empty.");
		}

		flDir = new File(strDirName);

		/* check for existance */
		if (flDir.exists()) {

//			logger.debug("Info: REUtil: initREDir: the RE file exists");
//			logger.debug("Info: REUtil: initREDir: canonical path : " + flDir.getCanonicalPath());
//			logger.debug("Info: REUtil: initREDir: absolute path  : " + flDir.getAbsolutePath());

			// delete it
			try {
				// boolDeleted = flDir.getCanonicalFile().delete();
				// do a recurseDelete()
				boolDeleted = recurseDelete(flDir);

				if (boolDeleted) {
//					logger.debug("Info: REUtil: initREDir: the RE file was deleted");
				} else {
//					logger.debug("Info: REUtil: initREDir: could not purge RE directory");
				}

			} catch (SecurityException excpSE) {
//				logger.error("Info: REUtil: initREDir: could not purge RE directory");
// todo				throw new Exception("Error: REUtil: initREDir: could not purge, earlier created, RE directory\n" + excpSE.toString());
			}
		}

		if (!flDir.exists()) {
//			logger.debug("Info: REUtil: initREDir: the remote execution directory does NOT exist");
		}
//		logger.debug("Info: REUtil: initREDir: creating...");

		/* create the directory */
		try {
			flDir.mkdirs();

//			logger.debug("Info: REUtil: initREDir: the RE file was created");
		} catch (SecurityException excpSE) {
			throw new Exception("Error: REUtil: initREDir: could not create RE directory\n" + excpSE.toString());
		}

/* debug */
		/* now list the contents of the directory */
/*
		strArrDirList = flDir.list();
		logger.debug("Info: REUtil: initREDir: the directory listing is:");
		for (int i=0; i<strArrDirList.length; i++) {
			logger.debug("Info: REUtil: initREDir: \t" + strArrDirList[i]);
		}
*/
/* debug */

	}

	public static boolean recurseDelete(File flDir) {
		boolean boolSuccess = false;
		String[] arrChildren;

		if (flDir.isDirectory()) {

			// get the list of files in the directory
			arrChildren = flDir.list();

			for (int i=0; i<arrChildren.length; i++) {
				boolSuccess = recurseDelete(new File(flDir, arrChildren[i]));
				if (!boolSuccess) {
					return false;
				}
			}
		}
    
		// if it is a file then delete
		// if it is a directory then its empty now.. so delete
		return flDir.delete();
	}

	/**
	 * CAUTION: this works for UNIX only at the moment
	 */
	public static String uniqueSep(String strIn) {
		String strOut = "";

//		logger.debug("Info: REUtil: uniqueSep: before \"" + strIn + "\"");

		strOut = strIn.replaceAll(REGEX_START + strPathSep + REGEX_STOP, strPathSep);

//		logger.debug("Info: REUtil: uniqueSep: after \"" + strOut + "\"");

		return strOut;
	}

	public static String getREPath() {
		return strRemExeDir;
	}

	public static String getPathSep() {
		if (strPathSep == null) {
			try {
				strPathSep = System.getProperties().getProperty(FILE_SEPARATOR_PROPERTY);
			} catch (SecurityException excpSE) {
				excpSE.printStackTrace();
			}
		}

		return strPathSep;
	}

	public static void initForJob(String strJobName) 
		throws Exception {

		File flDir;
		boolean boolDeleted = false;

		if ((strJobName == null) || (strJobName.trim().equals(""))) {
			throw new Exception("Error: REUtil: initForJob: job name either null or empty.");
		}

		flDir = new File(uniqueSep(strRemExeDir + strPathSep + strJobName));

//		logger.debug("REUtil: initForJob: dir name: \"" + flDir.toString() + "\"");

		/* check for existance */
		if (flDir.exists()) {

			// delete it
			try {
				// do a recurseDelete()
				boolDeleted = recurseDelete(flDir);

			} catch (SecurityException excpSE) {
				throw new Exception("Warn: REUtil: initForJob: Could not delete \"" + flDir.toString() + "\".");
			}
			if (!boolDeleted) {
				throw new Exception("Warn: REUtil: initForJob: Could not delete \"" + flDir.toString() + "\".");
			}

//			throw new Exception("Error: REUtil: initForJob: \"" + flDir.toString() + "\" already exists.");
		}
		/* create the directory */
		try {
			flDir.mkdirs();
		} catch (SecurityException excpSE) {
			throw new Exception("Error: REUtil: initForJob: could not create job directory\n" + excpSE.toString());
		}
	}

	public static void finalizeForJob(String strJobName) 
		throws Exception {

		File flDir;
		boolean boolDeleted = false;

		if ((strJobName == null) || (strJobName.trim().equals(""))) {
			throw new Exception("Error: REUtil: finlizeForJob: job name either null or empty.");
		}

		flDir = new File(uniqueSep(strRemExeDir + strPathSep + strJobName));

//		logger.debug("REUtil: finalizeForJob: dir name: \"" + flDir.toString() + "\"");

		/* check for existance */
		if (flDir.exists()) {

			// delete it
			try {
				// do a recurseDelete()
				boolDeleted = recurseDelete(flDir);

			} catch (SecurityException excpSE) {
				throw new Exception("Warn: REUtil: finalizeForJob: Could not delete \"" + flDir.toString() + "\".");
			}
			if (!boolDeleted) {
				throw new Exception("Warn: REUtil: finalizeForJob: Could not delete \"" + flDir.toString() + "\".");
			}

		} else {
			// nothing to do
		}
	}

	public static void initForTask(String strTaskID) 
		throws Exception {

		File flDir;
		boolean boolDeleted = false;

		if ((strTaskID == null) || (strTaskID.trim().equals(""))) {
			throw new Exception("Error: REUtil: initForTask: job name either null or empty.");
		}
		flDir = new File(uniqueSep(strRemExeDir + strPathSep + TASK_DIR_NAME_PRE + strTaskID));

		logger.debug("REUtil: initForTask: dir name: \"" + flDir.toString() + "\"");

		/* check for existance */
		if (flDir.exists()) {
			// delete it
			try {
				// do a recurseDelete()
				boolDeleted = recurseDelete(flDir);
			} catch (SecurityException excpSE) {
				throw new Exception("Warn: REUtil: initForTask: Could not delete \"" + flDir.toString() + "\".");
			}
			if (!boolDeleted) {
				throw new Exception("Warn: REUtil: initForTask: Could not delete \"" + flDir.toString() + "\".");
			}
		}
		/* create the directory */
		try {
			flDir.mkdirs();
		} catch (SecurityException excpSE) {
			throw new Exception("Error: REUtil: initForTask: could not create job directory\n" + excpSE.toString());
		}
	}

	public static void finalizeForTask(String strTaskID) 
		throws Exception {

		File flDir;
		boolean boolDeleted = false;

		if ((strTaskID == null) || (strTaskID.trim().equals(""))) {
			throw new Exception("Error: REUtil: finalizeForTask: task ID null or empty.");
		}
		flDir = new File(uniqueSep(strRemExeDir + strPathSep + TASK_DIR_NAME_PRE + strTaskID));

		logger.debug("REUtil: finalizeForTask: dir name: \"" + flDir.toString() + "\"");

		/* check for existance */
		if (flDir.exists()) {
			// delete it
			try {
				// do a recurseDelete()
				boolDeleted = recurseDelete(flDir);
			} catch (SecurityException excpSE) {
				throw new Exception("Warn: REUtil: finalizeForTask: Could not delete \"" + flDir.toString() + "\".");
			}
			if (!boolDeleted) {
				throw new Exception("Warn: REUtil: finalizeForTask: Could not delete \"" + flDir.toString() + "\".");
			}
		} else {
			// nothing to do
		}
	}

	public static String getTaskDir(long iTaskID) {
		return TASK_DIR_NAME_PRE + Long.toString(iTaskID);
	}

	public static String touchFile(String strJobName, String strFileName) 
		throws Exception {

		boolean boolDeleted = false;
		boolean boolCreated = false;
		String strFilePath = null;
		File flTouch;

		if ((strJobName == null) || (strJobName.trim().equals(""))) {
			throw new Exception("Error: REUtil: touchFile: Job name either null or empty.");
		}

//		flTouch = new File(uniqueSep(strRemExeDir + strPathSep + strJobName + strPathSep + strFileName));
		flTouch = new File(uniqueSep(strRemExeDir + strPathSep + TASK_DIR_NAME_PRE + strJobName + strPathSep + strFileName));

//		logger.debug("REUtil: touchFile: file name: \"" + flTouch.toString() + "\"");

		/* check for existance */
		if (flTouch.exists()) {

/*
			logger.debug("Warn: REUtil: touchFile: \"" + flTouch.toString() + "\" already exists");
			logger.debug("Warn: REUtil: touchFile: canonical path: \"" + flTouch.getCanonicalPath() + "\"");
			logger.debug("Warn: REUtil: touchFile: absolute path:  \"" + flTouch.getAbsolutePath() + "\"");

			// delete it
			try {
				// boolDeleted = flTouch.getCanonicalFile().delete();
				// do a recurseDelete()
				boolDeleted = recurseDelete(flDir);

			} catch (SecurityException excpSE) {
				throw new Exception("Warn: REUtil: touchFile: Could not delete \"" + flTouch.toString() + "\".");
			}
			if (!boolDeleted) {
				throw new Exception("Warn: REUtil: touchFile: Could not delete \"" + flTouch.toString() + "\".");
			}
*/
			// return the absolute path
			return flTouch.getAbsolutePath();

		}
		/* touch the file */
		try {
			boolCreated = flTouch.createNewFile();
		} catch (SecurityException excpSE) {
			throw new Exception("Error: REUtil: touchFile: Could not create file\n" + excpSE.toString());
		} catch (IOException excpIOE) {
			throw new Exception("Error: REUtil: touchFile: Could not create file\n" + excpIOE.toString());
		}
		if (!boolCreated) {
			throw new Exception("Error: REUtil: touchFile: Could not create file");
		}

		strFilePath = flTouch.getAbsolutePath();

		logger.debug("Info: REUtil: touchFile: touched file path \"" + strFilePath + "\".");

		/* return file path */
		/* subtract the user.home from the real path */
		// TODO will this work everywhere?
		/* it does not
		strFilePath = flTouch.getAbsolutePath().substring(strUserHome.length()+1);
		*/

//		logger.debug("Info: REUtil: touchFile: touched file path pruned \"" + strFilePath + "\".");
		return strFilePath;
	}

} /* end clsREUtil */

