type file;

(external o) a() {
    app {
        helperA @strcat(@arg("dir"),"/restart-extern.1.out") "/etc/group" "qux";
    }
}

b(external o) {
    app {
        helperB @strcat(@arg("dir"),"/restart-extern.2.out") "/etc/group" "baz";
    }
}

external sync;

sync=a();
b(sync);
