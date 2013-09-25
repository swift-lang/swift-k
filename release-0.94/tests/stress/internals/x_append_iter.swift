/*
	Iterate loops with variable number of loops
	to be called with cmdline arg -loops=<Number>

	todo : Fails to compile on 0.94 ? check
	       Compiles on 0.93 (Regression noted)
	       perhaps this isn't a very scalable test :(
	stats: 1K   -> 2.458s real
	       10K  -> Java heap space ? exec failed
	       100K -> ?
	       1M   -> java.langOutOfMemoryError thrown
	       10M  -> ?
#NIGHTLY 1000
#WEEKLY  1000 10000
*/

int limit   = @toint(@arg("loops"));
string result[];
result[0] = "banana";

iterate current {
	result[current+1] = @strcat(result[current], "banana");
	//	tracef("result[current] = %s \n", result[current]);
} until ( current > limit );

tracef("Result[0]    = %s \n", result[0]);
tracef("Result[%i]   = %s \n", limit, result[limit]);