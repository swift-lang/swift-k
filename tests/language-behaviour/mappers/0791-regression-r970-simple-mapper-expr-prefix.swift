type messagefile;

(messagefile t) write() { 
    app {
        echo @filename(t) stdout=@filename(t);
    }
}

string a = "0791-regression-r970";
string b = "-simple-mapper-";
string c = "expr-prefix";

messagefile outfile <simple_mapper;prefix=@strcat(a,b,c),suffix=".out">;

outfile = write();

