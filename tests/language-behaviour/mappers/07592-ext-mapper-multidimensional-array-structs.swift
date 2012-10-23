
type F;

type S {
 F a;
 F b;
 F c[];
}

app (S output[][]) touch() {
touch @filenames(output);
}

S o[][] <ext; exec="07592-ext-mapper-multidimensional-array-structs.sh.in">;

o = touch();


