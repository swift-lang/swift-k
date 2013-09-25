type messagefile;

app (messagefile t) greeting() { 
        echo "hello" stdout=@filename(t);
}
messagefile outfile <"001-echo.out">;
outfile = greeting();

