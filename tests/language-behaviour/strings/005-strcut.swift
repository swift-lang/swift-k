type messagefile {}

app (messagefile t) greeting(string m) { 
        echo m stdout=@filename(t);
}

messagefile outfile <"005-strcut.out">;

string v =  @strcut("abcdefghi", "abc(def)ghi");

outfile = greeting(v);
