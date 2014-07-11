import(sys)
import(task)
import('swift-lib')

URL_PREFIX := getURLPrefix()

WRAPPER_LOG_ALWAYS_TRANSFER := configProperty("alwaysTransferWrapperLog")
SWIFT:SCRIPT_NAME := contextAttribute("SWIFT:SCRIPT_NAME")
SWIFT:RUN_ID := contextAttribute("SWIFT:RUN_ID")
SWIFT:HOME := contextAttribute("SWIFT:HOME")
SITEDIR_KEEP := configProperty("keepSiteDir")


namespace(wrapperStaging) {
	
	checkErrorFile := function(jobdir, jobid) {
		if (file:exists("{jobdir}/_swift.error")) {
			log(LOG:INFO, "FAILURE jobid={jobid} - Failure file found")
			error := str:strip(file:read("{jobdir}/_swift.error"))
			file:remove("{jobdir}/_swift.error")
			error
		}
		else {
			log(LOG:INFO, "NO_STATUS_FILE jobid={jobid} - Both status files are missing")
			throw("No status file was found")
		}
	}
	
	checkJobStatus := function(jobdir, jobid, tr) {
		log(LOG:DEBUG, "START jobid={jobid}")
		try {
			file:remove("{jobdir}/_swift.success")
			log(LOG:INFO, "SUCCESS jobid={jobid} - Success file found")
		}
		else {
			throw(checkErrorFile(jobdir, jobid))
		}
	}

	initSharedDir := function(progress, rhost) {
		once(list(rhost, "shared")) {
			setProgress(progress, "Initializing site shared directory")

			log(LOG:INFO, "START host={rhost} - Initializing shared directory")

			wfdir := "{SWIFT:SCRIPT_NAME}-{SWIFT:RUN_ID}"
			dir:make(wfdir, host=rhost)
			transfer(srcdir="{SWIFT:HOME}/libexec/", srcfile="_swiftwrap.wrapperstaging", destdir=wfdir, desthost=rhost)

			wfdir
			to(cleanup, list(wfdir, rhost))
			log(LOG:INFO, "END host={rhost} - Done initializing shared directory")
		}
	}
	
	initDDir := function() {
		ddir := "{SWIFT:SCRIPT_NAME}-{SWIFT:RUN_ID}"
		
		once(ddir) {
			if(!file:exists(ddir)) {
				task:dir:make(ddir)
			}
		}
		ddir
	}

	stageWrapperParams := function(jobid, jobdir, wrapfile, dir, host) {
		log(LOG:INFO, "START jobid={jobid} - staging in wrapper params"),
		(provider, srchost, destdir, filename, srcdir) := splitFileURL(wrapfile, dir, destdir="parameters/{jobdir}")

		cache(list(destdir, host)) {
			dir:make(destdir, host=host, provider=provider)
		}

		log(LOG:INFO, "END jobid={jobid}")
	}

		
	readStandardFiles := function(jobdir, stdout, stderr) {
		concat(
			for(f, [["_swift.stderr", stderr], ["_swift.stdout", stdout]]) {
				(name, file) := each(f)
				destfile := "{jobdir}/{file}"
				"\n    {name}: "
				try {
					file:read(destfile)
					"\n"
				}
				else {
					""
				}
			}
		)
	}
		

