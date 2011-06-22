type messagefile;

app (messagefile t) greeting() { 
        echo "hello"
          "world" stdout=@filename(t);
}

messagefile outfile <"143-newlines.out">;

outfile = greeting();

