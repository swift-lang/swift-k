#NIGHTLY 1000 10000
#WEEKLY       10000 100000 1000000
define(S3, 100000)
define(S2, 10000)
define(S1, 1000)
-loops=$(esyscmd(`printf $STRESS') )

