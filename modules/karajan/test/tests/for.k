import(sys)
import(file = "../testlib.k")

map(
	map:entry("name", "for")
	map:entry("test",
		lambda() {
			list(
				sum(for(i, range(0, 6), 1))
				sum(for(i, range(0, 7), (1, 1)))
				sum(for(i, range(0, 8)) {1, 1, 1})
				r := range(0, 9)
				sum(for(i, r) {
					1, 1, 1, 1
				})
				r := range(1, 10)
				sum(for(i, r, i))
			)
		}
	)
	map:entry("expected-result", list(7, 16, 27, 40, 55))
)
