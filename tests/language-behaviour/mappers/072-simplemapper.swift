type messagefile;

(messagefile t) write() { 
    app {
        echo @filename(t) stdout=@filename(t);
    }
}

messagefile outfile <simple_mapper;
                     prefix="072-simplemapper",
                     suffix=".out">;

outfile = write();

