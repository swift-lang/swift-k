type messagefile;

app (messagefile t) greeting() { 
        echo "hello" stdout=@filename(t);
}

messagefile outfile <"141-space-in-filename.space here.out">;

outfile = greeting();

