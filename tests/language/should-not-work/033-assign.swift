
// THIS-SCRIPT-SHOULD-FAIL
// ... because a is uninitialized (and so is b)

int a,b;
int i=(a + b) * 5;
