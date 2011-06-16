type messagefile {}

(messagefile t) greeting(string m) { 
    app {
        echo m stdout=@filename(t);
    }
}

messagefile outfile <"003-strcat.out">;

string v = @strcat("abc","qux");

outfile = greeting(v);


