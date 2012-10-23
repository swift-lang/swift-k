type file;

(file f) generate (float p1) {
    app {
                generate "-aTOP -T4" "-p" p1 "-o" @f;
    }
}

