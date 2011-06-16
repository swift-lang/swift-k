type messagefile;

(messagefile t) greeting() { 
    app {
        echo "\"" stdout=@filename(t);
    }
}

messagefile outfile <"103-quote.out">;

outfile = greeting();

