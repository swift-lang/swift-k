type messagefile {}

(messagefile t) greeting(string m, int i) { 
    app {
        echo i stdout=@filename(t);
    }
}

messagefile outfile <"054-pass-int.out">;

int luftballons = 99;

outfile = greeting("hi", luftballons);

