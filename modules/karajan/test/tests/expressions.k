import(sys)
import(file = "../testlib.k")

map(
	map:entry("name", "expressions")
	map:entry("test",
		lambda() {
			f := function(x) {
				x * x
			}
			list(
				1+2
				1+2*3
				2*3+1
				(1+2)*3
				3+2-1
				6/2-1
				3*2 > 3+2
				(2*3)*4
				3*f(3)
				true & false
				true & true
				false | true
				false | false
				3*
				2
				
				//precedence
				(false | 2*3 > 1 & 8+5*3 >= 24) == false
				//?
				3*(10+1),
				
				(3+1)*((3+1)*(4+1) + 1)
			)
		}
	)
	map:entry("expected-result", [3, 7, 7, 9, 4, 2, true, 24, 27, false, true, true, false, 6, true, 33, 84])
)
