/*
	Foreach loops with variable number of loops
	to be called with cmdline arg -loops=<Number>
	todo: measure actual times with expected times.

	This is probably better for a larger system.
	Could take forever to execute on i5s for even modest scales.
	stats: 1K -> real  41m55.779s

#NIGHTLY 1000 10000
#WEEKLY       10000 100000 1000000
*/

int limit   = @toint(@arg("loops"));
int result[];

app sleep (int seconds){
    sleep seconds;
}

foreach num in [0:limit:1] {
	sleep(num%%5);
}