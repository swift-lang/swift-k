type messagefile;

(messagefile t) p(boolean b) { 
    app {
        echo b stdout=@filename(t);
    }
}

messagefile fffile <"034-or.ff.out">;
messagefile ftfile <"034-or.ft.out">;
messagefile tffile <"034-or.tf.out">;
messagefile ttfile <"034-or.tt.out">;

fffile = p(false || false);
ftfile = p(false || true);
tffile = p(true || false);
ttfile = p(true || true);

