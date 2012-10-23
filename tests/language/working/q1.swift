type messagefile {}

(messagefile t) greeting() { 
    app {
        echo "Hello, world!" stdout=@filename(t);
    }
}

messagefile outfile <"hello.txt">;

outfile = greeting();

