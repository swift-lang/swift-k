import(sys)
import(task)
import('swift-lib', export = true)

import('swift-int')
import('swift-int-staging')
import('swift-int-wrapper-staging')

SWIFT_CONFIG := contextAttribute("SWIFT:CONFIG")
SWIFT:SCRIPT_NAME := contextAttribute("SWIFT:SCRIPT_NAME")
SWIFT:RUN_ID := contextAttribute("SWIFT:RUN_ID")
SWIFT:HOME := contextAttribute("SWIFT:HOME")
SITEDIR_KEEP := configProperty("keepSiteDir")

CLEANUPS := contextAttribute("RUN:CLEANUPS")
ERRORS := contextAttribute("RUN:ERRORS")
WARNINGS := contextAttribute("RUN:WARNINGS")

namespace(swift) {
	export(stageIn, def("org.griphyn.vdl.karajan.lib.Stagein"))
	export(stageOut, def("org.griphyn.vdl.karajan.lib.Stageout"))

	export(parameterLog,
		function(direction, variable, id) {
			if (configProperty("logProvenance")) {
				thread := currentThread()
				log("info", "PARAM thread={thread} direction={direction} variable={variable} provenanceid={id}")
			}
		}
	)
	
	cleanup := function(dir, host) {
		log(LOG:INFO, "START dir={dir} host={host}")
		cdmfile := cdm:file()
		log(LOG:DEBUG, "cdmfile {cdmfile}")
		if (cdmfile != "" & cdm:get("GATHER_DIR") != "UNSET") {
			log(LOG:INFO, "submitting cdm_cleanup.sh to {dir}")
			task:transfer("cdm_cleanup.sh",
				srcdir="{SWIFT:HOME}/libexec",
				desthost=host, destdir=dir)
			task:transfer("cdm_lib.sh",
				srcdir="{SWIFT:HOME}/libexec",
				desthost=host, destdir=dir)
		        log(LOG:INFO, "execute: cdm_cleanup.sh")
			task:execute("/bin/bash", list("{dir}/cdm_cleanup.sh", cdm:get("GATHER_DIR"), cdm:get("GATHER_TARGET"), UID())
				host=host, batch=true, TCProfile(host, tr = "*"))
		}
		if (!SITEDIR_KEEP) {
			task:execute(siteProfile(host, "cleanupCommand"),
				list(siteProfile(host, "cleanupCommandOptions"), dir)
				host=host, batch=true, TCProfile(host, tr = "*"))
		}
		log(LOG:INFO, "END dir={dir} host={host}")
	}
	
	export(cleanups,
		function(cleanup) {
			log(LOG:INFO, "START cleanups={cleanup}")
			parallelFor(i, cleanup) {
				(dir, host) := each(i)
				try {
					cleanup(dir, host)
				}
				else catch(exception) {
					log(LOG:DEBUG, "EXCEPTION - Exception caught while cleaning up", exception)
					discard(append(WARNINGS, exception("Cleanup on {host} failed", exception)))
				}
			}
			log(LOG:INFO, "END cleanups={cleanup}")
		}
	)
	
	export(split,
		function(var) {
			each(str:split(getFieldValue(var), " "))
		}
	)

	export(quote,
		function(var, path = null) {
			str:quote(getFieldValue(var, if (path != null) { path = path }))
		}
	)

	export(types, function() {})
	
	export(mapping, def("org.griphyn.vdl.karajan.lib.Mapping$Cons"))
	export(parameter, def("org.griphyn.vdl.karajan.lib.Mapping$Parameter"))


	export(attributes,
	    function(attrs) {
	    	attributes = attrs
	    }
	)

	export(mains,
		function() {
			anyerrors := processBulkErrors("The following errors have occurred:", ERRORS)

			//this should be reached after everything is done
			if (!anyerrors) {
				log(LOG:DEBUG, "Starting cleanups")
				discard(append(WARNINGS, cleanups(CLEANUPS)))
				log(LOG:DEBUG, "Ending cleanups")
			}
			else {
				log(LOG:INFO, "Errors detected. Cleanup not done.")
			}
			anywarnings := processBulkErrors("The following warnings have occurred:", WARNINGS, onStdout=true)
			if (anyerrors) {
				throw("Execution completed with errors")
			}
		}
	)
	
