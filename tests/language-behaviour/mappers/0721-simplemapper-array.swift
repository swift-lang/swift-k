type messagefile;

(messagefile t) write() { 
    app {
        echo @filename(t) stdout=@filename(t);
    }
}

messagefile outfile[] <simple_mapper;
                     prefix="0721-simplemapper-array.",
                     suffix=".out">;

outfile[0] = write();
outfile[5] = write();

