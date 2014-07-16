type messagefile {}

type mystruct {
  int first;
  string second;
}

app (messagefile t) greeting(int m) { 
        echo m stdout=@filename(t);
}

messagefile outfile <"023-complex-type.out">;

mystruct m;

m.first = 3;

int i = m.first;

outfile = greeting(i);