	executeSelect := function(jobid, rhost, progress, tr, commands, attributes,
		stagein, stageout, replicationGroup, replicationChannel) {
		
		staging := configProperty("staging", host = rhost)
		if (staging == "swift") {
			k:tailcall(swiftStaging:execute2(jobid, rhost, progress, tr, commands, attributes,
				stagein, stageout, replicationGroup, replicationChannel))
		}
		else if (staging == "provider") {
			k:tailcall(providerStaging:execute2(jobid, rhost, progress, tr, commands, attributes,
				stagein, stageout, replicationGroup, replicationChannel))
		}
		else {
			k:tailcall(wrapperStaging:execute2(jobid, rhost, progress, tr, commands, attributes,
				stagein, stageout, replicationGroup, replicationChannel))
		}
	}
	
	export(execute,
		function(
			tr, commands, 
			attributes = null, 
			deperror = false, mdeperror = false, 
			channel(stagein), channel(stageout)) {
			
			progress := initProgressState()
			
			jobid := generateJobId(tr)
		
			log(LOG:DEBUG, "JOB_INIT jobid={jobid} tr={tr}")
		
			done := isDone(stageout)
			derr := try(deperror, false)
			merr := try(mdeperror, false)
		
			if(derr == false) {
				if(!done) {
					utry {
						throttled {
							setProgress(progress, "Selecting site")
							collectList := restartOnError(counter, swift:configProperty("executionRetries")) {
								jobid := "{jobid}-{counter}"
								if (swift:configProperty("replicationEnabled")) {
									replicationChannel := channel:new()
									//trigger the first job
									discard(append(replicationChannel, true)) 
									replicationGroup := UID()
									parallelFor(i, replicationChannel) {
										utry {
											log(LOG:DEBUG, "JOB_SITE_SELECT jobid={jobid}")
											allocateHost(rhost, constraints = jobConstraints(commands, stagein = stagein)) {
												log(LOG:DEBUG, "THREAD_ASSOCIATION jobid={jobid} host={rhost} thread=", currentThread(), " replicationGroup={replicationGroup}")
												k:tailcall(executeSelect(
													jobid, rhost, progress, 
													tr, commands,
													attributes,
													stagein, stageout, replicationGroup, replicationChannel
												))
											}
										}
										else catch(exception) {
											if (matches(exception, "^Abort$")) {
												// ignored
											}
											else {
												throw(exception)
											}
										}
									}
								}
								else {
									utry {
										log(LOG:DEBUG, "JOB_SITE_SELECT jobid={jobid}")
										allocateHost(rhost, constraints = jobConstraints(commands, stagein = stagein)) {
											log(LOG:DEBUG, "THREAD_ASSOCIATION jobid={jobid} host={rhost} thread=", currentThread())
											if (counter == 0) {
												k:tailcall(executeSelect(
													jobid, rhost, progress, 
													tr, k:getAndDelete(commands),
													k:getAndDelete(attributes, stagein), stageout, null, null
												))
											}
											else {
												k:tailcall(executeSelect(
													jobid, rhost, progress, 
													tr, commands,
													attributes, stagein, stageout, null, null
												))
											}
										}
									}
									else catch(exception) {
										if (matches(exception, "^Abort$")) {
											// ignored
										}
										else {
											throw(exception)
										}
									}
								}
							}
							processStageouts(stageout, collectList)
							log(LOG:INFO, "END_SUCCESS jobid={jobid} tr={tr}")
							setProgress(progress, "Finished successfully")
						}
					}
					else catch(exception) {
						log(LOG:INFO, "END_FAILURE jobid={jobid} tr={tr}")
						setProgress(progress, "Failed")
						if(!swift:configProperty("lazyErrors")) {
							throw(exception)
						}
						else {
							discard(append(ERRORS, exception))
							log(LOG:INFO, exception)
							echo(exception)
							mark(stageout, true, mapping=false)
						}
					}
				}
				else {
					setDatasetValues(stageout)
					setProgress(progress, "Finished in previous run")
				}
			}
			else { //derr == true
				if (merr) {
					exception := exception(concat("Swift: Application ", str:quote(tr),
						" not executed due to errors in mapping dependencies"))
					discard(append(ERRORS, exception))
					log(LOG:INFO, exception)
				}
				else if (derr) {
					exception := exception("Swift: Application {tr} not executed due to errors in dependencies")
					discard(append(ERRORS, exception))
					log(LOG:INFO, exception)
				}
				mark(stageout, true, mapping=merr)
			}
		}
	)
}
