type messagefile;

(messagefile t) greeting() { 
    app {
        echo "'" stdout=@filename(t);
    }
}

messagefile outfile <"1033-singlequote.out">;

outfile = greeting();

