import "stdlib.v2";

// while unlikely, it is possible for this script to fail:
// there is no guarantee that the average of some limited
// number of random numbers is close "enough" to the mean
// of the distribution from which they are sampled
// 
// On the other hand, since the sequences are deterministic,
// and since I tried this already, it shouldn't fail.

int N = 1000;

// ints
int[] a;

foreach i in [1:N] {
	a[i] = randomInt(1234, i, -10, 10);
}

assertAlmostEqual(avg(a), 0.0, 1.0);

// floats
float[] b;

foreach i in [1:N] {
	b[i] = randomFloat(4321, i, -10.0, 10.0);
}

assertAlmostEqual(avg(b), 0.0, 1.0);

// gaussian
float[] c;

foreach i in [1:N] {
	c[i] = randomGaussian(2314, i) * 10;
}

assertAlmostEqual(avg(c), 0.0, 1.0);
