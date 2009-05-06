element([tr, optional(arguments, stdin, stdout, stderr, deperror, mdeperror), channel(stagein), channel(stageout), channel(restartout)]

	vdl:initprogressstate()
	vdl:setprogress("Initializing")

	done := isDone(restartout)
	derr := try(deperror, false)
	merr := try(mdeperror, false)

	if(
		derr == false then(
			if(done
				vdl:setprogress("Finished in previous run")
			)
			if(
				sys:not(done) try(
					sequential(
						log(LOG:INFO, "START thread={#thread} tr={tr}") 
						vdl:setprogress("Selecting site")
						restartOnError(".*", vdl:configProperty("execution.retries"),
							replicationChannel := channel()
							discard(append(replicationChannel, true)) //trigger the first job
							replicationGroup := uid()
							parallelFor(i, replicationChannel
								try(
									execute2(
										tr, maybe(arguments=arguments), 
										maybe(stdin=stdin), maybe(stdout=stdout), maybe(stderr=stderr), 
										stagein, stageout, restartout, replicationGroup, replicationChannel
									)
									catch("^Abort$")
								)
							)
						)
						mark(restartout, err=false, mapping=false)
						graphStuff(tr, stagein, stageout, err=false, maybe(args=arguments))
						log(LOG:INFO, "END_SUCCESS thread={#thread} tr={tr}")
						vdl:setprogress("Finished successfully")
					)
					catch(".*"
						log(LOG:INFO, "END_FAILURE thread={#thread} tr={tr}")
						vdl:setprogress("Failed")
						if(
							vdl:configProperty("lazy.errors") == "false" then(
								throw(exception)
							)
							else (
								to(errors, exception)
								log(LOG:INFO, exception)
								mark(restartout, err=true, mapping=false)
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
			mark(restartout, err=true, mapping=merr)
			graphStuff(tr, stagein, stageout, err=true, maybe(args=arguments))
		)
	)
)
