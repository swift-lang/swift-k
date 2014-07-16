import(sys)
import(file = "../testlib.k")

map(
	map:entry("name", "while loop")
	map:entry("test",
		lambda() {
			testWhile := function() {
				list(
					while (i, 0) {
						i
						if (i < 7) {
							next(i + 1)
						}
					}
				)
			}
									
			list(
				testWhile()
			)
		}
	)
	map:entry("expected-result",
		[
			[0, 1, 2, 3, 4, 5, 6, 7]
		]
	)
)