/*
 * Convenience library for dealing with tests.
 * Tests are read from files named <suite>-<name>.k
 * Each test file returns a map with the following keys:
 * name, providers, subtests, testlist.
 * The name is the name. Providers is a list with the 
 * providers to be used. Subtest is a string list of subtest
 * names. Testlist is a list of elements to be executed for
 * each subtest. For each subtest name, there is an element
 * in the testlist.
 */

import("sys.k")
import("task.k")
 
namespace("test"
	/*
	 * A convenience element that returns a map constructed
	 * from the values of the arguments
	 */	
	element(test,  [name, providers, subtests, testlist]
		set(subtestlist,
			if(
				subtests == []
				then(list("-"))
				else(subtests)
			)
		)
	
		map(
			map:entry("name", name)
			map:entry("providers", providers)
			map:entry("subtests", subtestlist)
			map:entry("testlist", testlist)
		)
	)

	element(read, [test, when]
		cache(on = list(test, when)
			echo("Reading test file {test}")
			executeFile(test)
		)
	)

	element(prop, [test, name, when]
		map:get(name, read(test, when))
	)
	export(
	
		element(name, [test, when], prop(test, "name", when))
	
		element(testlist, [test, when], prop(test, "testlist", when))
	
		element(providers, [test, when], prop(test, "providers", when))
	
		element(subtests, [test, when], prop(test, "subtests", when))

		element(providerItemCount, [test, when]
			max(1, list:size(subtests(test, when)))
		)
	
		element(itemCount, [test, when]
			max(1, list:size(providers(test, when))) *
				providerItemCount(test, when)
		)
		
		element(all, [suitename, when]
			list(
				for(test, sort(filter(regexp = ".*\.k", file:list("{suitename}"))), "{suitename}/{test}")
			)
		)

	
		element(printAll, [suite, when]
			set(tests, all(suite, when))
		
			print(concat("Tests found: ", tests))
		)

		/*
		 * A convenience element that returns a map constructed
		 * from the values of the arguments
		 */	
		element(test,  [name, providers, subtests, testlist]
			set(subtestlist,
				if(
					subtests == []
					then(list("-"))
					else(subtests)
				)
			)
		
			map(
				map:entry("name", name)
				map:entry("providers", providers)
				map:entry("subtests", subtestlist)
				map:entry("testlist", testlist)
			)
		)
	)
)