type messagefile;

app (messagefile t) greeting (string s) {   
    echo s stdout=@filename(t);
}

app (messagefile o) capitalise(messagefile i) {   
    tr "[a-z]" "[A-Z]" stdin=@filename(i) stdout=@filename(o);
}

messagefile hellofile <"capitalise.1.txt">;
messagefile final <"capitalise.2.txt">;
hellofile = greeting("hello from Swift");
final = capitalise(hellofile);
