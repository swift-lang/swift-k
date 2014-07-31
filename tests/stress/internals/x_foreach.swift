/*
	Foreach loops with variable number of loops
	to be called with cmdline arg -loops=<Number>

#NIGHTLY 10000 100000
#WEEKLY  1000 10000 1000000
*/

int limit   = @toint(@arg("loops"));
int result[];

foreach num, index in [0:limit-1:1] {
	result[index] = index;
}

tracef("Result[0]   = %i \n", result[0]);
tracef("Result[%i]  = %i \n", limit, result[limit-1]);
