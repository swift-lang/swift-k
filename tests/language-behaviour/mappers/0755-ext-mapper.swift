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

cols outfile[] <ext; exec="./0755-ext-mapper.sh.in">;

outfile[0].l = write("slot 0");
outfile[1].r = write("slot 1");
outfile[2].l = write("slot 2");

