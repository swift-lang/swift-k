type messagefile;

(messagefile t) greeting() { 
    app {
        echo "hello"
          "world" stdout=@filename(t);
    }
}

messagefile outfile <"143-newlines.out">;

outfile = greeting();

