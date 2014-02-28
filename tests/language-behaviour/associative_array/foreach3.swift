int array[int][string][auto];

array[1]["hey"] << 5;
array[1]["hey"] << 10;
array[1]["hey"] << 20;


foreach v,i in array[1]["hey"]{
	trace( array[1]["hey"][i], i);
}
