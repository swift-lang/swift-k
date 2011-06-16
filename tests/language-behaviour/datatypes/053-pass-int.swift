type messagefile {}

(messagefile t) greeting(string m, int i) { 
    app {
        echo i stdout=@filename(t);
    }
}

messagefile outfile <"053-pass-int.out">;

outfile = greeting("hi", 7);

