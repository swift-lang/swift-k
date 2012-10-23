type messagefile;

(messagefile t) write(messagefile s[]) { 
    app {
        echo @filenames(s) stdout=@filename(t);
    }
}

string fns="07514-fixed-array-mapper-filenames.a.in 07514-fixed-array-mapper-filenames.b.in";

messagefile outfile[] <fixed_array_mapper; files=fns>;
messagefile realoutput <"07514-fixed-array-mapper-filenames.real.out">;

string fn;

realoutput = write(outfile);

