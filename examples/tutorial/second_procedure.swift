type messagefile {} 

(messagefile t) greeting (string s) {   
    app {
        echo s stdout=@filename(t);
    }
}

(messagefile o) capitalise(messagefile i) {   
    app {
        tr "[a-z]" "[A-Z]" stdin=@filename(i) stdout=@filename(o);
    }
}

messagefile hellofile <"hello.txt">;
messagefile final <"capitals.txt">;

hellofile = greeting("hello from Swift");
final = capitalise(hellofile);

