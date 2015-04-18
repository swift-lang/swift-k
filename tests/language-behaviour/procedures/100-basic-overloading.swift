import "stdlib.v2";

(int r) f(int x) {
	r = x + 1;
}

(float r) f(float x) {
	r = x + 2;
}

assertEqual(f(1), 2);
assertEqual(f(1.0), 3.0);