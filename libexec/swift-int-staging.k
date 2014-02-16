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
	if (configProperty("wrapperlog.always.transfer") == "true", 
		STAGING_MODE:IF_PRESENT, STAGING_MODE:ON_ERROR + STAGING_MODE:IF_PRESENT)

pinOption := configProperty("provider.staging.pin.swiftfiles")

PIN := if(pinOption == "true", "pinned:", "")
PROVENANCE_GRAPH_ENABLED := (configProperty("pgraph") != "false")
CLEANUP_ENABLED := (configProperty("sitedir.keep") != "true")

namespace(swift) {

	initDDir := function() {
		"{SWIFT:DEBUG_DIR_PREFIX}{SWIFT:SCRIPT_NAME}-{SWIFT:RUN_ID}.d"
	}
	
	inFiles := function(stageins) {
		pathnames(stageins)
	}
	
	fileSizes := function(files) {
		math:sum(
			for(f, files, file:size(f))
		)
	}

	export(cleanups,
		function(cleanup) {
			log(LOG:INFO, "START cleanups={cleanup}")
		}
	)

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
		function(progress, tr, stagein, stageout,  restartout
			replicationGroup, replicationChannel
			arguments = [], stdin = null, stdout = null, stderr = null, attributes = null) {
	
			stagein := list(unique(each(stagein)))
			stageout := list(unique(each(stageout)))

			allocateHost(rhost, constraints = jobConstraints(tr, stagein = stagein)) {

				ddir := initDDir()

				uid := UID()
				jobdir := substring(uid, 0, to=1)
				jobid := "{tr}-{uid}"

				log(LOG:DEBUG, "THREAD_ASSOCIATION jobid={jobid} thread=", currentThread(), " host={rhost} replicationGroup={replicationGroup}")
				
				wfdir := "{SWIFT:SCRIPT_NAME}-{SWIFT:RUN_ID}"
				tmpdir := dircat("{wfdir}/jobs/{jobdir}", jobid)
				cdmfile := cdm:file()

				try {
					log(LOG:DEBUG, "JOB_START jobid={jobid} tr={tr}", if (arguments != null, (" arguments=", arguments)), 
						" tmpdir={tmpdir} host={rhost}")

					setProgress(progress, "Submitting")

					swift:execute(
						progress,
						siteProfile(rhost, "swift:wrapperInterpreter"),
						list(
							siteProfile(rhost, "swift:wrapperInterpreterOptions"),
							"_swiftwrap.staging",
							"-e", executable(tr, rhost),
							"-out", if(stdout == null, "stdout.txt", getFieldValue(stdout)),
							"-err", if(stderr == null, "stderr.txt", getFieldValue(stderr)),
							"-i", if (stdin != null, getFieldValue(stdin)),
							"-d", flatten(unique(outFileDirs(stageout))),
							"-if", flatten(inFiles(stagein)),
							"-of", flatten(outFiles(stageout)),
							"-k",
							"-cdmfile", cdmfile,
							"-status", "provider"
							"-a", if (arguments != null, each(arguments))
						)
						directory = tmpdir
						redirect = false
						host = rhost

						TCProfile(rhost, if (attributes != null, attributes = attributes), tr = tr)
						replicationGroup = replicationGroup
						replicationChannel = replicationChannel
						jobid = jobid
					
						stagingMethod := siteProfile(rhost, "swift:stagingMethod", default="proxy")

						stageIn("{PIN}{stagingMethod}://localhost/{SWIFT:HOME}/libexec/_swiftwrap.staging", "_swiftwrap.staging")

						if (cdmfile != "") {
							d := swift:dirname(cdmfile)
							file := basename(cdmfile)
							dir := if (d == "", "./", "{d}/")
							loc := "{PIN}{stagingMethod}://localhost/"
							stageIn("{loc}{dir}{file}", cdmfile)
							stageIn("{loc}{SWIFT:HOME}/libexec/cdm.pl", "cdm.pl")
							stageIn("{loc}{SWIFT:HOME}/libexec/cdm_lib.sh", "cdm_lib.sh")
						}

						appStageins(jobid, stagein, stagingMethod)

						stageOut("wrapper.log", "{stagingMethod}://localhost/{ddir}/{jobid}.info", 
							mode = WRAPPER_TRANSFER_MODE)
						
						if (stdout == null) {
							// if not stdout requested, only stage on error
							stageOut("stdout.txt", "{stagingMethod}://localhost/{ddir}/{jobid}.stdout", 
								mode = STAGING_MODE:ON_ERROR + STAGING_MODE:IF_PRESENT)
						}
						else {
							stageOut("{stdout}", "{stagingMethod}://localhost/{ddir}/{stdout}",
								mode = STAGING_MODE:ALWAYS + STAGING_MODE:IF_PRESENT)
						}
						if (stderr == null) {
							stageOut("stderr.txt", "{stagingMethod}://localhost/{ddir}/{jobid}.stderr",
								mode = STAGING_MODE:ON_ERROR + STAGING_MODE:IF_PRESENT)
						}
						else {
							stageOut("{stderr}", "{stagingMethod}://localhost/{ddir}/{stderr}",
								mode = STAGING_MODE:ALWAYS + STAGING_MODE:IF_PRESENT)
						}
						stageOut("wrapper.error", "{stagingMethod}://localhost/{ddir}/{jobid}.error", 
							mode = STAGING_MODE:IF_PRESENT)
						appStageouts(jobid, stageout, stagingMethod)
						
						if (CLEANUP_ENABLED) {
							task:cleanUp(".")
						}
					)
					
					
					doRestartLog(restartout)
					log(LOG:DEBUG, "JOB_END jobid={jobid}")
				}
				else catch(prev) {
					if (matches(prev, "^Abort$")) {
						log(LOG:DEBUG, "JOB_CANCELED jobid={jobid}")
						throw(prev)
					}
					else {
						setProgress(progress, "Failed but can retry")
						exception := try(exception(readErrorFiles(ddir, jobid, stdout, stderr), prev), prev)
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
		}
	)
}

// Local variables:
// tab-width: 4
// indent-tabs-mode: 1
// End:
