type messagefile;

(messagefile t) greeting() { 
    app {
        echo "hello" stdout=@filename(t);
    }
}

messagefile outfile <"001-echo.out">;

boolean b = 1==1;

if(b) {
    outfile = greeting();
}

