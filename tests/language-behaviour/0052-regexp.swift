type messagefile {}

(messagefile t) greeting(string m) { 
    app {
        echo m stdout=@filename(t);
    }
}

messagefile outfile <"0052-regexp.out">;

string v =  @regexp("abcdefghi", "c(def)g","monkey");

outfile = greeting(v);

