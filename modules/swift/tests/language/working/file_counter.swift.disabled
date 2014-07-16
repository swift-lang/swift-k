// a two-step workflow that essentially does 
// ls "*.txt" | wc

type file {}

(file f) ls (string s) {
    app {
        ls s stdout=@filename(f);
    }
}

(file c) wc (file f) {
    app {
        wc stdin=@filename(f) stdout=@filename(c);
    }
}

file list, count;
list = ls("*.txt");
count = wc(list);
