element([tr, optional(arguments, stdin, stdout, stderr, deperror, mdeperror), channel(stagein), channel(stageout)]

	vdl:initprogressstate()
	vdl:setprogress("Initializing")

	done := isDone(stageout)
	derr := try(deperror, false)
	merr := try(mdeperror, false)

	if(
		derr == false then(
			if(
				sys:not(done) try(
					sequential(
						echo("{tr} started")
						log(LOG:INFO, "START thread={#thread} tr={tr}") 
						restartOnError(".*", vdl:configProperty("execution.retries"),
							replicationChannel := channel()
							discard(append(replicationChannel, true)) //trigger the first job
							replicationGroup := uid()
							parallelFor(i, replicationChannel
								try(
									execute2(
										tr, maybe(arguments=arguments), 
										maybe(stdin=stdin), maybe(stdout=stdout), maybe(stderr=stderr), 
										stagein, stageout, replicationGroup, replicationChannel
									)
									catch("^Abort$")
								)
							)
						)
						mark(stageout, err=false, mapping=false)
						graphStuff(tr, stagein, stageout, err=false, maybe(args=arguments))
						echo("{tr} completed")
						log(LOG:INFO, "END_SUCCESS thread={#thread} tr={tr}")
						vdl:setprogress("Finished successfully")
					)
					catch(".*"
						echo("{tr} failed")
						log(LOG:INFO, "END_FAILURE thread={#thread} tr={tr}")
						vdl:setprogress("Failed")
						if(
							vdl:configProperty("lazy.errors") == "false" then(
								throw(exception)
							)
							else (
								to(errors, exception)
								log(LOG:INFO, exception)
								mark(stageout, err=true, mapping=false)
								graphStuff(tr, stagein, stageout, err=true, maybe(args=arguments))
							)
						)
					)
				)
			)
		)
		else (
			if(
				merr == true then(
					exception := exception(concat("VDL2: Application ", str:quote(tr), 
						" not executed due to errors in mapping dependencies"))
					to(errors, exception)
					log(LOG:INFO, exception)
				)
				derr == true then(
					exception := exception("VDL2: Application {tr} not executed due to errors in dependencies")
					to(errors, exception)
					log(LOG:INFO, exception)
				)
			)
			mark(stageout, err=true, mapping=merr)
			graphStuff(tr, stagein, stageout, err=true, maybe(args=arguments))
		)
	)
)
