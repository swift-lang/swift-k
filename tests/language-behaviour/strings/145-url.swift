type messagefile;

app (messagefile t) greeting() { 
        echo "hello" stdout=@filename(t);
}

messagefile outfile <"file://localhost/145-url.out">;

outfile = greeting();

