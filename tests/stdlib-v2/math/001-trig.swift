import "stdlib.v2";

float sinPi = sin(PI);
float cosPi = cos(PI);

assertAlmostEqual(sinPi, 0.0, 1e-15);
assertAlmostEqual(cosPi, -1.0, 1e-15);

float a = 1.2;
float sinA = sin(a);
float cosA = cos(a);
float tanA = tan(a);

assertAlmostEqual(sinA * sinA + cosA * cosA, 1.0, 1e-15);
assertAlmostEqual(sinA / cosA, tanA, 1e-15);

assertAlmostEqual(asin(sinA), a, 1e-15);
assertAlmostEqual(acos(cosA), a, 1e-15);
assertAlmostEqual(atan(tanA), a, 1e-15);

float x = 10;
float y = -3;

float r = sqrt(x * x + y * y);
float a2 = atan2(y, x);

assertAlmostEqual(r * cos(a2), x, 1e-15);
assertAlmostEqual(r * sin(a2), y, 1e-15);
