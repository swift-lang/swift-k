import(sys)
import(file = "../testlib.k")

map(
	map:entry("name", "factorial")
	map:entry("test",
		lambda() {
			factorial := function(n) {
				if (n <= 1) {
					1
				}
				else {
					n * factorial(n-1)
				}
			}
			
			factorial(10)
		}
	)
	map:entry("expected-result", 3628800)
	map:entry("comparison", mathComparison)
)