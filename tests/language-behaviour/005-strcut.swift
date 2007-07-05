type messagefile {}

(messagefile t) greeting(string m) { 
    app {
        echo m stdout=@filename(t);
    }
}

messagefile outfile <"005-strcut.out">;

string v =  @strcut("abcdefghi", "abc(def)ghi");

outfile = greeting(v);

