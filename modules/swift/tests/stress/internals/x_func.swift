/*
	Test reliability of function calls over large scale

	tested on : 0.94
	using ints

	stats: 1K   -> 3.059s
	       10K  -> 5.047s (result is probably wrong)
	       100K -> 31.85s
	       1M   -> Exception in thread "Hang checker"
	       java.lang.StackOverflowError
	       at java.util.HashMap.put(HashMap.java:484)
	       at java.util.HashSet.add(HashSet.java:217)

#NIGHTLY 1000 10000 100000
#WEEKLY  1000 10000 100000 1000000
 */

int limit   = @toInt(@arg("loops"));
int range[] = [2:limit:1];
int array[];

(int out) sum (int a, int b){
     out = a + b ;
}

array[0] = 0;
array[1] = 1;

foreach num in range {
	array[num] = sum(array[num-1], array[num-2]);
}

tracef("Fibonacci[2]   = %i \n", array[2]);
tracef("Fibinacci[%i]  = %i \n", limit, array[limit]);

