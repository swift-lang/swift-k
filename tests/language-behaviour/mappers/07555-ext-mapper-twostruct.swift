type file;

type structInner {
    file links;
    file rechts;
}

type struct {
    file eerste;
    file twede;
    structInner derde;
};

(file t) write(string s) { 
    app {
        echo s stdout=@filename(t);
    }
}

struct outfiles <ext; exec="./07555-ext-mapper-twostruct.sh.in">;

outfiles.eerste = write("1st");
outfiles.twede = write("2nd");
outfiles.derde.links = write("3l");
outfiles.derde.rechts = write("3r");

