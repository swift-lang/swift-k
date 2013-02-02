import(sys)
import(file = "../testlib.k")

map(
	map:entry("name", "definition scoping")
	map:entry("test",
		lambda() {
			a := function() {
				b := function() {
					"b"
				}
				lambda() {
					//this is the b() above
					b()
				}
			}
			b := function() {
				"!b"
			}
			//define as c what a() returns which
			//is the lambda that calls the b() defined
			//inside a()
			c := a()
			apply(c)
		}
	)
	map:entry("expected-result", "b")
)