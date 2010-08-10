type file {}

app (file f) echo (string s) {
        echo s stdout=@filename(f);
}

(file fa[]) echo_batch (string sa[]) {
    foreach s, i in sa {
        fa[i] = echo(s);
    }
}

string m[] = ["hello","hi there","how are you"];
string f[] = ["f1.txt", "f2.txt", "f3.txt"];
file fa[]<array_mapper; files=f>;
fa = echo_batch(m);
