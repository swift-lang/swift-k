type file {}

// s has a default value
app (file t) echo (string s="hello world") {
  // redirect stdout to a file
  echo s stdout=@filename(t);
}

file hw1<"hw1.txt">, hw2<"hw2.txt">;
// procedure call using the default value
hw1 = echo();
// using a different value
hw2 = echo(s="hello again");
