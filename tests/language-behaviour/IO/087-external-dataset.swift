type file;

app (external o) a() {
        touch "foo";
}

app b(external o) {
        touch "bar";
}

external sync;

sync=a();
b(sync);

