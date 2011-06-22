type messagefile {}

app (messagefile t) greeting(string a, string b, string c, string d) { 
        echo a "," b "," c "," d stdout=@filename(t);
}

messagefile outfile <"0054-strsplit.out">;

string s[] = @strsplit("ab c def ghij", "\\s");

outfile = greeting(s[0], s[1], s[2], s[3]);

