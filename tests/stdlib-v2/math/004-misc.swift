import "stdlib.v2";

assertEqual(min(1, 2), 1);
assertEqual(min(1.0, 2.0), 1.0);

assertEqual(max(1, 2), 2);
assertEqual(max(1.0, 2.0), 2.0);

assertEqual(abs(-5), 5);
assertEqual(abs(-1.7), 1.7);

float nan = 0.0 / 0.0;
assert(isNaN(nan));

