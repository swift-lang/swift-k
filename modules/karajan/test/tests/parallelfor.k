import(sys)
import(file = "../testlib.k")

map(
	map:entry("name", "parallel for")
	map:entry("test",
		lambda() {
			list(
				sum(parallelFor(i, range(0, 6), 1))
				sum(parallelFor(i, range(0, 7), (1, 1)))
				sum(parallelFor(i, range(0, 8)) {
					1, 1, 1
				})
				r := range(0, 9)
				sum(parallelFor(i, r) {1, 1, 1, 1})
				r := range(1, 10)
				sum(parallelFor(i, r, i))
				
				try {
					parallelFor(i, range(0, 10)) {
						if (i == 5) {
							throw("Kaboom")
						}
					}
					"No failure"
				}
				else {
					"Yes failure"
				}
			)
		}
	)
	map:entry("expected-result", list(7, 16, 27, 40, 55, "Yes failure"))
)
