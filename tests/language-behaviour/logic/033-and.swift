type messagefile;

(messagefile t) p(boolean b) { 
    app {
        echo b stdout=@filename(t);
    }
}

messagefile fffile <"033-and.ff.out">;
messagefile ftfile <"033-and.ft.out">;
messagefile tffile <"033-and.tf.out">;
messagefile ttfile <"033-and.tt.out">;

fffile = p(false && false);
ftfile = p(false && true);
tffile = p(true && false);
ttfile = p(true && true);

