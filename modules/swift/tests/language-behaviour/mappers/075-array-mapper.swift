type messagefile;

(messagefile t) write() { 
    app {
        echo @filename(t) stdout=@filename(t);
    }
}

string fns[]=["075-array-mapper.first.out",
              "075-array-mapper.second.out",
              "075-array-mapper.third.out"];

messagefile outfile[] <array_mapper; files=fns>;

outfile[0] = write();
outfile[1] = write();
outfile[2] = write();

