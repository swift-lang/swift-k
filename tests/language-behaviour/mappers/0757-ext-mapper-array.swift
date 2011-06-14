type messagefile;

(messagefile t) write(string s) { 
    app {
        echo s stdout=@filename(t);
    }
}

messagefile outfile[] <ext; exec="./0757-ext-mapper-array.sh">;

outfile[0] = write("slot 0");
outfile[1] = write("slot 1");
outfile[2] = write("slot 2");

