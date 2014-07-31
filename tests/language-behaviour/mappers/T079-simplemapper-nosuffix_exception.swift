// THIS-SCRIPT-SHOULD-FAIL

// this one is like test T078-simplemapper-nosuffix, but demonstrates the 
// mapping exception on invalid values
type messagefile;

(messagefile t) write() { 
    app {
        echo @filename(t) stdout=@filename(t);
    }
}

messagefile outfile <simple_mapper;
                     prefix="T078-simplemapper-nosuffix", noauto="exception",
                     suffix="_nodot.out">;

outfile = write();

