element([tr, optional(arguments, stdin, stdout, stderr, deperror), channel(stagein), channel(stageout)]
	/* may need to substitute with site selection 
	   meaning filter out sites first by looking at transformation catalog,
	   if the application is not present at a site, then it should not be
	   a candidate. Then we choose a site by some criteria.
						   
	   [m] This is done in the scheduler.
    */
    
	done := isDone(stageout)
				
	if(
		not(done) sequential(
			mark(stageout, err=false)
			graphStuff(tr, stagein, stageout, err=false, maybe(args=arguments))
		)
	)
)