	export(execute2,
		function(rhost, progress, tr, arguments, attributes, stdin, stdout, stderr, 
			stagein, stageout, replicationGroup, replicationChannel) {

			ddir := initDDir()
			wfdir := try {
				initSharedDir(progress, rhost)
			}
			else catch(exception) {
				throw(exception("Could not initialize shared directory on {rhost}", exception))
			}

			uid := UID()
			jobid := "{tr}-{uid}"
			
			jobdir := concat(ddir, "/jobs/", substring(uid, from=0, to=1), "/{jobid}/")

			log(LOG:DEBUG, "THREAD_ASSOCIATION jobid={jobid} thread=", currentThread(), " host={rhost} replicationGroup={replicationGroup}")

			statusMode := configProperty("statusMode", host = rhost)
			wrapperMode := configProperty("wrapperParameterMode", host = rhost)

			wrapfile := "{jobdir}/_paramfile"

			stdout := if (stdout == null, "stdout.txt", getFieldValue(stdout))
			stderr := if (stderr == null, "stderr.txt", getFieldValue(stderr)),
			
			(localFileDirs, remoteFileDirs, inFiles, outFiles) := getStagingInfo(stagein, stageout)

			os := siteProfile(rhost, "SYSINFO:OS")
			
			scratch := siteProfile(rhost, "scratch")

			if(wrapperMode == "files") {
				file:write(wrapfile) {
					"-e ", executable(tr, rhost),
					"\n-out ", stdout,
					"\n-err ", stderr,
					"\n-i ", if (stdin != null, getFieldValue(stdin)),
					"\n-d ", str:join(remoteFileDirs, "|"),
					"\n-if ", str:join(remoteFileNames(inFiles), "|"),
					"\n-of ", str:join(remoteFileNames(outFiles), "|"),
					"\n-wt", WRAPPER_LOG_ALWAYS_TRANSFER,
					"\n-sk", SITEDIR_KEEP,
					"\n-cdmfile ", cdm:file(),
					"\n-status ", statusMode,
					for(a, arguments) {
						"\n-a ", a
					}
				}
			}
			

			setProgress(progress, "Stage in")

			try {
				if (wrapperMode == "files") {
					stageWrapperParams(jobid, jobdir, wrapfile, wfdir, rhost)
				}

				log(LOG:DEBUG, "JOB_START jobid={jobid} tr={tr}", if (arguments != null, (" arguments=", arguments)), " host={rhost}")

				setProgress(progress, "Submitting")

				if (wrapperMode == "files") {
					swift:execute(
						progress, 
						siteProfile(rhost, "wrapperInterpreter"),
						list(
							siteProfile(rhost, "wrapperInterpreterOptions"),
							"_swiftwrap.wrapperstaging",
							jobid,
							"-urlprefix", URL_PREFIX,
							"-jobdir", jobdir,
							"-scratch", scratch, 
							"-p", wrapfile
						)
						directory=wfdir
						redirect=false
						host=rhost
						TCProfile(rhost, if (attributes != null, attributes = attributes), tr=tr)
						replicationGroup=replicationGroup
						replicationChannel=replicationChannel
						jobid=jobid
					)
				}
				if (wrapperMode == "args") {
					swift:execute(
						siteProfile(rhost, "wrapperInterpreter"),
						list(
							siteProfile(rhost, "wrapperInterpreterOptions"),
							"_swiftwrap.wrapperstaging",
							jobid,
							"-urlprefix", URL_PREFIX,
							"-jobdir", jobdir,
							"-scratch", scratch,
							"-e", executable(tr, rhost),
							"-out", stdout,
							"-err", stderr,
							"-i", if (stdin != null, getFieldValue(stdin)),
							"-d", str:join(remoteFileDirs, "|"),
							"-if", str:join(remoteFileNames(inFiles), "|"),
							"-of", str:join(remoteFileNames(outFiles), "|"),
							"-wt", WRAPPER_LOG_ALWAYS_TRANSFER,
							"-sk", SITEDIR_KEEP,
							"-cdmfile", cdm:file(),
							"-status", statusMode,
							"-a", if (arguments != null, each(arguments))
						)
						directory=wfdir
						redirect=false
						host=rhost
						TCProfile(rhost, if(attributes != null, attributes = attributes), tr=tr)
						replicationGroup=replicationGroup
						replicationChannel=replicationChannel
						jobid=jobid
					)
				}
				
				setProgress(progress, "Checking status")
				if (statusMode == "files") {
					checkJobStatus(jobdir, jobid, tr)
				}

				if (wrapperMode == "files") {
					file:remove(wrapfile)
				}

				log(LOG:DEBUG, "STAGING_OUT jobid={jobid}")


				/* need to stage the files to upper scratch area in case they are not transfered to another site
				   before all the files get cleaned out */


				setProgress(progress, "Stage out")
				doRestartLog(stageout)
				
				log(LOG:DEBUG, "JOB_END jobid={jobid}")
			}
			else catch(prev) {
				if (matches(prev, "^Abort$")) {
					log(LOG:DEBUG, "JOB_CANCELED jobid={jobid}")
					throw(prev)
				}
				else {
					setProgress(progress, "Failed but can retry")
					exception := try(exception(checkErrorFile(jobdir, jobid)), prev)
				
					log(LOG:DEBUG, "APPLICATION_EXCEPTION jobid={jobid} - Application exception: ", exception)

					if (matches(exception,".*executable bit.*")) {
						throw(exception)
					}
					
					outs := readStandardFiles(jobdir, stdout, stderr)
					
					throw(
						exception(
							concat(
								"Exception in {tr}:",
								if (arguments != null, "\n    Arguments: {arguments}")
								"\n    Host: {rhost}",
								"\n    Directory: {jobdir}",
								"{outs}",
							)
							exception
						)
					)
				}
			}
		}
	)
}

// Local variables:
// mode: scheme
// tab-width: 4
// indent-tabs-mode: t
// End:
