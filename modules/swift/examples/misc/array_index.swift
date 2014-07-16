type file;

app (file t) echo_array (string s[]) {
        echo s[0] s[1] s[2] stdout=@filename(t);
}

string greetings[] = ["how","are","you"];
file hw = echo_array(greetings);
