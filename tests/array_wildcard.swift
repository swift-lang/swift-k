type file;

app (file t) echo_wildcard (string s[]) {
  echo s[*] stdout=@filename(t);
}

string greetings[] = ["how","are","you"];
file hw<"hw.txt"> = echo_wildcard(greetings);
