/*
	Linear tail recursion.
	Expect linear growth only, so usual loop numbers match
	as for earlier scripts.

	stats: 1K   -> 25s real (i5 8gb)
	       10K  -> (10% ram 8gb in use by java)
Uncaught exception: java.lang.StackOverflowError in vdl:unitstart @ x_recursion.kml, line: 45
java.lang.StackOverflowError
	at java.lang.String.valueOf(String.java:2959)
	at org.globus.cog.karajan.util.ThreadingContext.toString(ThreadingContext.java:87)
	at org.globus.cog.karajan.util.ThreadingContext.toString(ThreadingContext.java:87)
	...
Exception is: java.lang.StackOverflowError
Near Karajan line: vdl:unitstart @ x_recursion.kml, line: 45
Another uncaught exception while handling an uncaught exception.
java.lang.StackOverflowError
	at org.globus.cog.karajan.workflow.nodes.FlowNode.failImmediately(FlowNode.java:77)
	at org.globus.cog.karajan.workflow.nodes.FlowNode.failed(FlowNode.java:245)
	...
The initial exception was
java.lang.StackOverflowError
	at java.lang.String.valueOf(String.java:2959)
	at org.globus.cog.karajan.util.ThreadingContext.toString(ThreadingContext.java:87)
	at org.globus.cog.karajan.util.ThreadingContext.toString(ThreadingContext.java:87)

	       100K -> ?

#NIGHTLY 1000 10000
#WEEKLY  1000 10000 100000
*/

int limit = @toint(@arg("loops"));

(int out) sum (int n){
     if ( n == 0 ){
     	out = 0;
     }else{
          out = n + sum( n-1 );
     }
}

int result = sum(limit);

tracef("Sum(%i)  = %i \n", limit, result);