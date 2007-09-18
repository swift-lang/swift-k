type messagefile {}

(messagefile t) greeting(string m) { 
    app {
        echo m stdout=@filename(t);
    }
}

messagefile outfile <"0051-regexp.out">;

string v =  @regexp("abcdefghi", "abc(def)ghi","monkey");

outfile = greeting(v);

