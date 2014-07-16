type messagefile;

(messagefile t) greeting() { 
    app {
        echo "hello" stdout=@filename(t);
    }
}

messagefile infile <"077-regexpmapper-input.in">;

messagefile outfile <regexp_mapper; source="infile",
       match="(.*)in", transform=@strcat("077-regexp-mapper.foo.","\\1out")>;

outfile = greeting();

