type messagefile;

(messagefile t) write(string s) { 
    app {
        echo s stdout=@filename(t);
    }
}

messagefile infile[] <filesys_mapper; prefix="0753-filesystem-mapper",
                                      suffix=".in">;

messagefile outfile[] <fixed_array_mapper;
                       files="0753-filesystem-mapper.a.out 0753-filesystem-mapper.b.out 0753-filesystem-mapper.c.out"
                      >;

outfile[0] = write(@filename(infile[0]));
outfile[1] = write(@filename(infile[1]));
outfile[2] = write(@filename(infile[2]));

