int array1[int][string][int][string];

array1[0]["a"][0]["a"] = 1;
array1[0]["a"][0]["b"] = 1;
array1[0]["a"][1]["a"] = 1;
array1[0]["a"][1]["b"] = 1;

array1[0]["b"][0]["a"] = 1;
array1[0]["b"][0]["b"] = 1;
array1[0]["b"][1]["a"] = 1;
array1[0]["b"][1]["b"] = 1;

array1[1]["a"][0]["a"] = 1;
array1[1]["a"][0]["b"] = 1;
array1[1]["a"][1]["a"] = 1;
array1[1]["a"][1]["b"] = 1;

array1[1]["b"][0]["a"] = 1;
array1[1]["b"][0]["b"] = 1;
array1[1]["b"][1]["a"] = 1;
array1[1]["b"][1]["b"] = 1;

foreach v0,i0 in array1{
	trace(v0,i0);
}

foreach v1,i1 in array1[0]{
	trace(v1,i1);
}

foreach v2,i2 in array1[0]["a"]{
	trace(v2,i2);
}

foreach v3,i3 in array1[0]["a"][1]{
	trace(v3,i3);
}

// Following loop fails, "in" is not an array 
//foreach v4,i4 in array1[0]["a"][1]["a"]{
//	trace(v4,i4);
//}


