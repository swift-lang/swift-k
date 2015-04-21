import "stdlib.v2";

float exp1 = exp(1.0);
assertAlmostEqual(exp1, E, 1e-15);

float p = 1.7;
float expP = exp(p);
assertEqual(ln(expP), p);
assertEqual(log(expP, E), p);


assertEqual(log10(1000.0), 3.0);

assertAlmostEqual(pow(E, p), expP, 1e-15);

assertEqual(pow(2.0, 10.0), 1024.0);

assertEqual(sqrt(p), pow(p, 0.5));

assertEqual(cbrt(p), pow(p, 1.0 / 3.0));

