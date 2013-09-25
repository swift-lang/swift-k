type messagefile;

(messagefile t) p(boolean b) { 
    app {
        echo b stdout=@filename(t);
    }
}

messagefile tfile <"035-not.t.out">;
messagefile ffile <"035-not.f.out">;

tfile = p(!true);
ffile = p(!false);

