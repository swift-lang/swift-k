type messagefile;

(messagefile t) greeting() { 
    app {
        echo "hello" stdout=@filename(t);
    }
}

messagefile outfile <"141-space-in-filename.space here.out">;

outfile = greeting();

