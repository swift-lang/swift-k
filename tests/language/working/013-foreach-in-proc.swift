type file;

(file f) echo (string s) {
  app {
    echo s stdout=f;
  }

}

(file fa[]) echo_batch (string sa[]) {
    foreach s, i in sa {
        fa[i] = echo(s);
    }
}
