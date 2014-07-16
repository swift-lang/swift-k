type messagefile;

(messagefile t) greeting() { 
    app {
        helper "hello" stdout=@filename(t);
    }
}

messagefile outfile <"001-echo.out">;

outfile = greeting();

