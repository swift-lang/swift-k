import(sys)
import(file = "../testlib.k")

map(
	map:entry("name", "large list")
	map:entry("test",
		lambda() {
			l := list(
				for(i, range(1, 1000000)) {
					i
				}
			)
			1
		}
	)
	map:entry("expected-result", 0)
	map:entry("comparison", noComparison)
)