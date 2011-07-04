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

(struct t) singlewrite() { 
    app {
        touch @filenames(t);
    }
}

struct outfiles <ext; exec="./07555-ext-mapper-twostruct.sh.in">;

outfiles = singlewrite();

