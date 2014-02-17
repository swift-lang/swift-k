import(sys)
import(file = "../testlib.k")

map(
	map:entry("name", "futures")
	map:entry("test",
		lambda() {
			gen := function() {
				for(i, range(0, 10), i)
			}
			
			con := function(values) {
				sum(
					for(i, values,  i)
				)
			}
			
			delayed := function() {
				wait(delay=25)
				"Bla"
			}
			
			values := futureChannel(gen())
			bla := future(delayed())
			
			a := future(wait(delay=10), 1)
			b := future(wait(delay=20), 2)
			
			list(
				max(a, b)
				con(values)
				bla
			)
		}
	)
	map:entry("expected-result", [2, 55, "Bla"])
)
