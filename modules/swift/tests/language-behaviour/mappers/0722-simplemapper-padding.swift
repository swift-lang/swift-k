type messagefile;

(messagefile t) write() { 
    app {
        echo @filename(t) stdout=@filename(t);
    }
}

messagefile outfile[] <simple_mapper;
                     prefix="0722-simplemapper-padding.",
                     suffix=".out",
                     padding="9">;

outfile[0] = write();
outfile[5] = write();
outfile[75943] = write();

