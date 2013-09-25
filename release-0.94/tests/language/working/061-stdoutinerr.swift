type messagefile;

(messagefile e, messagefile t) greeting(messagefile i) {
    app {
        echo "hello" stdout=@filename(t) stdin=@filename(i) stderr=@filename(e);
    }
}

messagefile infile <"test.in">;
messagefile outfile <"test.out">;
messagefile errfile <"test.err">;

(errfile, outfile) = greeting(infile);

