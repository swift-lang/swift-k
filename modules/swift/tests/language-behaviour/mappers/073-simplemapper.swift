// this one is like test 072-simplemapper, but checks that
// a dot is added to the front of the suffix if its missing

type messagefile;

(messagefile t) write() { 
    app {
        echo @filename(t) stdout=@filename(t);
    }
}

messagefile outfile <simple_mapper;
                     prefix="073-simplemapper",
                     suffix=".out">;

outfile = write();

