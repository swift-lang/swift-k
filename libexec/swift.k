import(sys)
import(task)
import(rlog)
import('swift-operators', export = true)
import('swift-lib', export = true)
import('swift-xs', export = true)

SWIFT:SCRIPT_NAME := contextAttribute("SWIFT:SCRIPT_NAME")
SWIFT:RUN_ID := contextAttribute("SWIFT:RUN_ID")
SWIFT:HOME := contextAttribute("SWIFT:HOME")

namespace(swift) {

	pstaging := (configProperty("use.provider.staging") == "true")
	wstaging := (configProperty("use.wrapper.staging") == "true")
	
	impl := if (pstaging, "swift-int-staging.k", if (wstaging, "swift-int-wrapper-staging.k", "swift-int.k"))
	
	import(file = impl)
	
	import(java)

	export(stageIn, def("org.griphyn.vdl.karajan.lib.Stagein"))
	export(stageOut, def("org.griphyn.vdl.karajan.lib.Stageout"))

	export(parameterLog,
		function(direction, variable, id) {
			if (configProperty("provenance.log") == "true") {
				thread := currentThread()
				log("info", "PARAM thread={thread} direction={direction} variable={variable} provenanceid={id}")
			}
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
	
	export(arguments,
		function(...) {
			arguments = expandArguments(each(...))
		}
	)

	export(mapping, def("org.griphyn.vdl.karajan.lib.Mapping$Cons"))
	export(parameter, def("org.griphyn.vdl.karajan.lib.Mapping$Parameter"))


	export(attributes,
	    function(attrs) {
	    	attributes = attrs
	    }
	)

	export(mains,
		function(channel(cleanup), channel(errors), channel(warnings)) {
			anyerrors := processBulkErrors("The following errors have occurred:", errors)

			//this should be reached after everything is done
			if (!anyerrors) {
				log(LOG:DEBUG, "Starting cleanups")
				discard(append(warnings, from(warnings, cleanups(cleanup))))
				log(LOG:DEBUG, "Ending cleanups")
			}
			else {
				log(LOG:INFO, "Errors detected. Cleanup not done.")
			}
			anywarnings := processBulkErrors("The following warnings have occurred:", warnings, onStdout=true)
			if (anyerrors) {
				throw("Execution completed with errors")
			}
		}
	)

		/*
		 * This would run in parallel with the workflow so that we don't keep
		 * all the data in memory until the workflow is done
		 */
	export(mainp,
		CBFFunction(channel(cleanup)) {
			parallel(
				to(cleanup, unique(for(c, cleanup, c)))
			)
		}
	)	
	
	export(execute,
		function(
			tr, arguments = null, 
			stdin = null, stdout = null, stderr = null, 
			attributes = null, 
			deperror = false, mdeperror = false, 
			channel(stagein), channel(stageout)) {
		
			progress := initProgressState()
		
			done := isDone(stageout)
			derr := try(deperror, false)
			merr := try(mdeperror, false)
		
			if(derr == false) {
				if(!done) {
					try {
						throttled {
							setProgress(progress, "Selecting site")
							collectList := restartOnError(number(swift:configProperty("execution.retries"))) {
								if (swift:configProperty("replication.enabled") == "true") {
									replicationChannel := channel:new()
									//trigger the first job
									discard(append(replicationChannel, true)) 
									replicationGroup := UID()
									parallelFor(i, replicationChannel) {
										try {
											allocateHost(rhost, constraints = jobConstraints(tr, stagein = stagein)) {
												execute2(
													rhost, progress, 
													tr, if(arguments != null, arguments = unwrapClosedList(arguments)),
													stdin=stdin, stdout=stdout, stderr=stderr, attributes=attributes,
													stagein, stageout, replicationGroup, replicationChannel
												)
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
									try {
										allocateHost(rhost, constraints = jobConstraints(tr, stagein = stagein)) {
											execute2(
												rhost, progress, 
												tr, if(arguments != null, arguments = unwrapClosedList(arguments)),
												stdin=stdin, stdout=stdout, stderr=stderr, attributes=attributes,
												stagein, stageout, null, null
											)
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
							log(LOG:INFO, "END_SUCCESS thread=", currentThread(), " tr={tr}")
							setProgress(progress, "Finished successfully")
						}
					}
					else catch(exception) {
						log(LOG:INFO, "END_FAILURE thread=", currentThread(), " tr={tr}")
						setProgress(progress, "Failed")
						if(swift:configProperty("lazy.errors") == "false") {
							throw(exception)
						}
						else {
							to(errors, exception)
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
					to(errors, exception)
					log(LOG:INFO, exception)
				}
				else if (derr) {
					exception := exception("Swift: Application {tr} not executed due to errors in dependencies")
					to(errors, exception)
					log(LOG:INFO, exception)
				}
				mark(stageout, true, mapping=merr)
			}
		}
	)
}
