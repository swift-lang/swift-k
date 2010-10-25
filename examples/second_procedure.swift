type messagefile;

app (messagefile t) greeting (string s) {   
    echo s stdout=@filename(t);
}

app (messagefile o) capitalise(messagefile i) {   
    tr "[a-z]" "[A-Z]" stdin=@filename(i) stdout=@filename(o);
}

messagefile hellofile <"hello.txt">;
messagefile final <"capitals.txt">;

hellofile = greeting("hello from Swift");
final = capitalise(hellofile);

