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

	readErrorFiles := function(dir, jobid, stdout, stderr) {
		concat(
			if(file:exists("{dir}/{jobid}.error")) {
				str:strip(file:read("{dir}/{jobid}.error"))
				file:remove("{dir}/{jobid}.error")
			}
			stderr := if(stderr == null, "{jobid}.stderr", stderr)
			stdout := if(stdout == null, "{jobid}.stdout", stdout)
			if(file:exists("{dir}/{stderr}")) {
				"\n\n------- Application STDERR --------\n"
				str:strip(file:read("{dir}/{stderr}"))
				"\n-----------------------------------\n"
			}
			else if(file:exists("{dir}/{stdout}")) {
				"\n\n------- Application STDOUT --------\n"
				str:strip(file:read("{dir}/{stdout}"))
				"\n-----------------------------------\n"
			}
		)
	}
	
	export(execute2,
		function(rhost, progress, tr, arguments, attributes, stdin, stdout, stderr, 
			stagein, stageout, replicationGroup, replicationChannel) {

			uid := UID()
			jobdir := substring(uid, 0, to=1)
			jobid := "{tr}-{uid}"

			log(LOG:DEBUG, "THREAD_ASSOCIATION jobid={jobid} thread=", currentThread(), " host={rhost} replicationGroup={replicationGroup}")
			
			wfdir := "{SWIFT:SCRIPT_NAME}-{SWIFT:RUN_ID}"
			tmpdir := dircat("{wfdir}/jobs/{jobdir}", jobid),				
			
			(fileDirs, inFiles, outFiles, outCollect) := getStagingInfo(stagein, stageout)

			try {
				eargs := expandArguments(arguments, rhost)
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
						"-out", if(stdout == null, "stdout.txt", getFieldValue(stdout)),
						"-err", if(stderr == null, "stderr.txt", getFieldValue(stderr)),
						"-i", if (stdin != null, getFieldValue(stdin)),
						"-d", str:join(fileDirs, "|"),
						"-if", str:join(remoteFileNames(inFiles), "|"),
						"-of", str:join(remoteFileNames(outFiles), "|"),
						"-cf", str:join(remoteFileNames(outCollect), "|"),
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
				
					stagingMethod := siteProfile(rhost, "stagingMethod", default="proxy")

					stageIn("{PIN}{stagingMethod}://localhost/{SWIFT:HOME}/libexec/_swiftwrap.staging", "_swiftwrap.staging")

					if (CDM_FILE != "") {
						d := swift:dirname(CDM_FILE)
						file := basename(CDM_FILE)
						dir := if (d == "", "./", "{d}/")
						loc := "{PIN}{stagingMethod}://localhost/"
						stageIn("{loc}{dir}{file}", CDM_FILE)
						stageIn("{loc}{SWIFT:HOME}/libexec/cdm.pl", "cdm.pl")
						stageIn("{loc}{SWIFT:HOME}/libexec/cdm_lib.sh", "cdm_lib.sh")
					}

					appStageins(jobid, inFiles, stagingMethod)

					stageOut("wrapper.log", "{stagingMethod}://localhost/{DEBUG_DIR}/{jobid}.info", 
						mode = WRAPPER_TRANSFER_MODE)
					
					if (stdout == null) {
						// if not stdout requested, only stage on error
						stageOut("stdout.txt", "{stagingMethod}://localhost/{DEBUG_DIR}/{jobid}.stdout", 
							mode = STAGING_MODE:ON_ERROR + STAGING_MODE:IF_PRESENT)
					}
					else {
						stdoutf := getFieldValue(stdout)
						stageOut(stdoutf, "{stagingMethod}://localhost/{DEBUG_DIR}/{stdoutf}",
							mode = STAGING_MODE:IF_PRESENT)
					}
					if (stderr == null) {
						stageOut("stderr.txt", "{stagingMethod}://localhost/{DEBUG_DIR}/{jobid}.stderr",
							mode = STAGING_MODE:ON_ERROR + STAGING_MODE:IF_PRESENT)
					}
					else {
						stderrf := getFieldValue(stderr)
						stageOut(stderrf, "{stagingMethod}://localhost/{DEBUG_DIR}/{stderrf}",
							mode = STAGING_MODE:IF_PRESENT)
					}
					stageOut("wrapper.error", "{stagingMethod}://localhost/{DEBUG_DIR}/{jobid}.error", 
						mode = STAGING_MODE:IF_PRESENT)
						
					appStageouts(jobid, outFiles, outCollect, stagingMethod)
					
					if (!isEmpty(outCollect)) {
						stageOut("_collect", "{stagingMethod}://localhost/{DEBUG_DIR}/{jobid}.collect", 
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
					setProgress(progress, "Failed but can retry")
					exception := try(exception(readErrorFiles(DEBUG_DIR, jobid, stdout, stderr), prev), prev)
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
