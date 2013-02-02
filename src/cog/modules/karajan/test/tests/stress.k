import(sys)
import(file = "../testlib.k")

map(
	map:entry("name", "small stress test")
	map:entry("test",
		lambda() {
			(A, B) := parallel(1, 2)
			
			sum(
				parallel(
					for(i, range(1, 10)) {
						parallelFor(j, range(1, 30)) {
							for(k, range(1, 50)) {
								dummy := "{i}{j}{k}"
								1
							}
						}
						echo(A, nl = false)
					}
					
					for(i, range(1, 10)) {
						for(j, range(1, 30)) {
							parallelFor(k, range(1, 50)) {
								dummy := "{i}{j}{k}"
								1
							}
						}
						echo(B, nl = false)
					}
				)
			)
			echo(" ", nl = false)
		}
	)
	map:entry("expected-result", 2 * 10 * 30 * 50)
)
