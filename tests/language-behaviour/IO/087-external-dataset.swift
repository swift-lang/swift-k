type file;

(external o) a() {
    app {
        touch "foo";
    }
}

b(external o) {
    app {
        touch "bar";
    }
}

external sync;

sync=a();
b(sync);

