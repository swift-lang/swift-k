type messagefile {} 

(messagefile t) greeting (string s) {   
    app {
        echo s stdout=@filename(t);
    }
}

messagefile outfile <"hello2.txt">;

outfile = greeting("hello world");

