import(sys)
import(task)
import('swift-lib')


getURLPrefix := def("org.griphyn.vdl.karajan.lib.GetURLPrefix")

URL_PREFIX := getURLPrefix() 

WRAPPERLOG_ALWAYS_TRANSFER := vdl:configProperty("wrapperlog.always.transfer")
SITEDIR_KEEP := vdl:configProperty("sitedir.keep")


namespace(swift) {
	
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

	initSharedDir := function(progress, rhost) {
		once(list(rhost, "shared")) {
			setProgress(progress, "Initializing site shared directory")

			log(LOG:INFO, "START host={rhost} - Initializing shared directory")

			wfdir := "{VDL:SCRIPTNAME}-{VDL:RUNID}"
			dir:make(wfdir, host=rhost)
			transfer(srcdir="{swift.home}/libexec/", srcfile="_swiftwrap.wrapperstaging", destdir=wfdir, desthost=rhost)

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


	inFiles := function(stageins) {
		pathnames(stageins)
	}

	fileDirs := function(stageins, stageouts) {
		list(
			unique(
				inFileDirs(stageins)
				outFileDirs(stageouts)
			)
		)
	}

	cleanup := function(dir, host) {
		log(LOG:INFO, "START dir={dir} host={host}")
		if(vdl:configProperty("sitedir.keep") == "false") {
			task:execute(
				vdl:siteprofile(host, "swift:cleanupCommand"),
				arguments = list(
					siteProfile(host, "swift:cleanupCommandOptions"),
					dir
				)
				host=host, batch=true, TCProfile(host)
			)
			log(LOG:INFO, "END dir={dir} host={host}")
		}
	}

	cleanups := function(cleanup) {
		log(LOG:INFO, "START cleanups={cleanup}")
		parallelFor(i, cleanup) {
			(dir, host) := each(i)
			try {
				vdl:cleanup(dir, host)
			}
			else catch(exception) {
				log(LOG:DEBUG, "EXCEPTION - Exception caught while cleaning up", exception)
				to(warnings, exception("Cleanup on {host} failed", exception))
			}
		}
		log(LOG:INFO, "END cleanups={cleanup}")
	}

	stageWrapperParams := function(jobid, wrapfile, dir, host) {
		log(LOG:INFO, "START jobid={jobid} - staging in wrapper params")
		provider := provider(wrapfile)
		srchost := hostname(wrapfile)
		srcdir := vdl:dirname(wrapfile)
		destdir := dir
		filename := basename(wrapfile)

		cacheOn(list(destdir, host)
			dir:make(destdir, host=host, provider=provider)
		)

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
		function(progress, tr, stagein, stageout,  restartout
			replicationGroup, replicationChannel
			arguments = [], stdin = null, stdout = null, stderr = null, attributes = null) {

			stagein := list(unique(each(stagein)))
			stageout := list(unique(each(stageout)))

			allocateHost(rhost, constraints=jobConstraints(tr, stagein = stagein)) {

				ddir := initDDir()
				wfdir := try {
					initSharedDir(rhost)
				}
				else catch(exception) {
					throw(exception("Could not initialize shared directory on {rhost}", exception))
				}

				uid := UID()
				jobid := "{tr}-{uid}"
				
				jobdir := concat(ddir, "/jobs/", substring(uid, from=0, to=1), "/{jobid}/")

				log(LOG:DEBUG, "THREAD_ASSOCIATION jobid={jobid} thread={#thread} host={rhost} replicationGroup={replicationGroup}")

				statusMode := configProperty("status.mode", host = rhost)
				wrapperMode := configProperty("wrapper.parameter.mode", host = rhost)

				wrapfile := "{jobdir}/_paramfile"

				stdout := if (stdout == null, "stdout.txt", getFieldValue(stdout))
				stderr := if (stderr == null, "stderr.txt", getFieldValue(stderr))

				fileDirs := fileDirs(stagein, stageout)
				os := siteProfile(rhost, "SYSINFO:OS")
				
				scratch := siteProfile(rhost, "scratch")

				if(wrapperMode == "files") {
					file:write(wrapfile,
						"-e ",vdl:executable(tr, rhost),
						"\n-out ", stdout,
						"\n-err ", stderr,
						"\n-i ", if (stdin != null, getFieldValue(stdin)),
						"\n-d ", flatten(each(fileDirs)),
						"\n-if ", flatten(inFiles(stagein)),
						"\n-of ", flatten(outFiles(stageout)),
						"\n-wt", WRAPPERLOG_ALWAYS_TRANSFER,
						"\n-sk", SITEDIR_KEEP,
						"\n-cdmfile ", cdm:file(),
						"\n-status ", statusMode,
						for(a, arguments) {
							"\n-a ", a
						}
					)
				}
				

				setProgress(progress, "Stage in")

				try {
					if (wrapperMode == "files") {
						stageWrapperParams(jobid, wrapfile, wfdir, rhost)
					}

					log(LOG:DEBUG, "JOB_START jobid={jobid} tr={tr}", if (arguments != null, (" arguments=", arguments)), " host={rhost}")

					setProgress(progress, "Submitting")

					if (wrapperMode == "files") {
						swift:execute(
							progress, 
							siteProfile(rhost, "swift:wrapperInterpreter"),
							list(
								siteProfile(rhost, "swift:wrapperInterpreterOptions"),
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
							siteProfile(rhost, "swift:wrapperInterpreter"),
							list(
								siteProfile(rhost, "swift:wrapperInterpreterOptions"),
								"_swiftwrap.wrapperstaging",
								jobid,
								"-urlprefix", URL_PREFIX,
								"-jobdir", jobdir,
								"-scratch", scratch,
								"-e", vdl:executable(tr, rhost),
								"-out", stdout,
								"-err", stderr,
								"-i", if (stdin != null, getFieldValue(stdin)),
								"-d", flatten(each(fileDirs)),
								"-if", flatten(inFiles(stagein)),
								"-of", flatten(outFiles(stageout)),
								"-wt", WRAPPERLOG_ALWAYS_TRANSFER,
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
					doRestartlog(restartout)
					
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
									"\n    Directory: {tmpdir}",
									"{outs}",
								)
								exception
							)
						)
					}
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
