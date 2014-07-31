type messagefile {}

app (messagefile t) greeting(string m) { 
        echo m stdout=@filename(t);
}

messagefile outfile <"0031-strcat-op.out">;

string a = "abc";
string b = "qux";

string v = a + "," + b;

outfile = greeting(v);


