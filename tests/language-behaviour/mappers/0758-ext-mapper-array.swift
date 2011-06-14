type messagefile;

(messagefile t) write(string s) { 
    app {
        echo s stdout=@filename(t);
    }
}

messagefile outfile[] <ext; exec="./0758-ext-mapper-array.sh">;

foreach f,i in [0:2] {
 outfile[i] = write("slot X");
}
