type messagefile;

(messagefile e, messagefile t) greeting(messagefile i) {
    app {
        echo "hello" stdin=@filename(i) stderr=@filename(e) stdout=@filename(t);
    }
}

messagefile infile <"test.in">;
messagefile outfile <"test.out">;
messagefile errfile <"test.err">;

(errfile, outfile) = greeting(infile);

