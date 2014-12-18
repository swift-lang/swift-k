import(sys)
import(task)
import('swift-lib')
/*
 * Things that are not exposed to the translated file
 */

SWIFT:SCRIPT_NAME := contextAttribute("SWIFT:SCRIPT_NAME")
SWIFT:RUN_ID := contextAttribute("SWIFT:RUN_ID")
SWIFT:HOME := contextAttribute("SWIFT:HOME")
SWIFT:DEBUG_DIR_PREFIX := contextAttribute("SWIFT:DEBUG_DIR_PREFIX")

WRAPPER_TRANSFER_MODE :=
	if (configProperty("alwaysTransferWrapperLog"), 
		STAGING_MODE:IF_PRESENT, STAGING_MODE:ON_ERROR + STAGING_MODE:IF_PRESENT)

pinOption := configProperty("providerStagingPinSwiftFiles")

PIN := if(pinOption, "pinned:", "")
CLEANUP_ENABLED := !configProperty("keepSiteDir")

DEBUG_DIR := "{SWIFT:DEBUG_DIR_PREFIX}{SWIFT:SCRIPT_NAME}-{SWIFT:RUN_ID}.d"
CDM_FILE := cdm:file()

namespace(providerStaging) {
		
	fileSizes := function(files) {
		math:sum(
			for(f, files, file:size(f))
		)
	}

	readErrorFiles := function(dir, jobid, stdout, stdouts, stderr, stderrs) {
		concat(
			if(file:exists("{dir}/{jobid}.error")) {
				str:strip(file:read("{dir}/{jobid}.error"))
				file:remove("{dir}/{jobid}.error")
			}
			stderrs := if(stderr == null, "{dir}/{jobid}.stderr", stderrs)
			stdouts := if(stdout == null, "{dir}/{jobid}.stdout", stdouts)
			if(file:exists(stderrs)) {
				"\n\n------- Application STDERR --------\n"
				str:strip(file:read(stderrs))
				"\n-----------------------------------\n"
			}
			else if(file:exists(stdouts)) {
				"\n\n------- Application STDOUT --------\n"
				str:strip(file:read(stdouts))
				"\n-----------------------------------\n"
			}
		)
	}
	
	export(execute2,
		function(jobid, rhost, progress, tr, arguments, attributes, stdin, stdout, stderr, 
			stagein, stageout, replicationGroup, replicationChannel) {

			jobdir := substring(jobid, 0, to = 1)

			log(LOG:DEBUG, "THREAD_ASSOCIATION jobid={jobid} thread=", currentThread(), " host={rhost} replicationGroup={replicationGroup}")
			
			wfdir := "{SWIFT:SCRIPT_NAME}-{SWIFT:RUN_ID}"
			tmpdir := dircat("{wfdir}/jobs/{jobdir}", jobid)
			
			sm := siteProfile(rhost, "stagingMethod", default="file"),
			(fileDirs, inFiles, outFiles, outCollect) := getStagingInfo(stagein, stageout, defaultScheme = sm),
			(stdins, stdouts, stderrs) := getStandardFilesInfo(stdin, stdout, stderr, defaultScheme = sm)

			try {
				eargs := expandArguments(arguments, stagingMethod = sm)
				log(LOG:DEBUG, "JOB_START jobid={jobid} tr={tr}", if (arguments != null, (" arguments=", eargs)), 
					" tmpdir={tmpdir} host={rhost}")

				setProgress(progress, "Submitting")

				swift:execute(
					progress,
					siteProfile(rhost, "wrapperInterpreter"),
					list(
						siteProfile(rhost, "wrapperInterpreterOptions"),
						"_swiftwrap.staging",
						"-e", executable(tr, rhost),
						"-out", stdouts,
						"-err", stderrs,
						"-i", if (stdin != null, stdins),
						"-d", remoteFileNames(fileDirs),
						"-if", remoteFileNames(inFiles),
						"-of", remoteFileNames(outFiles),
						"-cf", remoteFileNames(outCollect),
						"-cdmfile", CDM_FILE,
						"-status", "provider"
						"-a", if (arguments != null, each(eargs))
					)
					directory = tmpdir
					redirect = false
					host = rhost

					TCProfile(rhost, if (attributes != null, attributes = attributes), tr = tr)
					replicationGroup = replicationGroup
					replicationChannel = replicationChannel
					jobid = jobid

					stageIn("{PIN}file://localhost/{SWIFT:HOME}/libexec/_swiftwrap.staging", "_swiftwrap.staging")

					if (CDM_FILE != "") {
						d := swift:dirname(CDM_FILE)
						file := basename(CDM_FILE)
						dir := if (d == "", "./", "{d}/")
						loc := "{PIN}file://localhost/"
						stageIn("{loc}{dir}{file}", CDM_FILE)
						stageIn("{loc}{SWIFT:HOME}/libexec/cdm.pl", "cdm.pl")
						stageIn("{loc}{SWIFT:HOME}/libexec/cdm_lib.sh", "cdm_lib.sh")
					}
					
					appStageins(jobid, inFiles)

					stageOut("wrapper.log", "file://localhost/{DEBUG_DIR}/{jobid}.info", 
						mode = WRAPPER_TRANSFER_MODE)
					
					if (stdout == null) {
						// if not stdout requested, only stage on error
						stageOut("stdout.txt", "file://localhost/{DEBUG_DIR}/{jobid}.stdout", 
							mode = STAGING_MODE:ON_ERROR + STAGING_MODE:IF_PRESENT)
					}
					if (stderr == null) {
						stageOut("stderr.txt", "file://localhost/{DEBUG_DIR}/{jobid}.stderr",
							mode = STAGING_MODE:ON_ERROR + STAGING_MODE:IF_PRESENT)
					}
					stageOut("wrapper.error", "file://localhost/{DEBUG_DIR}/{jobid}.error", 
						mode = STAGING_MODE:IF_PRESENT)
						
					appStageouts(jobid, outFiles, outCollect)
					
					if (!isEmpty(outCollect)) {
						stageOut("_collect", "file://localhost/{DEBUG_DIR}/{jobid}.collect", 
							mode = STAGING_MODE:ALWAYS)
					}
					
					if (CLEANUP_ENABLED) {
						task:cleanUp(".")
					}
				)
				
				
				if (!isEmpty(outCollect)) {
					readCollectList("{DEBUG_DIR}/{jobid}.collect")
				}
				else {
					[]
				}
				
				log(LOG:DEBUG, "JOB_END jobid={jobid}")
			}
			else catch(prev) {
				if (matches(prev, "^Abort$")) {
					log(LOG:DEBUG, "JOB_CANCELED jobid={jobid}")
					throw(prev)
				}
				else {
					exception := try(exception(readErrorFiles(DEBUG_DIR, jobid, stdout, stdouts, stderr, stderrs), prev), prev)
					setProgress(progress, "Failed but can retry")
					log(LOG:DEBUG, "APPLICATION_EXCEPTION jobid={jobid} - Application exception: ", exception)
				
					throw(
						exception(
							concat(
								"Exception in {tr}:",
								if (arguments != null, "\n    Arguments: {arguments}")
								"\n    Host: {rhost}",
								"\n    Directory: {tmpdir}"
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
// tab-width: 4
// indent-tabs-mode: 1
// End:
