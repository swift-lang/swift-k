type messagefile;

(messagefile t) write(string s) { 
    app {
        echo s stdout=@filename(t);
    }
}

string fns="q r s t";

messagefile outfile[] <fixed_array_mapper; files=fns>;
messagefile realoutput <"07513-fixed-array-mapper-filename.real.out">;

string fn;

fn = @filename(outfile);

realoutput = write(fn);

