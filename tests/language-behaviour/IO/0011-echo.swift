type messagefile;

app (messagefile t) greeting() { 
        echo "hello" stdout=@filename(t);
}

messagefile outfile <"0011-echo.out">;

outfile = greeting();

