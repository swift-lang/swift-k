import("sys.k")
import("task.k")
import("settings.k")
import("html.k")
import("misc.k")
import("charting.k")
import("history.k")
import("suites.k")

	
checkpoint(interval = 24 * 3600, file="checkpoint.xml", timestamped=true, automatic=true)
	
element(testHosts, []
	executeFile("hosts.k")
)

history(
	//Loop until the end of time
	while(
		set(timeContext, date:now())
			
		print("-------- {timeContext} --------")
		
		global(TEST_HOSTS, list(testHosts()))
		global(TEST_GRID, resources(testHosts()))
		
		//Clean up previous stuff
		maybe(file:remove("testing.log"))
		for(outfile, filter(regexp="err.*html", file:list(OUTPUT_DIR)), maybe(file:remove("{OUTPUT_DIR}/{outfile}")))
		
		html:write("{OUTPUT_DIR}/index.html"

			html:quickstart(title="Java CoG Kit Tests Page"
				table(border="0", width="100%", height="100%"
					tr(height = "100%"
						td("", width="100%", valign="top"
							h1("Test time: {timeContext}") br()
							h2("Test suites:")
								
							set(suites, suite:all(timeContext))
			
							for(suite, suites,
								
								set([ref, description]
									concat(suite:name(suite, timeContext), ".html")
									suite:description(suite, timeContext)
								)
									
								a(href=ref, description) br()
									
								suite:run(suite, timeContext)
							)

							br() br()
							a(href="testing.log", "Log file")
						)
					)
					tr(
						td("", align="right"
							a(href="sources.html", "sources")
						)
					)
				)
			)
		)

		//publish files
		if(
			PUBLISH
			then(
				html:write("{OUTPUT_DIR}/sources.html"
					html:quickstart(title="Test scripts"
						ul(
							element(flist, [dir, prefix]
								for(file, filter(regexp=".*\.k", file:list(dir))
									set(fname, basename(file))
						
									print("Publishing file {prefix}{fname}")
	
									task:transfer(
										srcdir="." 
										srcfile="{prefix}{fname}"
										desthost=PUBLISH_HOST
										destdir=PUBLISH_DIR
										provider="gridftp"
									)
								
									li()
									a(href=fname, fname)
								)
								
								for(file, file:list(dir)
									if(
										file:isDirectory(file)
										then(
											set(dname, basename(file))
											if(not(dname == "output") flist(file, "{dname}/"))
										)
									)
								)
							)
							
							flist(".", "")
						)
					)
				)
	
				for(file, file:list(OUTPUT_DIR)
					set(fname, basename(file))

					print("Publishing file {fname}")
					task:transfer(srcdir=OUTPUT_DIR, srcfile=fname,
							desthost=PUBLISH_HOST, destdir=PUBLISH_DIR, provider="gridftp")
				)
				print("Transfering log file")
				
				ignoreErrors(
					task:ransfer(srcdir=".", srcfile="testing.log",
							desthost=PUBLISH_HOST, destdir=PUBLISH_DIR, provider="gridftp")
				)
			)
		)
		print("DONE")
		print("Waiting for next cycle...")
		wait(delay=2000000)
	)
)

