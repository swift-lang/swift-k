type file;

app (file t) echo_wildcard (string s[]) {
        echo s[*] stdout=@filename(t);
}

string greetings[] = ["how","are","you"];
file hw = echo_wildcard(greetings);	
