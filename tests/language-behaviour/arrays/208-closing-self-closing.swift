int[] a;
a[3] = 1;
int[][] b;

foreach v, k in a {
	if (k < 100) {
		a[k + 10] = 1;
	}
	trace(k);
}

foreach i in [0:2] {
	a[i] = 1;
	b[0][i] = 2;
}
