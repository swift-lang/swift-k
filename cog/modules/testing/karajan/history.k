/*
 * History library. Keeps a history of hosts, and the results
 * of individual tests on each host. Can also write a chart
 * with the history of individual hosts
 */
 
import("sys.k")
import("charting.k")

namespace("history"
  export(
	/*
	 * history runs in parallel with its arguments. It
	 * receives arguments on 'history-channel' (any resemblance
	 * to real-life channels of any sort is purely coincidental)
	 */	
	parallelElement(history, [channel(historyChannel)]
		set(history, map())
		
		for(item, historyChannel,
			set([command, data], list:first(item), list:last(item))
			if(
				command == "write"
					writeHistory(history = history, 
						host = map:get("host", data), 
						suite = map:get("suite", data) 
						file = map:get("file", data)
					)
				
				
				command == "data"
					addToHistory(history, data)
			)
		)
	)
	
	/*
	 * Adds an entry to a map if an entry for a key
	 * does not already exist. If no object is specified
	 * and empty map is added
	 */
	element(getDefault, [map, key, optional(obj)]
		default(obj, map())
		if(
			not(map:contains(key, map))
			then(
				map:put(map, map:entry(key, obj))
			)
		)
		
		map:get(key, map)
	)
	
	/*
	 * Adds data to the data tree. The data argument
	 * contains the data to be added. The map argument
	 * is the root of the tree. The keytrail is a list
	 * with node names that lead to a leaf in the tree.
	 *
	 * The tree is organized in the following way: at depth 1 
	 * there are all the hosts. Each host has branches for all
	 * the test suites. Each suite has branches for all the tests, 
	 * which in turn have branches for all subtests. Subtests
	 * have branches for each of the providers which finally have
	 * branches for all the times tests occured. The leaves contain
	 * the results of the test (failed, ok, N/A, no-proxy)
	 */
	element(updateHistMap, [data, map, keytrail]
		if(
			list:isEmpty(keytrail)
			then(generateError("keytrail cannot be empty"))
			else(
				set(key, list:first(keytrail))

				if(
					equals(list:size(keytrail), 1)
					then(
						map:put(map, map:entry(key, data))
					)
					else(
						updateHistMap(data, getDefault(map, key), list:butFirst(keytrail))
					)
				)
			)
		)
	)
	
	/*
	 * Used internally to add data to the tree.
	 */
	element(addToHistory, [history, data]
		set(result, map:get("result", data))
		
		updateHistMap(result, history, 
			list(
				map:get("host", data)
				map:get("suite", data)
				map:get("test", data)
				map:get("subtest", data)
				map:get("provider", data)
				map:get("time", data)
			)
		)
	)
	
	/*
	 * Used to return a command that tells
	 * history to create a chart.
	 */
	element(write, [host, suite, file]
		channel:to(historyChannel
			list("write", 
				map(
					map:entry("host", host)
					map:entry("suite", suite)
					map:entry("file", file)
				)
			)
		)
	)
	
	/*
	 * Used to return the results of a test, together
	 * with all the info about the test
	 */
	element(data, [time, host, result, suite, test, subtest, provider]
		channel:to(historyChannel
			list("data",
				map(
					map:entry("time", time)
					map:entry("host", host)
					map:entry("result", result)
					map:entry("suite", suite)
					map:entry("test", test)
					map:entry("subtest", subtest)
					map:entry("provider", provider)
				)
			)
		)
	)
	
	
	/*
	 * Creates a chart with data from the tree for the
	 * indicated host and suite, and writes it to a file
	 */
	element(writeHistory, [history, suite, host, file]
		if(
			map:contains(host, history)
			then(
				set(suites, map:get(host, history))
				if(
					map:contains(suite, suites)
					then(
						set(tests, map:get(suite, suites))

						SaveChartAsPNG(file,
							chart("{host} {suite} history",
								CombinedDomainXYPlot(domainAxis = DateAxis("Time")
									for(test, tests,
										set(subtests, map:get(test, tests))
						
										for(subtest, subtests,
											set(providers, map:get(subtest, subtests))
							
											for(provider, providers,
												set(dates, map:get(provider, providers))
								
												StandardXYPlot(
													valueAxis = SymbolicAxis("", ["Failed", "Timeout", "Passed"])
													shapes = true
													filled = true
													XYSeries("{test} {subtest} {provider}"
														for(date, dates
															XYData(date, map:get(date, dates))
														)
													)
												)
											)
										)
									)
								)
							)
						)
					)
				)
			)
		)
	)
  )
)
