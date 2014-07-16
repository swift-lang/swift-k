type messagefile;

app (messagefile t) greeting() { 
        echo "hello" stdout=@filename(t);
}

messagefile outfile <"0012-echo-map.out"> = greeting();


