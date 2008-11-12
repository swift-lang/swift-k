type messagefile;

app (messagefile t) greeting() { 
    echo "Hello, world!" stdout=@filename(t);
}

messagefile outfile <"hello.txt">;

outfile = greeting();

