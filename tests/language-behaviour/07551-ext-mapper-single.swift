type messagefile;

(messagefile t) write(string s) { 
    app {
        echo s stdout=@filename(t);
    }
}

messagefile outfile <ext; exec="./07551-ext-mapper-single.sh">;

outfile = write("slot 0");

