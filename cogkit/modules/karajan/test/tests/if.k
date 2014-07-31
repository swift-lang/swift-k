import(sys)
import(file = "../testlib.k")

map(
	map:entry("name", "if")
	map:entry("test",
		lambda() {
			ift := function(v) {
				if (v == 0) {
					"0"
				}
				else if (v == 1) {
					"1"
				}
				else if (v == 2) {
					"2"
				}
				else if (v == 3) {
					"3"
				}
				else {
					"?"
				}
			}
			list(
				ift(5), ift(0), ift(1), ift(2), ift(3), ift(4)
			)
		}
	)
	map:entry("expected-result", ["?", "0", "1", "2", "3", "?"])
)
