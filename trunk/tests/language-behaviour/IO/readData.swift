type file;

type circle {
	int x;
	int y;
	float r;
	string name;
}

app (file f) createInt() {
		echo "77777" stdout=@filename(f);
}

app (file f) createFloat() {
		echo "3.1400001" stdout=@filename(f);
}


app (file f) write(string data) {
		echo data stdout=@filename(f);
}

int i;
float fl;

int ia[];

string sa[];

circle ca[];

circle c;

//will be generated
file f <"readData.int.in">;

//input file. assumed on disk
file g <"readData.intArray.in">;

// will be generated
file h <"readData.float.in">;

f = createInt();
h = createFloat();

i = readData(f);
fl = readData(h);

file o1 <"readData.int.out">;
o1 = write(@strcat(i));

ia = readData(g);

file o2 <"readData.intArray.out">;
o2 = write(@strcat(ia[0], " ", ia[1], " ", ia[2], " ", ia[3]));

sa = readData("readData.stringArray.in");

file o3 <"readData.stringArray.out">;
o3 = write(@strcat(sa[0], " ", sa[1], " ", sa[2], " ", sa[3]));

c = readData("readData.circle.in");

file o4 <"readData.circle.out">;
o4 = write(@strcat(c.x, " ", c.y, " ", c.r, " ", c.name));

ca = readData("readData.circleArray.in");

file o5 <"readData.circleArray.out">;
o5 = write(@strcat(ca[0].x, " ", ca[1].y, " ", ca[0].r, " ", ca[1].name));

file o6 <"readData.float.out">;
o6 = write(@strcat(fl));

