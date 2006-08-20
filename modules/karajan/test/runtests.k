import("sys.k")
import("task.k")
import("html.k")
import("settings.k")

/* define a few convenience comparisons: */

default_comparison := element([value1, value2] equals(value1, value2))


math_comparison := element([value1, value2] equalsNumeric(value1, value2))


no_comparison := element([value1, value2], true())


echo("Starting loop")

for(i, range(1, global_pass_count)
	print("Running pass {i}")
	
	tests := filter(regexp = ".*\.k", file:list(dir="tests"))
	print("Tests found: {tests}")
	
	for(testfile, tests
		
		test := executeFile("tests/{testfile}")
		name := map:get(key = "name", test)
		
		print("Executing test [{name}]...")
		
		for(pass, range(1, test_pass_count)
			print("Pass {pass}...", nl=false())
			if(map:contains(key = "expected-result", test)
			
				then(
					result := executeElement(map:get(key="test", test))
					expected := map:get(key="expected-result", test)
					comparison :=
						if(map:contains(key="comparison", test)
						
							then(
								map:get(key="comparison", test)
							)
							else(
								default_comparison
							)
						)

					if(executeElement(comparison, expected, result)
					
						then(
							print("PASSED")
						)
						else(
							print("FAILED")
							print("Expected result: {expected}. Actual result: {result}.")
							generateError("FAILED [{name}]")
						)
					)
				)
				else(//no expected result
					executeElement(map:get(key = "test", test))
					print("PASSED")
				)
			)
		)
	)		
	print("Pass {i} OK")
)
