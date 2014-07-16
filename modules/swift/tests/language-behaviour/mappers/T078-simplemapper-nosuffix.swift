// this one is like test 073-simplemapper, but doesn't 
// add the missing dot in front of the suffix

// The behavior of the suffix parameter was changed in r6390.
// The automatic adding of a dot to the suffix could be surprising
// and it did not compensate by helping the user much. Since r6390
// the user must specify the dot if it's to be part of the suffix.
// and also removes the noauto parametr

type messagefile;

(messagefile t) write() { 
    app {
        echo @filename(t) stdout=@filename(t);
    }
}

messagefile outfile <simple_mapper;
                     prefix="T078-simplemapper-nosuffix",
                     suffix="_nodot.out">;

outfile = write();

