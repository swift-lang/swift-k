int a[auto];

foreach v in [0:4] {
	a << v ;
}

foreach v,i in a {
	trace ( v, i, a[i]);
}