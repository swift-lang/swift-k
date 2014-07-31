/*
	Test reliability of function calls over large scale

	tested on : 0.94
	todo : Redo with floats as numbers either become too big
	     Floats give infinity for fib[10K]
	stats: 10K  -> ?
	       100K -> 
	       1M   -> Exception in thread "Hang checker"
	       java.lang.StackOverflowError
	       at java.util.HashMap.put(HashMap.java:484)
	       at java.util.HashSet.add(HashSet.java:217)

#NIGHTLY 1000 10000 100000
#WEEKLY  1000 10000 100000 1000000

 */

int limit   = @toInt(@arg("loops"));
int range[] = [2:limit:1];
float array[];

(float out) sum (float a, float b){
     out = a + b ;
}

array[0] = 0.0;
array[1] = 1.0;

foreach num in range {
	array[num] = sum(array[num-1], array[num-2]);
}

tracef("Fibonacci[2]   = %f \n", array[2]);
tracef("Fibinacci[%i]  = %f \n", limit, array[limit]);

