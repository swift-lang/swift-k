type messagefile {}

app (messagefile t) greeting(string m) { 
        echo m stdout=@filename(t);
}

messagefile outfile <"0052-regexp.out">;

string v =  @regexp("abcdefghi", "c(def)g","monkey");

outfile = greeting(v);

