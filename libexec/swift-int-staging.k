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

SWIFT:WFDIR := "{SWIFT:SCRIPT_NAME}-{SWIFT:RUN_ID}"

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

	readErrorFiles := function(dir, jobid) {
		concat(
			if (file:exists("{dir}/{jobid}.error")) {
				str:strip(file:read("{dir}/{jobid}.error"))
				file:remove("{dir}/{jobid}.error")
			}
			stderrs := "{dir}/{jobid}.stderr"
			stdouts := "{dir}/{jobid}.stdout"
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

			jobdir := substring(jobid, 0, to = 1)
			
			tmpdir := dircat("{SWIFT:WFDIR}/jobs/{jobdir}", jobid)
						
			stagingMethod := siteProfile(rhost, "stagingMethod", default="file"),
			(fileDirs, inFiles, outFiles, outCollect, tempRefs) := getStagingInfo(getAndDelete(stagein, stageout), defaultScheme = stagingMethod),
						
			wrapperMode := configProperty("wrapperParameterMode", host=rhost)

			utry {
				resolveFiles(commands, rhost, stagingMethod = stagingMethod)
				log(LOG:DEBUG, "JOB_START jobid={jobid} tr={tr}", swift:formatCommands(commands, "log"), " tmpdir={tmpdir}")

				setProgress(progress, "Submitting")
				
				wrapfile := "{DEBUG_DIR}/param-{jobid}"
				
				if (wrapperMode == "files") {
					sys:file:write(wrapfile) {
						"\n-d\n", remoteFileNames(getAndDelete(fileDirs)),
						"\n-if\n", remoteFileNames(inFiles),
						"\n-of\n", remoteFileNames(outFiles),
						"\n-cf\n", remoteFileNames(outCollect),
						"\n-cdmfile\n", cdm:file(),
						"\n-status\nprovider\n",
						swift:formatCommands(commands, "paramfile")
					}
				}
				
				wrapperScript := siteProfile(rhost, "wrapperScript.staging")

				swift:execute(
					progress,
					siteProfile(rhost, "wrapperInterpreter"),
					if (wrapperMode == "files") {
						list(
							siteProfile(rhost, "wrapperInterpreterOptions"),
							wrapperScript,
							"-p", jobdir
						)
					}
					else {
						list(
							siteProfile(rhost, "wrapperInterpreterOptions"),
							wrapperScript,
							"-d", remoteFileNames(getAndDelete(fileDirs)),
							"-if", remoteFileNames(inFiles),
							"-of", remoteFileNames(outFiles),
							"-cf", remoteFileNames(outCollect),
							"-cdmfile", CDM_FILE,
							"-status", "provider"
							swift:formatCommands(commands, "args")
						)
					}
					directory = tmpdir
					redirect = false
					host = rhost
					tempRefs = getAndDelete(tempRefs)

					TCProfile(rhost, if (attributes != null, attributes = attributes), commands = getAndDelete(commands), tr = tr)
					replicationGroup = replicationGroup
					replicationChannel = replicationChannel
					jobid = jobid

					stageIn("{PIN}file://localhost/{SWIFT:HOME}/libexec/{wrapperScript}", wrapperScript)
					
					if (wrapperMode == "files") {
						stageIn("file://localhost/{wrapfile}", "_paramfile")
					}

					if (CDM_FILE != "") {
						d := swift:dirname(CDM_FILE)
						file := basename(CDM_FILE)
						dir := if (d == "", "./", "{d}/")
						loc := "{PIN}file://localhost/"
						stageIn("{loc}{dir}{file}", CDM_FILE)
						stageIn("{loc}{SWIFT:HOME}/libexec/cdm.pl", "cdm.pl")
						stageIn("{loc}{SWIFT:HOME}/libexec/cdm_lib.sh", "cdm_lib.sh")
					}
					
					appStageins(jobid, getAndDelete(inFiles))
					appStageouts(jobid, getAndDelete(outFiles), outCollect)

					stageOut("_wrapper.log", "file://localhost/{DEBUG_DIR}/{jobid}.info", 
						mode = WRAPPER_TRANSFER_MODE)
					stageOut("_stdout.txt", "file://localhost/{DEBUG_DIR}/{jobid}.stdout", 
							mode = STAGING_MODE:ON_ERROR + STAGING_MODE:IF_PRESENT)
					stageOut("_stderr.txt", "file://localhost/{DEBUG_DIR}/{jobid}.stderr",
							mode = STAGING_MODE:ON_ERROR + STAGING_MODE:IF_PRESENT)
					stageOut("_wrapper.error", "file://localhost/{DEBUG_DIR}/{jobid}.error", 
						mode = STAGING_MODE:IF_PRESENT)
						
					
					if (!isEmpty(outCollect)) {
						stageOut("_collect", "file://localhost/{DEBUG_DIR}/{jobid}.collect", 
							mode = STAGING_MODE:ALWAYS)
					}
					
					if (CLEANUP_ENABLED) {
						task:cleanUp(".")
					}
				)
				
				if (wrapperMode == "files") {
					file:remove(wrapfile)
				}
				
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
					exception := utry(exception(readErrorFiles(DEBUG_DIR, jobid), prev), prev)
					setProgress(progress, "Failed but can retry")
					log(LOG:DEBUG, "APPLICATION_EXCEPTION jobid={jobid} - Application exception: ", exception)
				
					throw(
						exception(
							concat(
								"Exception in {tr}:",
								"\n    Host: {rhost}",
								"\n    Directory: {tmpdir}"
							)
							exception
						)
					)
				}
			} // try/catch
		} // function
	) // export
} // namespace

// Local variables:
// tab-width: 4
// indent-tabs-mode: 1
// End:
