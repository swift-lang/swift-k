type messagefile {} 
type countfile {} 

(messagefile t) greeting (string s) {   
    app {
        echo s stdout=@filename(t);
    }
}

(countfile t) countwords (messagefile f) {   
    app {
        wc "-w" @filename(f) stdout=@filename(t);
    }
}

messagefile outfile <"q13greeting.txt">;
countfile c <"count.txt">;

outfile = greeting("hello from Swift");
c = countwords(outfile);

