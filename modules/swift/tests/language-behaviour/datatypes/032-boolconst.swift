type messagefile;

app (messagefile t) p(boolean b) { 
        echo b stdout=@filename(t);
}

messagefile tfile <"032-boolconst.t.out">;
messagefile ffile <"032-boolconst.f.out">;

tfile = p(true);
ffile = p(false);

