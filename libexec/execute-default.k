element([tr, optional(arguments, stdin, stdout, stderr, attributes, deperror, mdeperror), channel(stagein), channel(stageout), channel(restartout)]

	vdl:initprogressstate()

	done := isDone(restartout)
	derr := try(deperror, false)
	merr := try(mdeperror, false)

	if(
		derr == false then(
			if(
				sys:not(done) try(
					throttled(
						log(LOG:INFO, "START thread={#thread} tr={tr}")
						vdl:setprogress("Selecting site")
						restartOnError(".*", vdl:configProperty("execution.retries"),
							replicationChannel := channel()
							discard(append(replicationChannel, true)) //trigger the first job
							replicationGroup := uid()
							parallelFor(i, replicationChannel
								ignoreErrors(match = "^Abort$"
									execute2(
										tr, maybe(arguments=unwrapClosedList(arguments)),
										maybe(stdin=stdin), maybe(stdout=stdout), maybe(stderr=stderr),
                    maybe(attributes=attributes),
										stagein, stageout, restartout, replicationGroup, replicationChannel
									)
								)
							)
						)
						mark(restartout, err=false, mapping=false)
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
								echo(exception)
								mark(restartout, err=true, mapping=false)
								graphStuff(tr, stagein, stageout, err=true, maybe(args=arguments))
							)
						)
					)
				)
				else (
					vdl:setprogress("Finished in previous run")
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
