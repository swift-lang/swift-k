import(sys)
import(task)
import('swift-lib')
/*
 * Things that are not exposed to the translated file
 */


WRAPPER_TRANSFER_MODE :=
	if (vdl:configProperty("wrapperlog.always.transfer") == "true", 
		STAGING_MODE:IF_PRESENT, STAGING_MODE:ON_ERROR + STAGING_MODE:IF_PRESENT)

pinOption := configProperty("provider.staging.pin.swiftfiles")

PIN := if(pinOption == "true", "pinned:", "")
PROVENANCE_GRAPH_ENABLED := (vdl:configProperty("pgraph") != "false")

namespace(swift) {

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

	readErrorFile := function(dir, jobid) {
		str:strip(file:read("{dir}/{jobid}.error"))
		file:remove("{dir}/{jobid}.error")
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
				jobdir := substring(uid, from=0, to=1)
				jobid := "{tr}-{uid}"

				log(LOG:DEBUG, "THREAD_ASSOCIATION jobid={jobid} thread=", currentThread(), " host={rhost} replicationGroup={replicationGroup}")

				wrapper := "_swiftwrap.staging"
				wrapfile := "{ddir}/param-{jobid}"
				
				stdout := if (stdout == null, "stdout.txt", getFieldValue(stdout))
				stderr := if (stderr == null, "stderr.txt", getFieldValue(stderr))

				wfdir := "{VDL:SCRIPTNAME}-{VDL:RUNID}"
				tmpdir := dircat("{wfdir}/jobs/{jobdir}", jobid)
				cdmfile := cdm:file()

				try {
					log(LOG:DEBUG, "JOB_START jobid={jobid} tr={tr}", if (arguments != null, (" arguments=", arguments)), " tmpdir={tmpdir} host={rhost}")

					setProgress(progress, "Submitting")

					swift:execute(
						siteProfile(rhost, "swift:wrapperInterpreter"),
						list(
							siteProfile(rhost, "swift:wrapperInterpreterOptions"),
							wrapper,
							"-e", vdl:executable(tr, rhost),
							"-out", stdout,
							"-err", stderr,
							"-i", if (stdin != null, getFieldValue(stdin)),
							"-d", flatten(unique(outFileDirs(stageout))),
							"-if", flatten(infiles(stagein)),
							"-of", flatten(outfiles(stageout)),
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

						stageIn("{PIN}{stagingMethod}://localhost/{swift.home}/libexec/{wrapper}", wrapper)

						if (cdmfile != "") {
							d := dirname(cdmfile)
							file := basename(cdmfile)
							dir := if (d == "", "./", str:concat(d,"/"))
							loc := "{PIN}{stagingMethod}://localhost/"
							stageIn("{loc}{dir}{file}", cdmfile)
							stageIn("{loc}{swift.home}/libexec/cdm.pl", "cdm.pl")
							stageIn("{loc}{swift.home}/libexec/cdm_lib.sh", "cdm_lib.sh")
						}

						appStageins(jobid, stagein, ".", stagingMethod)

						stageOut("wrapper.log", "{stagingMethod}://localhost/{ddir}/{jobid}.info", 
							mode = WRAPPER_TRANSFER_MODE)
						//stageOut("{stdout}", "{stagingMethod}://localhost/{ddir}/{stdout}")
						//stageOut("{stderr}", "{stagingMethod}://localhost/{ddir}/{stderr}")
						stageOut("wrapper.error", "{stagingMethod}://localhost/{ddir}/{jobid}.error", 
							mode = STAGING_MODE:IF_PRESENT)
						appStageouts(jobid, stageout, ".", stagingMethod)

						task:cleanUp(".") //the whole job directory
					) // execute
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
						exception := try(exception(readErrorFile(ddir, jobid)), prev)
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
