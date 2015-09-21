import(sys)
import(task)
import('swift-lib')

URL_PREFIX := getURLPrefix()

WRAPPER_LOG_ALWAYS_TRANSFER := configProperty("alwaysTransferWrapperLog")
SWIFT:SCRIPT_NAME := contextAttribute("SWIFT:SCRIPT_NAME")
SWIFT:RUN_ID := contextAttribute("SWIFT:RUN_ID")
SWIFT:HOME := contextAttribute("SWIFT:HOME")
SITEDIR_KEEP := configProperty("keepSiteDir")

SWIFT:DEBUG_DIR_PREFIX := contextAttribute("SWIFT:DEBUG_DIR_PREFIX")
DEBUG_DIR := "{SWIFT:DEBUG_DIR_PREFIX}{SWIFT:SCRIPT_NAME}-{SWIFT:RUN_ID}.d"

CLEANUPS := contextAttribute("RUN:CLEANUPS")

if (!file:exists(DEBUG_DIR)) {
	task:dir:make(DEBUG_DIR)
}

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
		utry {
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
			discard(append(CLEANUPS, list(wfdir, rhost)))
			log(LOG:INFO, "END host={rhost} - Done initializing shared directory")
		}
	}
	
	stageWrapperParams := function(jobid, jobdir, wrapfile, dir, host) {
		log(LOG:INFO, "START jobid={jobid} - staging in wrapper params"),
		(provider, srchost, destdir, filename, srcdir) := splitFileURL(wrapfile, dir, destdir="parameters/{jobdir}")

		cache(list(destdir, host)) {
			dir:make(destdir, host=host, provider=provider)
		}

		log(LOG:INFO, "END jobid={jobid}")
	}

		
	readStandardFiles := function(jobdir) {
		concat(
			stderrs := "{jobdir}/_stderr.txt"
			stdouts := "{jobdir}/_stdout.txt"
			if (file:exists(stderrs)) {
				"\n\n------- Application STDERR --------\n"
				str:strip(file:read(stderrs))
				"\n-----------------------------------\n"
			}
			if (file:exists(stdouts)) {
				"\n\n------- Application STDOUT --------\n"
				str:strip(file:read(stdouts))
				"\n-----------------------------------\n"
			}
		)
	}
		

	export(execute2,
		function(jobid, rhost, progress, tr, commands, attributes,  
			stagein, stageout, replicationGroup, replicationChannel) {

			wfdir := utry {
				initSharedDir(progress, rhost)
			}
			else catch(exception) {
				throw(exception("Could not initialize shared directory on {rhost}", exception))
			},

			jobdir := substring(jobid, 0, to = 1)
			
			jobdir := "{DEBUG_DIR}/jobs/{jobdir}/{jobid}/"

			statusMode := configProperty("statusMode", host = rhost)
			wrapperMode := configProperty("wrapperParameterMode", host = rhost)

			wrapfile := "{jobdir}/_paramfile"

			(fileDirs, inFiles, outFiles, outCollect, tempRefs) := getStagingInfo(stagein, stageout),

			os := siteProfile(rhost, "OS")
			resolveFiles(commands, rhost)
			
			scratch := siteProfile(rhost, "scratch")

			if (wrapperMode == "files") {
				file:write(wrapfile) {
					"\n-d\n", remoteFileNames(fileDirs),
					"\n-if\n", remoteFileNamesWS(inFiles),
					"\n-of\n", remoteFileNamesWS(outFiles),
					"\n-cf\n", remoteFileNamesWS(outCollect),
					"\n-wt\n", WRAPPER_LOG_ALWAYS_TRANSFER,
					"\n-sk\n", SITEDIR_KEEP,
					"\n-cdmfile\n", cdm:file(),
					"\n-status\n", statusMode,
					swift:formatCommands(commands, "paramfile")
				}
			}
			

			setProgress(progress, "Stage in")

			utry {
				if (wrapperMode == "files") {
					stageWrapperParams(jobid, jobdir, wrapfile, wfdir, rhost)
				}

				log(LOG:DEBUG, "JOB_START jobid={jobid} tr={tr}", swift:formatCommands(commands, "log"))

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
						tempRefs = tempRefs
						TCProfile(rhost, if (attributes != null, attributes = attributes), commands = getAndDelete(commands), tr = tr)
						replicationGroup=replicationGroup
						replicationChannel=replicationChannel
						jobid=jobid
					)
				}
				if (wrapperMode == "args") {
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
							"-d", remoteFileNames(fileDirs),
							"-if", remoteFileNamesWS(inFiles),
							"-of", remoteFileNamesWS(outFiles),
							"-cf", remoteFileNamesWS(outCollect),
							"-wt", WRAPPER_LOG_ALWAYS_TRANSFER,
							"-sk", SITEDIR_KEEP,
							"-cdmfile", cdm:file(),
							"-status", statusMode,
							swift:formatCommands(commands, "args")
						)
						directory=wfdir
						redirect=false
						host=rhost
						tempRefs = tempRefs
						TCProfile(rhost, if (attributes != null, attributes = attributes), commands = getAndDelete(commands), tr = tr)
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
				/* empty collect list */
				// TODO
				[] 
			}
			else catch(prev) {
				if (matches(prev, "^Abort$")) {
					log(LOG:DEBUG, "JOB_CANCELED jobid={jobid}")
					throw(prev)
				}
				else {
					setProgress(progress, "Failed but can retry")
					exception := utry(exception(checkErrorFile(jobdir, jobid)), prev)
				
					log(LOG:DEBUG, "APPLICATION_EXCEPTION jobid={jobid} - Application exception: ", exception)

					if (matches(exception,".*executable bit.*")) {
						throw(exception)
					}
					
					outs := readStandardFiles(jobdir)
					
					throw(
						exception(
							concat(
								"Exception in {tr}:",
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
