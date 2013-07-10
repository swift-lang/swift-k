import(sys)
import(file = "../testlib.k")

map(
	map:entry("name", "another stress test")
	map:entry("test",
		lambda() {
			bla := function(a, b, c, d) {
				if (a == list(b, c, d)) {
					1
				}
				else {
					2
				}
			}
			
			sum(
				for(i, range(1, 20)) {
					parallelFor(j, range(1, 250)) {
						bla(
							parallel(
								list(1, 2, 3)
								1, 2, 3
							)
						)
					}
					echo(".", nl=false)
				}
			)
		}
	)
	map:entry("expected-result", 5000)
)
