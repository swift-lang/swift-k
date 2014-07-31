import(sys)
import(file = "../testlib.k")

map(
	map:entry("name", "sum")
	map:entry("test",
		lambda() {
			sum(1, 2, 3)
		}
	)
	map:entry("expected-result", 6)
)
