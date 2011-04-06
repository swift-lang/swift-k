int arr[];

foreach a,i in [0:9] {
 arr[i] = i;
}

trace("arr",arr);

foreach a,i in arr {
 trace("for", a,i);
}
