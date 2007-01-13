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
						restartOnError(".*", 2,
							execute2(
								tr, maybe(arguments=arguments), 
								maybe(stdin=stdin), maybe(stdout=stdout), maybe(stderr=stderr), 
								stagein, stageout
							)
						)
						mark(stageout, err=false)
						graphStuff(tr, stagein, stageout, err=false, maybe(args=arguments))
					)
					catch(".*"
						if(
							vdl:configProperty("lazy.errors") == "false" then(
								throw(exception)
							)
							else (
								to(errors, exception)
								mark(stageout, err=true)
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
					to(errors, exception(concat("VDL2: Application ", str:quote(tr), 
						" not executed due to errors in mapping dependencies")))
				)
				derr == true then(
					to(errors, exception("VDL2: Application {tr} not executed due to errors in dependencies"))
				)
			)
			mark(stageout, err=true)
			graphStuff(tr, stagein, stageout, err=true, maybe(args=arguments))
		)
	)
)
