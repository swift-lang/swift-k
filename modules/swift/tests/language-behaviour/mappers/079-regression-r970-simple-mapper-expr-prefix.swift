type messagefile;

(messagefile t) write() { 
    app {
        echo @filename(t) stdout=@filename(t);
    }
}

string fn = "079-regression-r970-simple-mapper-expr-prefix";

messagefile outfile <simple_mapper;prefix=fn,suffix=".out">;

outfile = write();

