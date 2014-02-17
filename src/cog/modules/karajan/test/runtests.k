import(sys)
import(task)
import(settings)
import(testlib)

echo("Starting loop")

for(i, range(1, GLOBAL_PASS_COUNT)) {
	print("Running pass {i}")
	
	tests := filter(".*\\.k", file:list(dir="tests"))
	print("Tests found: {tests}")
	
	for(testfile, tests) {
		
		test := executeFile("tests/{testfile}")
		name := map:get(key = "name", test)
		
		print("Executing test [{name}]...")
		
		for(pass, range(1, TEST_PASS_COUNT)) {
			print("Pass {pass}...", nl=false)
			if (map:contains(key = "expected-result", test)) {
				result := apply(map:get(key="test", test))
				expected := map:get(key="expected-result", test)
				comparison :=
					if (map:contains(key="comparison", test)) {
						map:get(key="comparison", test)
					}
					else {
						defaultComparison
					}

				if (apply(comparison, expected, result)) {
					print("PASSED")
				}
				else {
					print("FAILED")
					print("Expected result: {expected}. Actual result: {result}.")
					throw("FAILED [{name}]")
				}
			}
			else {//no expected result
					apply(map:get(key = "test", test))
					print("PASSED")
			}
		}
	}
	print("Pass {i} OK")
}