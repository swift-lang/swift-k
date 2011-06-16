type messagefile {}

type mystruct {
  int first, second;
}

(messagefile t) greeting(mystruct m) { 
    app {
        echo m.first m.second stdout=@filename(t);
    }
}

messagefile outfile <"0231-complex-type.out">;

mystruct m;

m.first = 3;
m.second = 44;

outfile = greeting(m);

