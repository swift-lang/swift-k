import "stdlib.v2";

float[] a = [1.0, 2.0, 3.0, 5.0, 7.0, 8.0, 9.0];
assertEqual(sum(a), 35.0);
assertEqual(avg(a), 5.0);
assertEqual(moment(a, 1, 0.0), avg(a));
assertAlmostEqual(moment(a, 2, avg(a)), 8.285714285714286, 1e-10);

int[] b = [1, 2, 3, 5, 7, 8, 9];
assertEqual(sum(b), 35);
assertEqual(avg(b), 5.0);
assertEqual(moment(b, 1, 0.0), avg(b));
assertAlmostEqual(moment(b, 2, avg(b)), 8.285714285714286, 1e-10);
