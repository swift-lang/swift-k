type messagefile;

app (messagefile t) greeting(string s) { 
        echo s stdout=@filename(t);
}

messagefile outfile <"161-star-dot.out">;

type astruct {
  string a;
  string b;
  string c;
};

astruct foo[];

foo[0].a = "zero-A";
foo[0].b = "zero-B";
foo[0].c = "zero-C";

foo[1].a = "one-A";
foo[1].b = "one-B";
foo[1].c = "one-C";

foo[2].a = "two-A";
foo[2].b = "two-B";
foo[2].c = "two-C";

string s[] = foo[*].c;

string u = s[2];

outfile = greeting(u);

