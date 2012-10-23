type messagefile {}

app (messagefile t) greeting(string m, int i) { 
        echo i stdout=@filename(t);
}

messagefile outfile <"054-pass-int.out">;

int luftballons = 99;

outfile = greeting("hi", luftballons);

