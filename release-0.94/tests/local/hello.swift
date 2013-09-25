type file;

app (file t) echo (string s) {
  echo s stdout=@filename(t);
}

file hw<"hello.txt"> = echo("hello world");
