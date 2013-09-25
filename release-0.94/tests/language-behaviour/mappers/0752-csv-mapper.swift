type messagefile;

type cols {
  messagefile l;
  messagefile r;
}

(messagefile t) write(string s) { 
    app {
        echo s stdout=@filename(t);
    }
}

cols outfile[] <csv_mapper; file="0752-csv-mapper.csv.in">;

outfile[0].l = write("slot 0");
outfile[1].r = write("slot 1");
outfile[2].l = write("slot 2");

