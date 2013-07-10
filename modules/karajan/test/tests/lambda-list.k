import(sys)
import(file = "../testlib.k")

map(
	map:entry("name", "lambda list")
	map:entry("test",
		lambda() {
			list2 := [lambda() { "1" }, lambda() { "2" }]
			
			[for(item, list2, apply(item))]
		}
	)
	map:entry("expected-result", ["1", "2"])
	map:entry("comparison", mathComparison)
)