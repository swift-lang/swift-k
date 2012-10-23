element([tr, optional(arguments, stdin, stdout, stderr, deperror), channel(stagein), channel(stageout), channel(restartout)]

	done := isDone(stageout)
				
	if(
		sys:not(done) sequential(
			mark(stageout, err=false)
			graphStuff(tr, stagein, stageout, err=false, maybe(args=arguments))
		)
	)
)

