// THIS-SCRIPT-SHOULD-FAIL
// f(1) would be ambiguous

(int r) f(int x, string a = 1) {
	r = x + 1;
}

(int r) f(int x, boolean b = false) {
	r = x + 2;
}

