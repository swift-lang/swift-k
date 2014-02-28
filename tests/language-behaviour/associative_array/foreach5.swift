type mystruct{
    int a;
    int b;
}

int array[int][mystruct];

mystruct xx,yy;
xx.a = 1;
xx.b = 2;

yy.a = 101;
yy.b = 202;

array[1][xx] = 55;
array[1][yy] = 11;

foreach v in array[1]{
	trace(v);
}