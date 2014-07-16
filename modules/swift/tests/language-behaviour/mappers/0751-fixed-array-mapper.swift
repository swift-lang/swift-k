type messagefile;

(messagefile t) write(string s) { 
    app {
        echo s stdout=@filename(t);
    }
}

string fns="0751-fixed-array-mapper.first.out 0751-fixed-array-mapper.second.out 0751-fixed-array-mapper.third.out";

messagefile outfile[] <fixed_array_mapper; files=fns>;

outfile[0] = write("slot 0");
outfile[1] = write("slot 1");
outfile[2] = write("slot 2");

