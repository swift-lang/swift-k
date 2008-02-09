(file fa[]) echo_batch (string sa[]) {
    foreach s, i in sa {
        fa[i] = echo(s);
    }
}
