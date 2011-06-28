type messagefile {} 

app (messagefile t) greeting (string s) {   
        echo s stdout=@filename(t);
}

messagefile outfile <"hello2.txt">;

outfile = greeting("hello world");

