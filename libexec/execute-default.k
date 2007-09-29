element([tr, optional(arguments, stdin, stdout, stderr, deperror, mdeperror), channel(stagein), channel(stageout)]
	/* may need to substitute with site selection 
	   meaning filter out sites first by looking at transformation catalog,
	   if the application is not present at a site, then it should not be
	   a candidate. Then we choose a site by some criteria.
						   
	   [m] This is done in the scheduler.
    */
    
	done := isDone(stageout)
	derr := try(deperror, false)
	merr := try(mdeperror, false)

	if(
		derr == false then(
			if(
				not(done) try(
					sequential(
						echo("{tr} started")
						log(LOG:INFO, "START thread={#thread} tr={tr}") 
						restartOnError(".*", 2,
							execute2(
								tr, maybe(arguments=arguments), 
								maybe(stdin=stdin), maybe(stdout=stdout), maybe(stderr=stderr), 
								stagein, stageout
							)
						)
						mark(stageout, err=false, mapping=false)
						graphStuff(tr, stagein, stageout, err=false, maybe(args=arguments))
						echo("{tr} completed")
						log(LOG:INFO, "END SUCCESS thread={#thread} tr={tr}")
					)
					catch(".*"
						echo("{tr} failed")
						log(LOG:INFO, "END FAILURE thread={#thread} tr={tr}")
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
