
type F;

type S {
 F a;
 F b;
}

app (S output[][]) touch() {
touch @filenames(output);
}

S o[][] <ext; exec="07591-ext-mapper-multidimensional-array.sh.in">;

o = touch();


