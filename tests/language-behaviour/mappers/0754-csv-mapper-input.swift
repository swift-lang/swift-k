type messagefile;

type cols {
  messagefile l;
  messagefile r;
}

(messagefile t) write(messagefile s) { 
    app {
        cat stdin=@filename(s) stdout=@filename(t);
    }
}

cols infiles[] <csv_mapper; file="0754-csv-mapper.csv">;

messagefile outfile <"0754-csv-mapper-input.out">;

outfile = write(infiles[1].r);

