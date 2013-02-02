import(sys)
import(file = "../testlib.k")

map(
	map:entry("name", "parallel")
	map:entry("test",
		lambda() {
			list(
				parallel(
					1, 2, 1 + 2, 4
				)
			)
		}
	)
	map:entry("expected-result", [1, 2, 3, 4])
	map:entry("comparison", mathComparison)
)