type messagefile;

app (messagefile t) greeting (string s) {   
    echo s stdout=@filename(t);
}

messagefile outfile <"parameter.hello.txt">;
outfile = greeting("hello world");
