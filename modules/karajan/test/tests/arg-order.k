import(sys)
import(file = "../testlib.k")

map(
	map:entry("name", "arguments order")
	map:entry("test",
		lambda() {
			list(
				parallel(
					(wait(delay = 20), 1)
					(wait(delay = 30), 2)
					(wait(delay = 5),  3)
					
					4
				)
			)
		}
	)
	map:entry("expected-result", [1, 2, 3, 4])
	map:entry("comparison", mathComparison)
)