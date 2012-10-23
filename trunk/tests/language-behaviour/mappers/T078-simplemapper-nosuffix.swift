// this one is like test 073-simplemapper, but doesn't 
// add the missing dot in front of the suffix

type messagefile;

(messagefile t) write() { 
    app {
        echo @filename(t) stdout=@filename(t);
    }
}

messagefile outfile <simple_mapper;
                     prefix="T078-simplemapper-nosuffix", noauto="true",
                     suffix="_nodot.out">;

outfile = write();

