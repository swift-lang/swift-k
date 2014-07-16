type messagefile {}

app (messagefile t) greeting(string m, int i) { 
        echo i stdout=@filename(t);
}

messagefile outfile <"055-pass-int.out">;

int luftballons;

luftballons = 99;

outfile = greeting("hi", luftballons);

