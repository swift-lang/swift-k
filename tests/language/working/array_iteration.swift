type file {}

(file f) echo (string s) {
    app {
        echo s stdout=@filename(f);
    }
}

(file fa[]) echo_batch (string sa[]) {
    foreach s, i in sa {
        fa[i] = echo(s);
    }
}

string sa[] = ["hello","hi there","how are you"];
file fa[];
fa = echo_batch(sa);
