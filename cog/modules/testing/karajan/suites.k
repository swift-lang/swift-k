/*
 * Convenience library for dealing with suites.
 * Test suites are automatically picked up from
 * files named *-suite.k
 *
 * Right now there are two suites: execution and 
 * transfer. Each suite returns a map with the following
 * keys: name, description, and code
 * The first two are obvious. The third one is an element
 * argument with what's to be executed.
 */
import("sys.k")
import("html.k")
import("tests.k")
import("task.k")
import("misc.k")
import("history.k")

namespace("suite"
  export(
	
	element(generic, [name, ...]
		...()
	)
	
	element(testRow, [tests, when]
		tr(
			th("Tests")
			for(test, tests
				set(cols, test:itemCount(test, when))
				th(colspan = cols, test:name(test, when))
			)
		)
	)
	
	element(providerRow, [tests, when]
		tr(
			th("Providers")
			for(test, tests
				set([providers, cols], test:providers(test, when), test:providerItemCount(test, when))

				for(provider, providers
					th(colspan = cols, provider)
				)
			)
		)
	)
	
	element(subtestRow, [tests, when]
		tr(
			th("Subtests")
			for(test, tests
				set([providers, subtests], test:providers(test, when), test:subtests(test, when))
				for(provider, providers
					if(
						list:size(subtests) > 0
						then(
							for(subtest, subtests,
								th(width = 80, subtest)
							)
						)
						else(
							th(width = 80, "-")
						)
					)
				)
			)
		)
	)
	
	/*
	 * A single host suite works on just one service/host
	 * It reads all tests for that suite, and executes them
	 */
	element(singleHost, [name]
		generic(name, 
			set(timeContext, date:now())
			
			set(tests, test:all(name, timeContext))
			
			h1(concat("Test time: ", date:now()))

			test:printAll(name, timeContext)
			
			table(border = 1, rules = "all", cellpadding = 2, bgcolor="#e0e0e0"
				testRow(tests, timeContext)
				providerRow(tests, timeContext)
				subtestRow(tests, timeContext)
				
				for(host, TEST_HOSTS
					set(history, "{name}-history-{host}.png")
					tr(
						th(host, a("", href = history, img("images/history.png", border = 0)))
						
						for(test, tests
										
							set([providers, subtests, testlist]
								test:providers(test, timeContext)
								test:subtests(test, timeContext)
								test:testlist(test, timeContext)
							)
							
							for(provider, providers
								set(subtestNames, test:subtests(test, timeContext))
							
								for(subtest, testlist
									set(subtestName, list:first(subtestNames))
									set(testName, test:name(test, timeContext))
									print("Running {testName}:{subtestName} on {host} with provider {provider} - ", nl = false)
									
									set(result, timeoutWrapper(timeout = TEST_TIMEOUT, host = host,
											provider = provider, wrapped = subtest)
									)
									
									if(
										not(result == status:na())
										then(
											history:data(host = host, suite = name, test = testName, subtest = subtestName
												provider = provider, result = result, time = timeContext)
										)
									)
									
									set(subtestNames, butFirst(subtestNames))
								)
							)
						)
						
						history:write(host = host, suite = name, file="output/{history}")
					)
				)
			)
		)
	)
	
	/*
	 * A two host suite works with two services/hosts
	 * It reads all tests for that suite, and executes them
	 */
	element(twoHost, [name]
		generic(name, 
			set(timeContext, date:now())
			
			set(tests, test:all(name, timeContext))
			
			h1(concat("Test time: ", date:now()))

			test:printAll(name, timeContext)
			
			
			for(test, tests
				//no subtests for now
				set([providers, testlist]
					test:providers(test, timeContext)
					test:testlist(test, timeContext)
				)
				
				set(testName, test:name(test, timeContext))
	
				h2(testName)
				table(border = 1, rules = "all", cellpadding = 2, bgcolor="#e0e0e0"
					tr(
						th("Source\Destination")
						for(host, TEST_HOSTS, th(/*colspan="2",*/ host))
					)
					for(host1, TEST_HOSTS
						tr(
							th(host1)
							for(host2, TEST_HOSTS
								set(history, "{name}-history-{host1}-{host2}.png")
								if (
									host1 == host2 td(/*colspan="2",*/ bgcolor="#e0e0e0", align="center", "-")
									
									for(provider, providers
										/*td("", width="1%", bgcolor="#b0b0b0"
											a("", href = history, img("images/history.png", border = 0))
										)*/
										print("Running {testName} on {host1}, {host2} with provider {provider} - ", nl = false)
										
										set(result, timeoutWrapper(timeout = TEST_TIMEOUT, host = host1,
											host2 = host2, provider = provider, wrapped = first(testlist))
										)
									
										if(
											not(result == status:na())
											then(
												history:data(host = "{host1} to {host2}", suite = name, 
													test = testName, subtest = ""
													provider = provider, result = result
													time = timeContext)
											)
										)
										
										print("Done")
									)
								)
							)
						)
					)
				)
			)
		)
	)
		
	element(read, [suite, when]
		cache(on = list(suite, when)
			echo("Reading suite {suite} {when}")
			executeFile(suite)
		)
	)
	
	element(prop, [suite, name, when]
		map:get(name, read(suite, when))
	)
	
	element(description, [suite, when], prop(suite, "description", when))
	
	element(name, [suite, when], prop(suite, "name", when))
	
	element(code, [suite, when], prop(suite, "code", when))
	/*
		Hmm, I really want:
		for(prop, [description, name, code]
			element(eval(prop), [suite, when], prop(suite, prop, when))
		)
	*/
	
	
	element(fileName, [suite, when]
		concat(OUTPUT_DIR, "/", suite:name(suite, when), ".html")
	)
		
	element(all, [when]
		list(
			for(suite, sort(filter(regexp = ".*suite\.k", file:list(".")))
				suite
			)
		)
	)
	
	element(run, [suite, when]
		set([file, description, code]
			fileName(suite, when)
			description(suite, when)
			code(suite, when)
		)
		
		html:write(file
			html:quickstart(description, executeElement(code))
		)
	)
  )
)